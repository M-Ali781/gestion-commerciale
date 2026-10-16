package ma.ali.mahraoui.commerce.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import ma.ali.mahraoui.commerce.util.HibernateUtil;

/**
 * Initialisateur de cycle de vie du contexte web servlet.
 * Démarre et arrête proprement la SessionFactory JPA au déploiement de l'application.
 *
 * @author Ali Mahraoui
 */
@WebListener
public class PersistanceListener implements ServletContextListener {

    public static final String FABRIQUE = "commerce.fabrique";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setAttribute(FABRIQUE, HibernateUtil.getSessionFactory());
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        HibernateUtil.shutdown();
    }
}
