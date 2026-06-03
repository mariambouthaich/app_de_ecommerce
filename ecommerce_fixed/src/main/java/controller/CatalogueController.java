package controller;

import dao.CategorieDAO;
import dao.ProduitDAO;
import model.Categorie;
import model.Panier;
import model.Produit;
import model.User;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller : Catalogue — Version 3
 * Nouveautés : tri produits, toast, page détail, transitions fade, setPanier()
 */
public class CatalogueController implements Initializable {

    // ── Navbar ───────────────────────────────────────────────
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label badgePanier;
    @FXML
    private Button panierButton;
    @FXML
    private Button historiqueButton;
    @FXML
    private Button profilButton;

    // ── Filtres ──────────────────────────────────────────────
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categorieCombo;
    @FXML
    private Slider prixMinSlider;
    @FXML
    private Slider prixMaxSlider;
    @FXML
    private Label prixMinLabel;
    @FXML
    private Label prixMaxLabel;
    @FXML
    private Label nbResultatsLabel;

    // ── Tri ──────────────────────────────────────────────────
    @FXML
    private ComboBox<String> triCombo;

    // ── Grille ───────────────────────────────────────────────
    @FXML
    private GridPane produitsGrid;

    // ── Données ──────────────────────────────────────────────
    private final ProduitDAO produitDAO = new ProduitDAO();
    private final CategorieDAO categorieDAO = new CategorieDAO();

    private User currentUser;
    private Panier panier;
    private List<Produit> allProduits;
    private List<Categorie> allCategories;
    private double prixMax = 10000;

    // ────────────────────────────────────────────────────────
    // Initialisation
    // ────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allProduits = produitDAO.getAllProduits();
        allCategories = categorieDAO.getAllCategories();

        prixMax = allProduits.stream()
                .mapToDouble(Produit::getPrix).max().orElse(10000);

        // ComboBox catégories
        List<String> noms = allCategories.stream()
                .map(Categorie::getNom).collect(Collectors.toList());
        noms.add(0, "Toutes les catégories");
        categorieCombo.setItems(FXCollections.observableArrayList(noms));
        categorieCombo.getSelectionModel().selectFirst();

        // ComboBox tri
        triCombo.setItems(FXCollections.observableArrayList(
                "Par défaut", "Prix ↑ croissant", "Prix ↓ décroissant",
                "Nom A → Z", "Nom Z → A", "Stock disponible"));
        triCombo.getSelectionModel().selectFirst();

        // Sliders prix
        prixMinSlider.setMin(0);
        prixMinSlider.setMax(prixMax);
        prixMinSlider.setValue(0);
        prixMaxSlider.setMin(0);
        prixMaxSlider.setMax(prixMax);
        prixMaxSlider.setValue(prixMax);
        updatePrixLabels();

        // Listeners
        searchField.textProperty().addListener((o, v, n) -> filtrer());
        categorieCombo.valueProperty().addListener((o, v, n) -> filtrer());
        triCombo.valueProperty().addListener((o, v, n) -> filtrer());
        prixMinSlider.valueProperty().addListener((o, v, n) -> {
            updatePrixLabels();
            filtrer();
        });
        prixMaxSlider.valueProperty().addListener((o, v, n) -> {
            updatePrixLabels();
            filtrer();
        });

        afficherProduits(allProduits);
    }

    // ────────────────────────────────────────────────────────
    // Setters injectés depuis d'autres controllers
    // ────────────────────────────────────────────────────────
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.panier = new Panier(user.getId());
        if (welcomeLabel != null)
            welcomeLabel.setText("Bonjour, " + user.getNom() + " 👋");
    }

    /** Permet de restaurer le panier existant (depuis DetailProduit) */
    public void setPanier(Panier panier) {
        this.panier = panier;
        majBadge();
    }

    // ────────────────────────────────────────────────────────
    // Filtrage + Tri
    // ────────────────────────────────────────────────────────
    private void filtrer() {
        String texte = searchField.getText().toLowerCase().trim();
        String categNom = categorieCombo.getValue();
        double pMin = prixMinSlider.getValue();
        double pMax = prixMaxSlider.getValue();
        String tri = triCombo.getValue();

        List<Produit> filtres = allProduits.stream()
                .filter(p -> p.getNom().toLowerCase().contains(texte)
                        || (p.getDescription() != null
                                && p.getDescription().toLowerCase().contains(texte)))
                .filter(p -> {
                    if (categNom == null || categNom.equals("Toutes les catégories"))
                        return true;
                    return allCategories.stream()
                            .filter(c -> c.getNom().equals(categNom))
                            .anyMatch(c -> c.getId() == p.getCategorieId());
                })
                .filter(p -> p.getPrix() >= pMin && p.getPrix() <= pMax)
                .collect(Collectors.toList());

        // Tri
        if (tri != null)
            switch (tri) {
                case "Prix ↑ croissant" -> filtres.sort(Comparator.comparingDouble(Produit::getPrix));
                case "Prix ↓ décroissant" -> filtres.sort(Comparator.comparingDouble(Produit::getPrix).reversed());
                case "Nom A → Z" -> filtres.sort(Comparator.comparing(Produit::getNom));
                case "Nom Z → A" -> filtres.sort(Comparator.comparing(Produit::getNom).reversed());
                case "Stock disponible" -> filtres.sort(Comparator.comparingInt(Produit::getStock).reversed());
            }

        afficherProduits(filtres);
    }

    private void updatePrixLabels() {
        prixMinLabel.setText(String.format("%.0f MAD", prixMinSlider.getValue()));
        prixMaxLabel.setText(String.format("%.0f MAD", prixMaxSlider.getValue()));
    }

    // ────────────────────────────────────────────────────────
    // Afficher les cartes
    // ────────────────────────────────────────────────────────
    private void afficherProduits(List<Produit> produits) {
        produitsGrid.getChildren().clear();
        produitsGrid.getColumnConstraints().clear();

        int cols = 3;
        for (int i = 0; i < cols; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / cols);
            produitsGrid.getColumnConstraints().add(cc);
        }

        if (nbResultatsLabel != null)
            nbResultatsLabel.setText(produits.size() + " produit(s)");

        int col = 0, row = 0;
        for (int i = 0; i < produits.size(); i++) {
            Produit p = produits.get(i);
            VBox card = creerCarte(p);

            // Animation d'apparition décalée
            card.setOpacity(0);
            card.setTranslateY(20);
            int delay = i * 60;

            FadeTransition ft = new FadeTransition(Duration.millis(300), card);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(delay));

            TranslateTransition tt = new TranslateTransition(Duration.millis(300), card);
            tt.setToY(0);
            tt.setDelay(Duration.millis(delay));

            ft.play();
            tt.play();

            produitsGrid.add(card, col, row);
            GridPane.setMargin(card, new Insets(10));
            col++;
            if (col == cols) {
                col = 0;
                row++;
            }
        }
    }

    // ────────────────────────────────────────────────────────
    // Créer une carte produit
    // ────────────────────────────────────────────────────────
    private VBox creerCarte(Produit p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(0, 0, 16, 0));
        card.setAlignment(Pos.TOP_CENTER);

        // Image
        // Image auto-générée pour chaque produit
        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(160);
        imageView.setFitWidth(240);
        imageView.setFitHeight(160);
        imageView.setSmooth(true);
        imageView.setImage(ProduitImageGenerator.generer(p, 240, 160));

        StackPane imageBox = new StackPane(imageView);
        imageBox.getStyleClass().add("card-image-box");
        imageBox.setPrefHeight(160);

        // Badge stock
        Label stockBadge = new Label(p.getStock() > 0
                ? "✔ En stock"
                : "✘ Rupture");
        stockBadge.getStyleClass().add(
                p.getStock() > 0 ? "badge-stock-ok" : "badge-stock-empty");

        // Infos
        VBox infos = new VBox(6);
        infos.setPadding(new Insets(0, 14, 0, 14));

        Label nom = new Label(p.getNom());
        nom.getStyleClass().add("product-name");
        nom.setWrapText(true);

        Label desc = new Label(p.getDescription() != null ? p.getDescription() : "");
        desc.getStyleClass().add("product-desc");
        desc.setWrapText(true);
        desc.setMaxHeight(36);

        Label prix = new Label(String.format("%.2f MAD", p.getPrix()));
        prix.getStyleClass().add("product-price");

        infos.getChildren().addAll(nom, desc, prix);

        // Boutons
        HBox btns = new HBox(8);
        btns.setPadding(new Insets(0, 14, 0, 14));

        Button detailBtn = new Button("👁 Détail");
        detailBtn.getStyleClass().add("btn-detail");
        detailBtn.setOnAction(e -> ouvrirDetail(p));

        Button addBtn = new Button("🛒 Ajouter");
        addBtn.getStyleClass().add("btn-add");
        addBtn.setDisable(p.getStock() == 0);
        addBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addBtn, Priority.ALWAYS);
        addBtn.setOnAction(e -> ajouterAuPanier(p, addBtn));

        btns.getChildren().addAll(detailBtn, addBtn);
        card.getChildren().addAll(imageBox, stockBadge, infos, btns);

        // Hover animation
        DropShadow shadowNormal = new DropShadow(10, Color.rgb(0, 0, 0, 0.08));
        DropShadow shadowHover = new DropShadow(24, Color.rgb(99, 102, 241, 0.28));
        card.setEffect(shadowNormal);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(180), card);
        scaleUp.setToX(1.04);
        scaleUp.setToY(1.04);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(180), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        card.setOnMouseEntered(e -> {
            card.setEffect(shadowHover);
            scaleUp.playFromStart();
        });
        card.setOnMouseExited(e -> {
            card.setEffect(shadowNormal);
            scaleDown.playFromStart();
        });

        return card;
    }

    // ────────────────────────────────────────────────────────
    // Ouvrir la page détail
    // ────────────────────────────────────────────────────────
    private void ouvrirDetail(Produit p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/detail-produit.fxml"));
            Parent root = loader.load();
            DetailproduitController ctrl = loader.getController();
            ctrl.setProduit(p, currentUser, panier);

            Stage stage = (Stage) produitsGrid.getScene().getWindow();
            root.setOpacity(0);
            stage.setScene(new Scene(root, 1400, 900));
            stage.setTitle("🔍 " + p.getNom());

            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setToValue(1);
            ft.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ────────────────────────────────────────────────────────
    // Ajouter au panier + Toast
    // ────────────────────────────────────────────────────────
    private void ajouterAuPanier(Produit p, Button btn) {
        if (panier == null)
            return;
        panier.ajouterItem(p, 1);
        majBadge();

        // Toast
        Stage stage = (Stage) btn.getScene().getWindow();
        ToastController.show(stage.getScene(), "✅ " + p.getNom() + " ajouté au panier !",
                ToastController.Type.SUCCESS);

        // Feedback bouton
        String orig = btn.getText();
        btn.setText("✅ Ajouté !");
        btn.setDisable(true);
        new Thread(() -> {
            try {
                Thread.sleep(1200);
            } catch (InterruptedException ignored) {
            }
            javafx.application.Platform.runLater(() -> {
                btn.setText(orig);
                btn.setDisable(false);
            });
        }).start();
    }

    // ────────────────────────────────────────────────────────
    // Mettre à jour le badge panier
    // ────────────────────────────────────────────────────────
    private void majBadge() {
        if (panier == null || badgePanier == null)
            return;
        int total = panier.getItems().stream().mapToInt(i -> i.getQuantite()).sum();
        badgePanier.setText(String.valueOf(total));
        badgePanier.setVisible(total > 0);

        // Animation pulse sur le badge
        ScaleTransition pulse = new ScaleTransition(Duration.millis(200), badgePanier);
        pulse.setFromX(1.0);
        pulse.setToX(1.4);
        pulse.setFromY(1.0);
        pulse.setToY(1.4);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }

    // ────────────────────────────────────────────────────────
    // Réinitialiser filtres
    // ────────────────────────────────────────────────────────
    @FXML
    public void reinitialiserFiltres() {
        searchField.clear();
        categorieCombo.getSelectionModel().selectFirst();
        triCombo.getSelectionModel().selectFirst();
        prixMinSlider.setValue(0);
        prixMaxSlider.setValue(prixMax);
    }

    private String getImagePath(int categorieId) {
        return switch (categorieId) {
            case 1 -> "/images/electronique.png";
            case 2 -> "/images/vetements.png";
            case 3 -> "/images/alimentation.png";
            default -> "/images/default.png";
        };
    }

    // ────────────────────────────────────────────────────────
    // Navigation avec transitions fade
    // ────────────────────────────────────────────────────────
    private void naviguer(String fxml, int w, int h, String title,
            java.util.function.Consumer<Object> setup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            if (setup != null)
                setup.accept(loader.getController());
            Stage stage = (Stage) panierButton.getScene().getWindow();
            root.setOpacity(0);
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setToValue(1);
            ft.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirPanier() {
        naviguer("/fxml/panier.fxml", 900, 620, "🛒 Mon Panier",
                ctrl -> ((PanierController) ctrl).setContext(currentUser, panier));
    }

    @FXML
    public void ouvrirHistorique() {
        naviguer("/fxml/historique.fxml", 900, 620, "📋 Mes Commandes",
                ctrl -> ((HistoriqueController) ctrl).setCurrentUser(currentUser));
    }

    @FXML
    public void ouvrirProfil() {
        naviguer("/fxml/profil.fxml", 520, 540, "👤 Mon Profil",
                ctrl -> ((ProfilController) ctrl).setCurrentUser(currentUser));
    }

    @FXML
    public void seDeconnecter() {
        naviguer("/fxml/login-client.fxml", 480, 420, "Connexion", null);
    }
}