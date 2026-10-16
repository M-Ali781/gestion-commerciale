package ma.ali.mahraoui.commerce.entities;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Entité représentant un client régulier de l'entreprise.
 *
 * @author Ali Mahraoui
 */
@Entity
@Table(name = "clients")
public class Client extends User {

    private static final long serialVersionUID = 1L;

    @Column(name = "date_inscription")
    private LocalDate dateInscription;

    public Client() {
    }

    public Client(String nom, LocalDate dateInscription) {
        super(nom);
        this.dateInscription = dateInscription;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }
}
