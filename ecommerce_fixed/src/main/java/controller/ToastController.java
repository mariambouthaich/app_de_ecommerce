package controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Utilitaire d'affichage de notifications "toast" non-bloquantes.
 * Utilisé dans toute l'interface admin pour les confirmations et erreurs.
 *
 * Usage :
 *   ToastController.show(scene, "Produit ajouté !", ToastController.Type.SUCCESS);
 */
public class ToastController {

    public enum Type { SUCCESS, ERROR, INFO }

    /**
     * Affiche un toast en bas à droite de la scène pendant 3 secondes.
     *
     * @param scene   La scène parente
     * @param message Le message à afficher
     * @param type    SUCCESS (vert), ERROR (rouge), INFO (violet)
     */
    public static void show(Scene scene, String message, Type type) {
        if (scene == null) return;

        StackPane root = (StackPane) scene.lookup("#toastRoot");

        // Construire le toast
        HBox toast = buildToast(message, type);

        // Chercher le root de la scène (doit être un StackPane ou parent compatible)
        javafx.scene.Parent sceneRoot = scene.getRoot();
        if (!(sceneRoot instanceof StackPane)) {
            // Envelopper si nécessaire (fallback)
            StackPane wrapper = new StackPane(sceneRoot);
            wrapper.setId("toastRoot");
            scene.setRoot(wrapper);
            sceneRoot = wrapper;
        }

        StackPane overlay = (StackPane) sceneRoot;
        StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
        toast.setTranslateX(-20);
        toast.setTranslateY(-30);

        overlay.getChildren().add(toast);

        // Animation : slide-in + pause + fade-out
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toast);
        slideIn.setFromY(60);
        slideIn.setToY(-30);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> overlay.getChildren().remove(toast));

        // Jouer l'animation
        slideIn.play();
        fadeIn.play();

        SequentialTransition seq = new SequentialTransition(pause, fadeOut);
        seq.play();

        // Fermeture au clic
        toast.setOnMouseClicked(e -> {
            seq.stop();
            fadeOut.play();
        });
    }

    // ─── Construction du toast ────────────────────────────────────
    private static HBox buildToast(String message, Type type) {
        // Icône selon le type
        String icon;
        String styleClass;
        switch (type) {
            case SUCCESS -> { icon = "✅"; styleClass = "toast-success"; }
            case ERROR   -> { icon = "❌"; styleClass = "toast-error";   }
            default      -> { icon = "ℹ";  styleClass = "toast-info";    }
        }

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 13px; -fx-font-weight: bold;");
        msgLabel.setWrapText(false);
        msgLabel.setMaxWidth(320);

        HBox toast = new HBox(12, iconLabel, msgLabel);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getStyleClass().add(styleClass);
        toast.setMaxWidth(400);
        toast.setOpacity(0);
        toast.setStyle(toast.getStyle() + "; -fx-cursor: hand;");

        // Appliquer les styles CSS (si la scène les a déjà chargés)
        // Le toast hérite du stylesheet de la scène parent

        return toast;
    }
}