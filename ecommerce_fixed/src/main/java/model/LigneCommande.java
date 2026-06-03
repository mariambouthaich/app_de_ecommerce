package model;

public class LigneCommande {
    private int    id;
    private int    commandeId;
    private int    produitId;
    private int    quantite;
    private double prixUnitaire;
    private String nomProduit;

    public LigneCommande() {}

    public LigneCommande(int commandeId, int produitId, int quantite, double prixUnitaire) {
        this.commandeId   = commandeId;
        this.produitId    = produitId;
        this.quantite     = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int    getId()           { return id; }
    public int    getCommandeId()   { return commandeId; }
    public int    getProduitId()    { return produitId; }
    public int    getQuantite()     { return quantite; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public String getNomProduit()   { return nomProduit; }
    public double getTotal()        { return quantite * prixUnitaire; }

    public void setId(int id)                 { this.id = id; }
    public void setCommandeId(int v)          { this.commandeId = v; }
    public void setProduitId(int v)           { this.produitId = v; }
    public void setQuantite(int v)            { this.quantite = v; }
    public void setPrixUnitaire(double v)     { this.prixUnitaire = v; }
    public void setNomProduit(String v)       { this.nomProduit = v; }

    @Override
    public String toString() {
        return "LigneCommande{commandeId=" + commandeId + ", produitId=" + produitId
                + ", qte=" + quantite + ", prix=" + prixUnitaire + "}";
    }
}
