package dao;

import database.DatabaseConnection;
import model.User;
import java.sql.*;
import java.util.*;

public class UserDAO {

    public void ajouterUser(User user) {
        String sql = "INSERT INTO utilisateurs (nom, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.executeUpdate();
        } catch (Exception e) { System.err.println("❌ ajouterUser : " + e.getMessage()); }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { System.err.println("❌ getAllUsers : " + e.getMessage()); }
        return list;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM utilisateurs WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) { System.err.println("❌ getUserByEmail : " + e.getMessage()); }
        return null;
    }

    public void updateUser(User user) {
        String sql = "UPDATE utilisateurs SET nom=?, email=?, password=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        } catch (Exception e) { System.err.println("❌ updateUser : " + e.getMessage()); }
    }

    public void deleteUser(int id) {
        String sql = "DELETE FROM utilisateurs WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { System.err.println("❌ deleteUser : " + e.getMessage()); }
    }

    public User authenticate(String email, String motDePasse) {
        String sql = "SELECT * FROM utilisateurs WHERE email=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = mapRow(rs);
                String stored = u.getMotDePasse();
                if (stored != null && stored.equals(motDePasse)) return u;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> findAllClients() {
        String sql = "SELECT * FROM utilisateurs ORDER BY nom";
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM utilisateurs WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int countClientsThisMonth() {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE MONTH(date_creation)=MONTH(NOW()) AND YEAR(date_creation)=YEAR(NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public int countClientsLastMonth() {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE MONTH(date_creation)=MONTH(DATE_SUB(NOW(),INTERVAL 1 MONTH))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public List<Map<String, Object>> getTopClients(int limit, int days) {
        String sql = """
            SELECT u.id, u.nom,
                   COUNT(c.id) AS nb_commandes,
                   COALESCE(SUM(c.total),0) AS total
            FROM utilisateurs u
            JOIN commandes c ON c.user_id = u.id
            WHERE c.statut != 'ANNULEE'
              AND c.date_commande >= DATE_SUB(NOW(), INTERVAL ? DAY)
            GROUP BY u.id, u.nom
            ORDER BY total DESC LIMIT ?
        """;
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days); ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("nom", rs.getString("nom"));
                row.put("nbCommandes", rs.getInt("nb_commandes"));
                row.put("total", rs.getDouble("total"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setEmail(rs.getString("email"));
        // Support colonnes password ou mot_de_passe
        try { u.setPassword(rs.getString("password")); } catch (SQLException ignored) {}
        try { u.setMotDePasse(rs.getString("mot_de_passe")); } catch (SQLException ignored) {}
        try { u.setPrenom(rs.getString("prenom")); } catch (SQLException ignored) {}
        try { u.setRole(rs.getString("role")); } catch (SQLException ignored) {}
        try {
            Timestamp ts = rs.getTimestamp("date_creation");
            if (ts != null) u.setDateCreation(ts.toLocalDateTime());
        } catch (SQLException ignored) {}
        return u;
    }
}
