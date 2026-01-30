package com.TaskReminder.app.controller;

import com.TaskReminder.app.entity.Task;
import com.TaskReminder.app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private TaskService taskService;

    // ========== HOME PAGE REDIRECTS ==========

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:/tasks";
    }

    // ========== TEST/WELCOME PAGE ==========

    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("message", "Welcome to the Task Reminder App! " + "By Anurag");
        return "index";
    }

    @GetMapping("/welcome")
    public String welcome(Model model) {
        model.addAttribute("message", "Welcome to the Task Reminder App!  " + "By Anurag");
        model.addAttribute("totalTasks", taskService.getAllTasks().size());
        model.addAttribute("pendingTasks", taskService. getTasksByStatus("PENDING").size());
        model.addAttribute("inProgressTasks", taskService.getTasksByStatus("IN_PROGRESS").size());
        model.addAttribute("doneTasks", taskService.getTasksByStatus("DONE").size());
        model.addAttribute("overdueTasks", taskService.getOverdueTasks().size());
        model.addAttribute("todayTasks", taskService.getTasksDueToday().size());
        model.addAttribute("upcomingTasks", taskService.getUpcomingTasks().size());
        return "index";
    }

    // ========== DASHBOARD PAGE (UPDATED) ==========

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // Task Counts
        long totalTasks = taskService.countAllTasks();
        long pendingTasks = taskService.countPendingTasks();
        long inProgressTasks = taskService.countInProgressTasks();
        long doneTasks = taskService.countDoneTasks();
        long overdueTasks = taskService.countOverdueTasks();
        long todayTasks = taskService.countTasksDueToday();
        long upcomingTasks = taskService.countUpcomingTasks();
        long highPriorityTasks = taskService.countHighPriorityTasks();

        // Add counts to model
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("inProgressTasks", inProgressTasks);
        model.addAttribute("doneTasks", doneTasks);
        model.addAttribute("overdueTasks", overdueTasks);
        model.addAttribute("todayTasks", todayTasks);
        model.addAttribute("upcomingTasks", upcomingTasks);
        model.addAttribute("highPriorityTasks", highPriorityTasks);

        // Calculate percentages
        double completionRate = totalTasks > 0 ? (double) doneTasks / totalTasks * 100 : 0;
        double pendingRate = totalTasks > 0 ? (double) pendingTasks / totalTasks * 100 : 0;
        double inProgressRate = totalTasks > 0 ? (double) inProgressTasks / totalTasks * 100 : 0;
        double overdueRate = totalTasks > 0 ? (double) overdueTasks / totalTasks * 100 : 0;

        model. addAttribute("completionRate", String.format("%.1f", completionRate));
        model.addAttribute("pendingRate", String. format("%.1f", pendingRate));
        model.addAttribute("inProgressRate", String.format("%.1f", inProgressRate));
        model.addAttribute("overdueRate", String.format("%.1f", overdueRate));

        // Recent Tasks (latest 5)
        List<Task> recentTasks = taskService. getAllTasksOrderedByCreatedAt();
        if (recentTasks.size() > 5) {
            recentTasks = recentTasks.subList(0, 5);
        }
        model.addAttribute("recentTasks", recentTasks);

        // Overdue Tasks List (latest 5)
        List<Task> overdueTasksList = taskService.getOverdueTasks();
        if (overdueTasksList.size() > 5) {
            overdueTasksList = overdueTasksList.subList(0, 5);
        }
        model.addAttribute("overdueTasksList", overdueTasksList);

        // Today's Tasks
        List<Task> todayTasksList = taskService.getTasksDueToday();
        model.addAttribute("todayTasksList", todayTasksList);

        // High Priority Tasks (not done)
        List<Task> highPriorityTasksList = taskService.getAllTasks().stream()
                .filter(task -> "HIGH".equals(task.getPriority()) && !"DONE".equals(task.getStatus()))
                .limit(5)
                .toList();
        model.addAttribute("highPriorityTasksList", highPriorityTasksList);

        // Header Stats (for consistency with other pages)
        model.addAttribute("pendingCount", pendingTasks);
        model.addAttribute("inProgressCount", inProgressTasks);
        model.addAttribute("doneCount", doneTasks);
        model.addAttribute("overdueCount", overdueTasks);
        model.addAttribute("totalTaskCount", totalTasks);

        return "dashboard";
    }

    // ========== ABOUT PAGE ==========

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("appName", "Task Reminder App");
        model.addAttribute("author", "Anurag");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("description", "A simple task management application built with Spring Boot and Thymeleaf.");
        return "about";
    }
}