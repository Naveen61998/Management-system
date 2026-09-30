# Library Management System — One Command Project

A complete beginner-friendly Java full-stack Library Management System using:

- HTML
- CSS
- JavaScript
- Java 17
- Maven
- JDBC
- MySQL
- DAO architecture

## Features

- Dashboard
- Add/search/delete books
- Add/search/delete members
- Issue books
- Return books
- Automatic late fine calculation: ₹5 per late day
- MySQL database
- JDBC transactions
- Responsive browser UI

## Requirements

Install these once:

1. JDK 17+
2. Maven 3.9+
3. MySQL 8+

## Database setup

Start MySQL and run:

`database/library.sql`

Default database settings:

- Database: `library_db`
- Username: `root`
- Password: `root`

If your password is different, set:

Windows CMD:
`set LIBRARY_DB_PASSWORD=your_password`

PowerShell:
`$env:LIBRARY_DB_PASSWORD="your_password"`

Linux/macOS:
`export LIBRARY_DB_PASSWORD=your_password`

You can also set `LIBRARY_DB_USER` and `LIBRARY_DB_URL`.

## Run with one command

Windows:

`run.bat`

Linux/macOS:

`chmod +x run.sh && ./run.sh`

The Java server starts at:

`http://localhost:8080`

The application attempts to open the browser automatically.

## Important

Opening `index.html` directly is not the normal way to run this project. The Java server must be running because the HTML/JavaScript calls the JDBC-backed Java API.

## JDBC learning flow

Browser
→ JavaScript fetch()
→ Java HTTP server
→ DAO
→ JDBC
→ MySQL

Important JDBC classes used:

- DriverManager
- Connection
- PreparedStatement
- ResultSet
- SQLException

Transactions are used for issue/return operations with commit and rollback.

## GitHub

Upload the whole project folder to GitHub. Do not upload passwords or production database credentials.
