package ma.ali.mahraoui.commerce.service;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManagerFactory;
import ma.ali.mahraoui.commerce.dao.GenericDAO;
import ma.ali.mahraoui.commerce.util.HibernateUtil;

/**
 * Service métier de base pour les opérations CRUD standard.
 * Développé par Ali Mahraoui avec validations préalables systématiques.
 *
 * @param <T> Type de l'entité
 * @param <ID> Type de la clé d'identification
 * @author Ali Mahraoui
 */
public class CrudService<T, ID extends Serializable> {

    protected final GenericDAO<T, ID> dao;

    public CrudService(Class<T> type) {
        this(type, HibernateUtil.getSessionFactory());
    }

    public CrudService(Class<T> type, EntityManagerFactory emf) {
        this.dao = new GenericDAO<>(type, emf);
    }

    public T getById(ID id) {
        verifier(id);
        return dao.find(id);
    }

    public List<T> getAll() {
        return dao.findAll();
    }

    public void create(T entite) {
        verifier(entite);
        dao.create(entite);
    }

    public T update(T entite) {
        verifier(entite);
        return dao.update(entite);
    }

    public void delete(T entite) {
        verifier(entite);
        dao.delete(entite);
    }

    protected static void verifier(Object valeur) {
        if (valeur == null) {
            throw new IllegalArgumentException("Le paramètre ne doit pas être nul.");
        }
    }
}
