package test;

import dao.*;
import model.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe principale de test — Phase 3
 * ⚠️ Les mots de passe sont maintenant hachés SHA-256
 * pour être compatibles avec l'interface client (Personne 3).
 */
public class MainTest {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   TEST COMPLET E-COMMERCE            ║");
        System.out.println("╚══════════════════════════════════════╝\n");

        // ─────────────────────────────────────────────────────
        // 1. TEST UTILISATEURS  (mots de passe hachés SHA-256)
        // ─────────────────────────────────────────────────────
        System.out.println("══ 1. USERS ══════════════════════════════");
        UserDAO userDAO = new UserDAO();

        // ✅ Mot de passe haché — "82606" → hash SHA-256
        User u = new User("fatihaben", "benfa@mail.com", hashSHA256("82606"));
        userDAO.ajouterUser(u);

        // ✅ Utilisateur de test pour l'interface client
        User client = new User("Mariam Bouthaich", "mariam@mail.com", hashSHA256("mariam123"));
        userDAO.ajouterUser(client);

        System.out.println("Liste des utilisateurs :");
        userDAO.getAllUsers().forEach(System.out::println);

        // ─────────────────────────────────────────────────────
        // 2. TEST CATÉGORIES
        // ─────────────────────────────────────────────────────
        System.out.println("\n══ 2. CATÉGORIES ═════════════════════════");
        CategorieDAO categorieDAO = new CategorieDAO();

        // Insérer des catégories si la table est vide
        if (categorieDAO.getAllCategories().isEmpty()) {
            categorieDAO.ajouterCategorie(new Categorie("Électronique"));
            categorieDAO.ajouterCategorie(new Categorie("Informatique"));
            categorieDAO.ajouterCategorie(new Categorie("Accessoires"));
        }

        System.out.println("Catégories disponibles :");
        categorieDAO.getAllCategories().forEach(System.out::println);

        // ─────────────────────────────────────────────────────
        // 3. TEST PRODUITS
        // ─────────────────────────────────────────────────────
        System.out.println("\n══ 3. PRODUITS ═══════════════════════════");
        ProduitDAO produitDAO = new ProduitDAO();

        // Insérer des produits de test si nécessaire
        if (produitDAO.getAllProduits().isEmpty()) {
            produitDAO.ajouterProduit(new Produit("Laptop HP",        "Laptop HP 15 pouces i5",  5999.0, 10, 1));
            produitDAO.ajouterProduit(new Produit("Clavier USB",      "Clavier mécanique AZERTY", 299.0, 30, 2));
            produitDAO.ajouterProduit(new Produit("Souris Bluetooth", "Souris sans fil ergonomique", 199.0, 25, 2));
            produitDAO.ajouterProduit(new Produit("Écran 24\"",       "Écran Full HD 75Hz",      1899.0, 15, 1));
            produitDAO.ajouterProduit(new Produit("Casque Audio",     "Casque sans fil Bluetooth", 450.0, 20, 3));
        }

        System.out.println("Tous les produits :");
        produitDAO.getAllProduits().forEach(System.out::println);

        // ─────────────────────────────────────────────────────
        // 4. TEST PANIER + COMMANDE
        // ─────────────────────────────────────────────────────
        System.out.println("\n══ 4. PANIER & COMMANDE ══════════════════");

        Panier panier = new Panier(1);

        Produit laptop  = produitDAO.getProduitById(1);
        Produit clavier = produitDAO.getProduitById(2);

        if (laptop != null)  panier.ajouterItem(laptop, 1);
        if (clavier != null) panier.ajouterItem(clavier, 2);

        panier.afficher();

        PanierDAO panierDAO = new PanierDAO();
        int commandeId = panierDAO.validerCommande(panier);

        // ─────────────────────────────────────────────────────
        // 5. VÉRIFIER LES COMMANDES EN BD
        // ─────────────────────────────────────────────────────
        System.out.println("\n══ 5. VÉRIFICATION COMMANDES ═════════════");
        CommandeDAO commandeDAO = new CommandeDAO();

        System.out.println("Commandes de l'utilisateur 1 :");
        commandeDAO.getCommandesByUser(1).forEach(System.out::println);

        if (commandeId != -1) {
            System.out.println("Lignes de la commande #" + commandeId + " :");
            commandeDAO.getLignesByCommande(commandeId).forEach(System.out::println);
        }

        System.out.println("\n✅ Tests terminés !");
        System.out.println("──────────────────────────────────────────");
        System.out.println("📌 Comptes de test disponibles :");
        System.out.println("   mariam@mail.com  /  mariam123");
        System.out.println("   benfa@mail.com   /  82606");
    }

    // ─────────────────────────────────────────────
    // Hachage SHA-256 (même méthode que LoginClientController)
    // ─────────────────────────────────────────────
    public static String hashSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
