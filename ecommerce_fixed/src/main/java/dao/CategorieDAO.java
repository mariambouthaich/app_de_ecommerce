package dao;

import database.DatabaseConnection;
import model.Categorie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO : Opérations CRUD sur la table categories
 */
public class CategorieDAO {

    // ─────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────
    public void ajouterCategorie(Categorie c) {
        String sql = "INSERT INTO categories (nom) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNom());
            ps.executeUpdate();
            System.out.println("✅ Catégorie ajoutée : " + c.getNom());

        } catch (Exception e) {
            System.err.println("❌ Erreur ajouterCategorie : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // READ — Toutes les catégories
    // ─────────────────────────────────────────────
    public List<Categorie> getAllCategories() {
        List<Categorie> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categorie c = new Categorie();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                list.add(c);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur getAllCategories : " + e.getMessage());
        }
        return list;
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────
    public void updateCategorie(Categorie c) {
        String sql = "UPDATE categories SET nom=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNom());
            ps.setInt(2, c.getId());
            ps.executeUpdate();
            System.out.println("✅ Catégorie modifiée : " + c.getNom());

        } catch (Exception e) {
            System.err.println("❌ Erreur updateCategorie : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────
    public void deleteCategorie(int id) {
        String sql = "DELETE FROM categories WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("🗑 Catégorie supprimée (id=" + id + ")");

        } catch (Exception e) {
            System.err.println("❌ Erreur deleteCategorie : " + e.getMessage());
        }
    }
}
