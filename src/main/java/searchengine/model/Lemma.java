package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Setter
@Getter
@Table(indexes = @javax.persistence.Index(name = "lemma_index",columnList = "lemma, site_id", unique = true))
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    private Site site;

    @Column(nullable = false)
    private String lemma;

    @Column(nullable = false)
    private Integer frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
    private List <Index> indexSearches;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lemma lemma1 = (Lemma) o;
        return Objects.equals(id, lemma1.id) && Objects.equals(site, lemma1.site) && Objects.equals(lemma, lemma1.lemma) && Objects.equals(frequency, lemma1.frequency) && Objects.equals(indexSearches, lemma1.indexSearches);
    }

    @Override
    public int hashCode() {
        return lemma.hashCode();
    }
}
