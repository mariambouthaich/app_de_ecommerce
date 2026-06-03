package controller;

import dao.UserDAO;
import model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller : Inscription d'un nouveau client
 * Valide les champs, hache le mot de passe, enregistre en BD.
 */
public class RegisterController {

    @FXML private TextField     nomField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;
    @FXML private Button        registerButton;
    @FXML private Hyperlink     loginLink;

    private final UserDAO userDAO = new UserDAO();

    // ─────────────────────────────────────────────
    // Inscription
    // ─────────────────────────────────────────────
    @FXML
    public void handleRegister() {
        String nom      = nomField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();

        errorLabel.setText("");

        // Validation des champs
        if (nom.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("Tous les champs sont obligatoires.");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errorLabel.setText("Format d'email invalide.");
            return;
        }

        if (password.length() < 6) {
            errorLabel.setText("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!password.equals(confirm)) {
            errorLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        // Vérifier si l'email est déjà utilisé
        if (userDAO.getUserByEmail(email) != null) {
            errorLabel.setText("Un compte existe déjà avec cet email.");
            return;
        }

        // Créer l'utilisateur avec mot de passe haché
        User user = new User(nom, email, LoginClientController.hashSHA256(password));
        userDAO.ajouterUser(user);

        // Succès → retour au login
        showAlert(Alert.AlertType.INFORMATION,
                "Compte créé",
                "Votre compte a été créé avec succès ! Vous pouvez maintenant vous connecter.");
        handleGoToLogin();
    }

    // ─────────────────────────────────────────────
    // Retour au login
    // ─────────────────────────────────────────────
    @FXML
    public void handleGoToLogin() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/login-client.fxml"));
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(root, 480, 420));
            stage.setTitle("Connexion Client");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    // Utilitaire
    // ─────────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
