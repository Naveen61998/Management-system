#!/usr/bin/env bash
set -e
echo "=========================================="
echo "  Library Management System"
echo "=========================================="
command -v java >/dev/null || { echo "Java 17+ is required."; exit 1; }
command -v mvn >/dev/null || { echo "Maven is required."; exit 1; }
echo "Make sure MySQL is running and library_db has been created."
echo "If needed, run database/library.sql in MySQL."
mvn clean compile
mvn exec:java
