package ma.ali.mahraoui.commerce;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Arrays;
import javax.persistence.EntityManagerFactory;
import ma.ali.mahraoui.commerce.entities.Categorie;
import ma.ali.mahraoui.commerce.entities.Client;
import ma.ali.mahraoui.commerce.entities.Commande;
import ma.ali.mahraoui.commerce.entities.Fournisseur;
import ma.ali.mahraoui.commerce.entities.Produit;
import ma.ali.mahraoui.commerce.entities.User;
import ma.ali.mahraoui.commerce.service.CommandeService;
import ma.ali.mahraoui.commerce.service.CrudService;
import ma.ali.mahraoui.commerce.service.ProduitService;
import ma.ali.mahraoui.commerce.service.RapportVentesService;
import ma.ali.mahraoui.commerce.util.HibernateUtil;
import ma.ali.mahraoui.commerce.util.JpaTransactions;
import org.h2.tools.Server;

/**
 * Point d'entrée principal de l'application de Gestion Commerciale.
 * Développé par Ali Mahraoui pour illustrer l'architecture JPA / Hibernate complète.
 *
 * @author Ali Mahraoui
 */
public class ApplicationCommerce {

    public static void main(String[] args) throws Exception {
        System.out.println("===============================================================");
        System.out.println("   PLATEFORME DE GESTION COMMERCIALE - ALI MAHRAOUI           ");
        System.out.println("===============================================================");

        try {
            EntityManagerFactory fabrique = HibernateUtil.getSessionFactory();
            CrudService<Categorie, Long> categories = new CrudService<>(Categorie.class, fabrique);
            ProduitService produits = new ProduitService(fabrique);
            CommandeService commandes = new CommandeService(fabrique);
            JpaTransactions transactions = new JpaTransactions(fabrique);

            YearMonth moisActuel = YearMonth.now(ZoneId.of("Africa/Casablanca")).minusMonths(1);

            // Initialisation des données de démonstration si la base est vierge
            if (categories.getAll().isEmpty()) {
                System.out.println("\n[INIT] Insertion du jeu d'essai personnalisé pour Ali Mahraoui...");

                Categorie informatique = new Categorie("Informatique");
                Categorie papeterie = new Categorie("Papeterie");
                categories.create(informatique);
                categories.create(papeterie);

                Produit clavier = new Produit("Clavier mécanique", new BigDecimal("350.00"), informatique);
                Produit souris = new Produit("Souris sans fil", new BigDecimal("120.00"), informatique);
                Produit carnet = new Produit("Carnet A5", new BigDecimal("25.00"), papeterie);
                produits.create(clavier);
                produits.create(souris);
                produits.create(carnet);

                Commande commande = new Commande(moisActuel.atDay(10));
                commande.addProduit(clavier, 2);
                commande.addProduit(carnet, 4);
                commandes.create(commande);

                CrudService<User, Long> utilisateurs = new CrudService<>(User.class, fabrique);
                utilisateurs.create(new Client("Ali Mahraoui", LocalDate.now()));
                utilisateurs.create(new Fournisseur("Mahraoui Distribution SARL", "Casablanca"));

                System.out.println("[INIT] Jeu d'essai initialisé avec succès.");
            }

            System.out.println("\n--- 1. Liste des Catégories et Articles ---");
            categories.getAll().forEach(c -> System.out.println(" [Catégorie] " + c));
            produits.getAll().forEach(p -> System.out.println(" [Produit] " + p));

            System.out.println("\n--- 2. Named Query (Recherche libellé 'CLAVIER') ---");
            produits.searchByLibelle("CLAVIER").forEach(p -> System.out.println(" -> " + p));

            System.out.println("\n--- 3. Criteria API dynamique (Prix entre 20.00 et 150.00 MAD) ---");
            produits.search(null, new BigDecimal("20"), new BigDecimal("150")).forEach(p -> System.out.println(" -> " + p));

            System.out.println("\n--- 4. Pagination JPA (2 articles par page) ---");
            System.out.println("Page 1 :");
            produits.findPage(1, 2).forEach(p -> System.out.println("  * " + p));
            System.out.println("Page 2 :");
            produits.findPage(2, 2).forEach(p -> System.out.println("  * " + p));

            System.out.println("\n--- 5. Modèle d'héritage JOINED (Utilisateurs / Clients / Fournisseurs) ---");
            new CrudService<User, Long>(User.class, fabrique).getAll().forEach(u -> System.out.println(" [User JOINED] " + u));

            System.out.println("\n--- 6. Rapport financier et chiffre d'affaires (" + moisActuel + ") ---");
            new RapportVentesService(fabrique).rapport(moisActuel).forEach(v -> System.out.println("  $ " + v));

            System.out.println("\n--- 7. Cycle de vie : Modification, suppression et détection d'orphelins ---");
            Categorie testCat = new Categorie("Démonstration temporaire");
            new Produit("Article temporaire", new BigDecimal("10.00"), testCat);
            categories.create(testCat);
            testCat.setNom("Démonstration modifiée");
            categories.update(testCat);

            transactions.ecrire(em -> {
                Categorie chargee = em.find(Categorie.class, testCat.getId());
                chargee.removeProduit(chargee.getProduits().get(0));
                return null;
            });
            System.out.println("Catégorie après suppression de l'orphelin : " + categories.getById(testCat.getId()));
            categories.delete(testCat);
            System.out.println("Catégorie test supprimée : " + (categories.getById(testCat.getId()) == null));

            System.out.println("\n--- 8. Validation de l'atomicité transactionnelle (Rollback) ---");
            try {
                transactions.ecrire(em -> {
                    em.persist(new Categorie("Catégorie Rollback"));
                    throw new IllegalArgumentException("Exception provoquée pour tester le rollback.");
                });
            } catch (IllegalArgumentException ex) {
                System.out.println("Transaction annulée avec succès : " + ex.getMessage());
            }

            if (Arrays.asList(args).contains("--console")) {
                if (!"h2".equalsIgnoreCase(System.getProperty("commerce.database", "h2"))) {
                    throw new IllegalArgumentException("La console H2 nécessite le mode 'h2'.");
                }
                Server consoleH2 = Server.createWebServer("-webPort", "8082").start();
                try {
                    System.out.println("\nConsole H2 disponible à l'adresse : http://localhost:8082");
                    System.out.println("Connexion JDBC : jdbc:h2:mem:commerce | User: sa | Mot de passe vide");
                    System.out.println("Appuyez sur Entrée pour arrêter la console...");
                    System.in.read();
                } finally {
                    consoleH2.stop();
                }
            }

            System.out.println("\nApplication exécutée avec succès.");
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
