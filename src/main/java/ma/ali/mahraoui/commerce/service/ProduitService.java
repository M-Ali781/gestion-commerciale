package ma.ali.mahraoui.commerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import ma.ali.mahraoui.commerce.entities.Produit;
import ma.ali.mahraoui.commerce.util.HibernateUtil;
import ma.ali.mahraoui.commerce.util.JpaTransactions;

/**
 * Service métier dédié aux articles et au catalogue commercial.
 * Implémente la recherche dynamique multicritère, la pagination et les requêtes nommées.
 *
 * @author Ali Mahraoui
 */
public class ProduitService extends CrudService<Produit, Long> {

    private final JpaTransactions transactions;

    public ProduitService() {
        this(HibernateUtil.getSessionFactory());
    }

    public ProduitService(EntityManagerFactory emf) {
        super(Produit.class, emf);
        this.transactions = new JpaTransactions(emf);
    }

    @Override
    public Produit getById(Long id) {
        verifier(id);
        return transactions.lire(em -> em.createQuery(
                "SELECT p FROM Produit p JOIN FETCH p.categorie WHERE p.id = :id", Produit.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    @Override
    public List<Produit> getAll() {
        return transactions.lire(em -> em.createNamedQuery("Produit.findAll", Produit.class).getResultList());
    }

    public List<Produit> findByCategorie(Long idCategorie) {
        verifier(idCategorie);
        return transactions.lire(em -> em.createNamedQuery("Produit.findByCategorie", Produit.class)
                .setParameter("catId", idCategorie)
                .getResultList());
    }

    public List<Produit> searchByLibelle(String terme) {
        verifier(terme);
        return transactions.lire(em -> em.createNamedQuery("Produit.searchByLibelle", Produit.class)
                .setParameter("term", terme)
                .getResultList());
    }

    public List<Produit> search(String fragmentLibelle, BigDecimal prixMin, BigDecimal prixMax) {
        validerFourchettePrix(prixMin, prixMax);

        return transactions.lire(em -> {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Produit> query = builder.createQuery(Produit.class);
            Root<Produit> root = query.from(Produit.class);
            root.fetch("categorie", JoinType.INNER);

            List<Predicate> conditions = new ArrayList<>();

            if (fragmentLibelle != null && !fragmentLibelle.trim().isEmpty()) {
                String pattern = fragmentLibelle.trim().toLowerCase(Locale.ROOT)
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_");
                conditions.add(builder.like(builder.lower(root.get("libelle")), "%" + pattern + "%", '!'));
            }

            if (prixMin != null) {
                conditions.add(builder.ge(root.get("prix"), prixMin));
            }

            if (prixMax != null) {
                conditions.add(builder.le(root.get("prix"), prixMax));
            }

            query.select(root)
                 .where(conditions.toArray(new Predicate[0]))
                 .orderBy(builder.asc(root.get("id")));

            return em.createQuery(query).getResultList();
        });
    }

    public List<Produit> findPage(int page, int taillePage) {
        if (page < 1 || taillePage < 1 || ((long) page - 1) * taillePage > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Pagination invalide.");
        }
        int decalage = (page - 1) * taillePage;

        return transactions.lire(em -> em.createQuery(
                "SELECT p FROM Produit p JOIN FETCH p.categorie ORDER BY p.id", Produit.class)
                .setFirstResult(decalage)
                .setMaxResults(taillePage)
                .getResultList());
    }

    private static void validerFourchettePrix(BigDecimal min, BigDecimal max) {
        if ((min != null && min.signum() < 0) || (max != null && max.signum() < 0)
                || (min != null && max != null && min.compareTo(max) > 0)) {
            throw new IllegalArgumentException("Intervalle de prix invalide.");
        }
    }
}
