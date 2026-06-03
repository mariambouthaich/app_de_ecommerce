package controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.Produit;

/**
 * Génère une image unique par produit :
 * dégradé selon catégorie + emoji selon le nom
 */
public class ProduitImageGenerator {

    public static Image generer(Produit p, double w, double h) {
        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Color[] cols = getCouleurs(p.getCategorieId());

        // Fond dégradé
        LinearGradient grad = new LinearGradient(0, 0, 0, h, false,
                CycleMethod.NO_CYCLE, new Stop(0, cols[0]), new Stop(1, cols[1]));
        gc.setFill(grad);
        gc.fillRoundRect(0, 0, w, h, 14, 14);

        // Cercles déco
        gc.setFill(cols[2]); gc.setGlobalAlpha(0.15);
        gc.fillOval(-20, -20, 80, 80);
        gc.fillOval(w - 50, h - 50, 80, 80);
        gc.setGlobalAlpha(1.0);

        // Emoji
        gc.setFont(Font.font("Segoe UI Emoji", FontWeight.NORMAL, 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.fillText(getEmoji(p.getNom(), p.getCategorieId()), w / 2, h / 2 + 10);

        // Nom (tronqué)
        String nom = p.getNom().length() > 18
                ? p.getNom().substring(0, 16) + "…" : p.getNom();
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setGlobalAlpha(0.9);
        gc.fillText(nom, w / 2, h - 14);
        gc.setGlobalAlpha(1.0);

        // Badge prix
        String prix = String.format("%.0f MAD", p.getPrix());
        gc.setFill(Color.WHITE); gc.setGlobalAlpha(0.22);
        gc.fillRoundRect(w - 82, 7, 75, 22, 11, 11);
        gc.setGlobalAlpha(1.0);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.fillText(prix, w - 44.5, 22);

        WritableImage img = new WritableImage((int) w, (int) h);
        canvas.snapshot(null, img);
        return img;
    }

    private static Color[] getCouleurs(int categorieId) {
        return switch (categorieId) {
            case 1  -> new Color[]{Color.web("#4f46e5"), Color.web("#3730a3"), Color.web("#818cf8")};
            case 2  -> new Color[]{Color.web("#db2777"), Color.web("#9d174d"), Color.web("#f9a8d4")};
            case 3  -> new Color[]{Color.web("#16a34a"), Color.web("#14532d"), Color.web("#86efac")};
            case 4  -> new Color[]{Color.web("#0891b2"), Color.web("#164e63"), Color.web("#67e8f9")};
            case 5  -> new Color[]{Color.web("#ea580c"), Color.web("#7c2d12"), Color.web("#fdba74")};
            default -> new Color[]{Color.web("#0d9488"), Color.web("#134e4a"), Color.web("#5eead4")};
        };
    }

    private static String getEmoji(String nom, int cat) {
        String n = nom.toLowerCase();
        if (n.contains("laptop") || n.contains("ordinateur")) return "💻";
        if (n.contains("phone") || n.contains("galaxy") || n.contains("iphone")) return "📱";
        if (n.contains("casque") || n.contains("headphone")) return "🎧";
        if (n.contains("ecran") || n.contains("écran") || n.contains("monitor")) return "🖥️";
        if (n.contains("clavier")) return "⌨️";
        if (n.contains("souris")) return "🖱️";
        if (n.contains("usb") || n.contains("clé")) return "💾";
        if (n.contains("jean") || n.contains("pantalon")) return "👖";
        if (n.contains("robe")) return "👗";
        if (n.contains("veste") || n.contains("manteau")) return "🧥";
        if (n.contains("t-shirt") || n.contains("shirt")) return "👕";
        if (n.contains("chaussure") || n.contains("sneaker")) return "👟";
        if (n.contains("café") || n.contains("coffee")) return "☕";
        if (n.contains("chocolat")) return "🍫";
        if (n.contains("jus") || n.contains("orange")) return "🍊";
        if (n.contains("huile")) return "🫒";
        if (n.contains("miel")) return "🍯";
        return switch (cat) {
            case 1 -> "📦"; case 2 -> "🛍️"; case 3 -> "🥑";
            case 4 -> "🔧"; case 5 -> "🏠"; default -> "🛒";
        };
    }
}
