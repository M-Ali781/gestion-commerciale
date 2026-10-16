package ma.ali.mahraoui.commerce.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entité représentant une catégorie de produits commerciaux.
 * Gère le cycle de vie de ses produits via cascade et suppression d'orphelins.
 *
 * @author Ali Mahraoui
 */
@Entity
@Table(name = "categories")
public class Categorie implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 100)
    private String nom;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produit> produits = new ArrayList<>();

    public Categorie() {
    }

    public Categorie(String nom) {
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Produit> getProduits() {
        return Collections.unmodifiableList(produits);
    }

    public void addProduit(Produit produit) {
        if (produit == null) {
            throw new IllegalArgumentException("Le produit à ajouter ne peut pas être nul.");
        }
        if (produit.getCategorie() != null && produit.getCategorie() != this) {
            throw new IllegalArgumentException("Le produit appartient déjà à une catégorie.");
        }
        if (!produits.contains(produit)) {
            produits.add(produit);
        }
        produit.associerCategorie(this);
    }

    public void removeProduit(Produit produit) {
        if (produit != null && produits.remove(produit)) {
            produit.associerCategorie(null);
        }
    }

    @Override
    public String toString() {
        return "Categorie{id=" + id + ", nom='" + nom + "'}";
    }
}
