# 🍽 SmartBite API

SmartBite REST API is the backend service for the SmartBite restaurant management system.  
Built with **Spring Boot**, it provides secure, scalable and modular RESTful services for managing restaurants, users, menus, orders and more.

---

## 📌 Project Overview

SmartBite is designed to support restaurant operations through a clean and maintainable backend architecture.

This API handles:

- User management
- Role-based authentication & authorization
- Restaurant management
- Menu management
- Order processing
- Database persistence

---

## 🛠 Tech Stack

- Java 17+
- Spring Boot
- Spring Data JPA
- Spring Security (JWT)
- MySQL
- Maven
- Lombok

---

## 🏗 Architecture

The project follows a layered architecture:

com.smartbite.api
│
├── config # Security & configuration classes
├── controller # REST controllers
├── service # Business logic
├── repository # JPA repositories
├── model # Entities
├── dto # Data Transfer Objects
├── exception # Custom exceptions
└── util # Utility classes


---

## ⚙️ Setup & Installation

### 1️⃣ Clone the repository
```bash
git clone https://github.com/stevencr-123/smartbite-api.git
cd smartbite-api
