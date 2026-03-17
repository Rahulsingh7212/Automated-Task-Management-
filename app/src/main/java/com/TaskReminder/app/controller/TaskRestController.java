package com.TaskReminder.app.controller;

import com.TaskReminder.app.entity.Task;
import com.TaskReminder.app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskRestController {

    @Autowired
    private TaskService taskService;


    // ==========================================
    // ✅ NEW: CALENDAR SPECIFIC ENDPOINT
    // ==========================================
    @GetMapping("/calendar")
    public ResponseEntity<List<Map<String, Object>>> getTasksForCalendar() {
        List<Task> tasks = taskService.getAllTasks();

        // Transform Task entities into FullCalendar-friendly JSON
        List<Map<String, Object>> calendarEvents = tasks.stream().map(task -> {
            Map<String, Object> event = new HashMap<>();
            event.put("id", task.getId());
            event.put("title", task.getTitle());
            event.put("start", task.getDueDate()); // Maps 'dueDate' to 'start' for the calendar

            // Optional: Color coding based on priority
            String color = "#3788d8"; // Default Blue
            if ("High".equalsIgnoreCase(task.getPriority())) {
                color = "#dc3545"; // Red
            } else if ("Medium".equalsIgnoreCase(task.getPriority())) {
                color = "#ffc107"; // Yellow/Orange
            } else if ("Low".equalsIgnoreCase(task.getPriority())) {
                color = "#28a745"; // Green
            }
            event.put("color", color);

            // Optional: Click URL (if you want the calendar item to link somewhere)
            event.put("url", "/tasks/view/" + task.getId());

            return event;
        }).collect(Collectors.toList());

        return new ResponseEntity<>(calendarEvents, HttpStatus.OK);
    }

    // GET all tasks
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService. getAllTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET all tasks with PAGINATION
    @GetMapping("/page")
    public ResponseEntity<Page<Task>> getAllTasksPaginated(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<Task> tasks = taskService.getAllTasks(pageable);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // GET task by ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskService. getTaskById(id);
        if (task != null) {
            return new ResponseEntity<>(task, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // POST - Create new task
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task savedTask = taskService.saveTask(task);
        return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
    }

    // PUT - Update task
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        Task existingTask = taskService.getTaskById(id);
        if (existingTask != null) {
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task. getDescription());
            existingTask.setDueDate(task. getDueDate());
            existingTask.setStatus(task.getStatus());
            existingTask. setPriority(task.getPriority());
            Task updatedTask = taskService.saveTask(existingTask);
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // DELETE - Delete task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        Task existingTask = taskService.getTaskById(id);
        if (existingTask != null) {
            taskService.deleteTask(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // GET tasks by status ✅ ADD THIS
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable String status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET tasks by status with PAGINATION
    @GetMapping("/status/{status}/page")
    public ResponseEntity<Page<Task>> getTasksByStatusPaginated(
            @PathVariable String status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<Task> tasks = taskService.getTasksByStatus(status, pageable);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // GET tasks by priority ✅ ADD THIS
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Task>> getTasksByPriority(@PathVariable String priority) {
        List<Task> tasks = taskService.getTasksByPriority(priority);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET tasks by priority with PAGINATION
    @GetMapping("/priority/{priority}/page")
    public ResponseEntity<Page<Task>> getTasksByPriorityPaginated(
            @PathVariable String priority,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<Task> tasks = taskService.getTasksByPriority(priority, pageable);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET tasks by exact due date
    @GetMapping("/date/{dueDate}")
    public ResponseEntity<List<Task>> getTasksByDueDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat. ISO.DATE) LocalDate dueDate) {
        List<Task> tasks = taskService.getTasksByDueDate(dueDate);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET tasks by exact due date with PAGINATION
    @GetMapping("/date/{dueDate}/page")
    public ResponseEntity<Page<Task>> getTasksByDueDatePaginated(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<Task> tasks = taskService.getTasksByDueDate(dueDate, pageable);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET tasks before a date
    @GetMapping("/date/before/{date}")
    public ResponseEntity<List<Task>> getTasksBeforeDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Task> tasks = taskService.getTasksBeforeDate(date);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET tasks before a date with PAGINATION
    @GetMapping("/date/before/{date}/page")
    public ResponseEntity<Page<Task>> getTasksBeforeDatePaginated(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<Task> tasks = taskService.getTasksBeforeDate(date, pageable);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET tasks after a date
    @GetMapping("/date/after/{date}")
    public ResponseEntity<List<Task>> getTasksAfterDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Task> tasks = taskService.getTasksAfterDate(date);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET tasks after a date with PAGINATION
    @GetMapping("/date/after/{date}/page")
    public ResponseEntity<Page<Task>> getTasksAfterDatePaginated(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<Task> tasks = taskService.getTasksAfterDate(date, pageable);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }


    // ✅ GET tasks between two dates (using query parameters)
    @GetMapping("/date/between")
    public ResponseEntity<List<Task>> getTasksBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Task> tasks = taskService. getTasksBetweenDates(startDate, endDate);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET tasks between two dates with PAGINATION
    @GetMapping("/date/between/page")
    public ResponseEntity<Page<Task>> getTasksBetweenDatesPaginated(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<Task> tasks = taskService.getTasksBetweenDates(startDate, endDate, pageable);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }
}
