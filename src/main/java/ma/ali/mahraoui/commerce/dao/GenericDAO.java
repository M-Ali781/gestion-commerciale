package ma.ali.mahraoui.commerce.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManagerFactory;
import ma.ali.mahraoui.commerce.util.HibernateUtil;
import ma.ali.mahraoui.commerce.util.JpaTransactions;

/**
 * Composant d'accès aux données générique basé sur JPA.
 * Fournit les opérations fondamentales de persistance pour toute entité du modèle.
 *
 * @param <T> Type de l'entité gérée
 * @param <ID> Type de la clé primaire
 * @author Ali Mahraoui
 */
public class GenericDAO<T, ID extends Serializable> {

    private final Class<T> entityClass;
    protected final JpaTransactions transactions;

    public GenericDAO(Class<T> entityClass) {
        this(entityClass, HibernateUtil.getSessionFactory());
    }

    public GenericDAO(Class<T> entityClass, EntityManagerFactory emf) {
        this.entityClass = Objects.requireNonNull(entityClass, "Le type d'entité ne peut pas être nul.");
        this.transactions = new JpaTransactions(emf);
    }

    public T find(ID id) {
        if (id == null) {
            return null;
        }
        return transactions.lire(em -> em.find(entityClass, id));
    }

    public List<T> findAll() {
        return transactions.lire(em -> {
            String entityName = em.getMetamodel().entity(entityClass).getName();
            return em.createQuery("SELECT e FROM " + entityName + " e ORDER BY e.id", entityClass)
                     .getResultList();
        });
    }

    public void create(T entity) {
        Objects.requireNonNull(entity, "L'entité à insérer ne peut pas être nulle.");
        transactions.ecrire(em -> {
            em.persist(entity);
            return null;
        });
    }

    public T update(T entity) {
        Objects.requireNonNull(entity, "L'entité à mettre à jour ne peut pas être nulle.");
        return transactions.ecrire(em -> em.merge(entity));
    }

    public void delete(T entity) {
        Objects.requireNonNull(entity, "L'entité à supprimer ne peut pas être nulle.");
        transactions.ecrire(em -> {
            Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
            if (id == null) {
                throw new IllegalArgumentException("Entité non enregistrée.");
            }
            T attached = em.find(entityClass, id);
            if (attached == null) {
                throw new IllegalArgumentException("Entité introuvable.");
            }
            em.remove(attached);
            return null;
        });
    }
}
