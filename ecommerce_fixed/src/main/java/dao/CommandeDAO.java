package dao;

import database.DatabaseConnection;
import model.Commande;
import model.LigneCommande;
import java.sql.*;
import java.util.*;

public class CommandeDAO {

    public int creerCommande(int userId) {
        String sql = "INSERT INTO commandes (user_id, statut) VALUES (?, 'EN_ATTENTE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { System.err.println("❌ creerCommande : " + e.getMessage()); }
        return -1;
    }

    public void ajouterLigneCommande(LigneCommande lc) {
        String sql = "INSERT INTO lignes_commande (commande_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lc.getCommandeId());
            ps.setInt(2, lc.getProduitId());
            ps.setInt(3, lc.getQuantite());
            ps.setDouble(4, lc.getPrixUnitaire());
            ps.executeUpdate();
        } catch (Exception e) { System.err.println("❌ ajouterLigneCommande : " + e.getMessage()); }
    }

    public List<Commande> getCommandesByUser(int userId) {
        List<Commande> list = new ArrayList<>();
        String sql = "SELECT * FROM commandes WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Commande c = new Commande();
                c.setId(rs.getInt("id"));
                c.setUserId(rs.getInt("user_id"));
                c.setStatut(rs.getString("statut"));
                Timestamp ts = rs.getTimestamp("date_commande");
                if (ts != null) c.setDateCommande(ts.toLocalDateTime());
                list.add(c);
            }
        } catch (Exception e) { System.err.println("❌ getCommandesByUser : " + e.getMessage()); }
        return list;
    }

    public List<LigneCommande> getLignesByCommande(int commandeId) {
        List<LigneCommande> list = new ArrayList<>();
        String sql = "SELECT * FROM lignes_commande WHERE commande_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LigneCommande lc = new LigneCommande();
                lc.setId(rs.getInt("id"));
                lc.setCommandeId(rs.getInt("commande_id"));
                lc.setProduitId(rs.getInt("produit_id"));
                lc.setQuantite(rs.getInt("quantite"));
                lc.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                list.add(lc);
            }
        } catch (Exception e) { System.err.println("❌ getLignesByCommande : " + e.getMessage()); }
        return list;
    }

    public void updateStatut(int commandeId, String statut) {
        String sql = "UPDATE commandes SET statut=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, commandeId);
            ps.executeUpdate();
        } catch (Exception e) { System.err.println("❌ updateStatut : " + e.getMessage()); }
    }

    public void supprimerCommande(int commandeId) {
        String sql = "DELETE FROM commandes WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ps.executeUpdate();
        } catch (Exception e) { System.err.println("❌ supprimerCommande : " + e.getMessage()); }
    }

    public int countCommandesToday() {
        String sql = "SELECT COUNT(*) FROM commandes WHERE DATE(date_commande) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public int countCommandesYesterday() {
        String sql = "SELECT COUNT(*) FROM commandes WHERE DATE(date_commande) = DATE_SUB(CURDATE(),INTERVAL 1 DAY)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public double getRevenusToday() {
        String sql = "SELECT COALESCE(SUM(total),0) FROM commandes WHERE DATE(date_commande)=CURDATE() AND statut != 'ANNULEE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) { e.printStackTrace(); return 0.0; }
    }

    public double getRevenusYesterday() {
        String sql = "SELECT COALESCE(SUM(total),0) FROM commandes WHERE DATE(date_commande)=DATE_SUB(CURDATE(),INTERVAL 1 DAY) AND statut != 'ANNULEE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) { e.printStackTrace(); return 0.0; }
    }

    public Map<String, Double> getRevenusParJour(int days) {
        String sql = """
            SELECT DATE_FORMAT(date_commande,'%d/%m') as jour,
                   COALESCE(SUM(total),0) as revenu
            FROM commandes
            WHERE date_commande >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
              AND statut != 'ANNULEE'
            GROUP BY DATE(date_commande)
            ORDER BY DATE(date_commande)
        """;
        Map<String, Double> result = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.put(rs.getString("jour"), rs.getDouble("revenu"));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public Map<String, Integer> getNbCommandesParJour(int days) {
        String sql = """
            SELECT DATE_FORMAT(date_commande,'%d/%m') as jour, COUNT(*) as nb
            FROM commandes
            WHERE date_commande >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
            GROUP BY DATE(date_commande)
            ORDER BY DATE(date_commande)
        """;
        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.put(rs.getString("jour"), rs.getInt("nb"));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public Map<String, Double> getRevenusParCategorie() {
        String sql = """
            SELECT c.nom AS categorie,
                   COALESCE(SUM(lc.quantite * lc.prix_unitaire), 0) AS revenu
            FROM lignes_commande lc
            JOIN produits p ON lc.produit_id = p.id
            JOIN categories c ON p.categorie_id = c.id
            JOIN commandes cmd ON lc.commande_id = cmd.id
            WHERE cmd.statut != 'ANNULEE'
            GROUP BY c.nom ORDER BY revenu DESC
        """;
        Map<String, Double> result = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.put(rs.getString("categorie"), rs.getDouble("revenu"));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public List<Commande> getRecentCommandes(int limit) {
        String sql = """
            SELECT c.*, u.nom AS client_nom
            FROM commandes c
            LEFT JOIN utilisateurs u ON c.user_id = u.id
            ORDER BY c.date_commande DESC LIMIT ?
        """;
        List<Commande> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Commande cmd = mapRow(rs);
                cmd.setClientNomComplet(rs.getString("client_nom"));
                list.add(cmd);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Commande> findAll() {
        String sql = """
            SELECT c.*, u.nom AS client_nom
            FROM commandes c
            LEFT JOIN utilisateurs u ON c.user_id = u.id
            ORDER BY c.date_commande DESC
        """;
        List<Commande> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Commande cmd = mapRow(rs);
                cmd.setClientNomComplet(rs.getString("client_nom"));
                list.add(cmd);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Commande> findByClientId(int clientId) {
        String sql = "SELECT * FROM commandes WHERE user_id=? ORDER BY date_commande DESC";
        List<Commande> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<LigneCommande> getLignesCommande(int commandeId) {
        String sql = """
            SELECT lc.*, p.nom AS nom_produit
            FROM lignes_commande lc
            JOIN produits p ON lc.produit_id = p.id
            WHERE lc.commande_id = ?
        """;
        List<LigneCommande> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LigneCommande lc = new LigneCommande();
                lc.setId(rs.getInt("id"));
                lc.setCommandeId(rs.getInt("commande_id"));
                lc.setProduitId(rs.getInt("produit_id"));
                lc.setQuantite(rs.getInt("quantite"));
                lc.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                lc.setNomProduit(rs.getString("nom_produit"));
                list.add(lc);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Map<String, Long> countCommandesByStatut() {
        String sql = "SELECT statut, COUNT(*) as nb FROM commandes GROUP BY statut";
        Map<String, Long> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString("statut"), rs.getLong("nb"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    public int countCommandesByClient(int clientId) {
        String sql = "SELECT COUNT(*) FROM commandes WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public double getTotalDepenseByClient(int clientId) {
        String sql = "SELECT COALESCE(SUM(total),0) FROM commandes WHERE user_id=? AND statut!='ANNULEE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) { e.printStackTrace(); return 0.0; }
    }

    private Commande mapRow(ResultSet rs) throws SQLException {
        Commande c = new Commande();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setStatut(rs.getString("statut"));
        try { c.setTotal(rs.getDouble("total")); } catch (SQLException ignored) {}
        Timestamp ts = rs.getTimestamp("date_commande");
        if (ts != null) c.setDateCommande(ts.toLocalDateTime());
        return c;
    }
}
