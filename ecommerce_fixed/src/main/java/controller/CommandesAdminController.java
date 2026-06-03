package controller;

import dao.CommandeDAO;
import dao.UserDAO;
import model.Commande;
import model.LigneCommande;
import model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class CommandesAdminController implements Initializable {

    @FXML private TableView<Commande>            commandesTable;
    @FXML private TableColumn<Commande, Integer> colId;
    @FXML private TableColumn<Commande, String>  colUser;
    @FXML private TableColumn<Commande, String>  colDate;
    @FXML private TableColumn<Commande, String>  colStatut;
    @FXML private TableView<LigneCommande>            lignesTable;
    @FXML private TableColumn<LigneCommande, Integer> colProduitId;
    @FXML private TableColumn<LigneCommande, Integer> colQte;
    @FXML private TableColumn<LigneCommande, String>  colPrix;
    @FXML private TableColumn<LigneCommande, String>  colTotalLigne;
    @FXML private ComboBox<String> statutCombo;
    @FXML private Label            detailLabel;
    @FXML private Label            totalCommandeLabel;
    @FXML private TextField        searchField;
    @FXML private Label            countLabel;

    private final CommandeDAO commandeDAO = new CommandeDAO();
    private final UserDAO     userDAO     = new UserDAO();
    private List<Commande>    allCommandes = new ArrayList<>();
    private List<User>        allUsers;
    private Commande          selectedCommande = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allUsers = userDAO.getAllUsers();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(data -> {
            int uid = data.getValue().getUserId();
            String nom = allUsers.stream().filter(u -> u.getId() == uid)
                    .map(User::getNom).findFirst().orElse("ID " + uid);
            return new SimpleStringProperty(nom);
        });
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDateCommande() != null
                        ? data.getValue().getDateCommande().toString().substring(0, 16) : "—"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle(switch (s) {
                    case "EN_ATTENTE" -> "-fx-text-fill:#f59e0b;-fx-font-weight:bold;";
                    case "VALIDEE"    -> "-fx-text-fill:#10b981;-fx-font-weight:bold;";
                    case "LIVREE"     -> "-fx-text-fill:#6366f1;-fx-font-weight:bold;";
                    case "ANNULEE"    -> "-fx-text-fill:#ef4444;-fx-font-weight:bold;";
                    default           -> "";
                });
            }
        });

        colProduitId.setCellValueFactory(new PropertyValueFactory<>("produitId"));
        colQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colPrix.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.2f MAD", data.getValue().getPrixUnitaire())));
        colTotalLigne.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.2f MAD", data.getValue().getTotal())));

        statutCombo.setItems(FXCollections.observableArrayList(
                "EN_ATTENTE", "VALIDEE", "LIVREE", "ANNULEE"));

        commandesTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, c) -> { if (c != null) afficherLignes(c); });
        searchField.textProperty().addListener((o, v, n) -> filtrer(n));

        chargerCommandes();
    }

    private void chargerCommandes() {
        allCommandes.clear();
        for (User u : allUsers)
            allCommandes.addAll(commandeDAO.getCommandesByUser(u.getId()));
        commandesTable.getItems().setAll(allCommandes);
        countLabel.setText(allCommandes.size() + " commande(s)");
    }

    private void filtrer(String t) {
        if (t == null || t.isEmpty()) { commandesTable.getItems().setAll(allCommandes); return; }
        List<Commande> f = allCommandes.stream()
                .filter(c -> String.valueOf(c.getId()).contains(t)
                        || c.getStatut().toLowerCase().contains(t.toLowerCase()))
                .collect(Collectors.toList());
        commandesTable.getItems().setAll(f);
        countLabel.setText(f.size() + " commande(s)");
    }

    private void afficherLignes(Commande c) {
        selectedCommande = c;
        List<LigneCommande> lignes = commandeDAO.getLignesByCommande(c.getId());
        lignesTable.getItems().setAll(lignes);
        double total = lignes.stream().mapToDouble(LigneCommande::getTotal).sum();
        String nomUser = allUsers.stream().filter(u -> u.getId() == c.getUserId())
                .map(User::getNom).findFirst().orElse("?");
        detailLabel.setText(String.format("Commande #%d — Client : %s — Statut : %s",
                c.getId(), nomUser, c.getStatut()));
        totalCommandeLabel.setText(String.format("Total : %.2f MAD", total));
        statutCombo.setValue(c.getStatut());
    }

    @FXML public void changerStatut() {
        if (selectedCommande == null || statutCombo.getValue() == null) return;
        commandeDAO.updateStatut(selectedCommande.getId(), statutCombo.getValue());
        selectedCommande.setStatut(statutCombo.getValue());
        chargerCommandes();
    }

    @FXML public void supprimerCommande() {
        if (selectedCommande == null) return;
        new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la commande #" + selectedCommande.getId() + " ?",
                ButtonType.YES, ButtonType.NO).showAndWait()
                .filter(r -> r == ButtonType.YES)
                .ifPresent(r -> {
                    commandeDAO.supprimerCommande(selectedCommande.getId());
                    chargerCommandes();
                    lignesTable.getItems().clear();
                    detailLabel.setText(""); totalCommandeLabel.setText("");
                    selectedCommande = null;
                });
    }

    @FXML public void retourDashboard()  { nav("/fxml/dashboard.fxml"); }
    @FXML public void ouvrirProduits()   { nav("/fxml/admin-produits.fxml"); }
    @FXML public void ouvrirUsers()      { nav("/fxml/admin-users.fxml"); }
    @FXML public void ouvrirCategories() { nav("/fxml/admin-categories.fxml"); }
    @FXML public void seDeconnecter()    { nav("/fxml/accueil.fxml"); }

    private void nav(String f) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(f));
            Stage stage = (Stage) commandesTable.getScene().getWindow();
            stage.setScene(new Scene(root,
                    f.contains("accueil") ? 680 : 1200,
                    f.contains("accueil") ? 480 : 760));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
