package ma.ali.mahraoui.commerce;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManagerFactory;
import ma.ali.mahraoui.commerce.dao.GenericDAO;
import ma.ali.mahraoui.commerce.entities.Categorie;
import ma.ali.mahraoui.commerce.entities.Client;
import ma.ali.mahraoui.commerce.entities.Commande;
import ma.ali.mahraoui.commerce.entities.CommandeProduit;
import ma.ali.mahraoui.commerce.entities.CommandeProduitPK;
import ma.ali.mahraoui.commerce.entities.Fournisseur;
import ma.ali.mahraoui.commerce.entities.Produit;
import ma.ali.mahraoui.commerce.entities.User;
import ma.ali.mahraoui.commerce.service.CommandeService;
import ma.ali.mahraoui.commerce.service.CrudService;
import ma.ali.mahraoui.commerce.service.ProduitService;
import ma.ali.mahraoui.commerce.service.RapportVentesService;
import ma.ali.mahraoui.commerce.service.VenteCategorie;
import ma.ali.mahraoui.commerce.util.HibernateUtil;
import ma.ali.mahraoui.commerce.util.JpaTransactions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Suite de tests unitaires et d'intégration validant le modèle métier et la couche service.
 * Auteur : Ali Mahraoui
 *
 * @author Ali Mahraoui
 */
public class CommerceTest {

    private EntityManagerFactory fabrique;
    private CrudService<Categorie, Long> categories;
    private ProduitService produits;
    private CommandeService commandes;
    private JpaTransactions transactions;

    @Before
    public void preparer() {
        fabrique = HibernateUtil.creerPourTests("jdbc:h2:mem:test_" + UUID.randomUUID());
        categories = new CrudService<>(Categorie.class, fabrique);
        produits = new ProduitService(fabrique);
        commandes = new CommandeService(fabrique);
        transactions = new JpaTransactions(fabrique);
    }

    @After
    public void fermer() {
        if (fabrique != null && fabrique.isOpen()) {
            fabrique.close();
        }
    }

    private Produit produit(String nom, String prix, Categorie cat) {
        Produit p = new Produit(nom, new BigDecimal(prix), cat);
        produits.create(p);
        return p;
    }

    private Categorie categorie(String nom) {
        Categorie cat = new Categorie(nom);
        categories.create(cat);
        return cat;
    }

    private long nombre(String nomEntite) {
        return transactions.lire(em ->
                em.createQuery("SELECT COUNT(e) FROM " + nomEntite + " e", Long.class).getSingleResult());
    }

    @Test
    public void crudGeneriqueEtValidation() {
        Categorie cat = categorie("Papeterie");
        assertNotNull(cat.getId());
        assertEquals("Papeterie", categories.getById(cat.getId()).getNom());

        cat.setNom("Bureau");
        categories.update(cat);
        assertEquals("Bureau", categories.getById(cat.getId()).getNom());
        assertEquals(1, categories.getAll().size());

        categories.delete(cat);
        assertNull(categories.getById(cat.getId()));

        assertThrows(IllegalArgumentException.class, () -> categories.create(null));
        assertThrows(IllegalArgumentException.class, () -> categories.update(null));
        assertThrows(IllegalArgumentException.class, () -> categories.delete(null));
    }

    @Test
    public void produitReluAvecCategorieApresFermetureDuContexte() {
        Produit p = produit("Stylo", "1.20", categorie("Papeterie"));
        Produit relu = produits.getById(p.getId());
        assertNotNull(relu);
        assertEquals("Papeterie", relu.getCategorie().getNom());
        assertEquals(new BigDecimal("1.20"), relu.getPrix());
        assertNotNull(relu.getDateCreation());

        relu.setPrix(new BigDecimal("2.50"));
        produits.update(relu);
        assertEquals(new BigDecimal("2.50"), produits.getById(relu.getId()).getPrix());
        assertThrows(IllegalArgumentException.class, () -> relu.setPrix(new BigDecimal("-1")));
    }

    @Test
    public void cascadeEtSuppressionOrpheline() {
        Categorie cat = new Categorie("Bureau");
        Produit premier = new Produit("Cahier", new BigDecimal("15"), cat);
        new Produit("Crayon", new BigDecimal("3"), cat);
        categories.create(cat);
        assertEquals(2, nombre("Produit"));

        transactions.ecrire(em -> {
            Categorie geree = em.find(Categorie.class, cat.getId());
            Produit retire = geree.getProduits().stream()
                    .filter(p -> p.getId().equals(premier.getId()))
                    .findFirst()
                    .orElseThrow();
            geree.removeProduit(retire);
            assertNull(retire.getCategorie());
            return null;
        });

        assertNull(produits.getById(premier.getId()));
        assertEquals(1, nombre("Produit"));

        categories.delete(cat);
        assertEquals(0, nombre("Produit"));
    }

    @Test
    public void namedQueriesCriteriaEtPagination() {
        Categorie cat = categorie("Informatique");
        Produit clavier = produit("Clavier", "100", cat);
        produit("Souris", "50", cat);
        produit("Ecran", "900", cat);

        assertEquals(3, produits.findByCategorie(cat.getId()).size());
        assertEquals(clavier.getId(), produits.searchByLibelle("CLAV").get(0).getId());
        assertEquals(2, produits.search(null, new BigDecimal("50"), new BigDecimal("100")).size());
        assertEquals(1, produits.search("clav", null, new BigDecimal("100")).size());
        assertTrue(produits.search("absent", null, null).isEmpty());

        assertEquals(2, produits.findPage(1, 2).size());
        assertEquals(1, produits.findPage(2, 2).size());
        assertTrue(produits.findPage(3, 2).isEmpty());

        assertThrows(IllegalArgumentException.class, () -> produits.findPage(0, 2));
        assertThrows(IllegalArgumentException.class, () -> produits.search(null, new BigDecimal("2"), BigDecimal.ONE));
    }

    @Test
    public void heritageJoinedEtSuppressionParent() {
        CrudService<User, Long> users = new CrudService<>(User.class, fabrique);
        Client client = new Client("Ali Mahraoui", LocalDate.of(2026, 1, 1));
        Fournisseur fournisseur = new Fournisseur("Atlas Solutions", "Marrakech");

        users.create(client);
        users.create(fournisseur);

        assertEquals(2, users.getAll().size());
        assertTrue(users.getById(client.getId()) instanceof Client);
        assertTrue(users.getById(fournisseur.getId()) instanceof Fournisseur);

        users.delete(client);
        assertEquals(1, nombre("User"));
        assertEquals(0, nombre("Client"));
        assertEquals(1, nombre("Fournisseur"));
    }

    @Test
    public void cleComposeeEtDeuxCommandesPartageantUnProduit() {
        Produit produit = produit("Clavier", "100", categorie("Informatique"));

        Commande premiere = new Commande(LocalDate.of(2026, 9, 1));
        premiere.addProduit(produit, 2);
        commandes.create(premiere);

        Commande seconde = new Commande(LocalDate.of(2026, 9, 2));
        seconde.addProduit(produit, 3);
        commandes.create(seconde);

        GenericDAO<CommandeProduit, CommandeProduitPK> lignes = new GenericDAO<>(CommandeProduit.class, fabrique);
        CommandeProduitPK cle = new CommandeProduitPK(premiere.getId(), produit.getId());

        assertEquals(2, lignes.find(cle).getQuantite());
        assertEquals(cle, premiere.getLignes().get(0).getId());
        assertEquals(cle.hashCode(), new CommandeProduitPK(premiere.getId(), produit.getId()).hashCode());

        assertThrows(IllegalArgumentException.class, () -> premiere.addProduit(produit, 1));
        assertThrows(IllegalArgumentException.class, () -> new Commande().addProduit(produit, 0));
        assertEquals(2, nombre("CommandeProduit"));
    }

    @Test
    public void retraitLigneEtSuppressionCommandePreserventProduits() {
        Categorie cat = categorie("Informatique");
        Produit premier = produit("Clavier", "100", cat);
        Produit second = produit("Souris", "50", cat);

        Commande commande = new Commande();
        commande.addProduit(premier, 2);
        commande.addProduit(second, 3);
        commandes.create(commande);

        transactions.ecrire(em -> {
            em.find(Commande.class, commande.getId()).removeProduit(premier.getId());
            return null;
        });

        assertEquals(1, nombre("CommandeProduit"));
        commandes.delete(commande);
        assertEquals(0, nombre("CommandeProduit"));
        assertEquals(2, nombre("Produit"));
    }

    @Test
    public void rollbackEtReutilisationApresErreur() {
        categorie("Unique");
        assertThrows(RuntimeException.class, () -> categories.create(new Categorie("Unique")));

        assertThrows(IllegalArgumentException.class, () -> transactions.ecrire(em -> {
            em.persist(new Categorie("Annulée"));
            throw new IllegalArgumentException("Annulation");
        }));

        assertEquals(1, nombre("Categorie"));
        categorie("Suivante");
        assertEquals(2, nombre("Categorie"));
    }

    @Test
    public void interdireSuppressionProduitDejaCommande() {
        Produit produit = produit("Clavier", "100", categorie("Informatique"));
        Commande commande = new Commande();
        commande.addProduit(produit, 1);
        commandes.create(commande);

        assertThrows(RuntimeException.class, () -> produits.delete(produit));
        assertNotNull(produits.getById(produit.getId()));
        assertEquals(1, nombre("CommandeProduit"));
    }

    @Test
    public void rapportMensuelBornesAgregationEtPrixHistorique() {
        Categorie cat = categorie("Informatique");
        categorie("Sans ventes");

        Produit premier = produit("Clavier", "100", cat);
        Produit second = produit("Souris", "50", cat);

        for (LocalDate date : Arrays.asList(
                LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 10, 1))) {
            Commande commande = new Commande(date);
            commande.addProduit(premier, 2);
            commande.addProduit(second, 3);
            commandes.create(commande);
        }

        premier.setPrix(new BigDecimal("999"));
        produits.update(premier);

        List<VenteCategorie> rapport = new RapportVentesService(fabrique).rapport(YearMonth.of(2026, 9));
        assertEquals(2, rapport.size());
        assertEquals(0, new BigDecimal("700").compareTo(rapport.get(0).getChiffreAffaires()));
        assertEquals(0, rapport.get(1).getChiffreAffaires().signum());
    }
}
