package model;

import java.time.LocalDateTime;

public class Commande {
    private int           id;
    private int           userId;
    private int           clientId;
    private double        total;
    private String        statut;
    private LocalDateTime dateCommande;
    private String        clientNomComplet;
    private int           nbArticles;

    public Commande() {}

    public Commande(int userId) {
        this.userId = userId;
        this.statut = "EN_ATTENTE";
    }

    public int           getId()              { return id; }
    public int           getUserId()          { return userId; }
    public int           getClientId()        { return clientId; }
    public double        getTotal()           { return total; }
    public String        getStatut()          { return statut; }
    public LocalDateTime getDateCommande()    { return dateCommande; }
    public String        getClientNomComplet(){ return clientNomComplet; }
    public int           getNbArticles()      { return nbArticles; }

    public void setId(int id)                        { this.id = id; }
    public void setUserId(int userId)                { this.userId = userId; }
    public void setClientId(int clientId)            { this.clientId = clientId; }
    public void setTotal(double total)               { this.total = total; }
    public void setStatut(String statut)             { this.statut = statut; }
    public void setDateCommande(LocalDateTime v)     { this.dateCommande = v; }
    public void setClientNomComplet(String v)        { this.clientNomComplet = v; }
    public void setNbArticles(int v)                 { this.nbArticles = v; }

    @Override
    public String toString() {
        return "Commande{id=" + id + ", statut='" + statut + "', total=" + total + "}";
    }
}
