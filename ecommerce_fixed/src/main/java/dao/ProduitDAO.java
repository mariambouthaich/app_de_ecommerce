package dao;

import database.DatabaseConnection;
import model.Produit;
import java.sql.*;
import java.util.*;

public class ProduitDAO {

    // ── CREATE ──────────────────────────────────────────────
    public void ajouterProduit(Produit p) {
        String sql = "INSERT INTO produits (nom, description, prix, stock, categorie_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrix());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getCategorieId());
            ps.executeUpdate();
            System.out.println("✅ Produit ajouté : " + p.getNom());
        } catch (Exception e) {
            System.err.println("❌ Erreur ajouterProduit : " + e.getMessage());
        }
    }

    // ── READ — Tous les produits ────────────────────────────
    public List<Produit> getAllProduits() {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produits";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (Exception e) {
            System.err.println("❌ Erreur getAllProduits : " + e.getMessage());
        }
        return list;
    }

    // ── READ — Produits par catégorie ───────────────────────
    public List<Produit> getProduitsByCategorie(int categorieId) {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produits WHERE categorie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categorieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (Exception e) {
            System.err.println("❌ Erreur getProduitsByCategorie : " + e.getMessage());
        }
        return list;
    }

    // ── READ — Un produit par ID ────────────────────────────
    public Produit getProduitById(int id) {
        String sql = "SELECT * FROM produits WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (Exception e) {
            System.err.println("❌ Erreur getProduitById : " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE — Stock ──────────────────────────────────────
    public void updateStock(int id, int newStock) {
        String sql = "UPDATE produits SET stock=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("❌ Erreur updateStock : " + e.getMessage());
        }
    }

    // ── UPDATE — Produit complet ────────────────────────────
    public void updateProduit(Produit p) {
        String sql = "UPDATE produits SET nom=?, description=?, prix=?, stock=?, categorie_id=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrix());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getCategorieId());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
            System.out.println("✅ Produit modifié : " + p.getNom());
        } catch (Exception e) {
            System.err.println("❌ Erreur updateProduit : " + e.getMessage());
        }
    }

    // ── UPDATE — avec image ─────────────────────────────────
    public void save(Produit p) throws SQLException {
        String sql = "INSERT INTO produits(nom,description,prix,stock,image,categorie_id) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrix());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getImage());
            ps.setInt(6, p.getCategorieId());
            ps.executeUpdate();
        }
    }

    public void update(Produit p) throws SQLException {
        String sql = "UPDATE produits SET nom=?,description=?,prix=?,stock=?,image=?,categorie_id=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrix());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getImage());
            ps.setInt(6, p.getCategorieId());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        }
    }

    // ── DELETE ──────────────────────────────────────────────
    public void supprimerProduit(int id) {
        String sql = "DELETE FROM produits WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("🗑 Produit supprimé (id=" + id + ")");
        } catch (Exception e) {
            System.err.println("❌ Erreur supprimerProduit : " + e.getMessage());
        }
    }

    public void delete(int id) throws SQLException {
        supprimerProduit(id);
    }

    // ── Tous les produits (alias) ───────────────────────────
    public List<Produit> findAll() {
        return getAllProduits();
    }

    // ── Stock bas ───────────────────────────────────────────
    public List<Produit> getProduitsStockBas(int seuilStock) {
        String sql = "SELECT * FROM produits WHERE stock <= ? ORDER BY stock ASC LIMIT 10";
        List<Produit> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seuilStock);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countRuptureStock() {
        String sql = "SELECT COUNT(*) FROM produits WHERE stock = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public int countRuptureStockYesterday() {
        return countRuptureStock();
    }

    // ── Top produits vendus ─────────────────────────────────
    public List<Map<String, Object>> getTopProduits(int limit, int days) {
        String sql = """
            SELECT p.nom,
                   SUM(lc.quantite) AS total_vendu,
                   SUM(lc.quantite * lc.prix_unitaire) AS revenu
            FROM lignes_commande lc
            JOIN produits p ON lc.produit_id = p.id
            JOIN commandes c ON lc.commande_id = c.id
            WHERE c.statut != 'ANNULEE'
              AND c.date_commande >= DATE_SUB(NOW(), INTERVAL ? DAY)
            GROUP BY p.id, p.nom
            ORDER BY total_vendu DESC
            LIMIT ?
        """;
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days); ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("nom", rs.getString("nom"));
                row.put("ventes", rs.getInt("total_vendu"));
                row.put("revenu", rs.getDouble("revenu"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Utilitaires privés ──────────────────────────────────
    private Produit mapResultSet(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setId(rs.getInt("id"));
        p.setNom(rs.getString("nom"));
        p.setDescription(rs.getString("description"));
        p.setPrix(rs.getDouble("prix"));
        p.setStock(rs.getInt("stock"));
        p.setCategorieId(rs.getInt("categorie_id"));
        return p;
    }

    private Produit mapRow(ResultSet rs) throws SQLException {
        Produit p = mapResultSet(rs);
        try { p.setImage(rs.getString("image")); } catch (SQLException ignored) {}
        return p;
    }

    public void afficherProduits() {
        getAllProduits().forEach(System.out::println);
    }
}
