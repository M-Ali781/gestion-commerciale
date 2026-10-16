package ma.ali.mahraoui.commerce.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import ma.ali.mahraoui.commerce.entities.Categorie;
import ma.ali.mahraoui.commerce.entities.CommandeProduit;
import ma.ali.mahraoui.commerce.entities.Produit;
import ma.ali.mahraoui.commerce.util.JpaTransactions;

/**
 * Service d'analyse financière et de reporting des ventes mensuelles.
 * Développé par Ali Mahraoui avec agrégation JPA Criteria multitable et valorisation historique.
 *
 * @author Ali Mahraoui
 */
public class RapportVentesService {

    private final JpaTransactions transactions;

    public RapportVentesService(EntityManagerFactory emf) {
        this.transactions = new JpaTransactions(emf);
    }

    public List<VenteCategorie> rapport(YearMonth mois) {
        Objects.requireNonNull(mois, "Le mois est obligatoire.");

        return transactions.lire(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Tuple> query = cb.createTupleQuery();
            Root<CommandeProduit> ligne = query.from(CommandeProduit.class);

            Join<CommandeProduit, Produit> produit = ligne.join("produit");
            Join<Produit, Categorie> categorie = produit.join("categorie");

            Expression<BigDecimal> totalLigne = cb.prod(
                    ligne.<BigDecimal>get("prixUnitaire"),
                    ligne.<Integer>get("quantite").as(BigDecimal.class)
            );

            query.multiselect(
                    categorie.get("id").alias("categorieId"),
                    cb.sum(totalLigne).alias("chiffreAffairesTotal")
            ).where(
                    cb.greaterThanOrEqualTo(ligne.get("commande").get("dateCommande"), mois.atDay(1)),
                    cb.lessThan(ligne.get("commande").get("dateCommande"), mois.plusMonths(1).atDay(1))
            ).groupBy(categorie.get("id"));

            Map<Long, BigDecimal> caParCategorie = new HashMap<>();
            List<Tuple> resultats = em.createQuery(query).getResultList();
            for (Tuple tuple : resultats) {
                Long catId = tuple.get("categorieId", Long.class);
                BigDecimal ca = tuple.get("chiffreAffairesTotal", BigDecimal.class);
                caParCategorie.put(catId, ca);
            }

            List<Categorie> toutesCategories = em.createQuery(
                    "SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class)
                    .getResultList();

            List<VenteCategorie> synthèses = new ArrayList<>();
            for (Categorie cat : toutesCategories) {
                BigDecimal ca = caParCategorie.getOrDefault(cat.getId(), BigDecimal.ZERO);
                synthèses.add(new VenteCategorie(cat.getId(), cat.getNom(), ca));
            }

            return synthèses;
        });
    }
}
