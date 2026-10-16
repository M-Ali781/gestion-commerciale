package ma.ali.mahraoui.commerce.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Entité représentant un fournisseur de marchandises.
 *
 * @author Ali Mahraoui
 */
@Entity
@Table(name = "fournisseurs")
public class Fournisseur extends User {

    private static final long serialVersionUID = 1L;

    @Column(name = "adresse", length = 255)
    private String adresse;

    public Fournisseur() {
    }

    public Fournisseur(String nom, String adresse) {
        super(nom);
        this.adresse = adresse;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
