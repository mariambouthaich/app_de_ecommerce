package model;

/**
 * Modèle : Un article dans le panier (produit + quantité souhaitée)
 * Le panier existe AVANT la commande — il n'est pas enregistré en BD.
 */
public class PanierItem {
    private Produit produit;
    private int     quantite;

    // ── Constructeur ─────────────────────────────────────────
    public PanierItem(Produit produit, int quantite) {
        this.produit  = produit;
        this.quantite = quantite;
    }

    // ── Getters / Setters ────────────────────────────────────
    public Produit getProduit()  { return produit; }
    public int     getQuantite() { return quantite; }

    public void setProduit(Produit produit)   { this.produit = produit; }
    public void setQuantite(int quantite)     { this.quantite = quantite; }

    /** Sous-total de cet article */
    public double getSousTotal() {
        return produit.getPrix() * quantite;
    }

    @Override
    public String toString() {
        return "PanierItem{produit='" + produit.getNom()
                + "', qte=" + quantite
                + ", sousTotal=" + getSousTotal() + "}";
    }
}
