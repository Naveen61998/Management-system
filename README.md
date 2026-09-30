# Library Management System

Complete beginner-friendly Java web application using Java 17, JDBC, MySQL, HTML, CSS and JavaScript.

## Features
- Dashboard
- Book CRUD basics: add, search, delete
- Member CRUD basics: add, search, delete
- Issue books
- Return books
- Automatic available-book count
- Fine calculation: ₹5 per late day
- JDBC PreparedStatement
- JDBC ResultSet
- JDBC transactions with commit/rollback
- DAO architecture
- Responsive frontend

## Requirements
- JDK 17+
- Maven 3.9+
- MySQL 8+

## Setup
1. Open MySQL Workbench.
2. Run `database/library.sql`.
3. If your MySQL password is not `root`, set `LIBRARY_DB_PASSWORD` or edit `Database.java`.
4. In the project folder run:
   `mvn clean compile`
5. Start:
   `mvn exec:java -Dexec.mainClass=com.library.Main`
6. Open `http://localhost:8080`

## JDBC learning points
Connection, DriverManager, PreparedStatement, ResultSet, executeQuery, executeUpdate, transactions, commit, rollback and try-with-resources.

## Project structure
- `model` = Java data objects
- `dao` = JDBC/database operations
- `config` = database connection
- `server` = HTTP API
- `static` = frontend
- `database` = MySQL schema

This is an educational project. A production system should add authentication, authorization, stronger validation, connection pooling, CSRF protection and structured JSON handling.
