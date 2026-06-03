package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modèle : Panier d'un utilisateur (gestion en mémoire, pas en BD)
 * Contient une liste de PanierItem.
 */
public class Panier {
    private int             userId;   // propriétaire du panier
    private List<PanierItem> items;

    // ── Constructeur ─────────────────────────────────────────
    public Panier(int userId) {
        this.userId = userId;
        this.items  = new ArrayList<>();
    }

    // ── Getter ───────────────────────────────────────────────
    public int              getUserId() { return userId; }
    public List<PanierItem> getItems()  { return items; }

    // ── Opérations sur le panier ─────────────────────────────

    /**
     * Ajouter un produit au panier.
     * Si le produit est déjà présent, la quantité est incrémentée.
     */
    public void ajouterItem(Produit produit, int quantite) {
        for (PanierItem item : items) {
            if (item.getProduit().getId() == produit.getId()) {
                item.setQuantite(item.getQuantite() + quantite);
                System.out.println("✅ Quantité mise à jour : " + item);
                return;
            }
        }
        items.add(new PanierItem(produit, quantite));
        System.out.println("✅ Produit ajouté au panier : " + produit.getNom());
    }

    /**
     * Supprimer un article du panier par l'ID du produit.
     */
    public void supprimerItem(int produitId) {
        items.removeIf(item -> item.getProduit().getId() == produitId);
        System.out.println("🗑 Article supprimé du panier.");
    }

    /**
     * Vider tout le panier.
     */
    public void vider() {
        items.clear();
        System.out.println("🗑 Panier vidé.");
    }

    /**
     * Calculer le total du panier.
     */
    public double getTotal() {
        return items.stream()
                .mapToDouble(PanierItem::getSousTotal)
                .sum();
    }

    /**
     * Afficher le contenu du panier dans la console.
     */
    public void afficher() {
        if (items.isEmpty()) {
            System.out.println("🛒 Le panier est vide.");
            return;
        }
        System.out.println("=== 🛒 Panier (userId=" + userId + ") ===");
        for (PanierItem item : items) {
            System.out.println("  " + item);
        }
        System.out.printf("  TOTAL : %.2f MAD%n", getTotal());
    }

    @Override
    public String toString() {
        return "Panier{userId=" + userId + ", items=" + items.size()
                + ", total=" + getTotal() + "}";
    }
}
