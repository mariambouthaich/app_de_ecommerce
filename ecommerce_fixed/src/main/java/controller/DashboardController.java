package controller;

import dao.*;
import model.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    // ── KPI ───────────────────────────────────────────────────
    @FXML private Label nbUsersLabel;
    @FXML private Label nbProduitsLabel;
    @FXML private Label nbCommandesLabel;
    @FXML private Label caTotalLabel;
    @FXML private Label nbCategoriesLabel;

    // ── Alertes stock ─────────────────────────────────────────
    @FXML private Label stockAlertLabel;
    @FXML private Label stockFaibleLabel;
    @FXML private Label produitPlusCherLabel;

    // ── Statuts commandes ─────────────────────────────────────
    @FXML private Label cmdEnAttenteLabel;
    @FXML private Label cmdValideeLabel;
    @FXML private Label cmdLivreeLabel;
    @FXML private Label cmdAnnuleeLabel;

    // ── Conteneurs dynamiques ─────────────────────────────────
    @FXML private VBox topProduitsContainer;
    @FXML private VBox caCategorieContainer;
    @FXML private VBox dernieresCommandesContainer;
    @FXML private VBox usersRecentContainer;

    // ── Misc ──────────────────────────────────────────────────
    @FXML private Label adminNomLabel;
    @FXML private Label notifLabel;

    private final UserDAO      userDAO      = new UserDAO();
    private final ProduitDAO   produitDAO   = new ProduitDAO();
    private final CommandeDAO  commandeDAO  = new CommandeDAO();
    private final CategorieDAO categorieDAO = new CategorieDAO();

    private User     adminUser;
    private int      lastCommandeCount = 0;
    private Timeline notifTimeline;

    public void setAdminUser(User user) {
        this.adminUser = user;
        if (adminNomLabel != null && user != null)
            adminNomLabel.setText("Bonjour, " + user.getNom() + " 👋");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerTout();
        demarrerNotifications();
    }

    private void chargerTout() {
        chargerKPI();
        chargerAlerteStock();
        chargerStatutsCommandes();
        chargerTopProduits();
        chargerCACategorie();
        chargerDernieresCommandes();
        chargerUtilisateursRecents();
    }

    // ── 1. KPI ────────────────────────────────────────────────
    private void chargerKPI() {
        List<User>      users      = userDAO.getAllUsers();
        List<Produit>   produits   = produitDAO.getAllProduits();
        List<Categorie> categories = categorieDAO.getAllCategories();

        nbUsersLabel.setText(String.valueOf(users.size()));
        nbProduitsLabel.setText(String.valueOf(produits.size()));
        nbCategoriesLabel.setText(String.valueOf(categories.size()));

        int totalCmd = 0; double ca = 0;
        for (User u : users) {
            List<Commande> cmds = commandeDAO.getCommandesByUser(u.getId());
            totalCmd += cmds.size();
            for (Commande c : cmds)
                ca += commandeDAO.getLignesByCommande(c.getId())
                        .stream().mapToDouble(LigneCommande::getTotal).sum();
        }
        nbCommandesLabel.setText(String.valueOf(totalCmd));
        caTotalLabel.setText(String.format("%.2f MAD", ca));
        lastCommandeCount = totalCmd;
    }

    // ── 2. Alertes stock ──────────────────────────────────────
    private void chargerAlerteStock() {
        List<Produit> produits = produitDAO.getAllProduits();

        long ruptures = produits.stream().filter(p -> p.getStock() == 0).count();
        long faibles  = produits.stream().filter(p -> p.getStock() > 0 && p.getStock() <= 5).count();

        if (stockAlertLabel != null)
            stockAlertLabel.setText(ruptures + " produit(s)");
        if (stockFaibleLabel != null)
            stockFaibleLabel.setText(faibles + " produit(s)");

        if (produitPlusCherLabel != null)
            produits.stream()
                    .max(Comparator.comparingDouble(Produit::getPrix))
                    .ifPresent(p -> produitPlusCherLabel.setText(
                            p.getNom() + " — " + String.format("%.2f MAD", p.getPrix())));
    }

    // ── 3. Statuts commandes ──────────────────────────────────
    private void chargerStatutsCommandes() {
        Map<String, Long> statuts = commandeDAO.countCommandesByStatut();
        if (cmdEnAttenteLabel != null) cmdEnAttenteLabel.setText(String.valueOf(statuts.getOrDefault("EN_ATTENTE", 0L)));
        if (cmdValideeLabel   != null) cmdValideeLabel.setText(String.valueOf(statuts.getOrDefault("VALIDEE",    0L)));
        if (cmdLivreeLabel    != null) cmdLivreeLabel.setText(String.valueOf(statuts.getOrDefault("LIVREE",      0L)));
        if (cmdAnnuleeLabel   != null) cmdAnnuleeLabel.setText(String.valueOf(statuts.getOrDefault("ANNULEE",    0L)));
    }

    // ── 4. Top 5 produits ─────────────────────────────────────
    private void chargerTopProduits() {
        if (topProduitsContainer == null) return;
        topProduitsContainer.getChildren().clear();

        Map<String, Integer> map = new LinkedHashMap<>();
        List<Produit> produits = produitDAO.getAllProduits();
        for (User u : userDAO.getAllUsers())
            for (Commande c : commandeDAO.getCommandesByUser(u.getId()))
                for (LigneCommande lc : commandeDAO.getLignesByCommande(c.getId()))
                    produits.stream()
                            .filter(p -> p.getId() == lc.getProduitId())
                            .findFirst()
                            .ifPresent(p -> map.merge(p.getNom(), lc.getQuantite(), Integer::sum));

        List<Map.Entry<String, Integer>> top5 = map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5).collect(Collectors.toList());

        int maxVal = top5.stream().mapToInt(Map.Entry::getValue).max().orElse(1);
        String[] colors = {"#f59e0b", "#3b82f6", "#10b981", "#8b5cf6", "#ec4899"};
        int rank = 1;
        for (Map.Entry<String, Integer> e : top5) {
            double pct = (double) e.getValue() / maxVal;
            String color = colors[(rank - 1) % colors.length];

            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 8 12; -fx-background-radius: 8;" +
                    " -fx-background-color: rgba(255,255,255,0.04);");

            Label rankLbl = new Label("#" + rank);
            rankLbl.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12px; -fx-min-width:24;");

            // Nom du produit visible en gras
            Label nameLbl = new Label(e.getKey());
            nameLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1a202c;");
            nameLbl.setMinWidth(150);
            HBox.setHgrow(nameLbl, Priority.ALWAYS);
            nameLbl.setMaxWidth(Double.MAX_VALUE);

            // Barre de progression manuelle
            StackPane bar = new StackPane();
            Region bg = new Region();
            bg.setPrefSize(130, 7);
            bg.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius:4;");
            Region fg = new Region();
            fg.setPrefSize(pct * 130, 7);
            fg.setStyle("-fx-background-color:" + color + "; -fx-background-radius:4;");
            bar.getChildren().addAll(bg, fg);
            bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label countLbl = new Label(e.getValue() + " cmd");
            countLbl.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12px; -fx-font-weight:bold;");

            row.getChildren().addAll(rankLbl, nameLbl, bar, countLbl);
            topProduitsContainer.getChildren().add(row);
            rank++;
        }
        if (top5.isEmpty()) {
            topProduitsContainer.getChildren().add(
                new Label("Aucune commande enregistrée"));
        }
    }

    // ── 5. CA par catégorie ───────────────────────────────────
    private void chargerCACategorie() {
        if (caCategorieContainer == null) return;
        caCategorieContainer.getChildren().clear();

        List<Categorie> cats     = categorieDAO.getAllCategories();
        List<Produit>   produits = produitDAO.getAllProduits();
        List<User>      users    = userDAO.getAllUsers();

        Map<String, Double> caMap = new LinkedHashMap<>();
        for (Categorie cat : cats) {
            Set<Integer> ids = new HashSet<>();
            produits.stream().filter(p -> p.getCategorieId() == cat.getId())
                    .forEach(p -> ids.add(p.getId()));
            double caCateg = 0;
            for (User u : users)
                for (Commande c : commandeDAO.getCommandesByUser(u.getId()))
                    for (LigneCommande lc : commandeDAO.getLignesByCommande(c.getId()))
                        if (ids.contains(lc.getProduitId()))
                            caCateg += lc.getTotal();
            if (caCateg > 0) caMap.put(cat.getNom(), caCateg);
        }

        double total = caMap.values().stream().mapToDouble(Double::doubleValue).sum();
        String[] colors = {"#3b82f6", "#f59e0b", "#10b981", "#8b5cf6", "#ec4899", "#f97316"};
        int idx = 0;

        for (Map.Entry<String, Double> e : caMap.entrySet()) {
            String color = colors[idx % colors.length];
            double pct   = total > 0 ? e.getValue() / total : 0;
            int pctInt   = (int) Math.round(pct * 100);

            // Conteneur de la ligne entière
            VBox card = new VBox(6);
            card.setStyle("-fx-padding: 10 14; -fx-background-radius: 8;" +
                    " -fx-background-color: rgba(255,255,255,0.04);");

            // Ligne 1 : pastille couleur + NOM CATÉGORIE + montant + %
            HBox header = new HBox(8);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Pastille colorée sous forme de texte visible
            Label dot = new Label("⬤");
            dot.setStyle("-fx-text-fill:" + color + "; -fx-font-size:16px;");

            // Nom catégorie en gras — ex: "Électronique", "Vêtements"
            Label nameLbl = new Label(e.getKey());
            nameLbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a202c;");
            HBox.setHgrow(nameLbl, Priority.ALWAYS);
            nameLbl.setMaxWidth(Double.MAX_VALUE);
            nameLbl.setMinWidth(100);

            // Montant en couleur
            Label caLbl = new Label(String.format("%.2f MAD", e.getValue()));
            caLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");

            // Badge pourcentage
            Label pctLbl = new Label(pctInt + "%");
            pctLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#ffffff;" +
                    " -fx-background-color:" + color + "; -fx-background-radius:20; -fx-padding:2 8;");

            header.getChildren().addAll(dot, nameLbl, caLbl, pctLbl);

            // Ligne 2 : barre de progression pleine largeur
            StackPane bar = new StackPane();
            bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Region bg = new Region();
            bg.setPrefHeight(10);
            bg.setMaxWidth(Double.MAX_VALUE);
            bg.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius:5;");
            Region fg = new Region();
            fg.setPrefHeight(10);
            fg.setStyle("-fx-background-color:" + color + "; -fx-background-radius:5;");
            // On va lier la largeur dynamiquement via un binding approximatif
            fg.prefWidthProperty().bind(bar.widthProperty().multiply(pct));
            bar.getChildren().addAll(bg, fg);
            StackPane.setAlignment(fg, javafx.geometry.Pos.CENTER_LEFT);

            card.getChildren().addAll(header, bar);
            caCategorieContainer.getChildren().add(card);
            idx++;
        }

        if (caMap.isEmpty()) {
            Label empty = new Label("Aucune donnée de CA disponible");
            empty.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12px;");
            caCategorieContainer.getChildren().add(empty);
        }
    }

    // ── 6. Dernières commandes ────────────────────────────────
    private void chargerDernieresCommandes() {
        if (dernieresCommandesContainer == null) return;
        dernieresCommandesContainer.getChildren().clear();

        List<Commande> recent = commandeDAO.getRecentCommandes(5);
        for (Commande c : recent) {
            HBox row = new HBox();
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10 14; -fx-background-radius: 8;" +
                    " -fx-background-color: rgba(255,255,255,0.04);");

            Label idLbl = new Label("#" + c.getId());
            idLbl.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12px;");
            idLbl.setMinWidth(30);

            String client = c.getClientNomComplet() != null
                    ? c.getClientNomComplet() : "Client #" + c.getUserId();
            Label clientLbl = new Label(client);
            clientLbl.setStyle("-fx-font-size:13px;");
            HBox.setHgrow(clientLbl, Priority.ALWAYS);
            clientLbl.setMaxWidth(Double.MAX_VALUE);

            double montant = commandeDAO.getLignesByCommande(c.getId())
                    .stream().mapToDouble(LigneCommande::getTotal).sum();
            Label montantLbl = new Label(String.format("%.2f MAD", montant));
            montantLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold;");
            montantLbl.setMinWidth(110);

            String statut = c.getStatut() != null ? c.getStatut() : "—";
            String statutColor = switch (statut) {
                case "VALIDEE"    -> "#10b981";
                case "EN_ATTENTE" -> "#f59e0b";
                case "LIVREE"     -> "#3b82f6";
                case "ANNULEE"    -> "#ef4444";
                default           -> "#9ca3af";
            };
            Label statutLbl = new Label(statut);
            statutLbl.setStyle("-fx-text-fill:" + statutColor +
                    "; -fx-font-size:12px; -fx-font-weight:bold;");
            statutLbl.setMinWidth(90);

            row.getChildren().addAll(idLbl, clientLbl, montantLbl, statutLbl);
            dernieresCommandesContainer.getChildren().add(row);
        }
        if (recent.isEmpty())
            dernieresCommandesContainer.getChildren().add(
                new Label("Aucune commande récente"));
    }

    // ── 7. Utilisateurs récents ───────────────────────────────
    private void chargerUtilisateursRecents() {
        if (usersRecentContainer == null) return;
        usersRecentContainer.getChildren().clear();

        List<User> recent = userDAO.getAllUsers().stream()
                .sorted(Comparator.comparingInt(User::getId).reversed())
                .limit(5).collect(Collectors.toList());

        for (User u : recent) {
            HBox row = new HBox(12);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10 14; -fx-background-radius: 8;" +
                    " -fx-background-color: rgba(255,255,255,0.04);");

            // Initiale avatar
            String initiale = (u.getNom() != null && !u.getNom().isEmpty())
                    ? String.valueOf(u.getNom().charAt(0)).toUpperCase() : "?";
            Label avatar = new Label(initiale);
            avatar.setStyle("-fx-background-color:#2563eb; -fx-background-radius:50;" +
                    " -fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:14px;" +
                    " -fx-min-width:34; -fx-min-height:34; -fx-max-width:34; -fx-max-height:34;" +
                    " -fx-alignment:center;");

            VBox info = new VBox(2);
            Label nomLbl = new Label(u.getNom() != null ? u.getNom() : "—");
            nomLbl.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");
            Label emailLbl = new Label(u.getEmail() != null ? u.getEmail() : "");
            emailLbl.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11px;");
            info.getChildren().addAll(nomLbl, emailLbl);
            HBox.setHgrow(info, Priority.ALWAYS);

            int nbCmds = commandeDAO.getCommandesByUser(u.getId()).size();
            Label badge = new Label(nbCmds + " cmd" + (nbCmds > 1 ? "s" : ""));
            badge.setStyle("-fx-font-size:11px; -fx-padding:3 8;" +
                    " -fx-background-radius:20;");

            row.getChildren().addAll(avatar, info, badge);
            usersRecentContainer.getChildren().add(row);
        }
        if (recent.isEmpty())
            usersRecentContainer.getChildren().add(
                new Label("Aucun utilisateur enregistré"));
    }

    // ── Notifications temps réel ──────────────────────────────
    private void demarrerNotifications() {
        notifTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            Platform.runLater(() -> {
                int total = userDAO.getAllUsers().stream()
                        .mapToInt(u -> commandeDAO.getCommandesByUser(u.getId()).size())
                        .sum();
                if (total > lastCommandeCount && notifLabel != null) {
                    int n = total - lastCommandeCount;
                    lastCommandeCount = total;
                    notifLabel.setText("🔔 " + n + " nouvelle(s) commande(s) !");
                    notifLabel.setVisible(true);
                    FadeTransition blink = new FadeTransition(Duration.millis(500), notifLabel);
                    blink.setFromValue(1); blink.setToValue(0.2);
                    blink.setAutoReverse(true); blink.setCycleCount(8); blink.play();
                    new Timeline(new KeyFrame(Duration.seconds(8),
                            ev -> notifLabel.setVisible(false))).play();
                    chargerTout();
                }
            });
        }));
        notifTimeline.setCycleCount(Timeline.INDEFINITE);
        notifTimeline.play();
    }

    // ── Navigation ────────────────────────────────────────────
    @FXML public void ouvrirProduits()     { naviguer("/fxml/admin-produits.fxml",   "Produits");     }
    @FXML public void ouvrirUtilisateurs() { naviguer("/fxml/admin-users.fxml",      "Utilisateurs"); }
    @FXML public void ouvrirCommandes()    { naviguer("/fxml/admin-commandes.fxml",  "Commandes");    }
    @FXML public void ouvrirCategories()   { naviguer("/fxml/admin-categories.fxml", "Catégories");   }
    @FXML public void seDeconnecter() {
        if (notifTimeline != null) notifTimeline.stop();
        naviguer("/fxml/accueil.fxml", "Accueil");
    }

    private void naviguer(String fxml, String title) {
        try {
            Parent root  = FXMLLoader.load(getClass().getResource(fxml));
            Stage  stage = (Stage) nbUsersLabel.getScene().getWindow();
            root.setOpacity(0);
            if (fxml.contains("accueil")) {
                stage.setMaximized(false);
                stage.setResizable(false);
                stage.setScene(new Scene(root, 680, 480));
            } else {
                stage.setScene(new Scene(root, 1280, 800));
                stage.setMaximized(true);
                stage.setResizable(true);
            }
            stage.setTitle("🛠 " + title);
            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setToValue(1); ft.play();
        } catch (Exception e) { e.printStackTrace(); }
    }
}