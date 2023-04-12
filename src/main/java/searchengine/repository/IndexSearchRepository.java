package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexSearch;
import searchengine.model.Lemma;

import java.util.List;

@Repository
public interface IndexSearchRepository extends JpaRepository<IndexSearch, Integer> {

    List<IndexSearch> findByLemma(Lemma lemma);
}
