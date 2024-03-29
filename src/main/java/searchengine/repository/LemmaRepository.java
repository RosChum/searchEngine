package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Query(value = "UPDATE Lemma l set l.frequency = l.frequency + 1 WHERE l.lemma = :lemma AND l.site = :site")
    @Modifying(clearAutomatically = true)
    void updateFrequency(@Param("lemma") String name, @Param("site") Site site);


    List<Lemma> findByLemma(String text);

    List<Lemma> findBySite(Site site);

    Lemma findByLemmaAndSite(String lemma, Site site);

    @Query("SELECT l FROM Lemma as l WHERE l.lemma = :lemma AND l.site = :site")
    List<Lemma> findLemmasByLemmaAndSite(@Param("lemma") String lemma, @Param("site") Site site);

    boolean existsLemmaByLemmaAndSite(String lemma, Site site);

}
