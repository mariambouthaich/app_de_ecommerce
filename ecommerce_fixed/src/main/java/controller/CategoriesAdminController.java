package controller;

import dao.CategorieDAO;
import model.Categorie;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CategoriesAdminController implements Initializable {
    @FXML private TableView<Categorie>            table;
    @FXML private TableColumn<Categorie, Integer> colId;
    @FXML private TableColumn<Categorie, String>  colNom;
    @FXML private TextField nomField;
    @FXML private Label     formTitleLabel;
    @FXML private Label     errorLabel;
    @FXML private Label     countLabel;
    @FXML private Button    supprimerBtn;

    private final CategorieDAO categorieDAO = new CategorieDAO();
    private Categorie selectedCategorie = null;
    private boolean   modeEdition       = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        table.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, c) -> { if (c != null) remplirFormulaire(c); });
        chargerCategories(); viderFormulaire();
    }

    private void chargerCategories() {
        List<Categorie> list = categorieDAO.getAllCategories();
        table.getItems().setAll(list);
        countLabel.setText(list.size() + " catégorie(s)");
    }

    private void remplirFormulaire(Categorie c) {
        selectedCategorie = c; modeEdition = true;
        formTitleLabel.setText("✏️ Modifier la catégorie");
        nomField.setText(c.getNom());
        supprimerBtn.setDisable(false); errorLabel.setText("");
    }

    private void viderFormulaire() {
        selectedCategorie = null; modeEdition = false;
        formTitleLabel.setText("➕ Nouvelle catégorie");
        nomField.clear(); supprimerBtn.setDisable(true); errorLabel.setText("");
    }

    @FXML public void sauvegarder() {
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) { errorLabel.setText("Le nom est obligatoire."); return; }
        if (modeEdition && selectedCategorie != null) {
            selectedCategorie.setNom(nom);
            categorieDAO.updateCategorie(selectedCategorie);
        } else {
            categorieDAO.ajouterCategorie(new Categorie(nom));
        }
        chargerCategories(); viderFormulaire();
    }

    @FXML public void supprimer() {
        if (selectedCategorie == null) return;
        new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + selectedCategorie.getNom() + "\" ?",
                ButtonType.YES, ButtonType.NO).showAndWait()
                .filter(r -> r == ButtonType.YES)
                .ifPresent(r -> { categorieDAO.deleteCategorie(selectedCategorie.getId());
                    chargerCategories(); viderFormulaire(); });
    }

    @FXML public void nouveau() { viderFormulaire(); }
    @FXML public void retourDashboard() { nav("/fxml/dashboard.fxml"); }
    @FXML public void ouvrirProduits()  { nav("/fxml/admin-produits.fxml"); }
    @FXML public void ouvrirCommandes() { nav("/fxml/admin-commandes.fxml"); }
    @FXML public void ouvrirUsers()     { nav("/fxml/admin-users.fxml"); }
    @FXML public void seDeconnecter()   { nav("/fxml/accueil.fxml"); }

    private void nav(String f) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(f));
            Stage stage = (Stage) table.getScene().getWindow();
            stage.setScene(new Scene(root,
                    f.contains("accueil") ? 680 : 1200,
                    f.contains("accueil") ? 480 : 760));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
