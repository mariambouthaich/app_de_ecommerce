package controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller : Splash Screen animé
 * Affiché au démarrage pendant 3 secondes avant le login.
 */
public class SplashController implements Initializable {

    @FXML private VBox      splashBox;
    @FXML private Label     logoLabel;
    @FXML private Label     titleLabel;
    @FXML private Label     subtitleLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label     loadingLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // ── 1. Fade in du logo ───────────────────────────────
        splashBox.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), splashBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // ── 2. Animation scale du logo ───────────────────────
        ScaleTransition scale = new ScaleTransition(Duration.millis(800), logoLabel);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);

        // ── 3. Barre de progression ──────────────────────────
        progressBar.setProgress(0);
        Timeline progress = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(Duration.millis(2500),
                        new KeyValue(progressBar.progressProperty(), 1,
                                Interpolator.EASE_IN))
        );

        // ── 4. Messages de chargement animés ────────────────
        String[] messages = {
                "Chargement des produits...",
                "Connexion à la base de données...",
                "Préparation de votre boutique...",
                "Presque prêt !"
        };
        Timeline msgTimeline = new Timeline(
                new KeyFrame(Duration.millis(0),   e -> loadingLabel.setText(messages[0])),
                new KeyFrame(Duration.millis(700),  e -> loadingLabel.setText(messages[1])),
                new KeyFrame(Duration.millis(1400), e -> loadingLabel.setText(messages[2])),
                new KeyFrame(Duration.millis(2100), e -> loadingLabel.setText(messages[3]))
        );

        // ── 5. Fade out final puis transition vers login ─────
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), splashBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.millis(2600));
        fadeOut.setOnFinished(e -> ouvrirLogin());

        // ── Lancer toutes les animations ensemble ────────────
        ParallelTransition parallel = new ParallelTransition(fadeIn, scale);
        parallel.play();
        progress.play();
        msgTimeline.play();
        fadeOut.play();
    }

    // ────────────────────────────────────────────────────────
    // Transition vers la page login
    // ────────────────────────────────────────────────────────
    private void ouvrirLogin() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/accueil.fxml"));
            Stage stage = (Stage) splashBox.getScene().getWindow();

            // Transition avec fade
            Scene scene = new Scene(root, 680, 480);
            stage.setScene(scene);
            stage.setTitle("🛍 E-Commerce — Connexion");

            // Fade in de la nouvelle scène
            root.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(400), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
