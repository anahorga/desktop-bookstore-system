# 📚 Library Management System

A JavaFX desktop application built for library employees, designed with **Layered Architecture**, **SOLID principles**, and clean **OOP** design patterns. The system supports role-based access for Administrators, Employees, and Customers, providing full book inventory management, order tracking, and automated PDF reporting.

---

## ✨ Features

- **Role-Based Access Control** — Separate dashboards for `Administrator`, `Employee`, and `Customer` roles with granular rights
- **Book Inventory Management** — Full CRUD operations: add, delete, sell, and view books in real time
- **Order Tracking** — Every book sale is recorded as an order with timestamp, user, price and stock data
- **PDF Report Generation** — Admins can generate monthly sales reports per employee (powered by iText)
- **Secure Authentication** — Passwords hashed with SHA-256 before storage; no plain-text credentials in DB
- **In-Memory Caching** — Cache Decorator pattern on the book repository reduces redundant DB queries
- **Bootstrap Script** — Automated schema creation and role/rights seeding on first run
- **Input Validation** — Email format and password strength validation before any user is persisted
- **DTO Pattern** — Views consume DTOs, never raw entities, preventing accidental data exposure

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| UI | JavaFX, GridPane layouts |
| Business Logic | Java 17+, Service layer interfaces |
| Persistence | JDBC, MySQL 8 |
| Security | SHA-256 (MessageDigest) |
| PDF Generation | iText 7 |
| Design Patterns | Singleton, Builder, Decorator, Cache, DTO |
| Testing | JUnit 5 |
| Build Tool | Maven |

---

## 🏗️ Architecture

The project follows a strict **Layered Architecture** with clear separation of concerns:

```
┌─────────────────────────────────────┐
│           View (JavaFX)             │  ← DTOs only, no entity access
├─────────────────────────────────────┤
│          Controller Layer           │  ← Handles UI events, delegates to services
├─────────────────────────────────────┤
│           Service Layer             │  ← Business logic, validation, hashing
├─────────────────────────────────────┤
│         Repository Layer            │  ← JDBC queries, Cache Decorator
├─────────────────────────────────────┤
│        Database (MySQL 8)           │
└─────────────────────────────────────┘
```

---

## 📁 Project Structure

```
src/
├── main/java/
│   ├── controller/          # AdminController, BookController, LoginController
│   ├── database/            # Bootstrap, Constants, Connection wrappers
│   ├── launcher/            # Singleton ComponentFactories (Admin, Employee, Login)
│   ├── mapper/              # BookMapper, UserMapper (Entity ↔ DTO)
│   ├── model/               # Entities: Book, User, Role, Right, Order, Report
│   │   ├── builder/         # Builder pattern for all models
│   │   └── validation/      # UserValidator, Notification<T>
│   ├── repository/
│   │   ├── book/            # BookRepository + MySQL + Cache Decorator
│   │   ├── order/           # OrderRepository + MySQL
│   │   ├── security/        # RightsRolesRepository + MySQL
│   │   └── user/            # UserRepository + MySQL
│   ├── service/
│   │   ├── book/            # BookService + Impl
│   │   ├── order/           # OrderService + Impl
│   │   └── user/            # AuthenticationService + UserService + Impls
│   └── view/                # AdminView, BookView, LoginView
│       └── model/           # BookDTO, UserDTO + builders
└── test/java/
    ├── BookRepositoryMockTest.java
    └── BookRepositoryMySQLTest.java
```

---

## ⚙️ Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+

### 1. Clone the repository

```bash
git clone https://github.com/your-username/libradesk.git
cd libradesk
```

### 2. Configure the database connection

Edit `src/main/java/database/JDBConnectionWrapper.java`:

```java
private static final String USER = "your_mysql_user";
private static final String PASSWORD = "your_mysql_password";
```

> ⚠️ **Security note:** Never commit real credentials to version control. Use environment variables or a `.env` file and add it to `.gitignore`.

### 3. Run the Bootstrap script

Run `Bootstrap.main()` once to create all tables and seed roles, rights, and permissions:

```bash
mvn exec:java -Dexec.mainClass="database.Bootstrap"
```

### 4. Launch the application

```bash
mvn javafx:run
```

---

## 🔐 Roles & Permissions

| Role | Rights |
|---|---|
| `administrator` | create/delete/update user, create/delete/update/sell book, buy/return book |
| `employee` | create book, delete book, update book, sell book |
| `customer` | buy book, return book |

---

## 🗃️ Database Schema

```
user ──< user_role >── role ──< role_right >── right
book
order  (links user → book, with price, stock, timestamp)
```

Both `library` (production) and `test_library` (test) schemas are supported — controlled by the `componentsForTest` flag in the component factories.

---

## 🧪 Running Tests

```bash
mvn test
```

Tests cover both the mock repository (in-memory) and the MySQL repository (requires `test_library` schema to be bootstrapped).

---

## 🧩 Design Patterns Used

**Singleton** — Each `ComponentFactory` (Login, Employee, Admin) is a thread-safe double-checked locking Singleton, managing the full dependency graph for its context.

**Builder** — Every model (`Book`, `User`, `Order`, `Report`) and DTO has a dedicated builder, enabling readable and flexible object construction.

**Decorator** — `BookRepositoryCacheDecorator` wraps the MySQL repository to add transparent in-memory caching, invalidated on any write operation.

**DTO** — Views only interact with `BookDTO` and `UserDTO`, keeping model details out of the presentation layer.

**Notification** — A generic `Notification<T>` wrapper propagates either a result or a list of validation errors, avoiding exception-based flow control for business errors.

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
