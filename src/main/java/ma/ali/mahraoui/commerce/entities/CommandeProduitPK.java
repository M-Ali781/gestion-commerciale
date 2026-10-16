package ma.ali.mahraoui.commerce.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Clé primaire composite pour l'association Commande - Produit.
 *
 * @author Ali Mahraoui
 */
@Embeddable
public class CommandeProduitPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "commande_id")
    private Long commandeId;

    @Column(name = "produit_id")
    private Long produitId;

    public CommandeProduitPK() {
    }

    public CommandeProduitPK(Long commandeId, Long produitId) {
        this.commandeId = commandeId;
        this.produitId = produitId;
    }

    public Long getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Long commandeId) {
        this.commandeId = commandeId;
    }

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CommandeProduitPK)) {
            return false;
        }
        CommandeProduitPK that = (CommandeProduitPK) obj;
        return Objects.equals(commandeId, that.commandeId) &&
               Objects.equals(produitId, that.produitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandeId, produitId);
    }

    @Override
    public String toString() {
        return "CommandeProduitPK{commandeId=" + commandeId + ", produitId=" + produitId + "}";
    }
}
