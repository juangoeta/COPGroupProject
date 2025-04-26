/**
 * Library Book Management System
 * 
 * This project was developed as a collaborative effort between two team members:
 * 
 * Juan Goeta (Backend Developer):
 * - Designed and implemented the database connection layer (DatabaseManager)
 * - Created the data models (Author and Book classes)
 * - Implemented all database operations (CRUD)
 * - Handled data validation and business logic
 * 
 * Lazaro Fajardo (Frontend Developer):
 * - Designed and implemented the JavaFX user interface
 * - Created the table view and input forms
 * - Implemented event handlers and user interactions
 * - Managed the UI layout and styling
 */

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Represents an author in the library system.
 * Each author has a unique ID and a name.
 */
class Author {
    private final int authorID;
    private String name;

    public Author(int authorID, String name) {
        this.authorID = authorID;
        this.name = name;
    }

    public int getAuthorID() {
        return authorID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

/**
 * Represents a book in the library system.
 * Contains information about the book's ID, title, author, and publication year.
 */
class Book {
    private final int bookID;
    private String title;
    private String authorName;
    private int yearPublished;

    public Book(int bookID, String title, String authorName, int yearPublished) {
        this.bookID = bookID;
        this.title = title;
        this.authorName = authorName;
        this.yearPublished = yearPublished;
    }

    public int getBookID() {
        return bookID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    protected void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getYearPublished() {
        return yearPublished;
    }

    public void setYearPublished(int yearPublished) {
        this.yearPublished = yearPublished;
    }
}

/**
 * Manages database operations for the library system.
 * Implements the Singleton pattern to ensure only one database connection exists.
 */
class DatabaseManager {
    private static DatabaseManager instance;
    private final Connection connection;

    private DatabaseManager() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/library";
        String user = "scott";
        String password = "tiger";
        connection = DriverManager.getConnection(url, user, password);
    }

    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Retrieves all books from the database with their associated author information.
     * @return List of all books in the system
     */
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT b.BookID, b.Title, a.Name as author_name, b.YearPublished " +
                      "FROM Books b " +
                      "JOIN Authors a ON b.AuthorID = a.AuthorID";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                books.add(new Book(
                    rs.getInt("BookID"),
                    rs.getString("Title"),
                    rs.getString("author_name"),
                    rs.getInt("YearPublished")
                ));
            }
        }
        return books;
    }

    /**
     * Retrieves all authors from the database.
     * @return List of all authors in the system
     */
    public List<Author> getAllAuthors() throws SQLException {
        List<Author> authors = new ArrayList<>();
        String query = "SELECT AuthorID, Name FROM Authors";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                authors.add(new Author(
                    rs.getInt("AuthorID"),
                    rs.getString("Name")
                ));
            }
        }
        return authors;
    }

    /**
     * Adds a new book to the database.
     * @param title The title of the book
     * @param authorId The ID of the book's author
     * @param yearPublished The year the book was published
     */
    public void addBook(String title, int authorId, int yearPublished) throws SQLException {
        String query = "INSERT INTO Books (Title, AuthorID, YearPublished) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.setInt(2, authorId);
            pstmt.setInt(3, yearPublished);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing book's information in the database.
     * @param bookId The ID of the book to update
     * @param title The new title
     * @param authorId The new author ID
     * @param yearPublished The new publication year
     */
    public void updateBook(int bookId, String title, int authorId, int yearPublished) throws SQLException {
        String query = "UPDATE Books SET Title = ?, AuthorID = ?, YearPublished = ? WHERE BookID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.setInt(2, authorId);
            pstmt.setInt(3, yearPublished);
            pstmt.setInt(4, bookId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a book from the database.
     * @param bookId The ID of the book to delete
     */
    public void deleteBook(int bookId) throws SQLException {
        String query = "DELETE FROM Books WHERE BookID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Safely closes the database connection when the application shuts down.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

/**
 * Main application class for the Library Book Manager.
 * Implements a JavaFX application with a GUI for managing books in a library.
 */
public class Main extends Application {
    private TableView<Book> bookTable;
    private TextField titleField;
    private ComboBox<Author> authorComboBox;
    private TextField yearField;
    private DatabaseManager dbManager;
    private Book selectedBook;

    @Override
    public void start(Stage primaryStage) {
        try {
            dbManager = DatabaseManager.getInstance();
            setupUI(primaryStage);
            refreshData();
        } catch (SQLException e) {
            showError("Database Error", "Failed to connect to the database", e.getMessage());
        }
    }

    /**
     * Sets up the main user interface components.
     * @param primaryStage The main application window
     */
    private void setupUI(Stage primaryStage) {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        setupTableView();
        GridPane inputGrid = setupInputFields();
        HBox buttonBox = setupButtons();

        mainLayout.getChildren().addAll(bookTable, inputGrid, buttonBox);

        Scene scene = new Scene(mainLayout, 600, 400);
        primaryStage.setTitle("Library Book Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Configures the table view for displaying books.
     * Sets up columns and their data bindings, and handles row selection.
     */
    private void setupTableView() {
        bookTable = new TableView<>();
        
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        titleCol.setPrefWidth(200);
        
        TableColumn<Book, String> authorCol = new TableColumn<>("Author Name");
        authorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthorName()));
        authorCol.setPrefWidth(200);
        
        TableColumn<Book, Integer> yearCol = new TableColumn<>("Year Published");
        yearCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getYearPublished()).asObject());
        yearCol.setPrefWidth(200);
        
        bookTable.getColumns().addAll(titleCol, authorCol, yearCol);
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedBook = newSelection;
            if (newSelection != null) {
                titleField.setText(newSelection.getTitle());
                yearField.setText(String.valueOf(newSelection.getYearPublished()));
                authorComboBox.getItems().stream()
                    .filter(author -> author.getName().equals(newSelection.getAuthorName()))
                    .findFirst()
                    .ifPresent(author -> authorComboBox.setValue(author));
            }
        });
    }

    /**
     * Creates and configures the input fields for book information.
     * @return A GridPane containing all input fields
     */
    private GridPane setupInputFields() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        titleField = new TextField();
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);

        authorComboBox = new ComboBox<>();
        grid.add(new Label("Author:"), 0, 1);
        grid.add(authorComboBox, 1, 1);

        yearField = new TextField();
        grid.add(new Label("Year Published:"), 0, 2);
        grid.add(yearField, 1, 2);

        return grid;
    }

    /**
     * Creates and configures the action buttons for the application.
     * @return An HBox containing all action buttons
     */
    private HBox setupButtons() {
        HBox buttonBox = new HBox(10);
        
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> handleAdd());
        
        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> handleUpdate());
        
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> handleDelete());
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshData());
        
        buttonBox.getChildren().addAll(addButton, updateButton, deleteButton, refreshButton);
        return buttonBox;
    }

    /**
     * Refreshes the data displayed in the table and author combo box.
     * Retrieves the latest data from the database.
     */
    private void refreshData() {
        try {
            List<Author> authors = dbManager.getAllAuthors();
            authorComboBox.setItems(FXCollections.observableArrayList(authors));
            
            List<Book> books = dbManager.getAllBooks();
            bookTable.setItems(FXCollections.observableArrayList(books));
        } catch (SQLException e) {
            showError("Database Error", "Failed to refresh data", e.getMessage());
        }
    }

    /**
     * Handles the addition of a new book to the system.
     * Validates input and adds the book to the database.
     */
    private void handleAdd() {
        if (validateInput()) {
            try {
                dbManager.addBook(
                    titleField.getText(),
                    authorComboBox.getValue().getAuthorID(),
                    Integer.parseInt(yearField.getText())
                );
                clearFields();
                refreshData();
            } catch (SQLException e) {
                showError("Database Error", "Failed to add book", e.getMessage());
            }
        }
    }

    /**
     * Handles updating an existing book's information.
     * Validates input and updates the book in the database.
     */
    private void handleUpdate() {
        if (selectedBook == null) {
            showError("Selection Error", "No book selected", "Please select a book to update.");
            return;
        }
        
        if (validateInput()) {
            try {
                dbManager.updateBook(
                    selectedBook.getBookID(),
                    titleField.getText(),
                    authorComboBox.getValue().getAuthorID(),
                    Integer.parseInt(yearField.getText())
                );
                clearFields();
                refreshData();
            } catch (SQLException e) {
                showError("Database Error", "Failed to update book", e.getMessage());
            }
        }
    }

    /**
     * Handles the deletion of a book from the system.
     * Removes the selected book from the database.
     */
    private void handleDelete() {
        if (selectedBook == null) {
            showError("Selection Error", "No book selected", "Please select a book to delete.");
            return;
        }

        try {
            dbManager.deleteBook(selectedBook.getBookID());
            clearFields();
            refreshData();
        } catch (SQLException e) {
            showError("Database Error", "Failed to delete book", e.getMessage());
        }
    }

    /**
     * Validates the input fields for book information.
     * @return true if all input is valid, false otherwise
     */
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (titleField.getText().trim().isEmpty()) {
            errorMessage.append("Title is required.\n");
        }

        if (authorComboBox.getValue() == null) {
            errorMessage.append("Author must be selected.\n");
        }

        try {
            int year = Integer.parseInt(yearField.getText().trim());
            if (year < 1901 || year > 2155) {
                errorMessage.append("Year must be between 1901 and 2155.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Year must be a valid number.\n");
        }

        if (errorMessage.length() > 0) {
            showError("Validation Error", "Please correct the following errors:", errorMessage.toString());
            return false;
        }

        return true;
    }

    /**
     * Clears all input fields and resets the selection.
     */
    private void clearFields() {
        titleField.clear();
        authorComboBox.setValue(null);
        yearField.clear();
        selectedBook = null;
        bookTable.getSelectionModel().clearSelection();
    }

    /**
     * Displays an error dialog with the specified information.
     * @param title The title of the error dialog
     * @param header The header text of the error dialog
     * @param content The detailed error message
     */
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 