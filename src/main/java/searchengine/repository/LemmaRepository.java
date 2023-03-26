package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

//    @Query("select case when count(l)> 0 then true else false end FROM Lemma as l WHERE l.lemma = :lemma like l.site = :site")
//    boolean existsByLemma(@Param("lemma") String lemma,@Param("site") Site site);

    @Transactional
    @Query("UPDATE Lemma l set l.frequency = :frequency WHERE l.lemma = :lemma and l.site = :site")
    @Modifying(clearAutomatically = true)
    void updateFrequency(@Param("lemma") String name, @Param("frequency") int frequency, @Param("site") Site site);

    Lemma findByLemma(String lemma);

    @Query("SELECT l FROM Lemma as l WHERE l.lemma = :lemma AND l.site = :site")
    Lemma findByLemmaAndSite(@Param("lemma") String lemma, @Param("site") Site site);

}
