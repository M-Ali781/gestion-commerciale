package ma.ali.mahraoui.commerce.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Objet de transfert de données représentant le chiffre d'affaires consolidé par catégorie.
 *
 * @author Ali Mahraoui
 */
public final class VenteCategorie implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long categorieId;
    private final String categorie;
    private final BigDecimal chiffreAffaires;

    public VenteCategorie(Long categorieId, String categorie, BigDecimal chiffreAffaires) {
        this.categorieId = categorieId;
        this.categorie = Objects.requireNonNull(categorie, "Le nom de la catégorie ne peut pas être nul.");
        this.chiffreAffaires = chiffreAffaires != null ? chiffreAffaires : BigDecimal.ZERO;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public String getCategorie() {
        return categorie;
    }

    public BigDecimal getChiffreAffaires() {
        return chiffreAffaires;
    }

    @Override
    public String toString() {
        return categorie + " : " + chiffreAffaires + " MAD";
    }
}
