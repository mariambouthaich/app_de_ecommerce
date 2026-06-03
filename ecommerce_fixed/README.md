# 🛍 Projet E-Commerce JavaFX

## Structure du projet

```
ecommerce/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── MainApp.java              ← Point d'entrée
    │   ├── model/                    ← Personne 1
    │   │   ├── User.java
    │   │   ├── Produit.java
    │   │   ├── Categorie.java
    │   │   ├── Commande.java
    │   │   ├── LigneCommande.java
    │   │   ├── Panier.java
    │   │   └── PanierItem.java
    │   ├── dao/                      ← Personne 1
    │   │   ├── UserDAO.java
    │   │   ├── ProduitDAO.java
    │   │   ├── CategorieDAO.java
    │   │   ├── CommandeDAO.java
    │   │   └── PanierDAO.java
    │   ├── database/                 ← Personne 1
    │   │   └── DatabaseConnection.java
    │   ├── controller/               ← Personne 3 (Client)
    │   │   ├── LoginClientController.java
    │   │   ├── RegisterController.java
    │   │   ├── CatalogueController.java
    │   │   ├── PanierController.java
    │   │   ├── CommandeController.java
    │   │   ├── HistoriqueController.java
    │   │   └── ProfilController.java
    │   └── test/
    │       └── MainTest.java
    └── resources/
        ├── fxml/                     ← Personne 3
        │   ├── login-client.fxml
        │   ├── register.fxml
        │   ├── catalogue.fxml
        │   ├── panier.fxml
        │   ├── commande.fxml
        │   ├── historique.fxml
        │   └── profil.fxml
        ├── css/
        │   └── styles-client.css     ← Personne 3
        └── images/                   ← Ajouter les images ici
            ├── electronique.png
            ├── informatique.png
            ├── accessoires.png
            └── default.png
```

## Prérequis

- Java 17+
- Maven 3.8+
- MySQL 8.0 (port 3308)

## Configuration BD

Modifier `DatabaseConnection.java` si nécessaire :
```java
private static final String URL      = "jdbc:mysql://localhost:3308/ecommerce";
private static final String USER     = "root";
private static final String PASSWORD = "Salma_1337";
```

## Lancement

### 1. Initialiser la BD
Lancer `MainTest.java` pour créer les données de test.

### 2. Lancer l'application
```bash
mvn javafx:run
```
Ou clic droit sur `MainApp.java` → Run dans IntelliJ.

## Comptes de test

| Email | Mot de passe |
|---|---|
| mariam@mail.com | mariam123 |
| benfa@mail.com  | 82606 |

## Images produits

Placer les images PNG dans `src/main/resources/images/` :
- `electronique.png` → catégorie 1
- `informatique.png` → catégorie 2
- `accessoires.png`  → catégorie 3
- `default.png`      → toutes autres catégories
