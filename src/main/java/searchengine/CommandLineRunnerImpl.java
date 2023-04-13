package searchengine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import searchengine.config.SitesList;
import searchengine.repository.SiteRepository;

@Configuration
public class CommandLineRunnerImpl implements CommandLineRunner {

    private SiteRepository siteRepository;
    private SitesList sitesList;

    @Autowired
    public CommandLineRunnerImpl(SiteRepository siteRepository, SitesList sitesList) {
        this.siteRepository = siteRepository;
        this.sitesList = sitesList;
    }

    @Override
    public void run(String... args) throws Exception {
//        sitesList.getSites().forEach(siteFromAppProperties -> {
//            siteFromAppProperties.setUrl(siteFromAppProperties.getUrl().replace("www.", ""));
//            searchengine.model.Site site = new Site();
//            site.setName(siteFromAppProperties.getName());
//            site.setUrl(siteFromAppProperties.getUrl());
//            site.setStatus(IndexingStatus.INDEXING);
//            site.setStatusTime(LocalDateTime.now());
//            site.setLastError(null);
//            siteRepository.save(site);

//        });

    }
}
