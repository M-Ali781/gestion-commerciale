package ma.ali.mahraoui.commerce.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Gestionnaire du cycle de vie de la SessionFactory Hibernate / JPA.
 * Conçu par Ali Mahraoui pour offrir un accès unifié aux contextes de persistance.
 *
 * @author Ali Mahraoui
 */
public final class HibernateUtil {

    private static volatile SessionFactory sessionFactory;

    private HibernateUtil() {
        // Empêche l'instanciation de cette classe utilitaire
    }

    /**
     * Fournit l'instance unique de SessionFactory selon la configuration active.
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null || sessionFactory.isClosed()) {
                    sessionFactory = initialiserSessionFactory();
                }
            }
        }
        return sessionFactory;
    }

    private static SessionFactory initialiserSessionFactory() {
        Configuration configuration = new Configuration().configure();
        String typeBase = System.getProperty("commerce.database", "h2");

        if ("mysql".equalsIgnoreCase(typeBase)) {
            configuration.setProperty("hibernate.connection.url", recupererVariable("COMMERCE_DB_URL"));
            configuration.setProperty("hibernate.connection.username", recupererVariable("COMMERCE_DB_USER"));
            String secret = System.getenv("COMMERCE_DB_PASSWORD");
            configuration.setProperty("hibernate.connection.password", secret == null ? "" : secret);
        } else if ("h2".equalsIgnoreCase(typeBase)) {
            appliquerParametresH2(configuration, "jdbc:h2:mem:commerce;DB_CLOSE_DELAY=-1");
        } else {
            throw new IllegalArgumentException("La propriété 'commerce.database' doit être 'h2' ou 'mysql' (valeur actuelle : " + typeBase + ").");
        }

        return configuration.buildSessionFactory();
    }

    /**
     * Crée une SessionFactory isolée spécifiquement pour les tests unitaires et d'intégration.
     */
    public static SessionFactory creerPourTests(String urlConnexion) {
        Configuration configuration = new Configuration().configure();
        appliquerParametresH2(configuration, urlConnexion);
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "false");
        return configuration.buildSessionFactory();
    }

    private static void appliquerParametresH2(Configuration config, String urlJdbc) {
        config.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        config.setProperty("hibernate.connection.url", urlJdbc);
        config.setProperty("hibernate.connection.username", "sa");
        config.setProperty("hibernate.connection.password", "");
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    }

    private static String recupererVariable(String nomVariable) {
        String valeur = System.getenv(nomVariable);
        if (valeur == null || valeur.trim().isEmpty()) {
            throw new IllegalStateException("Configuration incomplète : la variable d'environnement " + nomVariable + " est obligatoire.");
        }
        return valeur.trim();
    }

    /**
     * Ferme la SessionFactory globale et libère les ressources associées.
     */
    public static synchronized void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        sessionFactory = null;
    }
}
