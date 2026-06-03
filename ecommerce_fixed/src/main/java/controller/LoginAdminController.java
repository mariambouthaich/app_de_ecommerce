package controller;

import dao.UserDAO;
import model.User;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller : Connexion Admin
 * Vérifie email + mot de passe + rôle ADMIN en base de données
 */
public class LoginAdminController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;
    @FXML private Button        retourBtn;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String pass  = passwordField.getText();

        errorLabel.setText("");

        if (email.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        User user = userDAO.getUserByEmail(email);

        if (user == null) {
            errorLabel.setText("Aucun compte trouvé avec cet email.");
            return;
        }

        // Vérification mot de passe SHA-256
        if (!LoginClientController.hashSHA256(pass).equals(user.getPassword())) {
            errorLabel.setText("Mot de passe incorrect.");
            return;
        }

        // Vérification du rôle ADMIN
        // Note : si la colonne role n'existe pas encore, utiliser identifiants fixes
        // Sinon utiliser : if (!"ADMIN".equals(user.getRole()))
        // Pour compatibilité avec la BD existante, on autorise les emails admin connus
        if (!email.equalsIgnoreCase("admin@mail.com")
                && !email.equalsIgnoreCase("aya@mail.com")) {
            errorLabel.setText("Accès refusé. Compte non administrateur.");
            return;
        }

        // Succès → Dashboard
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController ctrl = loader.getController();
            ctrl.setAdminUser(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            root.setOpacity(0);
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("🛠 Dashboard — " + user.getNom());
            stage.setMaximized(true);
            stage.setResizable(true);

            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setToValue(1); ft.play();

        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void retourAccueil() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/accueil.fxml"));
            Stage stage = (Stage) retourBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 680, 480));
            stage.setTitle("🛍 E-Commerce");
        } catch (Exception e) { e.printStackTrace(); }
    }
}