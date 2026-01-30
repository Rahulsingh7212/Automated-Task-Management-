package com.TaskReminder.app.service;

import com.TaskReminder.app.entity.OtpVerification;
import com.TaskReminder.app.entity.OtpVerification.OtpType;
import com.TaskReminder.app.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String generateAndSendOtp(String email, OtpType otpType) {
        // Delete any existing OTPs for this email and type
        otpRepository.deleteByEmailAndOtpType(email, otpType);

        // Generate new OTP
        String otp = generateOtp();

        // Save OTP to database
        OtpVerification otpVerification = new OtpVerification(
                email,
                otp,
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES),
                otpType
        );
        otpRepository.save(otpVerification);

        // Send OTP via email
        String purpose = otpType == OtpType.REGISTRATION ? "Account Verification" : "Password Reset";
        emailService.sendOtpEmail(email, otp, purpose);

        return otp;
    }

    @Transactional
    public boolean verifyOtp(String email, String otp, OtpType otpType) {
        Optional<OtpVerification> otpVerificationOpt = otpRepository
                .findByEmailAndOtpAndOtpTypeAndUsedFalse(email, otp, otpType);

        if (otpVerificationOpt.isEmpty()) {
            return false;
        }

        OtpVerification otpVerification = otpVerificationOpt.get();

        if (otpVerification.isExpired()) {
            return false;
        }

        // Mark OTP as used
        otpVerification.setUsed(true);
        otpRepository.save(otpVerification);

        return true;
    }

    public boolean hasValidOtp(String email, OtpType otpType) {
        Optional<OtpVerification> otpOpt = otpRepository
                .findTopByEmailAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(email, otpType);

        return otpOpt.isPresent() && !otpOpt.get().isExpired();
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    // Clean up expired OTPs every hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
}