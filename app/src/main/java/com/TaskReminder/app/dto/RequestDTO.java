package com.TaskReminder.app.dto;

import com.TaskReminder.app.enums.Enums.*;
import java.time.LocalDateTime;

public class RequestDTO {

    // ==================== REGISTER REQUEST ====================
    public static class RegisterRequest {
        private String fullName;
        private String email;
        private String password;
        private String confirmPassword;

        // Constructors
        public RegisterRequest() {}

        public RegisterRequest(String fullName, String email, String password, String confirmPassword) {
            this.fullName = fullName;
            this.email = email;
            this.password = password;
            this.confirmPassword = confirmPassword;
        }

        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

        public boolean isPasswordMatching() {
            return password != null && password.equals(confirmPassword);
        }
    }

    // ==================== LOGIN REQUEST ====================
    public static class LoginRequest {
        private String email;
        private String password;
        private boolean rememberMe;

        // Constructors
        public LoginRequest() {}

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public boolean isRememberMe() { return rememberMe; }
        public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
    }

    // ==================== OTP REQUEST ====================
    public static class OtpRequest {
        private String email;
        private String otp;

        // Constructors
        public OtpRequest() {}

        public OtpRequest(String email, String otp) {
            this.email = email;
            this.otp = otp;
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }

    // ==================== FORGOT PASSWORD REQUEST ====================
    public static class ForgotPasswordRequest {
        private String email;

        // Constructors
        public ForgotPasswordRequest() {}

        public ForgotPasswordRequest(String email) {
            this.email = email;
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // ==================== RESET PASSWORD REQUEST ====================
    public static class ResetPasswordRequest {
        private String password;
        private String confirmPassword;

        // Constructors
        public ResetPasswordRequest() {}

        // Getters and Setters
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

        public boolean isPasswordMatching() {
            return password != null && password.equals(confirmPassword);
        }
    }

    // ==================== TASK REQUEST ====================
    public static class TaskRequest {
        private String title;
        private String description;
        private LocalDateTime dueDate;
        private TaskPriority priority = TaskPriority.MEDIUM;
        private TaskStatus status = TaskStatus.PENDING;
        private LocalDateTime reminderTime;

        // Constructors
        public TaskRequest() {}

        public TaskRequest(String title, String description, LocalDateTime dueDate) {
            this.title = title;
            this.description = description;
            this.dueDate = dueDate;
        }

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

        public TaskPriority getPriority() { return priority; }
        public void setPriority(TaskPriority priority) { this.priority = priority; }

        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }

        public LocalDateTime getReminderTime() { return reminderTime; }
        public void setReminderTime(LocalDateTime reminderTime) { this.reminderTime = reminderTime; }
    }
}