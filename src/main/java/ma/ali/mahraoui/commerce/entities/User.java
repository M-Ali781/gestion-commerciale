package ma.ali.mahraoui.commerce.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Entité racine pour les utilisateurs du système commercial.
 * Utilise la stratégie d'héritage JOINED.
 *
 * @author Ali Mahraoui
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    public User() {
    }

    public User(String nom) {
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", nom='" + nom + "'}";
    }
}
