package com.tasktracker.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test") // Use test properties (H2 db) so we don't mess up the dev DB
public class SeleniumUserStoryTests {

    @LocalServerPort
    private int port;

    private static WebDriver driver;
    private WebDriverWait wait;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/index.html";
    }

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // Removed headless argument so the browser visibly opens for the user
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-proxy-server");
        options.addArguments("start-maximized");
        driver = new ChromeDriver(options);
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(500); // Give Tomcat a tiny moment
        // No longer clearing local storage here so state persists across Ordered tests
    }

    @Test
    @Order(1)
    @DisplayName("US1 & US2: Create Account and Login")
    void testRegistrationAndLogin() throws InterruptedException {
        driver.get(getBaseUrl());
        
        // Focus on Registration Form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reg-username"))).sendKeys("seleniumuser");
        driver.findElement(By.id("reg-email")).sendKeys("selenium@example.com");
        driver.findElement(By.id("reg-password")).sendKeys("password123");
        driver.findElement(By.id("reg-fullname")).sendKeys("Selenium User");

        driver.findElement(By.id("btn-register")).click();

        // Handle alert
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertThat(alert.getText()).contains("Registered");
        alert.accept();

        // Focus on Login Form
        driver.findElement(By.id("login-username")).sendKeys("seleniumuser");
        driver.findElement(By.id("login-password")).sendKeys("password123");
        Thread.sleep(1000); // Wait so user can see input
        driver.findElement(By.id("btn-login")).click();

        // Wait for dashboard to appear
        WebElement dashboard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-section")));
        assertThat(dashboard.isDisplayed()).isTrue();

        WebElement greeting = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-greeting")));
        assertThat(greeting.getText()).contains("Hi, seleniumuser");
    }

    @Test
    @Order(2)
    @DisplayName("US3: View and Update Profile")
    void testUpdateProfile() throws InterruptedException {
        driver.get(getBaseUrl());
        
        WebElement bioInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("prof-bio")));
        bioInput.sendKeys("This is an automated test bio.");
        Thread.sleep(1000); // Visual delay
        driver.findElement(By.id("btn-update-profile")).click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertThat(alert.getText()).contains("Profile updated");
        alert.accept();
    }

    @Test
    @Order(3)
    @DisplayName("US4 & US5 & US8 & US9: Create, List, Filter, and Search Tasks")
    void testTaskManagement() throws InterruptedException {
        driver.get(getBaseUrl());
        
        // Ensure we are logged in from previous steps
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-section")));

        // Create Task
        driver.findElement(By.id("task-title")).sendKeys("Automated Task");
        driver.findElement(By.id("task-desc")).sendKeys("Created by Selenium");
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2026-12-31';", driver.findElement(By.id("task-due-date")));
        Thread.sleep(1000); // Visual delay
        driver.findElement(By.id("btn-create-task")).click();

        // Wait a bit for the UI to fetch
        Thread.sleep(1500);

        // US5: View all assigned tasks (My Tasks)
        driver.findElement(By.id("btn-my-tasks")).click();
        Thread.sleep(1000);
        
        // US8: Filter Tasks
        Select statusFilter = new Select(driver.findElement(By.id("status-filter")));
        statusFilter.selectByValue("OPEN");
        Thread.sleep(1000); // Wait for fetch

        // US9: Search Tasks
        driver.findElement(By.id("search-input")).clear();
        driver.findElement(By.id("search-input")).sendKeys("Automated");
        driver.findElement(By.id("btn-search")).click();
        
        WebElement taskList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tasks-list")));
        assertThat(taskList.getText()).contains("Automated Task");
    }

    @Test
    @Order(4)
    @DisplayName("US11: Create Team")
    void testCreateTeam() throws InterruptedException {
        driver.get(getBaseUrl());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-section")));
        
        driver.findElement(By.id("new-team-name")).sendKeys("Automation Team");
        Thread.sleep(1000); // Visual delay
        driver.findElement(By.id("btn-create-team")).click();
        
        Thread.sleep(1500);
        
        WebElement teamList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("team-list")));
        assertThat(teamList.getText()).contains("Automation Team");
    }

    @Test
    @Order(5)
    @DisplayName("US6, US7, US10: Team Collaboration (Comments/Attachments/Assign/Complete)")
    void testTaskCollaboration() throws InterruptedException, IOException {
        driver.get(getBaseUrl());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-section")));
        
        // Open Details of first task
        wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(By.id("tasks-list"), By.tagName("button")));
        WebElement taskList = driver.findElement(By.id("tasks-list"));
        taskList.findElement(By.tagName("button")).click(); // Details button
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("task-details-container")));
        
        // US10: Add Comment
        driver.findElement(By.id("new-comment")).sendKeys("This is a Selenium test comment");
        Thread.sleep(1000); // Visual delay
        driver.findElement(By.id("btn-add-comment")).click();
        Thread.sleep(1500); // Wait for fetch
        
        WebElement commentList = driver.findElement(By.id("comment-list"));
        assertThat(commentList.getText()).contains("Selenium test comment");

        // US10: Add Attachment
        File tempFile = File.createTempFile("selenium-test", ".txt");
        tempFile.deleteOnExit();
        driver.findElement(By.id("new-attachment")).sendKeys(tempFile.getAbsolutePath());
        driver.findElement(By.id("btn-upload")).click();
        Thread.sleep(1000);
        WebElement attachmentList = driver.findElement(By.id("attachment-list"));
        assertThat(attachmentList.getText()).contains("selenium-test");

        // US6: Mark Completed
        driver.findElement(By.id("btn-mark-completed")).click();
        Thread.sleep(1000); // Wait for fetch
        assertThat(driver.findElement(By.id("det-status")).getText()).isEqualTo("COMPLETED");

        // Try Assigning Task
        driver.findElement(By.id("assign-user-id")).clear();
        driver.findElement(By.id("assign-user-id")).sendKeys("1");
        Thread.sleep(1000); // Visual delay
        driver.findElement(By.id("btn-assign-task")).click();
        Thread.sleep(1500);
        assertThat(driver.findElement(By.id("det-assignee")).getText()).contains("selenium");
    }

    @Test
    @Order(6)
    @DisplayName("US12: Logout")
    void testLogout() throws InterruptedException {
        driver.get(getBaseUrl());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-section")));
        
        Thread.sleep(1000); // Visual delay
        driver.findElement(By.id("btn-logout")).click();
        Thread.sleep(2000); // Final delay at end of suite
        
        // Should show auth section
        WebElement authSection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("auth-section")));
        assertThat(authSection.isDisplayed()).isTrue();
    }
}
