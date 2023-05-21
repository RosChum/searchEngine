package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexingStatus;
import searchengine.model.Site;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    Site findByUrl(String url);

    @Query("SELECT s FROM Site as s WHERE s.status = :status")
    List<Site> findByStatus(@Param("status") IndexingStatus status);

    boolean existsByUrl(String url);

    @Transactional
    @Query("UPDATE Site s set s.status = :status, s.lastError = :error, s.statusTime = :time WHERE s.name = :name")
    @Modifying(clearAutomatically = true)
    void updateStatus(@Param("name") String name, @Param("status") IndexingStatus status, @Param("error")
            String error, @Param("time") LocalDateTime statusTime);

    @Transactional
    @Query("UPDATE Site s set s.statusTime = :time WHERE s.name = :name")
    @Modifying(clearAutomatically = true)
    void updateStatusTime(@Param("name") String name, @Param("time") LocalDateTime statusTime);
}
