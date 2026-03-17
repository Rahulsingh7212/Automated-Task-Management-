package com.TaskReminder.app.controller;

import com.TaskReminder.app.service.UserService;
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

    // ========== LOGIN PAGE ==========
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully!");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! Please login.");
        }

        return "auth/login";
    }

    // ========== REGISTER PAGE ==========
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    // ========== PROCESS REGISTRATION ==========
    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match!");
            redirectAttributes.addFlashAttribute("fullName", fullName);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/auth/register";
        }

        // Validate password length
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Password must be at least 6 characters!");
            redirectAttributes.addFlashAttribute("fullName", fullName);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/auth/register";
        }

        // Check if email exists
        if (userService.emailExists(email)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Email is already registered!");
            redirectAttributes.addFlashAttribute("fullName", fullName);
            return "redirect:/auth/register";
        }

        try {
            userService.registerUser(fullName, email, password);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Please login.");
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Registration failed: " + e.getMessage());
            return "redirect:/auth/register";
        }
    }
}