package ma.ali.mahraoui.commerce.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

/**
 * Entité représentant un article commercial ou produit de vente.
 * Comporte des requêtes nommées pour optimiser les chargements par catégorie ou libellé.
 *
 * @author Ali Mahraoui
 */
@Entity
@Table(name = "produits")
@NamedQueries({
    @NamedQuery(name = "Produit.findAll", query = "SELECT p FROM Produit p JOIN FETCH p.categorie ORDER BY p.id"),
    @NamedQuery(name = "Produit.findByCategorie", query = "SELECT p FROM Produit p JOIN FETCH p.categorie WHERE p.categorie.id = :catId ORDER BY p.id"),
    @NamedQuery(name = "Produit.searchByLibelle", query = "SELECT p FROM Produit p JOIN FETCH p.categorie WHERE LOWER(p.libelle) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY p.id")
})
public class Produit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle", nullable = false, length = 150)
    private String libelle;

    @Column(name = "prix", nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    public Produit() {
    }

    public Produit(String libelle, BigDecimal prix, Categorie categorie) {
        this.libelle = libelle;
        setPrix(prix);
        if (categorie != null) {
            categorie.addProduit(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        if (prix == null || prix.signum() < 0) {
            throw new IllegalArgumentException("Prix nul ou négatif.");
        }
        this.prix = prix;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    void associerCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    @PrePersist
    @PreUpdate
    private void valider() {
        setPrix(prix);
    }

    @Override
    public String toString() {
        return "Produit{id=" + id + ", libelle='" + libelle + "', prix=" + prix + "}";
    }
}
