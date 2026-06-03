package controller;

import dao.CommandeDAO;
import model.Commande;
import model.LigneCommande;
import model.User;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller : Historique des commandes du client
 * Affiche la liste des commandes avec statuts colorés et détail par commande.
 */
public class HistoriqueController implements Initializable {

    @FXML private TableView<Commande>            commandesTable;
    @FXML private TableColumn<Commande, String>  colId;
    @FXML private TableColumn<Commande, String>  colDate;
    @FXML private TableColumn<Commande, String>  colStatut;
    @FXML private TableView<LigneCommande>       lignesTable;
    @FXML private TableColumn<LigneCommande, String>  colProduit;
    @FXML private TableColumn<LigneCommande, String>  colQte;
    @FXML private TableColumn<LigneCommande, String>  colPrix;
    @FXML private TableColumn<LigneCommande, String>  colTotal;
    @FXML private Label  detailLabel;
    @FXML private Button retourButton;

    private User   currentUser;
    private final CommandeDAO commandeDAO = new CommandeDAO();

    // ─────────────────────────────────────────────
    // Initialisation des colonnes
    // ─────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Colonnes commandes
        colId.setCellValueFactory(data ->
                new SimpleStringProperty("#" + data.getValue().getId()));

        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDateCommande() != null
                        ? data.getValue().getDateCommande().toString().substring(0, 16)
                        : "—"));

        colStatut.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatut()));

        // Coloriser les statuts
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    switch (statut) {
                        case "EN_ATTENTE"  -> setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                        case "VALIDEE"     -> setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                        case "ANNULEE"     -> setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        default            -> setStyle("-fx-text-fill: #6366f1; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Colonnes lignes commande
        colProduit.setCellValueFactory(data ->
                new SimpleStringProperty("Produit #" + data.getValue().getProduitId()));

        colQte.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getQuantite())));

        colPrix.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f MAD", data.getValue().getPrixUnitaire())));

        colTotal.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f MAD", data.getValue().getTotal())));

        // Sélection d'une commande → afficher ses lignes
        commandesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, commande) -> {
                    if (commande != null) afficherLignes(commande);
                });
    }

    // ─────────────────────────────────────────────
    // Injecter l'utilisateur
    // ─────────────────────────────────────────────
    public void setCurrentUser(User user) {
        this.currentUser = user;
        chargerCommandes();
    }

    // ─────────────────────────────────────────────
    // Charger les commandes de l'utilisateur
    // ─────────────────────────────────────────────
    private void chargerCommandes() {
        List<Commande> commandes = commandeDAO.getCommandesByUser(currentUser.getId());
        commandesTable.getItems().setAll(commandes);

        if (commandes.isEmpty()) {
            detailLabel.setText("Vous n'avez pas encore passé de commande.");
        }
    }

    // ─────────────────────────────────────────────
    // Afficher les lignes d'une commande sélectionnée
    // ─────────────────────────────────────────────
    private void afficherLignes(Commande commande) {
        List<LigneCommande> lignes = commandeDAO.getLignesByCommande(commande.getId());
        lignesTable.getItems().setAll(lignes);

        double total = lignes.stream().mapToDouble(LigneCommande::getTotal).sum();
        detailLabel.setText(String.format("Commande #%d — Total : %.2f MAD",
                commande.getId(), total));
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
}
