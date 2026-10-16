package ma.ali.mahraoui.commerce.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ma.ali.mahraoui.commerce.entities.Categorie;
import ma.ali.mahraoui.commerce.entities.Commande;
import ma.ali.mahraoui.commerce.entities.Produit;
import ma.ali.mahraoui.commerce.service.RapportVentesService;
import ma.ali.mahraoui.commerce.util.HibernateUtil;
import ma.ali.mahraoui.commerce.util.JpaTransactions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests unitaires de la servlet RapportVentesServlet.
 * Vérifie la génération JSON conforme et les codes statut HTTP.
 *
 * @author Ali Mahraoui
 */
public class RapportVentesServletTest {

    private EntityManagerFactory fabrique;
    private RapportVentesServlet servlet;

    @Before
    public void preparer() {
        fabrique = HibernateUtil.creerPourTests("jdbc:h2:mem:web_" + UUID.randomUUID());
        new JpaTransactions(fabrique).ecrire(em -> {
            Categorie cat = new Categorie("Bureau \"Atlas\"");
            Produit produit = new Produit("Cahier", new BigDecimal("12.50"), cat);
            em.persist(cat);

            Commande commande = new Commande(YearMonth.now(ZoneId.of("Africa/Casablanca")).minusMonths(1).atDay(1));
            commande.addProduit(produit, 2);
            em.persist(commande);
            return null;
        });
        servlet = new RapportVentesServlet(new RapportVentesService(fabrique));
    }

    @After
    public void fermer() {
        if (fabrique != null && fabrique.isOpen()) {
            fabrique.close();
        }
    }

    private String executerServlet(String moisParam, Map<String, Object> reponseCaptures) throws Exception {
        StringWriter contenu = new StringWriter();
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getParameter".equals(method.getName())) {
                        return moisParam;
                    }
                    return null;
                });

        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("getWriter".equals(method.getName())) {
                        return new PrintWriter(contenu);
                    }
                    if (method.getName().startsWith("set")) {
                        reponseCaptures.put(method.getName(), args != null && args.length > 0 ? args[0] : null);
                    }
                    return null;
                });

        servlet.doGet(request, response);
        return contenu.toString();
    }

    @Test
    public void rapportDuMoisPrecedentEtJsonEchappe() throws Exception {
        Map<String, Object> reponse = new HashMap<>();
        String json = executerServlet(null, reponse);

        assertEquals("application/json", reponse.get("setContentType"));
        assertEquals("UTF-8", reponse.get("setCharacterEncoding"));

        YearMonth moisAttendu = YearMonth.now(ZoneId.of("Africa/Casablanca")).minusMonths(1);
        assertTrue(json.contains("\"mois\":\"" + moisAttendu + "\""));
        assertTrue(json.contains("Bureau \\\"Atlas\\\""));
        assertTrue(json.contains("\"chiffreAffaires\":25"));
    }

    @Test
    public void moisExpliciteSansVente() throws Exception {
        String json = executerServlet("2000-01", new HashMap<>());
        assertTrue(json.contains("\"chiffreAffaires\":0"));
    }

    @Test
    public void moisInvalideRetourne400() throws Exception {
        Map<String, Object> reponse = new HashMap<>();
        String json = executerServlet("invalide", reponse);

        assertTrue(json.contains("AAAA-MM"));
        assertEquals(400, reponse.get("setStatus"));
    }
}
