package com.TaskReminder.app.repository;

import com.TaskReminder.app.entity.OtpVerification;
import com.TaskReminder.app.entity.OtpVerification.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findByEmailAndOtpAndOtpTypeAndUsedFalse(
            String email, String otp, OtpType otpType);

    Optional<OtpVerification> findTopByEmailAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(
            String email, OtpType otpType);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification o WHERE o.expiryTime < :now")
    void deleteExpiredOtps(LocalDateTime now);

    @Modifying
    @Transactional
    void deleteByEmailAndOtpType(String email, OtpType otpType);
}