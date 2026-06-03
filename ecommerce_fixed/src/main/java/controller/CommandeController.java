package controller;

import dao.PanierDAO;
import model.Panier;
import model.PanierItem;
import model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller : Confirmation de commande
 * Affiche le récapitulatif, recueille l'adresse, enregistre la commande en BD.
 */
public class CommandeController implements Initializable {

    @FXML private VBox    recapBox;
    @FXML private Label   totalLabel;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private TextField codePostalField;
    @FXML private Label   errorLabel;
    @FXML private Button  confirmerButton;
    @FXML private Button  retourButton;

    private User   currentUser;
    private Panier panier;
    private final PanierDAO panierDAO = new PanierDAO();

    // ─────────────────────────────────────────────
    // Initialisation
    // ─────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    // ─────────────────────────────────────────────
    // Injecter le contexte depuis PanierController
    // ─────────────────────────────────────────────
    public void setContext(User user, Panier panier) {
        this.currentUser = user;
        this.panier      = panier;
        afficherRecap();
    }

    // ─────────────────────────────────────────────
    // Afficher le récapitulatif du panier
    // ─────────────────────────────────────────────
    private void afficherRecap() {
        recapBox.getChildren().clear();

        for (PanierItem item : panier.getItems()) {
            String ligne = String.format("• %s × %d  →  %.2f MAD",
                    item.getProduit().getNom(),
                    item.getQuantite(),
                    item.getSousTotal());
            Label l = new Label(ligne);
            l.getStyleClass().add("recap-item");
            recapBox.getChildren().add(l);
        }

        totalLabel.setText(String.format("Total à payer : %.2f MAD", panier.getTotal()));
    }

    // ─────────────────────────────────────────────
    // Confirmer la commande
    // ─────────────────────────────────────────────
    @FXML
    public void confirmerCommande() {
        errorLabel.setText("");

        // Validation de l'adresse
        if (adresseField.getText().trim().isEmpty()
                || villeField.getText().trim().isEmpty()
                || codePostalField.getText().trim().isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs de livraison.");
            return;
        }

        // Enregistrer la commande en BD (validerCommande gère stock + lignes)
        int commandeId = panierDAO.validerCommande(panier);

        if (commandeId == -1) {
            errorLabel.setText("Erreur lors de la validation. Vérifiez les stocks.");
            return;
        }

        // Succès — afficher confirmation et retour catalogue
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Commande confirmée !");
        alert.setHeaderText("🎉 Merci pour votre commande !");
        alert.setContentText(String.format(
                "Commande #%d enregistrée avec succès.\nLivraison à : %s, %s %s",
                commandeId,
                adresseField.getText().trim(),
                codePostalField.getText().trim(),
                villeField.getText().trim()));
        alert.showAndWait();

        retourCatalogue();
    }

    // ─────────────────────────────────────────────
    // Retour au panier
    // ─────────────────────────────────────────────
    @FXML
    public void retourPanier() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/panier.fxml"));
            Parent root = loader.load();

            PanierController ctrl = loader.getController();
            ctrl.setContext(currentUser, panier);

            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 620));
            stage.setTitle("🛒 Mon Panier");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────
    // Retour au catalogue après succès
    // ─────────────────────────────────────────────
    private void retourCatalogue() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/catalogue.fxml"));
            Parent root = loader.load();

            CatalogueController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);

            Stage stage = (Stage) confirmerButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("🛍 Boutique");
        } catch (IOException e) { e.printStackTrace(); }
    }
}
