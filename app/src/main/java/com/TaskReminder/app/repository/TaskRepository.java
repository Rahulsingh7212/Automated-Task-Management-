package com.TaskReminder.app.repository;

import com.TaskReminder.app.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find all tasks ordered by created date (newest first)
    List<Task> findAllByOrderByCreatedAtDesc();

    // Find tasks by status
    List<Task> findByStatus(String status);

    // Find tasks by priority
    List<Task> findByPriority(String priority);

    // ✅ ADD THESE - Find tasks by date
    List<Task> findByDueDate(LocalDate dueDate);

    // Find tasks before a date
    List<Task> findByDueDateBefore(LocalDate date);

    // Find tasks after a date
    List<Task> findByDueDateAfter(LocalDate date);

    // Find tasks between two dates
    List<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    // ========== SEARCH BY TITLE (KEYWORD) ==========

    // Find tasks by title containing keyword (case-insensitive)
    List<Task> findByTitleContainingIgnoreCase(String title);

    // ========== COMBINED FILTER QUERIES ==========

    // Filter by status AND priority
    List<Task> findByStatusAndPriority(String status, String priority);

    // Filter by status AND title keyword
    List<Task> findByStatusAndTitleContainingIgnoreCase(String status, String title);

    // Filter by priority AND title keyword
    List<Task> findByPriorityAndTitleContainingIgnoreCase(String priority, String title);

    // Filter by status AND priority AND title keyword
    List<Task> findByStatusAndPriorityAndTitleContainingIgnoreCase(String status, String priority, String title);

    // ========== SORTING QUERIES ==========

    // Find all ordered by due date ascending
    List<Task> findAllByOrderByDueDateAsc();

    // Find all ordered by priority descending
    List<Task> findAllByOrderByPriorityDesc();

    // Find all ordered by title ascending
    List<Task> findAllByOrderByTitleAsc();


    // ✅ PAGINATION METHODS - Add these below

    // Pageable version of findAll (already inherited, but explicit for clarity)
    Page<Task> findAll(Pageable pageable);

    // Find tasks by status with pagination
    Page<Task> findByStatus(String status, Pageable pageable);

    // Find tasks by priority with pagination
    Page<Task> findByPriority(String priority, Pageable pageable);

    // Find tasks by due date with pagination
    Page<Task> findByDueDate(LocalDate dueDate, Pageable pageable);

    // Find tasks before a date with pagination
    Page<Task> findByDueDateBefore(LocalDate date, Pageable pageable);

    // Find tasks after a date with pagination
    Page<Task> findByDueDateAfter(LocalDate date, Pageable pageable);

    // Find tasks between two dates with pagination
    Page<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // ========== PAGINATION WITH FILTERS ==========

    // Find by title keyword with pagination
    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Find by status and priority with pagination
    Page<Task> findByStatusAndPriority(String status, String priority, Pageable pageable);

    // Find by status and title keyword with pagination
    Page<Task> findByStatusAndTitleContainingIgnoreCase(String status, String title, Pageable pageable);

    // Find by priority and title keyword with pagination
    Page<Task> findByPriorityAndTitleContainingIgnoreCase(String priority, String title, Pageable pageable);

    // Find by status, priority and title keyword with pagination
    Page<Task> findByStatusAndPriorityAndTitleContainingIgnoreCase(String status, String priority, String title, Pageable pageable);


    // ==========================================================
    // ✅ NEW: COUNT METHODS FOR HEADER STATS - ADD THESE
    // ==========================================================

    /**
     * Count tasks by status
     * Used for: Pending, In Progress, Done counts
     */
    long countByStatus(String status);

    /**
     * Count tasks by due date
     * Used for: Today's tasks count
     */
    long countByDueDate(LocalDate dueDate);

    /**
     * Count tasks between dates
     * Used for: Upcoming tasks count
     */
    long countByDueDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Count tasks before a date with specific status
     * Used for: Overdue tasks (alternative method)
     */
    long countByDueDateBeforeAndStatusNot(LocalDate date, String status);

    /**
     * Count tasks by priority
     * Used for: Priority-based counts
     */
    long countByPriority(String priority);

    // ==================== OPTIONAL: CUSTOM QUERY METHODS ====================

    /**
     * Count overdue tasks using custom query
     * More efficient than filtering in Java
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :today AND t.status != 'DONE'")
    long countOverdueTasksQuery(@Param("today") LocalDate today);

    /**
     * Count tasks by status using custom query
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    long countTasksByStatusQuery(@Param("status") String status);

    /**
     * Count high priority tasks that are not done
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.priority = 'HIGH' AND t.status != 'DONE'")
    long countActiveHighPriorityTasks();

    /**
     * Count tasks due within next N days
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status != 'DONE'")
    long countTasksDueInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
