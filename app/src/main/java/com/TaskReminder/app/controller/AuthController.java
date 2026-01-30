package com.TaskReminder.app.controller;

import com.TaskReminder.app.entity.OtpVerification.OtpType;
import com.TaskReminder.app.entity.User;
import com.TaskReminder.app.service.EmailService;
import com.TaskReminder.app.service.OtpService;
import com.TaskReminder.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    // ==================== LOGIN ====================
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid email or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("success", "Registration successful! Please login.");
        }

        return "auth/login";
    }

    // ==================== REGISTER ====================
    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Validation
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
            return "redirect:/auth/register";
        }

        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters!");
            return "redirect:/auth/register";
        }

        if (userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email already registered!");
            return "redirect:/auth/register";
        }

        try {
            // Register user (not enabled yet)
            userService.registerUser(email, password, fullName);

            // Generate and send OTP
            otpService.generateAndSendOtp(email, OtpType.REGISTRATION);

            // Store email in session for OTP verification
            session.setAttribute("pendingVerificationEmail", email);
            session.setAttribute("otpType", "REGISTRATION");

            redirectAttributes.addFlashAttribute("success", "OTP sent to your email!");
            return "redirect:/auth/verify-otp";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/auth/register";
        }
    }

    // ==================== OTP VERIFICATION ====================
    @GetMapping("/verify-otp")
    public String showOtpPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("pendingVerificationEmail");
        if (email == null) {
            return "redirect:/auth/register";
        }

        model.addAttribute("email", email);
        model.addAttribute("otpType", session.getAttribute("otpType"));
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String otp,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("pendingVerificationEmail");
        String otpTypeStr = (String) session.getAttribute("otpType");

        if (email == null || otpTypeStr == null) {
            return "redirect:/auth/register";
        }

        OtpType otpType = OtpType.valueOf(otpTypeStr);

        if (otpService.verifyOtp(email, otp, otpType)) {
            if (otpType == OtpType.REGISTRATION) {
                // Enable user account
                userService.enableUser(email);

                // Send welcome email
                User user = userService.findByEmail(email).orElse(null);
                if (user != null) {
                    emailService.sendWelcomeEmail(email, user.getFullName());
                }

                // Clear session
                session.removeAttribute("pendingVerificationEmail");
                session.removeAttribute("otpType");

                redirectAttributes.addFlashAttribute("success", "Account verified successfully! Please login.");
                return "redirect:/auth/login";

            } else if (otpType == OtpType.PASSWORD_RESET) {
                // Allow password reset
                session.setAttribute("passwordResetVerified", true);
                return "redirect:/auth/reset-password";
            }
        }

        redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP!");
        return "redirect:/auth/verify-otp";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("pendingVerificationEmail");
        String otpTypeStr = (String) session.getAttribute("otpType");

        if (email == null || otpTypeStr == null) {
            return "redirect:/auth/register";
        }

        OtpType otpType = OtpType.valueOf(otpTypeStr);
        otpService.generateAndSendOtp(email, otpType);

        redirectAttributes.addFlashAttribute("success", "New OTP sent to your email!");
        return "redirect:/auth/verify-otp";
    }

    // ==================== FORGOT PASSWORD ====================
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email not found!");
            return "redirect:/auth/forgot-password";
        }

        // Generate and send OTP
        otpService.generateAndSendOtp(email, OtpType.PASSWORD_RESET);

        // Store in session
        session.setAttribute("pendingVerificationEmail", email);
        session.setAttribute("otpType", "PASSWORD_RESET");

        redirectAttributes.addFlashAttribute("success", "OTP sent to your email!");
        return "redirect:/auth/verify-otp";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(HttpSession session) {
        Boolean verified = (Boolean) session.getAttribute("passwordResetVerified");
        if (verified == null || !verified) {
            return "redirect:/auth/forgot-password";
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String password,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Boolean verified = (Boolean) session.getAttribute("passwordResetVerified");
        String email = (String) session.getAttribute("pendingVerificationEmail");

        if (verified == null || !verified || email == null) {
            return "redirect:/auth/forgot-password";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
            return "redirect:/auth/reset-password";
        }

        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters!");
            return "redirect:/auth/reset-password";
        }

        // Update password
        userService.updatePassword(email, password);

        // Send confirmation email
        emailService.sendPasswordResetConfirmation(email);

        // Clear session
        session.removeAttribute("pendingVerificationEmail");
        session.removeAttribute("otpType");
        session.removeAttribute("passwordResetVerified");

        redirectAttributes.addFlashAttribute("success", "Password reset successfully! Please login.");
        return "redirect:/auth/login";
    }
}