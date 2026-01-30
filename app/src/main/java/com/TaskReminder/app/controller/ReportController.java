package com.TaskReminder.app.controller;

import com.TaskReminder.app.service.TaskService;
import org.springframework. beans.factory.annotation.Autowired;
import org.springframework. stereotype.Controller;
import org. springframework.ui.Model;
import org.springframework.web.bind. annotation. GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java. time.format.DateTimeFormatter;


@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public String showReports(Model model) {

        // Task counts by status
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
        double overdueRate = totalTasks > 0 ? (double) overdueTasks / totalTasks * 100 : 0;
        double pendingRate = totalTasks > 0 ? (double) pendingTasks / totalTasks * 100 : 0;
        double inProgressRate = totalTasks > 0 ? (double) inProgressTasks / totalTasks * 100 : 0;

        model.addAttribute("completionRate", String.format("%.1f", completionRate));
        model.addAttribute("overdueRate", String.format("%.1f", overdueRate));
        model.addAttribute("pendingRate", String.format("%.1f", pendingRate));
        model.addAttribute("inProgressRate", String.format("%.1f", inProgressRate));

        // Priority breakdown
        long lowPriority = taskService.countTasksByPriority("LOW");
        long mediumPriority = taskService.countTasksByPriority("MEDIUM");
        long highPriority = taskService.countTasksByPriority("HIGH");

        model.addAttribute("lowPriorityTasks", lowPriority);
        model.addAttribute("mediumPriorityTasks", mediumPriority);
        model.addAttribute("highPriorityTasks", highPriority);

        // Current date for report
        model.addAttribute("reportDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        // Header stats (same as tasks page)
        model.addAttribute("pendingCount", pendingTasks);
        model.addAttribute("inProgressCount", inProgressTasks);
        model.addAttribute("doneCount", doneTasks);
        model.addAttribute("overdueCount", overdueTasks);
        model.addAttribute("totalTaskCount", totalTasks);

        return "reports";
    }
}