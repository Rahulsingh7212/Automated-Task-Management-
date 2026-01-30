package com.TaskReminder.app.dto;

import com.TaskReminder.app.entity.User;
import com.TaskReminder.app.entity.Task;
import com.TaskReminder.app.enums.Enums.*;
import java.time.LocalDateTime;

public class ResponseDTO {

    // ==================== API RESPONSE (Generic) ====================
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;

        // Constructors
        public ApiResponse() {
            this.timestamp = LocalDateTime.now();
        }

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }

        // Static factory methods
        public static <T> ApiResponse<T> success(String message) {
            return new ApiResponse<>(true, message);
        }

        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message);
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // ==================== USER RESPONSE ====================
    public static class UserResponse {
        private Long id;
        private String email;
        private String fullName;
        private String role;
        private boolean enabled;
        private LocalDateTime createdAt;

        // Constructors
        public UserResponse() {}

        // From Entity
        public static UserResponse fromUser(User user) {
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setEmail(user.getEmail());
            response.setFullName(user.getFullName());
            response.setRole(user.getRole());
            response.setEnabled(user.isEnabled());
            response.setCreatedAt(user.getCreatedAt());
            return response;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // ==================== AUTH RESPONSE ====================
    public static class AuthResponse {
        private boolean success;
        private String message;
        private UserResponse user;
        private String redirectUrl;

        // Constructors
        public AuthResponse() {}

        public AuthResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Static factory methods
        public static AuthResponse success(String message, UserResponse user) {
            AuthResponse response = new AuthResponse(true, message);
            response.setUser(user);
            return response;
        }

        public static AuthResponse error(String message) {
            return new AuthResponse(false, message);
        }

        public static AuthResponse redirect(String message, String url) {
            AuthResponse response = new AuthResponse(true, message);
            response.setRedirectUrl(url);
            return response;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public UserResponse getUser() { return user; }
        public void setUser(UserResponse user) { this.user = user; }

        public String getRedirectUrl() { return redirectUrl; }
        public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
    }

    // ==================== OTP RESPONSE ====================
    public static class OtpResponse {
        private boolean success;
        private String message;
        private String email;
        private int expiryMinutes;

        // Constructors
        public OtpResponse() {}

        public OtpResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Static factory methods
        public static OtpResponse sent(String email, int expiryMinutes) {
            OtpResponse response = new OtpResponse(true, "OTP sent successfully");
            response.setEmail(maskEmail(email));
            response.setExpiryMinutes(expiryMinutes);
            return response;
        }

        public static OtpResponse verified() {
            return new OtpResponse(true, "OTP verified successfully");
        }

        public static OtpResponse invalid() {
            return new OtpResponse(false, "Invalid or expired OTP");
        }

        // Helper to mask email
        private static String maskEmail(String email) {
            if (email == null || !email.contains("@")) return email;
            String[] parts = email.split("@");
            String local = parts[0];
            return local.charAt(0) + "***@" + parts[1];
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public int getExpiryMinutes() { return expiryMinutes; }
        public void setExpiryMinutes(int expiryMinutes) { this.expiryMinutes = expiryMinutes; }
    }

    // ==================== TASK RESPONSE ====================
    public static class TaskResponse {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime dueDate;
        private String priority;
        private String status;
        private LocalDateTime reminderTime;
        private LocalDateTime createdAt;
        private boolean overdue;

        // Constructors
        public TaskResponse() {}

        // From Entity (if you have Task entity)
        public static TaskResponse fromTask(Task task) {
            TaskResponse response = new TaskResponse();
            response.setId(task.getId());
            response.setTitle(task.getTitle());
            response.setDescription(task.getDescription());
            response.setDueDate(task.getDueDate());
            response.setCreatedAt(task.getCreatedAt());

            // Check if overdue
            if (task.getDueDate() != null) {
                response.setOverdue(LocalDateTime.now().isAfter(task.getDueDate()));
            }

            return response;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getReminderTime() { return reminderTime; }
        public void setReminderTime(LocalDateTime reminderTime) { this.reminderTime = reminderTime; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public boolean isOverdue() { return overdue; }
        public void setOverdue(boolean overdue) { this.overdue = overdue; }
    }
}