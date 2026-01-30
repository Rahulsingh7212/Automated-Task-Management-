package com.TaskReminder.app.service;

import com.TaskReminder.app.entity.Task;
import com.TaskReminder.app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;  // ✅ ADD THIS IMPORT
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // Status and Priority options
    public static final List<String> STATUSES = Arrays.asList("PENDING", "IN_PROGRESS", "DONE");
    public static final List<String> PRIORITIES = Arrays.asList("LOW", "MEDIUM", "HIGH");

    public List<String> getStatuses() {
        return STATUSES;
    }

    public List<String> getPriorities() {
        return PRIORITIES;
    }

    // ==================== BASIC CRUD OPERATIONS ====================

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getAllTasksOrderedByCreatedAt() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    // ==================== MARK AS DONE ====================

    // ==================== MARK AS DONE ====================

    public Task markAsDone(Long id) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt. isPresent()) {
            Task task = taskOpt.get();
            task.setStatus("DONE");
            task.setCompletedAt(LocalDateTime.now());  // ✅ ADD THIS LINE
            return taskRepository.save(task);
        }
        return null;
    }

    // ==================== FILTER BY STATUS ====================

    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }

    // ==================== FILTER BY PRIORITY ====================

    public List<Task> getTasksByPriority(String priority) {
        return taskRepository.findByPriority(priority);
    }

    // ==================== DATE-BASED QUERIES ====================

    public List<Task> getTasksByDueDate(LocalDate dueDate) {
        return taskRepository.findByDueDate(dueDate);
    }

    public List<Task> getTasksBeforeDate(LocalDate date) {
        return taskRepository.findByDueDateBefore(date);
    }

    public List<Task> getTasksAfterDate(LocalDate date) {
        return taskRepository.findByDueDateAfter(date);
    }

    public List<Task> getTasksBetweenDates(LocalDate startDate, LocalDate endDate) {
        return taskRepository.findByDueDateBetween(startDate, endDate);
    }

    // ==================== SEARCH BY KEYWORD ====================

    public List<Task> searchTasksByTitle(String keyword) {
        return taskRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // ==================== COMBINED FILTER METHOD ====================

    public List<Task> filterTasks(String status, String priority, String keyword) {
        List<Task> tasks;

        boolean hasStatus = status != null && !status.isEmpty() && !status.equals("All");
        boolean hasPriority = priority != null && !priority.isEmpty() && !priority.equals("All");
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasStatus && hasPriority && hasKeyword) {
            tasks = taskRepository.findByStatusAndPriorityAndTitleContainingIgnoreCase(status, priority, keyword);
        } else if (hasStatus && hasPriority) {
            tasks = taskRepository.findByStatusAndPriority(status, priority);
        } else if (hasStatus && hasKeyword) {
            tasks = taskRepository. findByStatusAndTitleContainingIgnoreCase(status, keyword);
        } else if (hasPriority && hasKeyword) {
            tasks = taskRepository. findByPriorityAndTitleContainingIgnoreCase(priority, keyword);
        } else if (hasStatus) {
            tasks = taskRepository.findByStatus(status);
        } else if (hasPriority) {
            tasks = taskRepository.findByPriority(priority);
        } else if (hasKeyword) {
            tasks = taskRepository. findByTitleContainingIgnoreCase(keyword);
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks;
    }

    // ==================== SORTING METHOD ====================

    public List<Task> sortTasks(List<Task> tasks, String sortBy) {
        if (sortBy == null || sortBy.isEmpty() || tasks == null || tasks.isEmpty()) {
            return tasks;
        }

        List<Task> sortedTasks = new ArrayList<>(tasks);

        switch (sortBy) {
            case "dueDate":
                sortedTasks. sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "priority":
                sortedTasks. sort((t1, t2) -> {
                    int p1 = getPriorityOrder(t1.getPriority());
                    int p2 = getPriorityOrder(t2.getPriority());
                    return Integer.compare(p2, p1);
                });
                break;
            case "title":
                sortedTasks.sort(Comparator.comparing(Task:: getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
                break;
            case "createdAt":
                sortedTasks.sort(Comparator.comparing(Task::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            default:
                break;
        }
        return sortedTasks;
    }

    private int getPriorityOrder(String priority) {
        if (priority == null) return 0;
        switch (priority.toUpperCase()) {
            case "HIGH":  return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }

    // ==================== FILTER AND SORT COMBINED ====================

    public List<Task> filterAndSortTasks(String status, String priority, String keyword, String sortBy) {
        List<Task> tasks = filterTasks(status, priority, keyword);
        return sortTasks(tasks, sortBy);
    }

    // ==================== BASIC PAGINATION METHODS ====================

    public Page<Task> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    public Page<Task> getTasksByStatus(String status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable);
    }

    public Page<Task> getTasksByPriority(String priority, Pageable pageable) {
        return taskRepository.findByPriority(priority, pageable);
    }

    public Page<Task> getTasksByDueDate(LocalDate dueDate, Pageable pageable) {
        return taskRepository.findByDueDate(dueDate, pageable);
    }

    public Page<Task> getTasksBeforeDate(LocalDate date, Pageable pageable) {
        return taskRepository.findByDueDateBefore(date, pageable);
    }

    public Page<Task> getTasksAfterDate(LocalDate date, Pageable pageable) {
        return taskRepository.findByDueDateAfter(date, pageable);
    }

    public Page<Task> getTasksBetweenDates(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return taskRepository.findByDueDateBetween(startDate, endDate, pageable);
    }

    // ==========================================================
    // ✅ NEW PAGINATION METHODS FOR CONTROLLER
    // ==========================================================

    /**
     * Get filtered tasks with pagination - Main method used by TaskController
     */
    public Page<Task> getFilteredTasksWithPagination(String status, String priority, String keyword, Pageable pageable) {

        boolean hasStatus = status != null && ! status.isEmpty() && !status.equals("All");
        boolean hasPriority = priority != null && !priority.isEmpty() && !priority.equals("All");
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        // All three filters
        if (hasStatus && hasPriority && hasKeyword) {
            return taskRepository.findByStatusAndPriorityAndTitleContainingIgnoreCase(status, priority, keyword, pageable);
        }
        // Status + Priority
        if (hasStatus && hasPriority) {
            return taskRepository. findByStatusAndPriority(status, priority, pageable);
        }
        // Status + Keyword
        if (hasStatus && hasKeyword) {
            return taskRepository.findByStatusAndTitleContainingIgnoreCase(status, keyword, pageable);
        }
        // Priority + Keyword
        if (hasPriority && hasKeyword) {
            return taskRepository.findByPriorityAndTitleContainingIgnoreCase(priority, keyword, pageable);
        }
        // Only Status
        if (hasStatus) {
            return taskRepository.findByStatus(status, pageable);
        }
        // Only Priority
        if (hasPriority) {
            return taskRepository.findByPriority(priority, pageable);
        }
        // Only Keyword
        if (hasKeyword) {
            return taskRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        }
        // No filters - return all with pagination
        return taskRepository.findAll(pageable);
    }

    /**
     * Get overdue tasks with pagination
     * Overdue = due date before today AND status is NOT DONE
     */
    public Page<Task> getOverdueTasksWithPagination(Pageable pageable) {
        List<Task> overdueTasks = taskRepository.findByDueDateBefore(LocalDate.now())
                .stream()
                .filter(task -> !"DONE".equals(task.getStatus()))
                .collect(Collectors.toList());

        return convertListToPage(overdueTasks, pageable);
    }

    /**
     * Get tasks due today with pagination
     */
    public Page<Task> getTasksDueTodayWithPagination(Pageable pageable) {
        return taskRepository.findByDueDate(LocalDate.now(), pageable);
    }

    /**
     * Get upcoming tasks with pagination (next 7 days)
     */
    public Page<Task> getUpcomingTasksWithPagination(Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        return taskRepository.findByDueDateBetween(today, nextWeek, pageable);
    }

    /**
     * Get upcoming tasks after today with pagination
     */
    public Page<Task> getTasksAfterTodayWithPagination(Pageable pageable) {
        return taskRepository.findByDueDateAfter(LocalDate.now(), pageable);
    }

    // ==========================================================
    // ✅ HELPER METHOD - Convert List to Page
    // ==========================================================

    /**
     * Converts a List to a Page for manual pagination
     * Used when we need to filter in-memory (e.g., overdue tasks excluding DONE)
     */
    private Page<Task> convertListToPage(List<Task> list, Pageable pageable) {
        if (list == null || list.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        // If start is beyond the list size, return empty page
        if (start >= list.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, list.size());
        }

        List<Task> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }

    // ==================== UTILITY METHODS ====================

    public long countTasksByStatus(String status) {
        return taskRepository.findByStatus(status).size();
    }

    public long countTasksByPriority(String priority) {
        return taskRepository.findByPriority(priority).size();
    }

    public List<Task> getOverdueTasks() {
        List<Task> overdueTasks = new ArrayList<>(taskRepository. findByDueDateBefore(LocalDate.now()));
        overdueTasks.removeIf(task -> "DONE".equals(task.getStatus()));
        return overdueTasks;
    }

    public List<Task> getTasksDueToday() {
        return taskRepository.findByDueDate(LocalDate.now());
    }

    public List<Task> getUpcomingTasks() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        return taskRepository.findByDueDateBetween(today, nextWeek);
    }

    // ==================== ADDITIONAL PAGINATION UTILITIES ====================

    /**
     * Get total count of all tasks
     */
    public long getTotalTaskCount() {
        return taskRepository.count();
    }

    /**
     * Get count of filtered tasks
     */
    public long getFilteredTaskCount(String status, String priority, String keyword) {
        return filterTasks(status, priority, keyword).size();
    }

    // ==========================================================
    // ✅ NEW: HEADER STATS COUNT METHODS - ADD THESE
    // ==========================================================

    /**
     * Count all tasks in the database
     * Used for header stats - Total Tasks
     */
    public long countAllTasks() {
        return taskRepository.count();
    }

    /**
     * Count tasks with PENDING status
     * Used for header stats - Pending count
     */
    public long countPendingTasks() {
        return taskRepository.countByStatus("PENDING");
    }

    /**
     * Count tasks with IN_PROGRESS status
     * Used for header stats - In Progress count
     */
    public long countInProgressTasks() {
        return taskRepository.countByStatus("IN_PROGRESS");
    }

    /**
     * Count tasks with DONE status
     * Used for header stats - Completed count
     */
    public long countDoneTasks() {
        return taskRepository.countByStatus("DONE");
    }

    /**
     * Count overdue tasks (due date before today AND status is NOT DONE)
     * Used for header stats - Overdue count
     */
    public long countOverdueTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.findByDueDateBefore(today)
                .stream()
                .filter(task -> !"DONE".equals(task.getStatus()))
                .count();
    }

    /**
     * Count tasks due today
     * Used for header stats - Today's tasks count
     */
    public long countTasksDueToday() {
        return taskRepository.countByDueDate(LocalDate.now());
    }

    /**
     * Count upcoming tasks (next 7 days excluding today)
     * Used for header stats - Upcoming tasks count
     */
    public long countUpcomingTasks() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        return taskRepository.countByDueDateBetween(tomorrow, nextWeek);
    }

    /**
     * Count HIGH priority tasks that are not done
     * Used for header stats - High priority count
     */
    public long countHighPriorityTasks() {
        return taskRepository.findByPriority("HIGH")
                .stream()
                .filter(task -> !"DONE".equals(task.getStatus()))
                .count();
    }
}

