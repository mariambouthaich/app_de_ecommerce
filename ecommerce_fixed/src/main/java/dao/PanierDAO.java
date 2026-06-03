
package dao;

import model.Panier;
import model.PanierItem;
import model.Produit;
import model.LigneCommande;

/**
 * DAO : Gestion du panier
 * Le panier est en mémoire (pas de table BD).
 * La méthode validerCommande() convertit le panier en commande BD.
 */
public class PanierDAO {

    private final CommandeDAO commandeDAO = new CommandeDAO();
    private final ProduitDAO  produitDAO  = new ProduitDAO();

    /**
     * Valider le panier = créer une commande + ses lignes en BD.
     * Le stock de chaque produit est automatiquement décrémenté.
     *
     * @param panier le panier de l'utilisateur
     * @return l'ID de la commande créée, ou -1 si échec
     */
    public int validerCommande(Panier panier) {
        if (panier.getItems().isEmpty()) {
            System.out.println("⚠ Le panier est vide, impossible de valider.");
            return -1;
        }

        // 1. Vérifier les stocks avant de commencer
        for (PanierItem item : panier.getItems()) {
            Produit p = produitDAO.getProduitById(item.getProduit().getId());
            if (p == null || p.getStock() < item.getQuantite()) {
                System.out.println("❌ Stock insuffisant pour : " + item.getProduit().getNom());
                return -1;
            }
        }

        // 2. Créer la commande
        int commandeId = commandeDAO.creerCommande(panier.getUserId());
        if (commandeId == -1) return -1;

        // 3. Ajouter chaque ligne + décrémenter le stock
        for (PanierItem item : panier.getItems()) {
            LigneCommande lc = new LigneCommande(
                    commandeId,
                    item.getProduit().getId(),
                    item.getQuantite(),
                    item.getProduit().getPrix()
            );
            commandeDAO.ajouterLigneCommande(lc);

            // Décrémenter le stock
            int nouveauStock = item.getProduit().getStock() - item.getQuantite();
            produitDAO.updateStock(item.getProduit().getId(), nouveauStock);
        }

        System.out.printf("✅ Commande #%d validée ! Total : %.2f MAD%n",
                commandeId, panier.getTotal());

        // 4. Vider le panier
        panier.vider();

        return commandeId;
    }
}