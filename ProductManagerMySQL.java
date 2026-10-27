import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ProductManagerMySQL {
    // MySQL connection configuration (Update username and password if necessary)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/inventory_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";      // Change to your MySQL username
    private static final String DB_PASSWORD = "password"; // Change to your MySQL password

    public static void main(String[] args) {
        initializeDatabase();
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        do {
            printMenu();
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume leftover newline character

                switch (choice) {
                    case 1:
                        insertProduct(scanner);
                        break;
                    case 2:
                        viewProducts();
                        break;
                    case 3:
                        searchProduct(scanner);
                        break;
                    case 4:
                        updateProduct(scanner);
                        break;
                    case 5:
                        deleteProduct(scanner);
                        break;
                    case 6:
                        System.out.println("\n👋 Exiting application. Thank you!");
                        break;
                    default:
                        System.out.println("❌ Invalid choice! Please enter a number between 1 and 6.");
                }
            } catch (InputMismatchException e) {
                System.out.println("❌ Invalid input format! Please enter a valid menu number.");
                scanner.nextLine(); // Clear the invalid input token
                choice = 0;
            }
            System.out.println();
        } while (choice != 6);

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("========================================");
        System.out.println("      PRODUCT MANAGEMENT SYSTEM (MySQL) ");
        System.out.println("========================================");
        System.out.println("1. Add Product (Insert)");
        System.out.println("2. View All Products");
        System.out.println("3. Search Product");
        System.out.println("4. Update Product");
        System.out.println("5. Delete Product");
        System.out.println("6. Exit");
        System.out.println("========================================");
        System.out.print("Enter your choice (1-6): ");
    }

    public static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS products (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "name VARCHAR(255) NOT NULL, " +
                     "description TEXT, " +
                     "price DECIMAL(10,2) NOT NULL, " +
                     "stock INT NOT NULL)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("❌ Database failure during initialization: " + e.getMessage());
        }
    }

    public static void insertProduct(Scanner scanner) {
        System.out.println("\n--- Add New Product ---");
        System.out.print("Enter product name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter product description (optional): ");
        String description = scanner.nextLine().trim();

        double price;
        int stock;

        try {
            System.out.print("Enter product price ($): ");
            price = scanner.nextDouble();
            System.out.print("Enter stock quantity: ");
            stock = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid input! Price must be a decimal number and stock must be an integer.");
            scanner.nextLine(); // Clear buffer
            return;
        }

        String sql = "INSERT INTO products(name, description, price, stock) VALUES(?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, description.isEmpty() ? null : description);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, stock);
            
            pstmt.executeUpdate();
            System.out.println("✅ Product added successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Database failure while inserting product: " + e.getMessage());
        }
    }

    public static void viewProducts() {
        System.out.println("\n--- Product Records ---");
        String sql = "SELECT * FROM products";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean hasProducts = false;
            System.out.printf("%-5s | %-20s | %-25s | %-10s | %-6s\n", "ID", "Name", "Description", "Price", "Stock");
            System.out.println("-".repeat(75));

            while (rs.next()) {
                hasProducts = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String desc = rs.getString("description");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");

                System.out.printf("%-5d | %-20s | %-25s | $%-9.2f | %-6d\n", 
                    id, name, (desc != null ? desc : "-"), price, stock);
            }

            if (!hasProducts) {
                System.out.println("ℹ️ No product records found in the database.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Database failure while retrieving records: " + e.getMessage());
        }
    }

    public static void searchProduct(Scanner scanner) {
        System.out.println("\n--- Search Product ---");
        System.out.print("Enter product name or ID to search: ");
        String keyword = scanner.nextLine().trim();

        boolean isId = keyword.matches("\\d+");
        String sql = isId ? "SELECT * FROM products WHERE id = ?" : "SELECT * FROM products WHERE name LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (isId) {
                pstmt.setInt(1, Integer.parseInt(keyword));
            } else {
                pstmt.setString(1, "%" + keyword + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = false;
                System.out.printf("%-5s | %-20s | %-25s | %-10s | %-6s\n", "ID", "Name", "Description", "Price", "Stock");
                System.out.println("-".repeat(75));

                while (rs.next()) {
                    found = true;
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String desc = rs.getString("description");
                    double price = rs.getDouble("price");
                    int stock = rs.getInt("stock");

                    System.out.printf("%-5d | %-20s | %-25s | $%-9.2f | %-6d\n", 
                        id, name, (desc != null ? desc : "-"), price, stock);
                }

                if (!found) {
                    System.out.println("❌ Missing Record: No matching products found for \"" + keyword + "\".");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Database failure during search: " + e.getMessage());
        }
    }

    public static void updateProduct(Scanner scanner) {
        System.out.println("\n--- Update Product ---");
        System.out.print("Enter Product ID to update: ");
        int id;
        try {
            id = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid ID format! Please enter a valid number.");
            scanner.nextLine();
            return;
        }

        String selectSql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            
            selectStmt.setInt(1, id);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("❌ Missing Record: Product with ID " + id + " does not exist.");
                    return;
                }

                String currentName = rs.getString("name");
                String currentDesc = rs.getString("description");
                double currentPrice = rs.getDouble("price");
                int currentStock = rs.getInt("stock");

                System.out.printf("Current -> Name: %s | Desc: %s | Price: $%.2f | Stock: %d\n", 
                    currentName, (currentDesc != null ? currentDesc : "-"), currentPrice, currentStock);
                System.out.println("Press Enter to keep the existing value.");

                System.out.print("Enter new name [" + currentName + "]: ");
                String newName = scanner.nextLine().trim();
                if (newName.isEmpty()) newName = currentName;

                System.out.print("Enter new description [" + (currentDesc != null ? currentDesc : "-") + "]: ");
                String newDesc = scanner.nextLine().trim();
                if (newDesc.isEmpty()) newDesc = currentDesc;

                System.out.print("Enter new price [" + currentPrice + "]: ");
                String priceStr = scanner.nextLine().trim();
                double newPrice = currentPrice;
                if (!priceStr.isEmpty()) {
                    try {
                        newPrice = Double.parseDouble(priceStr);
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid price format. Update cancelled.");
                        return;
                    }
                }

                System.out.print("Enter new stock [" + currentStock + "]: ");
                String stockStr = scanner.nextLine().trim();
                int newStock = currentStock;
                if (!stockStr.isEmpty()) {
                    try {
                        newStock = Integer.parseInt(stockStr);
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid stock format. Update cancelled.");
                        return;
                    }
                }

                String updateSql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, newName);
                    updateStmt.setString(2, newDesc);
                    updateStmt.setDouble(3, newPrice);
                    updateStmt.setInt(4, newStock);
                    updateStmt.setInt(5, id);
                    updateStmt.executeUpdate();
                    System.out.println("✅ Product updated successfully!");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Database failure during update: " + e.getMessage());
        }
    }

    public static void deleteProduct(Scanner scanner) {
        System.out.println("\n--- Delete Product ---");
        System.out.print("Enter Product ID to delete: ");
        int id;
        try {
            id = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid ID format! Please enter a valid number.");
            scanner.nextLine();
            return;
        }

        String selectSql = "SELECT name FROM products WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            selectStmt.setInt(1, id);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("❌ Missing Record: Product with ID " + id + " does not exist.");
                    return;
                }

     String productName = rs.getString("name");
                System.out.print("Are you sure you want to delete '" + productName + "' (ID: " + id + ")? (y/n): ");
                String confirm = scanner.nextLine().trim().toLowerCase();

                if (confirm.equals("y")) {
                    String deleteSql = "DELETE FROM products WHERE id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, id);
                        deleteStmt.executeUpdate();
                        System.out.println("✅ Product deleted successfully!");
                    }
                } else {
                    System.out.println("🚫 Deletion cancelled.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Database failure during deletion: " + e.getMessage());
        }
    }
}