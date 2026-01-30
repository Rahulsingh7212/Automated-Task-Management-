package com.TaskReminder.app.controller;

import com.TaskReminder.app.entity.Task;
import com.TaskReminder.app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // ========== DISPLAY ALL TASKS WITH FILTERS, SORTING & PAGINATION ==========
    @GetMapping
    public String getAllTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        // Handle sortBy parameter (from sort links in filter section)
        if (sortBy != null && !sortBy.isEmpty()) {
            sortField = sortBy;
            sortDir = "asc";
        }

        // Create sort direction
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        // Fetch paginated and filtered tasks
        Page<Task> taskPage = taskService.getFilteredTasksWithPagination(status, priority, keyword, pageable);

        // Calculate start and end count for display
        long totalElements = taskPage.getTotalElements();
        int startCount = totalElements > 0 ? (page * size) + 1 : 0;
        int endCount = (int) Math.min((long) (page * size) + size, totalElements);

        // Add pagination attributes to model
        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", taskPage.getTotalPages());
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("totalItems", totalElements); // keeping for backward compatibility
        model.addAttribute("pageSize", size);
        model.addAttribute("size", size); // keeping for backward compatibility
        model.addAttribute("startCount", startCount);
        model.addAttribute("endCount", endCount);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        // Add filter attributes to model
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);

        // Add base URL for pagination (used in overdue, today, upcoming pages)
        model.addAttribute("baseUrl", "/tasks");


        // ✅ ADD HEADER STATS
        addHeaderStats(model);

        return "tasks";

    }

    // Show add task form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());

        // ✅ ADD HEADER STATS
        addHeaderStats(model);

        return "add-task";
    }

    // Save new task
    @PostMapping("/save")
    public String saveTask(@ModelAttribute Task task) {
        taskService.saveTask(task);
        return "redirect:/tasks";
    }

    // Alternative endpoint for adding task
    @PostMapping("/add")
    public String addTask(@ModelAttribute Task task) {
        taskService.saveTask(task);
        return "redirect:/tasks";
    }

    // Show edit task form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            return "redirect:/tasks";
        }
        model.addAttribute("task", task);
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());

        // ✅ ADD HEADER STATS
        addHeaderStats(model);

        return "edit-task";
    }

    // Update task
    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute Task task) {
        Task existingTask = taskService.getTaskById(id);
        if (existingTask != null) {
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setDueDate(task.getDueDate());
            existingTask.setStatus(task.getStatus());
            existingTask.setPriority(task.getPriority());
            taskService.saveTask(existingTask);
        }
        return "redirect:/tasks";
    }

    // Alternative endpoint for updating task
    @PostMapping("/edit/{id}")
    public String editTask(@PathVariable Long id, @ModelAttribute Task task) {
        Task existingTask = taskService.getTaskById(id);
        if (existingTask != null) {
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setDueDate(task.getDueDate());
            existingTask.setStatus(task.getStatus());
            existingTask.setPriority(task.getPriority());
            taskService.saveTask(existingTask);
        }
        return "redirect:/tasks";
    }

    // Delete task
    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }

    // Mark task as done
    @GetMapping("/markdone/{id}")
    public String markAsDone(@PathVariable Long id) {
        taskService.markAsDone(id);
        return "redirect:/tasks";
    }

    // ========== ✅ NEW: VIEW SINGLE TASK DETAILS ==========
    @GetMapping("/view/{id}")
    public String viewTask(@PathVariable Long id, Model model) {
        Task task = taskService. getTaskById(id);
        if (task == null) {
            return "redirect:/tasks";
        }
        model.addAttribute("task", task);
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());

        // ✅ Check if task is overdue (for the warning banner)
        boolean isOverdue = false;
        if (task.getDueDate() != null && task.getStatus() != null) {
            isOverdue = task.getDueDate().isBefore(LocalDate.now())
                    && !task.getStatus().toString().equals("DONE");
        }
        model.addAttribute("isOverdue", isOverdue);

        // ✅ Calculate days until due or days overdue
        if (task.getDueDate() != null) {
            long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), task.getDueDate());
            model.addAttribute("daysUntilDue", daysUntilDue);
        }

        // ✅ ADD HEADER STATS
        addHeaderStats(model);

        return "view-task";
    }

    // ========== VIEW OVERDUE TASKS WITH PAGINATION ==========
    @GetMapping("/overdue")
    public String getOverdueTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage = taskService.getOverdueTasksWithPagination(pageable);

        addPaginationAttributes(model, taskPage, page, size, sortField, sortDir);
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());
        model.addAttribute("pageTitle", "Overdue Tasks");
        model.addAttribute("baseUrl", "/tasks/overdue");

        // ✅ ADD HEADER STATS
        addHeaderStats(model);

        return "tasks";
    }

    // ========== VIEW TASKS DUE TODAY WITH PAGINATION ==========
    @GetMapping("/today")
    public String getTasksDueToday(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "priority") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage = taskService.getTasksDueTodayWithPagination(pageable);

        addPaginationAttributes(model, taskPage, page, size, sortField, sortDir);
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());
        model.addAttribute("pageTitle", "Tasks Due Today");
        model.addAttribute("baseUrl", "/tasks/today");

        // ✅ ADD HEADER STATS
        addHeaderStats(model);


        return "tasks";
    }

    // ========== VIEW UPCOMING TASKS WITH PAGINATION ==========
    @GetMapping("/upcoming")
    public String getUpcomingTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage = taskService.getUpcomingTasksWithPagination(pageable);

        addPaginationAttributes(model, taskPage, page, size, sortField, sortDir);
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());
        model.addAttribute("pageTitle", "Upcoming Tasks");
        model.addAttribute("baseUrl", "/tasks/upcoming");

        // ✅ ADD HEADER STATS
        addHeaderStats(model);


        return "tasks";
    }

    // ===============================================
    // ✅ NEW: CALENDAR VIEW
    // ===============================================
    @GetMapping("/calendar")
    public String viewCalendar(Model model) {
        // We must add header stats here too, or the navbar counters will be empty!
        addHeaderStats(model);
        return "calendar"; // This looks for calendar.html in templates folder
    }

    // ========== HELPER METHOD FOR PAGINATION ATTRIBUTES ==========
    private void addPaginationAttributes(Model model, Page<Task> taskPage, int page, int size, String sortField, String sortDir) {
        long totalElements = taskPage.getTotalElements();
        int startCount = totalElements > 0 ? (page * size) + 1 : 0;
        int endCount = (int) Math.min((long) (page * size) + size, totalElements);

        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", taskPage.getTotalPages());
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("totalItems", totalElements);
        model.addAttribute("pageSize", size);
        model.addAttribute("size", size);
        model.addAttribute("startCount", startCount);
        model.addAttribute("endCount", endCount);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        // Empty filter values for special pages
        model.addAttribute("selectedStatus", null);
        model.addAttribute("selectedPriority", null);
        model.addAttribute("keyword", null);
        model.addAttribute("sortBy", null);
    }


    // ========== ✅ NEW: HELPER METHOD FOR HEADER STATS ==========
    private void addHeaderStats(Model model) {
        model.addAttribute("pendingCount", taskService.countPendingTasks());
        model.addAttribute("inProgressCount", taskService.countInProgressTasks());
        model.addAttribute("doneCount", taskService.countDoneTasks());
        model.addAttribute("overdueCount", taskService.countOverdueTasks());
        model.addAttribute("totalTaskCount", taskService.countAllTasks());
    }
}
