
package model;

/**
 * Modèle : Catégorie de produits
 */
public class Categorie {
    private int    id;
    private String nom;

    // ── Constructeurs ────────────────────────────────────────
    public Categorie() {}

    public Categorie(String nom) {
        this.nom = nom;
    }

    // ── Getters / Setters ────────────────────────────────────
    public int    getId()  { return id; }
    public String getNom() { return nom; }

    public void setId(int id)      { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public String toString() {
        return "Categorie{id=" + id + ", nom='" + nom + "'}";
    }
}