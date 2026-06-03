package controller;

import dao.CategorieDAO;
import dao.ProduitDAO;
import model.Categorie;
import model.Produit;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ProduitsAdminController implements Initializable {
    @FXML private TableView<Produit>            table;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String>  colNom;
    @FXML private TableColumn<Produit, String>  colDesc;
    @FXML private TableColumn<Produit, Double>  colPrix;
    @FXML private TableColumn<Produit, Integer> colStock;
    @FXML private TableColumn<Produit, String>  colCategorie;
    @FXML private TextField        nomField;
    @FXML private TextArea         descField;
    @FXML private TextField        prixField;
    @FXML private TextField        stockField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private Label            formTitleLabel;
    @FXML private Label            errorLabel;
    @FXML private ImageView        imagePreview;
    @FXML private Label            imagePathLabel;
    @FXML private Button           uploadImageBtn;
    @FXML private TextField        searchField;
    @FXML private Label            countLabel;
    @FXML private Button           supprimerBtn;

    private final ProduitDAO   produitDAO   = new ProduitDAO();
    private final CategorieDAO categorieDAO = new CategorieDAO();
    private List<Produit>   allProduits;
    private List<Categorie> allCategories;
    private Produit         selectedProduit = null;
    private boolean         modeEdition     = false;
    private String          imagePath       = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allCategories = categorieDAO.getAllCategories();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colPrix.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.2f MAD", v));
            }
        });
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(v));
                setStyle(v == 0 ? "-fx-text-fill:#ef4444;-fx-font-weight:bold;"
                        : v <= 5 ? "-fx-text-fill:#f59e0b;-fx-font-weight:bold;"
                        : "-fx-text-fill:#10b981;");
            }
        });
        colCategorie.setCellValueFactory(data -> {
            int id = data.getValue().getCategorieId();
            return new SimpleStringProperty(allCategories.stream()
                    .filter(c -> c.getId() == id)
                    .map(Categorie::getNom).findFirst().orElse("—"));
        });
        categorieCombo.setItems(FXCollections.observableArrayList(
                allCategories.stream().map(Categorie::getNom).collect(Collectors.toList())));
        table.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, p) -> { if (p != null) remplirFormulaire(p); });
        searchField.textProperty().addListener((o, v, n) -> filtrer(n));
        chargerProduits();
        viderFormulaire();
    }

    @FXML
    public void uploadImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fc.showOpenDialog(uploadImageBtn.getScene().getWindow());
        if (file != null) {
            imagePath = file.toURI().toString();
            imagePathLabel.setText(file.getName());
            try {
                imagePreview.setImage(new Image(imagePath, 160, 120, true, true));
            } catch (Exception ignored) {}
        }
    }

    private void chargerProduits() {
        allProduits = produitDAO.getAllProduits();
        table.getItems().setAll(allProduits);
        countLabel.setText(allProduits.size() + " produit(s)");
    }

    private void filtrer(String t) {
        if (t == null || t.isEmpty()) { table.getItems().setAll(allProduits); return; }
        List<Produit> f = allProduits.stream()
                .filter(p -> p.getNom().toLowerCase().contains(t.toLowerCase())
                        || (p.getDescription() != null
                        && p.getDescription().toLowerCase().contains(t.toLowerCase())))
                .collect(Collectors.toList());
        table.getItems().setAll(f);
        countLabel.setText(f.size() + " produit(s)");
    }

    private void remplirFormulaire(Produit p) {
        selectedProduit = p; modeEdition = true;
        formTitleLabel.setText("Modifier le produit");
        nomField.setText(p.getNom());
        descField.setText(p.getDescription() != null ? p.getDescription() : "");
        prixField.setText(String.valueOf(p.getPrix()));
        stockField.setText(String.valueOf(p.getStock()));
        allCategories.stream().filter(c -> c.getId() == p.getCategorieId())
                .findFirst().ifPresent(c -> categorieCombo.setValue(c.getNom()));
        supprimerBtn.setDisable(false);
        errorLabel.setText("");
    }

    private void viderFormulaire() {
        selectedProduit = null; modeEdition = false;
        formTitleLabel.setText("Nouveau produit");
        nomField.clear(); descField.clear(); prixField.clear(); stockField.clear();
        categorieCombo.getSelectionModel().clearSelection();
        if (imagePreview != null) imagePreview.setImage(null);
        if (imagePathLabel != null) imagePathLabel.setText("Aucune image");
        imagePath = "";
        supprimerBtn.setDisable(true);
        errorLabel.setText("");
    }

    @FXML
    public void sauvegarder() {
        errorLabel.setText("");
        String nom = nomField.getText().trim(), desc = descField.getText().trim();
        String prixS = prixField.getText().trim(), stokS = stockField.getText().trim();
        String catNom = categorieCombo.getValue();
        if (nom.isEmpty() || prixS.isEmpty() || stokS.isEmpty() || catNom == null) {
            errorLabel.setText("Champs obligatoires manquants."); return;
        }
        double prix; int stock;
        try { prix = Double.parseDouble(prixS); stock = Integer.parseInt(stokS); }
        catch (NumberFormatException e) { errorLabel.setText("Prix/stock invalides."); return; }
        if (prix <= 0 || stock < 0) { errorLabel.setText("Prix > 0 et stock >= 0."); return; }
        int catId = allCategories.stream().filter(c -> c.getNom().equals(catNom))
                .mapToInt(Categorie::getId).findFirst().orElse(-1);
        if (modeEdition && selectedProduit != null) {
            selectedProduit.setNom(nom); selectedProduit.setDescription(desc);
            selectedProduit.setPrix(prix); selectedProduit.setStock(stock);
            selectedProduit.setCategorieId(catId);
            produitDAO.updateProduit(selectedProduit);
            new Alert(Alert.AlertType.INFORMATION, "Produit modifie !") {{ setHeaderText(null); showAndWait(); }};
        } else {
            produitDAO.ajouterProduit(new Produit(nom, desc, prix, stock, catId));
            new Alert(Alert.AlertType.INFORMATION, "Produit ajoute !") {{ setHeaderText(null); showAndWait(); }};
        }
        chargerProduits(); viderFormulaire();
    }

    @FXML
    public void supprimer() {
        if (selectedProduit == null) return;
        new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + selectedProduit.getNom() + "\" ?",
                ButtonType.YES, ButtonType.NO).showAndWait()
                .filter(r -> r == ButtonType.YES)
                .ifPresent(r -> { produitDAO.supprimerProduit(selectedProduit.getId());
                    chargerProduits(); viderFormulaire(); });
    }

    @FXML public void nouveau() { viderFormulaire(); }
    @FXML public void retourDashboard()    { naviguer("/fxml/dashboard.fxml"); }
    @FXML public void ouvrirUtilisateurs() { naviguer("/fxml/admin-users.fxml"); }
    @FXML public void ouvrirCommandes()    { naviguer("/fxml/admin-commandes.fxml"); }
    @FXML public void ouvrirCategories()   { naviguer("/fxml/admin-categories.fxml"); }
    @FXML public void seDeconnecter()      { naviguer("/fxml/accueil.fxml"); }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) table.getScene().getWindow();
            stage.setScene(new Scene(root,
                    fxml.contains("accueil") ? 680 : 1200,
                    fxml.contains("accueil") ? 480 : 760));
        } catch (Exception e) { e.printStackTrace(); }
    }
}