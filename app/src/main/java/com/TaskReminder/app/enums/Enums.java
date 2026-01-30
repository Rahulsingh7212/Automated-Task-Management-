package com.TaskReminder.app.enums;

public class Enums {

    // User Roles
    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }

    // OTP Types
    public enum OtpType {
        REGISTRATION,
        PASSWORD_RESET
    }

    // Account Status
    public enum AccountStatus {
        PENDING,
        ACTIVE,
        SUSPENDED,
        DELETED
    }

    // Task Status
    public enum TaskStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    // Task Priority
    public enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    // Reminder Frequency
    public enum ReminderFrequency {
        ONCE,
        DAILY,
        WEEKLY,
        MONTHLY
    }
}