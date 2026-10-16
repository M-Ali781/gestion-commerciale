package ma.ali.mahraoui.commerce.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
 * Entité représentant une commande client avec ses lignes d'articles associées.
 *
 * @author Ali Mahraoui
 */
@Entity
@Table(name = "commandes")
public class Commande implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande = LocalDate.now();

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeProduit> lignes = new ArrayList<>();

    public Commande() {
    }

    public Commande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public List<CommandeProduit> getLignes() {
        return Collections.unmodifiableList(lignes);
    }

    public void addProduit(Produit produit, int quantite) {
        if (produit == null || produit.getId() == null) {
            throw new IllegalArgumentException("Enregistrer le produit avant la commande.");
        }
        for (CommandeProduit ligne : lignes) {
            if (ligne.getProduit().getId().equals(produit.getId())) {
                throw new IllegalArgumentException("Le produit figure déjà dans la commande.");
            }
        }
        lignes.add(new CommandeProduit(this, produit, quantite));
    }

    public void removeProduit(Long produitId) {
        if (produitId == null) {
            throw new IllegalArgumentException("L'identifiant du produit ne peut pas être nul.");
        }
        Iterator<CommandeProduit> it = lignes.iterator();
        while (it.hasNext()) {
            CommandeProduit ligne = it.next();
            if (ligne.getProduit().getId().equals(produitId)) {
                it.remove();
                return;
            }
        }
        throw new IllegalArgumentException("Produit absent de la commande.");
    }

    @Override
    public String toString() {
        return "Commande{id=" + id + ", dateCommande=" + dateCommande + ", lignes=" + lignes.size() + "}";
    }
}
