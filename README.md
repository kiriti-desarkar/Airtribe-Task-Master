# Task Tracker — Task Tracking & Management Application

A robust, production-ready backend system for task tracking and team collaboration, built with **Spring Boot 3**, **Spring Security**, **JWT Authentication**, **MySQL**, and **WebSocket** real-time notifications.

## 🚀 Features

### Core Features
- **User Authentication & Management**
  - Secure registration and login with JWT token-based authentication
  - BCrypt password hashing
  - Profile management (view and update)
  - Stateless session management

- **Task Management**
  - Full CRUD operations for tasks
  - Task attributes: title, description, status, priority, due date
  - Task filtering by status, priority, assignee, and team
  - Full-text search across task titles and descriptions
  - Sorting by any field (created date, due date, priority, etc.)
  - Pagination support for large datasets

- **Team/Project Collaboration**
  - Create and manage teams/projects
  - Invite team members with role-based access (Owner, Admin, Member)
  - Assign tasks within teams
  - View team-specific task boards

- **Comments & Attachments**
  - Add threaded comments to tasks for collaboration
  - Upload and download file attachments (up to 10MB)
  - Attachment metadata tracking

- **Real-Time Notifications** *(Optional Extension)*
  - WebSocket-based real-time notification delivery via STOMP
  - Notification types: task assigned, task updated, task completed, comment added, team invitation
  - Mark as read (individual and bulk)
  - Unread notification count

### API Documentation
- Interactive **Swagger UI** available at `/swagger-ui.html`
- OpenAPI 3.0 specification at `/api-docs`

---

## 🛠 Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 3.2 | Application framework |
| Spring Security 6 | Authentication & authorization |
| Spring Data JPA | Database access layer |
| MySQL | Primary database |
| JWT (jjwt 0.12) | Token-based authentication |
| Spring WebSocket | Real-time notifications |
| Lombok | Boilerplate reduction |
| Springdoc OpenAPI | API documentation |
| Maven | Dependency management |
| H2 Database | Test database |
| JUnit 5 | Testing framework |

---

## 📋 Prerequisites

- **Java 17+** ([Download](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ([Download](https://dev.mysql.com/downloads/))

---

## ⚙️ Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/task-tracker.git
cd task-tracker
```

### 2. Configure Database

The application will automatically create the database `tasktracker` if it doesn't exist. Update `src/main/resources/application.properties` if your MySQL credentials differ:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tasktracker?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

### 5. Access API Documentation

Open your browser and navigate to:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT token |
| POST | `/api/auth/logout` | Logout (client-side) |

### User Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Get current user profile |
| PUT | `/api/users/me` | Update profile |
| GET | `/api/users/{id}` | Get user by ID |

### Tasks
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tasks` | Create a task |
| GET | `/api/tasks` | List/filter/search tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| PUT | `/api/tasks/{id}` | Update task |
| DELETE | `/api/tasks/{id}` | Delete task |
| PATCH | `/api/tasks/{id}/status` | Update task status |
| PATCH | `/api/tasks/{id}/assign` | Assign task to user |
| GET | `/api/tasks/my-tasks` | Get my assigned tasks |

### Teams
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/teams` | Create team |
| GET | `/api/teams` | List my teams |
| GET | `/api/teams/{id}` | Get team details |
| PUT | `/api/teams/{id}` | Update team |
| DELETE | `/api/teams/{id}` | Delete team |
| POST | `/api/teams/{id}/members` | Add team member |
| DELETE | `/api/teams/{id}/members/{userId}` | Remove member |
| GET | `/api/teams/{id}/tasks` | List team tasks |

### Comments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tasks/{taskId}/comments` | Add comment |
| GET | `/api/tasks/{taskId}/comments` | List comments |
| DELETE | `/api/tasks/{taskId}/comments/{id}` | Delete comment |

### Attachments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tasks/{taskId}/attachments` | Upload file |
| GET | `/api/tasks/{taskId}/attachments` | List attachments |
| GET | `/api/tasks/{taskId}/attachments/{id}/download` | Download file |
| DELETE | `/api/tasks/{taskId}/attachments/{id}` | Delete attachment |

### Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications` | List notifications |
| GET | `/api/notifications/unread-count` | Get unread count |
| PATCH | `/api/notifications/{id}/read` | Mark as read |
| PATCH | `/api/notifications/read-all` | Mark all as read |
| WebSocket | `/ws` | Real-time notification stream |

---

## 🔐 Authentication Flow

1. **Register** a new account via `POST /api/auth/register`
2. **Login** via `POST /api/auth/login` — receive a JWT `accessToken`
3. **Include the token** in all subsequent requests:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

---

## 📁 Project Structure

```
src/main/java/com/tasktracker/
├── TaskTrackerApplication.java     # Main application entry point
├── config/                          # Configuration classes
│   ├── SecurityConfig.java          # Spring Security configuration
│   ├── WebSocketConfig.java         # WebSocket/STOMP configuration
│   ├── CorsConfig.java             # CORS configuration
│   └── OpenApiConfig.java          # Swagger/OpenAPI configuration
├── security/                        # Security components
│   ├── JwtTokenProvider.java        # JWT token generation & validation
│   ├── JwtAuthenticationFilter.java # JWT request filter
│   └── CustomUserDetailsService.java
├── controller/                      # REST API controllers
│   ├── AuthController.java
│   ├── UserController.java
│   ├── TaskController.java
│   ├── TeamController.java
│   ├── CommentController.java
│   ├── AttachmentController.java
│   └── NotificationController.java
├── service/                         # Business logic layer
│   ├── AuthService.java
│   ├── UserService.java
│   ├── TaskService.java
│   ├── TeamService.java
│   ├── CommentService.java
│   ├── AttachmentService.java
│   └── NotificationService.java
├── repository/                      # Data access layer (Spring Data JPA)
├── entity/                          # JPA entities  
├── dto/                             # Data Transfer Objects
│   ├── request/                     # Request DTOs with validation
│   └── response/                    # Response DTOs
├── enums/                           # Enum types
└── exception/                       # Custom exceptions & global handler
```

---

## 🧪 Testing

Run all tests:
```bash
mvn test
```

Tests use **H2 in-memory database** for fast, isolated testing. Test coverage includes:
- Authentication flows (register, login, validation)
- Task CRUD operations
- Task filtering and searching
- Authorization enforcement

---

## 🏗 Design Decisions

- **Stateless Authentication**: JWT tokens enable horizontal scaling without session storage
- **Layered Architecture**: Clear separation between controllers, services, and repositories
- **Global Exception Handling**: Consistent error responses via `@ControllerAdvice`
- **DTO Pattern**: Decouples API contracts from database entities
- **Pagination**: All list endpoints support pagination for performance
- **Database Indexes**: Optimized queries on frequently filtered columns

---

## 📝 License

This project is created as part of the Airtribe backend development assignment.
