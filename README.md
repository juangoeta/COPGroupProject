# Library Book Management System

## Project Overview
This is a JavaFX application for managing a library's book collection. The system allows users to:
- View all books in the library
- Add new books
- Update existing book information
- Delete books
- Associate books with authors

## Project Structure
- `src/main/java/LibraryBookManager.java` - Contains the main application code
- The project uses JavaFX for the UI and MySQL for the database

## Database Setup

### 1. Install MySQL
- Download and install MySQL Server from [MySQL Official Website](https://dev.mysql.com/downloads/mysql/)
- During installation, note down your root password

### 2. Create Database and Tables
1. Open MySQL Command Line Client or MySQL Workbench
2. Run the following SQL commands:

```sql
-- Create the database
CREATE DATABASE library;

-- Use the database
USE library;

-- Create Authors table
CREATE TABLE Authors (
    AuthorID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL
);

-- Create Books table
CREATE TABLE Books (
    BookID INT AUTO_INCREMENT PRIMARY KEY,
    Title VARCHAR(255) NOT NULL,
    AuthorID INT,
    YearPublished INT,
    FOREIGN KEY (AuthorID) REFERENCES Authors(AuthorID)
);
```

### 3. Add Test Authors
Run these SQL commands to add some test authors:

```sql
USE library;

INSERT INTO Authors (Name) VALUES 
('J.K. Rowling'),
('George R.R. Martin'),
('Stephen King'),
('Agatha Christie'),
('J.R.R. Tolkien');
```

### 4. Configure Database Connection
In the `DatabaseManager` class, update these values to match your MySQL setup:
```java
String url = "jdbc:mysql://localhost:3306/library";
String user = "your_username";  // Default is usually "root"
String password = "your_password";
```

## Running the Application

### Prerequisites
- Java JDK 11 or higher
- MySQL Server
- JavaFX SDK

### Steps to Run
1. Clone the repository
2. Open the project in your preferred IDE (IntelliJ IDEA or Eclipse recommended)
3. Make sure the database is set up and running
4. Update the database credentials in `DatabaseManager` class if needed
5. Run `mvn javafx:run` from the command line or run the `LibraryBookManager` class from your IDE

## Features
- View all books in a table format
- Add new books with title, author, and publication year
- Update existing book information
- Delete books from the library
- Automatic refresh of data after changes
- Input validation for required fields

## Troubleshooting
- If you get a database connection error, verify:
  - MySQL server is running
  - Database credentials are correct
  - The library database and tables exist
- If the UI doesn't load, ensure:
  - JavaFX is properly configured in your IDE
  - All required dependencies are included

## Team Members
- Juan Goeta: Backend Development (Database, Models, Business Logic)
- Lazaro Fajardo: Frontend Development (JavaFX UI)

## Additional Notes
- The application uses a singleton pattern for database connection
- All database operations are handled through the `DatabaseManager` class
- The UI is built using JavaFX components with a clean separation from the backend logic