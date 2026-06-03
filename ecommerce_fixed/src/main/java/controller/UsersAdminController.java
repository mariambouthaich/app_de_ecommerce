package controller;

import dao.UserDAO;
import model.User;
import javafx.beans.property.SimpleStringProperty;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UsersAdminController implements Initializable {
    @FXML private TableView<User>            table;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colNom;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colPassword;
    @FXML private TextField     nomField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         formTitleLabel;
    @FXML private Label         errorLabel;
    @FXML private TextField     searchField;
    @FXML private Label         countLabel;
    @FXML private Button        supprimerBtn;

    private final UserDAO userDAO = new UserDAO();
    private List<User>    allUsers;
    private User          selectedUser = null;
    private boolean       modeEdition  = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPassword.setCellValueFactory(data -> new SimpleStringProperty("••••••••"));
        table.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, u) -> { if (u != null) remplirFormulaire(u); });
        searchField.textProperty().addListener((o, v, n) -> filtrer(n));
        chargerUsers(); viderFormulaire();
    }

    private void chargerUsers() {
        allUsers = userDAO.getAllUsers();
        table.getItems().setAll(allUsers);
        countLabel.setText(allUsers.size() + " utilisateur(s)");
    }

    private void filtrer(String t) {
        if (t == null || t.isEmpty()) { table.getItems().setAll(allUsers); return; }
        List<User> f = allUsers.stream()
                .filter(u -> u.getNom().toLowerCase().contains(t.toLowerCase())
                        || u.getEmail().toLowerCase().contains(t.toLowerCase()))
                .collect(Collectors.toList());
        table.getItems().setAll(f);
        countLabel.setText(f.size() + " utilisateur(s)");
    }

    private void remplirFormulaire(User u) {
        selectedUser = u; modeEdition = true;
        formTitleLabel.setText("✏️ Modifier l'utilisateur");
        nomField.setText(u.getNom()); emailField.setText(u.getEmail());
        passwordField.clear(); supprimerBtn.setDisable(false); errorLabel.setText("");
    }

    private void viderFormulaire() {
        selectedUser = null; modeEdition = false;
        formTitleLabel.setText("➕ Nouvel utilisateur");
        nomField.clear(); emailField.clear(); passwordField.clear();
        supprimerBtn.setDisable(true); errorLabel.setText("");
    }

    @FXML public void sauvegarder() {
        errorLabel.setText("");
        String nom = nomField.getText().trim(), email = emailField.getText().trim();
        String pass = passwordField.getText();
        if (nom.isEmpty() || email.isEmpty()) { errorLabel.setText("Nom et email requis."); return; }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errorLabel.setText("Format email invalide."); return; }
        if (modeEdition && selectedUser != null) {
            selectedUser.setNom(nom); selectedUser.setEmail(email);
            if (!pass.isEmpty()) selectedUser.setPassword(LoginClientController.hashSHA256(pass));
            userDAO.updateUser(selectedUser);
        } else {
            if (pass.isEmpty()) { errorLabel.setText("Mot de passe requis."); return; }
            userDAO.ajouterUser(new User(nom, email, LoginClientController.hashSHA256(pass)));
        }
        chargerUsers(); viderFormulaire();
    }

    @FXML public void supprimer() {
        if (selectedUser == null) return;
        new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + selectedUser.getNom() + " ?",
                ButtonType.YES, ButtonType.NO).showAndWait()
                .filter(r -> r == ButtonType.YES)
                .ifPresent(r -> { userDAO.deleteUser(selectedUser.getId()); chargerUsers(); viderFormulaire(); });
    }

    @FXML public void nouveau() { viderFormulaire(); }
    @FXML public void retourDashboard()  { nav("/fxml/dashboard.fxml"); }
    @FXML public void ouvrirProduits()   { nav("/fxml/admin-produits.fxml"); }
    @FXML public void ouvrirCommandes()  { nav("/fxml/admin-commandes.fxml"); }
    @FXML public void ouvrirCategories() { nav("/fxml/admin-categories.fxml"); }
    @FXML public void seDeconnecter()    { nav("/fxml/accueil.fxml"); }

    private void nav(String f) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(f));
            Stage stage = (Stage) table.getScene().getWindow();
            stage.setScene(new Scene(root, f.contains("accueil") ? 680 : 1200, f.contains("accueil") ? 480 : 760));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
