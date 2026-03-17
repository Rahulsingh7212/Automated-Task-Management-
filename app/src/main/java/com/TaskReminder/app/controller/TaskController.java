package com.TaskReminder.app.controller;

import com.TaskReminder.app.entity.Task;
import com.TaskReminder.app.entity.User;
import com.TaskReminder.app.service.TaskService;
import com.TaskReminder.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    // ✅ Helper: Get current logged-in user
    private User getCurrentUser(Principal principal) {
        return userService.getCurrentUser(principal.getName());
    }

    // ========== DISPLAY ALL TASKS ==========
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
            Model model,
            Principal principal) {  // ✅ ADD Principal

        User user = getCurrentUser(principal);  // ✅ GET USER

        if (sortBy != null && !sortBy.isEmpty()) {
            sortField = sortBy;
            sortDir = "asc";
        }

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // ✅ FILTER BY USER
        Page<Task> taskPage = taskService.getFilteredTasksByUser(
                user, status, priority, keyword, pageable);

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

        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("baseUrl", "/tasks");

        addHeaderStats(model, user);  // ✅ PASS USER

        return "tasks";
    }

    // ========== ADD TASK ==========
    @GetMapping("/add")
    public String showAddForm(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        model.addAttribute("task", new Task());
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());
        addHeaderStats(model, user);
        return "add-task";
    }

    @PostMapping("/save")
    public String saveTask(@ModelAttribute Task task, Principal principal) {
        User user = getCurrentUser(principal);  // ✅ GET USER
        taskService.saveTask(task, user);         // ✅ SAVE WITH USER
        return "redirect:/tasks";
    }

    @PostMapping("/add")
    public String addTask(@ModelAttribute Task task, Principal principal) {
        User user = getCurrentUser(principal);
        taskService.saveTask(task, user);
        return "redirect:/tasks";
    }

    // ========== EDIT TASK ==========
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        User user = getCurrentUser(principal);
        Task task = taskService.getTaskById(id);
        if (task == null || !task.getUser().getId().equals(user.getId())) {
            return "redirect:/tasks";  // ✅ Security: only own tasks
        }
        model.addAttribute("task", task);
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());
        addHeaderStats(model, user);
        return "edit-task";
    }

    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id,
                             @ModelAttribute Task task,
                             Principal principal) {
        User user = getCurrentUser(principal);
        Task existingTask = taskService.getTaskById(id);
        if (existingTask != null && existingTask.getUser().getId().equals(user.getId())) {
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setDueDate(task.getDueDate());
            existingTask.setStatus(task.getStatus());
            existingTask.setPriority(task.getPriority());
            taskService.saveTask(existingTask);
        }
        return "redirect:/tasks";
    }

    @PostMapping("/edit/{id}")
    public String editTask(@PathVariable Long id,
                           @ModelAttribute Task task,
                           Principal principal) {
        User user = getCurrentUser(principal);
        Task existingTask = taskService.getTaskById(id);
        if (existingTask != null && existingTask.getUser().getId().equals(user.getId())) {
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setDueDate(task.getDueDate());
            existingTask.setStatus(task.getStatus());
            existingTask.setPriority(task.getPriority());
            taskService.saveTask(existingTask);
        }
        return "redirect:/tasks";
    }

    // ========== DELETE TASK ==========
    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, Principal principal) {
        User user = getCurrentUser(principal);
        Task task = taskService.getTaskById(id);
        if (task != null && task.getUser().getId().equals(user.getId())) {
            taskService.deleteTask(id);
        }
        return "redirect:/tasks";
    }

    // ========== MARK AS DONE ==========
    @GetMapping("/markdone/{id}")
    public String markAsDone(@PathVariable Long id, Principal principal) {
        User user = getCurrentUser(principal);
        Task task = taskService.getTaskById(id);
        if (task != null && task.getUser().getId().equals(user.getId())) {
            taskService.markAsDone(id);
        }
        return "redirect:/tasks";
    }

    // ========== VIEW SINGLE TASK ==========
    @GetMapping("/view/{id}")
    public String viewTask(@PathVariable Long id, Model model, Principal principal) {
        User user = getCurrentUser(principal);
        Task task = taskService.getTaskById(id);
        if (task == null || !task.getUser().getId().equals(user.getId())) {
            return "redirect:/tasks";
        }
        model.addAttribute("task", task);
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());

        boolean isOverdue = false;
        if (task.getDueDate() != null && task.getStatus() != null) {
            isOverdue = task.getDueDate().isBefore(LocalDate.now())
                    && !task.getStatus().toString().equals("DONE");
        }
        model.addAttribute("isOverdue", isOverdue);

        if (task.getDueDate() != null) {
            long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(), task.getDueDate());
            model.addAttribute("daysUntilDue", daysUntilDue);
        }

        addHeaderStats(model, user);
        return "view-task";
    }

    // ========== OVERDUE TASKS ==========
    @GetMapping("/overdue")
    public String getOverdueTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model,
            Principal principal) {

        User user = getCurrentUser(principal);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage = taskService.getOverdueTasksByUser(user, pageable);

        addPaginationAttributes(model, taskPage, page, size, sortField, sortDir);
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());
        model.addAttribute("pageTitle", "Overdue Tasks");
        model.addAttribute("baseUrl", "/tasks/overdue");
        addHeaderStats(model, user);

        return "tasks";
    }

    // ========== TODAY TASKS ==========
    @GetMapping("/today")
    public String getTasksDueToday(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "priority") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model,
            Principal principal) {

        User user = getCurrentUser(principal);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage = taskService.getTasksDueTodayByUser(user, pageable);

        addPaginationAttributes(model, taskPage, page, size, sortField, sortDir);
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());
        model.addAttribute("pageTitle", "Tasks Due Today");
        model.addAttribute("baseUrl", "/tasks/today");
        addHeaderStats(model, user);

        return "tasks";
    }

    // ========== UPCOMING TASKS ==========
    @GetMapping("/upcoming")
    public String getUpcomingTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model,
            Principal principal) {

        User user = getCurrentUser(principal);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage = taskService.getUpcomingTasksByUser(user, pageable);

        addPaginationAttributes(model, taskPage, page, size, sortField, sortDir);
        model.addAttribute("statuses", taskService.getStatuses());
        model.addAttribute("priorities", taskService.getPriorities());
        model.addAttribute("pageTitle", "Upcoming Tasks");
        model.addAttribute("baseUrl", "/tasks/upcoming");
        addHeaderStats(model, user);

        return "tasks";
    }

    // ========== PAGINATION HELPER ==========
    private void addPaginationAttributes(Model model, Page<Task> taskPage,
                                         int page, int size,
                                         String sortField, String sortDir) {
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
        model.addAttribute("selectedStatus", null);
        model.addAttribute("selectedPriority", null);
        model.addAttribute("keyword", null);
        model.addAttribute("sortBy", null);
    }

    // ✅ UPDATED: Header stats per user
    private void addHeaderStats(Model model, User user) {
        model.addAttribute("pendingCount", taskService.countPendingTasksByUser(user));
        model.addAttribute("inProgressCount", taskService.countInProgressTasksByUser(user));
        model.addAttribute("doneCount", taskService.countDoneTasksByUser(user));
        model.addAttribute("overdueCount", taskService.countOverdueTasksByUser(user));
        model.addAttribute("totalTaskCount", taskService.countTasksByUser(user));
    }
}