package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexSearch;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface IndexSearchRepository extends JpaRepository<IndexSearch, Integer> {

    List<IndexSearch> findAllByLemma(Lemma lemma);
    List<IndexSearch> findAllByPage(Page page);
}
