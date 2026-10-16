package ma.ali.mahraoui.commerce.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

/**
 * Entité d'association représentant une ligne de commande détaillée.
 * Fige le prix unitaire d'achat au moment de la passation de commande.
 *
 * @author Ali Mahraoui
 */
@Entity
@Table(name = "commande_produit")
public class CommandeProduit implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private CommandeProduitPK id = new CommandeProduitPK();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("commandeId")
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("produitId")
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    public CommandeProduit() {
    }

    public CommandeProduit(Commande commande, Produit produit, int quantite) {
        this.commande = commande;
        this.produit = produit;
        this.id = new CommandeProduitPK(commande != null ? commande.getId() : null,
                                       produit != null ? produit.getId() : null);
        this.prixUnitaire = produit != null ? produit.getPrix() : null;
        setQuantite(quantite);
    }

    public CommandeProduitPK getId() {
        return id;
    }

    public void setId(CommandeProduitPK id) {
        this.id = id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être strictement positive.");
        }
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public void rattacherProduit(Produit reference) {
        if (reference == null || !reference.getId().equals(this.produit.getId())) {
            throw new IllegalArgumentException("La référence fournie doit correspondre au même identifiant produit.");
        }
        this.produit = reference;
    }

    @PrePersist
    @PreUpdate
    private void valider() {
        setQuantite(quantite);
    }
}
