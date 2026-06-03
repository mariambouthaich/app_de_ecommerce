package model;

import java.time.LocalDateTime;

public class User {
    private int           id;
    private String        nom;
    private String        prenom;
    private String        email;
    private String        password;
    private String        motDePasse;
    private String        role;
    private int           nbCommandes;
    private double        totalDepense;
    private LocalDateTime dateCreation;

    public User() {}

    public User(String nom, String email, String password) {
        this.nom      = nom;
        this.email    = email;
        this.password = password;
    }

    public int           getId()            { return id; }
    public String        getNom()           { return nom; }
    public String        getPrenom()        { return prenom; }
    public String        getEmail()         { return email; }
    public String        getPassword()      { return password != null ? password : motDePasse; }
    public String        getMotDePasse()    { return motDePasse != null ? motDePasse : password; }
    public String        getRole()          { return role; }
    public int           getNbCommandes()   { return nbCommandes; }
    public double        getTotalDepense()  { return totalDepense; }
    public LocalDateTime getDateCreation()  { return dateCreation; }

    public void setId(int id)                        { this.id = id; }
    public void setNom(String nom)                   { this.nom = nom; }
    public void setPrenom(String prenom)             { this.prenom = prenom; }
    public void setEmail(String email)               { this.email = email; }
    public void setPassword(String p)                { this.password = p; this.motDePasse = p; }
    public void setMotDePasse(String p)              { this.motDePasse = p; this.password = p; }
    public void setRole(String role)                 { this.role = role; }
    public void setNbCommandes(int v)                { this.nbCommandes = v; }
    public void setTotalDepense(double v)            { this.totalDepense = v; }
    public void setDateCreation(LocalDateTime v)     { this.dateCreation = v; }

    @Override
    public String toString() {
        return "User{id=" + id + ", nom='" + nom + "', email='" + email + "'}";
    }
}
