package controller;

import dao.UserDAO;
import model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller : Profil client
 * Permet de modifier le nom, l'email et le mot de passe.
 */
public class ProfilController implements Initializable {

    @FXML private TextField     nomField;
    @FXML private TextField     emailField;
    @FXML private PasswordField ancienPasswordField;
    @FXML private PasswordField nouveauPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;
    @FXML private Label         successLabel;
    @FXML private Button        sauvegarderButton;
    @FXML private Button        retourButton;

    private User      currentUser;
    private final UserDAO userDAO = new UserDAO();

    // ─────────────────────────────────────────────
    // Initialisation
    // ─────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    // ─────────────────────────────────────────────
    // Injecter l'utilisateur et pré-remplir les champs
    // ─────────────────────────────────────────────
    public void setCurrentUser(User user) {
        this.currentUser = user;
        nomField.setText(user.getNom());
        emailField.setText(user.getEmail());
    }

    // ─────────────────────────────────────────────
    // Sauvegarder les modifications
    // ─────────────────────────────────────────────
    @FXML
    public void sauvegarder() {
        errorLabel.setText("");
        successLabel.setText("");

        String nom   = nomField.getText().trim();
        String email = emailField.getText().trim();

        // Validation nom / email
        if (nom.isEmpty() || email.isEmpty()) {
            errorLabel.setText("Le nom et l'email sont obligatoires.");
            return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errorLabel.setText("Format d'email invalide.");
            return;
        }

        // Vérifier si l'email est pris par un autre compte
        User existing = userDAO.getUserByEmail(email);
        if (existing != null && existing.getId() != currentUser.getId()) {
            errorLabel.setText("Cet email est déjà utilisé par un autre compte.");
            return;
        }

        // Mise à jour du mot de passe (optionnel)
        String newPassword = currentUser.getPassword(); // inchangé par défaut
        String ancien  = ancienPasswordField.getText();
        String nouveau = nouveauPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!ancien.isEmpty() || !nouveau.isEmpty() || !confirm.isEmpty()) {
            // L'utilisateur souhaite changer son mot de passe
            if (!LoginClientController.hashSHA256(ancien).equals(currentUser.getPassword())) {
                errorLabel.setText("Ancien mot de passe incorrect.");
                return;
            }
            if (nouveau.length() < 6) {
                errorLabel.setText("Le nouveau mot de passe doit contenir au moins 6 caractères.");
                return;
            }
            if (!nouveau.equals(confirm)) {
                errorLabel.setText("Les nouveaux mots de passe ne correspondent pas.");
                return;
            }
            newPassword = LoginClientController.hashSHA256(nouveau);
        }

        // Appliquer les changements
        currentUser.setNom(nom);
        currentUser.setEmail(email);
        currentUser.setPassword(newPassword);
        userDAO.updateUser(currentUser);

        // Vider les champs mot de passe
        ancienPasswordField.clear();
        nouveauPasswordField.clear();
        confirmPasswordField.clear();

        successLabel.setText("✅ Profil mis à jour avec succès !");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
