# POJA Diploma Management API

This repository contains the backend API for an Academic Diploma Management System, built as the **PROG4 Final Exam**. It is generated from the **POJA starter template** and leverages Java with Spring Boot to provide a highly scalable, robust, and clean architecture.

## 🚀 Project Overview

The **POJA Diploma** API automates core academic processes leading up to graduation. It provides secure, automated endpoints to manage students, grades, teachers, and courses.

### Key Features
* **Academic Tracking:** Manage courses and assign specialized professors to specific classrooms.
* **Grade Management:** Securely record student grades across different semesters.
* **Graduation Eligibility Automation:** Backend logic to track student averages, credits, and promotions leading up to the official validation of their Bachelor's degree (Licence).

## 🛠️ Tech Stack & Architecture

* **Framework:** Spring Boot (Java)
* **Template Engine:** POJA (Production-Ready Architecture)
* **Build Tool:** Gradle
* **Database:** PostgreSQL (with Neon branching / local instance support)
* **Code Quality:** Google Java Format configured via pre-commit shell scripts

## 📁 Repository Structure

```text
├── .github/workflows/    # CI/CD deployment pipelines
├── .shell/               # Automation and deployment shell scripts
├── src/                  # Main Java and Spring Boot source files
├── build.gradle          # Dependency definitions and Gradle tasks
└── README.md             # Project documentation
```

## ⚙️ Local Setup & Installation

### Prerequisites
* Java JDK 21
* Gradle 8.x
* A running PostgreSQL instance

### 1. Clone the Repository
```bash
git clone https://github.com/Baby055/poja-diploma.git
cd poja-diploma
```

### 2. Configure Environment Variables
Create a local configuration file or set your system environment variables for your database connection:
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/your_db_name
export DATABASE_USERNAME=your_username
export DATABASE_PASSWORD=your_password
```

### 3. Code Formatting
Before committing, ensure your code complies with the project's formatting rules:
```bash
# On Linux/macOS
./format.sh

# On Windows
format.bat
```

### 4. Build and Run the Application
```bash
./gradlew bootRun
```
The server will start locally (typically on port `8080`).

## 🧪 Testing

To run the complete automated test suite included in the POJA boilerplate:
```bash
./gradlew test
```

---
*Developed by RANAIVOMANANA Sombin'ny Aina as part of the HEI Madagascar Computer Science curriculum.*