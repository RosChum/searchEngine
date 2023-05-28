package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    boolean existsByPath(String path);

    List<Page> findBySite(Site site);

    Page findByPathAndSite(String path, Site site);

    boolean existsByPathAndSite(String path, Site site);
}
