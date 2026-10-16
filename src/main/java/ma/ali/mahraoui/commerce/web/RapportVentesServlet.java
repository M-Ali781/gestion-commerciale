package ma.ali.mahraoui.commerce.web;

import java.io.IOException;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ma.ali.mahraoui.commerce.service.RapportVentesService;
import ma.ali.mahraoui.commerce.service.VenteCategorie;

/**
 * Endpoint Servlet REST fournissant les synthèses mensuelles du chiffre d'affaires au format JSON.
 *
 * @author Ali Mahraoui
 */
@WebServlet("/rapports/ventes")
public class RapportVentesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private RapportVentesService rapportVentesService;

    public RapportVentesServlet() {
    }

    public RapportVentesServlet(RapportVentesService rapportVentesService) {
        this.rapportVentesService = rapportVentesService;
    }

    @Override
    public void init() throws ServletException {
        if (rapportVentesService == null) {
            EntityManagerFactory emf = (EntityManagerFactory) getServletContext().getAttribute(PersistanceListener.FABRIQUE);
            if (emf == null) {
                throw new ServletException("Fabrique de persistance indisponible dans le contexte d'application.");
            }
            rapportVentesService = new RapportVentesService(emf);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        String paramMois = req.getParameter("mois");
        YearMonth moisCible;
        try {
            moisCible = (paramMois == null || paramMois.trim().isEmpty())
                    ? YearMonth.now(ZoneId.of("Africa/Casablanca")).minusMonths(1)
                    : YearMonth.parse(paramMois.trim());
        } catch (DateTimeParseException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"erreur\":\"Le mois doit respecter le format AAAA-MM.\"}");
            return;
        }

        List<VenteCategorie> stats = rapportVentesService.rapport(moisCible);

        StringBuilder json = new StringBuilder(256);
        json.append("{\"mois\":\"").append(moisCible).append("\",\"categories\":[");

        for (int i = 0; i < stats.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            VenteCategorie item = stats.get(i);
            json.append("{\"id\":").append(item.getCategorieId())
                .append(",\"nom\":\"").append(echapperJson(item.getCategorie()))
                .append("\",\"chiffreAffaires\":").append(item.getChiffreAffaires().toPlainString())
                .append('}');
        }

        json.append("]}");
        resp.getWriter().write(json.toString());
    }

    private static String echapperJson(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(input.length() + 16);
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\b':
                    out.append("\\b");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 32) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                    break;
            }
        }
        return out.toString();
    }
}
