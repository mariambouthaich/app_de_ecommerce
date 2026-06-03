package controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller : Page d'accueil principale
 * Choix entre portail Admin ou portail Client
 */
public class AccueilController implements Initializable {

    @FXML private VBox  rootBox;
    @FXML private Button adminBtn;
    @FXML private Button clientBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Animation d'entrée
        rootBox.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), rootBox);
        ft.setToValue(1);
        ft.play();

        // Hover animation sur les boutons
        animerBouton(adminBtn);
        animerBouton(clientBtn);
    }

    private void animerBouton(Button btn) {
        ScaleTransition up   = new ScaleTransition(Duration.millis(150), btn);
        up.setToX(1.05); up.setToY(1.05);
        ScaleTransition down = new ScaleTransition(Duration.millis(150), btn);
        down.setToX(1.0); down.setToY(1.0);
        btn.setOnMouseEntered(e -> up.playFromStart());
        btn.setOnMouseExited(e  -> down.playFromStart());
    }

    // ────────────────────────────────────────────────────────
    // Navigation
    // ────────────────────────────────────────────────────────
    @FXML
    public void ouvrirAdmin() {
        naviguer("/fxml/login-admin.fxml", 480, 440, "🛠 Administration");
    }

    @FXML
    public void ouvrirClient() {
        naviguer("/fxml/login-client.fxml", 480, 420, "🛍 Espace Client");
    }

    private void naviguer(String fxml, int w, int h, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage  stage = (Stage) adminBtn.getScene().getWindow();
            root.setOpacity(0);
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setToValue(1); ft.play();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
