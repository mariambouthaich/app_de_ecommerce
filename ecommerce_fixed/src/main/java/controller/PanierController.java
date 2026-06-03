package controller;

import dao.PanierDAO;
import model.Panier;
import model.PanierItem;
import model.User;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller : Gestion du panier
 * Affiche les articles, permet de modifier les quantités, calculer le total et valider.
 */
public class PanierController implements Initializable {

    @FXML private TableView<PanierItem>            panierTable;
    @FXML private TableColumn<PanierItem, String>  colNom;
    @FXML private TableColumn<PanierItem, Double>  colPrix;
    @FXML private TableColumn<PanierItem, Integer> colQte;
    @FXML private TableColumn<PanierItem, Double>  colSousTotal;
    @FXML private Label  totalLabel;
    @FXML private Button validerButton;
    @FXML private Button retourButton;
    @FXML private Button supprimerButton;
    @FXML private Button viderButton;

    private User   currentUser;
    private Panier panier;
    private final PanierDAO panierDAO = new PanierDAO();

    // ─────────────────────────────────────────────
    // Injecter le contexte depuis CatalogueController
    // ─────────────────────────────────────────────
    public void setContext(User user, Panier panier) {
        this.currentUser = user;
        this.panier      = panier;
        rafraichirTable();
    }

    // ─────────────────────────────────────────────
    // Initialisation des colonnes
    // ─────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNom.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduit().getNom()));

        colPrix.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getProduit().getPrix()).asObject());

        colQte.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantite()).asObject());

        // Colonne quantité éditable (spinner)
        colQte.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 99, 1);
            {
                spinner.valueProperty().addListener((obs, old, val) -> {
                    PanierItem item = getTableRow().getItem();
                    if (item != null && val != null) {
                        item.setQuantite(val);
                        rafraichirTotal();
                        panierTable.refresh();
                    }
                });
            }
            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    spinner.getValueFactory().setValue(qty);
                    setGraphic(spinner);
                }
            }
        });

        colSousTotal.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getSousTotal()).asObject());
        colSousTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("%.2f MAD", val));
            }
        });
    }

    // ─────────────────────────────────────────────
    // Rafraîchir la table et le total
    // ─────────────────────────────────────────────
    private void rafraichirTable() {
        if (panier == null) return;
        panierTable.getItems().setAll(panier.getItems());
        rafraichirTotal();
    }

    private void rafraichirTotal() {
        if (panier == null) return;
        totalLabel.setText(String.format("Total : %.2f MAD", panier.getTotal()));
    }

    // ─────────────────────────────────────────────
    // Supprimer l'article sélectionné
    // ─────────────────────────────────────────────
    @FXML
    public void supprimerArticle() {
        PanierItem selected = panierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélectionnez un article à supprimer.");
            return;
        }
        panier.supprimerItem(selected.getProduit().getId());
        rafraichirTable();
    }

    // ─────────────────────────────────────────────
    // Vider le panier
    // ─────────────────────────────────────────────
    @FXML
    public void viderPanier() {
        Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION,
                "Vider tout le panier ?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            panier.vider();
            rafraichirTable();
        }
    }

    // ─────────────────────────────────────────────
    // Valider la commande → écran de confirmation
    // ─────────────────────────────────────────────
    @FXML
    public void validerCommande() {
        if (panier == null || panier.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Votre panier est vide.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/commande.fxml"));
            Parent root = loader.load();

            CommandeController ctrl = loader.getController();
            ctrl.setContext(currentUser, panier);

            Stage stage = (Stage) validerButton.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 520));
            stage.setTitle("✅ Confirmer la commande");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────
    // Retour au catalogue
    // ─────────────────────────────────────────────
    @FXML
    public void retourCatalogue() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/catalogue.fxml"));
            Parent root = loader.load();

            CatalogueController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);

            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("🛍 Boutique");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────
    // Utilitaire
    // ─────────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
