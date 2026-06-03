package controller;

import model.Panier;
import model.Produit;
import model.User;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller : Page détail d'un produit
 * Affiche image grande, description complète, stock, sélecteur de quantité.
 */
public class DetailproduitController implements Initializable {

    @FXML
    private ImageView imageView;
    @FXML
    private Label nomLabel;
    @FXML
    private Label prixLabel;
    @FXML
    private Label descLabel;
    @FXML
    private Label stockLabel;
    @FXML
    private Label categorieLabel;
    @FXML
    private Spinner<Integer> quantiteSpinner;
    @FXML
    private Button ajouterBtn;
    @FXML
    private Button retourBtn;
    @FXML
    private HBox starsBox;

    private Produit currentProduit;
    private User currentUser;
    private Panier panier;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        quantiteSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
    }

    // ────────────────────────────────────────────────────────
    // Injecter le produit depuis CatalogueController
    // ────────────────────────────────────────────────────────
    public void setProduit(Produit p, User user, Panier panier) {
        this.currentProduit = p;
        this.currentUser = user;
        this.panier = panier;

        // Remplir les infos
        nomLabel.setText(p.getNom());
        prixLabel.setText(String.format("%.2f MAD", p.getPrix()));
        descLabel.setText(p.getDescription() != null
                ? p.getDescription()
                : "Aucune description disponible.");

        // Stock
        if (p.getStock() > 10) {
            stockLabel.setText("✔ En stock (" + p.getStock() + " disponibles)");
            stockLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        } else if (p.getStock() > 0) {
            stockLabel.setText("⚠ Stock limité (" + p.getStock() + " restants)");
            stockLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        } else {
            stockLabel.setText("✘ Rupture de stock");
            stockLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            ajouterBtn.setDisable(true);
        }

        // Spinner limité au stock disponible
        quantiteSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, Math.max(1, p.getStock()), 1));

        // Image
        imageView.setImage(ProduitImageGenerator.generer(p, 400, 300));

        // Animation d'entrée sur le bouton
        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), ajouterBtn);
        pulse.setFromX(1.0);
        pulse.setToX(1.03);
        pulse.setFromY(1.0);
        pulse.setToY(1.03);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(4);
        pulse.play();
    }

    // ────────────────────────────────────────────────────────
    // Ajouter au panier
    // ────────────────────────────────────────────────────────
    @FXML
    public void ajouterAuPanier() {
        int qte = quantiteSpinner.getValue();
        panier.ajouterItem(currentProduit, qte);

        // Toast notification
        Stage stage = (Stage) ajouterBtn.getScene().getWindow();
        ToastController.show(stage.getScene(),
                "🛒 " + currentProduit.getNom() + " × " + qte + " ajouté !",
                ToastController.Type.SUCCESS);

        // Retour au catalogue après ajout
        retourCatalogue();
    }

    // ────────────────────────────────────────────────────────
    // Retour au catalogue
    // ────────────────────────────────────────────────────────
    @FXML
    public void retourCatalogue() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/catalogue.fxml"));
            Parent root = loader.load();
            CatalogueController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            ctrl.setPanier(panier);

            Stage stage = (Stage) retourBtn.getScene().getWindow();

            // Transition fade
            root.setOpacity(0);
            stage.setScene(new Scene(root, 1400, 900));
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                    Duration.millis(300), root);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getImagePath(int categorieId) {
        return switch (categorieId) {
            case 1 -> "/images/electronique.png";
            case 2 -> "/images/vetements.png";
            case 3 -> "/images/alimentation.png";
            default -> "/images/default.png";
        };
    }
}
