package ma.ali.mahraoui.commerce.util;

import java.util.Objects;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/**
 * Encapsule l'exécution transactionnelle et la gestion des contextes de persistance JPA.
 * Conçu par Ali Mahraoui pour garantir la fiabilité atomique des opérations de lecture et d'écriture.
 *
 * @author Ali Mahraoui
 */
public final class JpaTransactions {

    private final EntityManagerFactory emf;

    public JpaTransactions(EntityManagerFactory emf) {
        this.emf = Objects.requireNonNull(emf, "La fabrique EntityManagerFactory ne peut pas être nulle.");
    }

    /**
     * Exécute une opération de lecture dans un EntityManager dédié sans transaction active.
     */
    public <R> R lire(Function<EntityManager, R> action) {
        Objects.requireNonNull(action, "L'action de lecture ne peut pas être nulle.");
        EntityManager em = emf.createEntityManager();
        try {
            return action.apply(em);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Exécute une opération d'écriture au sein d'une transaction atomique avec rollback automatique en cas d'anomalie.
     */
    public <R> R ecrire(Function<EntityManager, R> action) {
        Objects.requireNonNull(action, "L'action d'écriture ne peut pas être nulle.");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            R resultat = action.apply(em);
            transaction.commit();
            return resultat;
        } catch (RuntimeException ex) {
            if (transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (RuntimeException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
            }
            throw ex;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
