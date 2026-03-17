package com.TaskReminder.app.controller;

import com.TaskReminder.app.entity.User;
import com.TaskReminder.app.service.TaskService;
import com.TaskReminder.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;  // ✅ FIX: Was missing

    // ========== HOME PAGE REDIRECTS ==========

    @GetMapping("/")
    public String home() {
        return "redirect:/tasks";
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:/tasks";
    }

    // ========== TEST/WELCOME PAGE ==========

    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("message", "Welcome to the Task Reminder App! By Anurag");
        return "index";
    }

    @GetMapping("/welcome")
    public String welcome(Model model, Principal principal) {
        User user = getCurrentUser(principal);  // ✅ FIX

        model.addAttribute("message", "Welcome to the Task Reminder App! By Anurag");

        model.addAttribute("totalTasks", taskService.countTasksByUser(user));
        model.addAttribute("pendingTasks", taskService.countPendingTasksByUser(user));
        model.addAttribute("inProgressTasks", taskService.countInProgressTasksByUser(user));
        model.addAttribute("doneTasks", taskService.countDoneTasksByUser(user));
        model.addAttribute("overdueTasks", taskService.getOverdueTasksByUser(user).size());
        model.addAttribute("todayTasks", taskService.getTasksDueTodayByUser(user).size());
        model.addAttribute("upcomingTasks", taskService.getUpcomingTasksByUser(user).size());

        model.addAttribute("currentUser", user);
        addHeaderStats(model, user);

        return "index";
    }

    // ========== DASHBOARD PAGE ==========

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {  // ✅ FIX: Added Principal
        User user = getCurrentUser(principal);  // ✅ FIX: Now user is defined

        model.addAttribute("message", "Task Reminder Dashboard");

        // Task counts - using user-specific methods
        model.addAttribute("totalTasks", taskService.countTasksByUser(user));
        model.addAttribute("pendingTasks", taskService.countPendingTasksByUser(user));
        model.addAttribute("inProgressTasks", taskService.countInProgressTasksByUser(user));
        model.addAttribute("doneTasks", taskService.countDoneTasksByUser(user));

        // Priority counts
        model.addAttribute("highPriorityTasks",
                taskService.getTasksByUserAndPriority(user, "HIGH").size());
        model.addAttribute("mediumPriorityTasks",
                taskService.getTasksByUserAndPriority(user, "MEDIUM").size());
        model.addAttribute("lowPriorityTasks",
                taskService.getTasksByUserAndPriority(user, "LOW").size());

        // Date-based counts
        model.addAttribute("overdueTasks",
                taskService.getOverdueTasksByUser(user).size());
        model.addAttribute("todayTasks",
                taskService.getTasksDueTodayByUser(user).size());
        model.addAttribute("upcomingTasks",
                taskService.getUpcomingTasksByUser(user).size());

        // Recent tasks for this user
        model.addAttribute("recentTasks", taskService.getTasksByUser(user));

        // ✅ FIX: user is now properly defined
        model.addAttribute("currentUser", user);

        // Header stats
        addHeaderStats(model, user);

        return "dashboard";
    }

    // ========== REPORTS PAGE ==========

    @GetMapping("/reports")
    public String reports(Model model, Principal principal) {
        User user = getCurrentUser(principal);

        model.addAttribute("tasks", taskService.getTasksByUser(user));
        model.addAttribute("totalTasks", taskService.countTasksByUser(user));
        model.addAttribute("pendingTasks", taskService.countPendingTasksByUser(user));
        model.addAttribute("inProgressTasks", taskService.countInProgressTasksByUser(user));
        model.addAttribute("doneTasks", taskService.countDoneTasksByUser(user));

        model.addAttribute("highPriorityTasks",
                taskService.getTasksByUserAndPriority(user, "HIGH").size());
        model.addAttribute("mediumPriorityTasks",
                taskService.getTasksByUserAndPriority(user, "MEDIUM").size());
        model.addAttribute("lowPriorityTasks",
                taskService.getTasksByUserAndPriority(user, "LOW").size());

        model.addAttribute("overdueTasks",
                taskService.getOverdueTasksByUser(user).size());
        model.addAttribute("todayTasks",
                taskService.getTasksDueTodayByUser(user).size());
        model.addAttribute("upcomingTasks",
                taskService.getUpcomingTasksByUser(user).size());

        model.addAttribute("currentUser", user);
        addHeaderStats(model, user);

        return "reports";
    }

    // ========== CALENDAR PAGE ==========

    @GetMapping("/calendar")
    public String calendar(Model model, Principal principal) {
        User user = getCurrentUser(principal);

        model.addAttribute("tasks", taskService.getTasksByUser(user));
        model.addAttribute("currentUser", user);
        addHeaderStats(model, user);

        return "calendar";
    }

    // ========== ABOUT PAGE ==========

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("appName", "Task Reminder App");
        model.addAttribute("author", "Anurag");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("description",
                "A simple task management application built with Spring Boot and Thymeleaf.");
        return "about";
    }

    // ==========================================================
    // ✅ HELPER METHODS
    // ==========================================================

    /**
     * Get current logged-in user from Principal
     */
    private User getCurrentUser(Principal principal) {
        return userService.getCurrentUser(principal.getName());
    }

    /**
     * Add header statistics to model for navbar display
     */
    private void addHeaderStats(Model model, User user) {
        model.addAttribute("pendingCount", taskService.countPendingTasksByUser(user));
        model.addAttribute("inProgressCount", taskService.countInProgressTasksByUser(user));
        model.addAttribute("doneCount", taskService.countDoneTasksByUser(user));
        model.addAttribute("overdueCount", taskService.countOverdueTasksByUser(user));
        model.addAttribute("totalTaskCount", taskService.countTasksByUser(user));
    }
}