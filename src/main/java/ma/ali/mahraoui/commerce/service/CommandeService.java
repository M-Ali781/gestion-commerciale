package ma.ali.mahraoui.commerce.service;

import javax.persistence.EntityManagerFactory;
import ma.ali.mahraoui.commerce.entities.Commande;
import ma.ali.mahraoui.commerce.entities.CommandeProduit;
import ma.ali.mahraoui.commerce.entities.Produit;
import ma.ali.mahraoui.commerce.util.HibernateUtil;
import ma.ali.mahraoui.commerce.util.JpaTransactions;

/**
 * Service de gestion des commandes et facturations.
 * Garantit l'intégrité référentielle des articles commandés au sein du contexte de persistance.
 *
 * @author Ali Mahraoui
 */
public class CommandeService extends CrudService<Commande, Long> {

    private final JpaTransactions transactions;

    public CommandeService() {
        this(HibernateUtil.getSessionFactory());
    }

    public CommandeService(EntityManagerFactory emf) {
        super(Commande.class, emf);
        this.transactions = new JpaTransactions(emf);
    }

    @Override
    public void create(Commande commande) {
        verifier(commande);
        transactions.ecrire(em -> {
            for (CommandeProduit ligne : commande.getLignes()) {
                Produit produitExistant = em.find(Produit.class, ligne.getProduit().getId());
                if (produitExistant == null) {
                    throw new IllegalArgumentException("Produit introuvable.");
                }
                ligne.rattacherProduit(produitExistant);
            }
            em.persist(commande);
            return null;
        });
    }

    @Override
    public Commande getById(Long id) {
        verifier(id);
        return transactions.lire(em -> em.createQuery(
                "SELECT DISTINCT c FROM Commande c " +
                "LEFT JOIN FETCH c.lignes l " +
                "LEFT JOIN FETCH l.produit " +
                "WHERE c.id = :id", Commande.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }
}
