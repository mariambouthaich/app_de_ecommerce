package database;
import java.sql.Connection;
import java.sql.DriverManager;
public class DatabaseConnection {
    // ── Paramètres de connexion ──────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/ecommerce";
    private static final String USER     = "root";
    private static final String PASSWORD = "azert1234";
    // ────────────────────────────────────────────────────────
    private static Connection instance = null;

    /** Constructeur privé → interdit d'instancier depuis l'extérieur */
    private DatabaseConnection() {}
    public static Connection getConnection() {
        try {
            if (instance == null || instance.isClosed()) {
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion à la base de données réussie !");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
        }
        return instance;
    }
    public static void closeConnection() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                System.out.println("🔒 Connexion fermée.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

