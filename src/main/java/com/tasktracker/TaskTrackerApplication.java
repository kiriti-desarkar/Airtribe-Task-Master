package com.tasktracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Task Tracker application.
 * <p>
 * This application provides a RESTful API for task tracking and management,
 * supporting team collaboration, real-time notifications, and secure authentication.
 * </p>
 */
@SpringBootApplication
public class TaskTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskTrackerApplication.class, args);
    }
}
