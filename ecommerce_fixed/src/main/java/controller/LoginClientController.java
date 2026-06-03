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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Controller : Connexion Client
 * Vérifie les identifiants, redirige vers le catalogue ou affiche une erreur.
 */
public class LoginClientController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;

    private final UserDAO userDAO = new UserDAO();

    // ─────────────────────────────────────────────
    // Connexion
    // ─────────────────────────────────────────────
    @FXML
    public void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        errorLabel.setText("");

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        User user = userDAO.getUserByEmail(email);

        if (user == null) {
            errorLabel.setText("Aucun compte trouvé avec cet email.");
            return;
        }

        // Vérification mot de passe (SHA-256)
        if (!hashSHA256(password).equals(user.getPassword())) {
            errorLabel.setText("Mot de passe incorrect.");
            return;
        }

        // Succès → ouvrir le catalogue
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/catalogue.fxml"));
            Parent root = loader.load();

            CatalogueController ctrl = loader.getController();
            ctrl.setCurrentUser(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("🛍 Boutique — " + user.getNom());
        } catch (IOException e) {
            errorLabel.setText("Erreur lors du chargement du catalogue.");
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    // Rediriger vers l'inscription
    // ─────────────────────────────────────────────
    @FXML
    public void handleRegister() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/register.fxml"));
            Stage stage = (Stage) registerLink.getScene().getWindow();
            stage.setScene(new Scene(root, 480, 560));
            stage.setTitle("Créer un compte");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    // Hachage SHA-256
    // ─────────────────────────────────────────────
    public static String hashSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
