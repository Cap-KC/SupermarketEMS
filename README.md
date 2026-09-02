# 🏪 Supermarket Employee Management System

A desktop-based **Employee Management System (EMS)** developed using **JavaFX, Java, and MySQL** to streamline employee and HR-related operations in a supermarket environment.

The system provides role-based access for **Administrators and Employees**, with features for employee management, attendance tracking using QR codes, leave request processing, and administrative audit logging.

---

## 📸 Screenshots

### Admin Dashboard & QR Attendance Scanner

|                   Admin Dashboard                  |                   QR Attendance Scanner                   |
| :------------------------------------------------: | :-------------------------------------------------------: |
| ![Admin Dashboard](docs/screenshots/dashboard.png) | ![QR Attendance Scanner](docs/screenshots/qr_scanner.png) |

### Leave Management & Audit Logs

|                    Leave Management                    |                   Audit Logs                   |
| :----------------------------------------------------: | :--------------------------------------------: |
| ![Leave Requests](docs/screenshots/leave_requests.png) | ![Audit Logs](docs/screenshots/audit_logs.png) |


## ✨ Features

### 👨‍💼 Role-Based Access Control

The system provides different functionality and access permissions based on the user's role.

* **Admin**

  * Manage employee records
  * Manage attendance
  * Review leave requests
  * Monitor audit logs
  * Access administrative features

* **Employee**

  * View personal information
  * Track attendance
  * Submit leave requests
  * View leave request status

### 📷 QR Code Attendance

* QR-based employee identification
* Real-time QR code scanning using a webcam
* Employee clock-in and clock-out
* Attendance records stored in MySQL

### 📝 Leave Management

Employees can submit leave requests through the system, while administrators can review and process them.

* Submit leave requests
* View pending requests
* Approve or reject requests
* Track leave request status
* Maintain employee leave balances

### 📋 Audit Logging

Administrative actions are recorded to provide accountability and traceability.

Audit records can include:

* User/action information
* Action type
* Timestamp
* IP address
* Relevant system activity

### 👥 Employee Management

* Centralized employee records
* Add and update employee information
* Manage employee status
* View employee details
* Maintain employee-related records

---

## 🛠️ Tech Stack

| Technology         | Purpose                         |
| ------------------ | ------------------------------- |
| **Java 21**        | Core programming language       |
| **JavaFX 21**      | Desktop GUI development         |
| **FXML**           | UI layout and view definition   |
| **CSS**            | JavaFX UI styling               |
| **MySQL 8.0+**     | Relational database             |
| **JDBC**           | Database connectivity           |
| **Apache Maven**   | Dependency and build management |
| **ZXing**          | QR code generation and decoding |
| **Webcam Capture** | Webcam integration              |
| **jBCrypt**        | Password hashing                |

### Architecture

The application follows a layered structure based on **MVC and DAO principles**:

```text
┌─────────────────────────┐
│       JavaFX UI         │
│      FXML + CSS         │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│      Controllers        │
│     Application Logic   │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│          DAO            │
│      JDBC Operations    │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│        MySQL DB         │
└─────────────────────────┘
```

The main layers are:

* **Model** — Represents application data and entities.
* **View** — JavaFX FXML interfaces and CSS styles.
* **Controller** — Handles UI events and application flow.
* **DAO** — Handles database operations using JDBC.
* **Database** — Stores persistent application data.

---

## 📦 Dependencies

The project uses the following major dependencies:

### JavaFX

Used to build the desktop graphical user interface.

### MySQL Connector/J

Provides JDBC connectivity between the Java application and MySQL database.

### ZXing

Used for QR code generation and decoding.

### Webcam Capture

Provides access to the computer's webcam for real-time QR code scanning.

### jBCrypt

Used for securely hashing user passwords before storing them in the database.

---

## ⚙️ Prerequisites

Before running the application, make sure you have the following installed:

* **JDK 21** or later
* **Apache Maven 3.8+**
* **MySQL Server 8.0+**
* **MySQL Workbench** *(recommended)*
* A working **webcam** for QR attendance functionality
* Git

You can verify the installations using:

```bash
java -version
mvn -version
mysql --version
git --version
```

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/SupermarketEMS.git
cd SupermarketEMS
```

Replace `YOUR_USERNAME` with your GitHub username.

---

### 2. Create the Database

Open **MySQL Workbench** or another MySQL client and create the database:

```sql
CREATE DATABASE supermarket_ems;

USE supermarket_ems;
```

---

### 3. Import the Database Schema

If the project contains a database schema file, execute:

```text
src/main/resources/database/schema.sql
```

The SQL file should create the required tables, relationships, constraints, and sample data.

---

### 4. Configure Database Connection

Update your database configuration with your local MySQL credentials.

For example:

```properties
db.url=jdbc:mysql://localhost:3306/supermarket_ems
db.user=your_mysql_username
db.password=your_mysql_password
```

> ⚠️ **Security:** Do not commit your actual MySQL password or other sensitive credentials to GitHub. Consider using a local configuration file and adding it to `.gitignore`.

---

### 5. Build the Project

Run:

```bash
mvn clean compile
```

To package the application:

```bash
mvn clean package
```

---

### 6. Run the Application

If the JavaFX Maven plugin is configured in `pom.xml`, run:

```bash
mvn javafx:run
```

The JavaFX application should launch.

---

## 📁 Project Structure

```text
SupermarketEMS/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/supermarketems/
│       │       │
│       │       ├── controller/
│       │       │   └── # JavaFX FXML Controllers
│       │       │
│       │       ├── dao/
│       │       │   └── # Data Access Objects
│       │       │
│       │       ├── model/
│       │       │   └── # Application Data Models
│       │       │
│       │       └── HelloApplication.java
│       │
│       └── resources/
│           ├── com/example/supermarketems/
│           │   ├── *.fxml
│           │   └── *.css
│           │
│           └── database/
│               └── schema.sql
│
├── docs/
│   └── screenshots/
│       ├── dashboard.png
│       ├── qr_scanner.png
│       ├── leave_requests.png
│       └── audit_logs.png
│
├── pom.xml
├── .gitignore
└── README.md
```

---

## 🔄 Application Flow

The general application flow is:

```text
User
  │
  ▼
JavaFX / FXML Interface
  │
  ▼
Controller
  │
  ▼
DAO
  │
  ▼
JDBC
  │
  ▼
MySQL Database
```

For example, when an administrator updates an employee:

```text
Admin
  ↓
Employee Management UI
  ↓
AdminDashboardController
  ↓
EmployeeDAO
  ↓
JDBC PreparedStatement
  ↓
MySQL
  ↓
Updated Employee Record
```

This separation helps keep the user interface, application logic, and database operations organized and maintainable.

---

## 🔐 Security

The system incorporates several security-related practices:

* Role-based access control
* Password hashing using **jBCrypt**
* JDBC `PreparedStatement` for database operations
* Administrative activity logging
* Separation of database access through DAO classes
* Restricted access to administrative functionality

> **Note:** This project is intended for educational and demonstration purposes. Additional security hardening would be required before deploying it in a production environment.

---

## 🧪 Testing

Testing can be performed for the following major components:

* User authentication
* Role-based authorization
* Employee CRUD operations
* QR code generation and scanning
* Clock-in and clock-out
* Leave request submission
* Leave approval/rejection
* Database operations
* Audit log creation
* Invalid input handling

---

## 🔮 Future Improvements

Potential future enhancements include:

* 📊 Employee attendance and HR analytics dashboard
* 📧 Email notifications for leave requests
* 📅 Advanced shift and schedule management
* 📱 Mobile companion application
* 📄 Automated report generation
* 🔔 Real-time system notifications
* 📈 Employee performance analytics
* ☁️ Cloud-based database deployment
* 🔐 Two-factor authentication
* 🖥️ Improved responsive JavaFX interface

---

## 🎓 Project Purpose

This project was developed as a **Software Engineering / Database Systems project** to demonstrate practical implementation of:

* Object-Oriented Programming
* JavaFX desktop application development
* MVC architecture
* DAO design pattern
* JDBC database connectivity
* Relational database design
* SQL and MySQL
* Authentication and authorization
* QR-based system integration
* Software architecture and separation of concerns

---

## 👨‍💻 Author

**Kaveen Chamikara**

Software Engineering Undergraduate

### Connect

* GitHub: `https://github.com/Cap-KC`
* LinkedIn: `https://www.linkedin.com/in/kaveen-chamikara/`

---

## 📄 License

This project is licensed under the **MIT License**.

See the `LICENSE` file for more information.

---

⭐ If you find this project useful or interesting, consider giving it a **star** on GitHub!
