package searchengine;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ApplicationTest {

    private final Application application;

    @Value("${server.port}")
    String serverPort;

    @Autowired
    ApplicationTest(Application application) {
        this.application = application;
    }

    @Test
    void contextLoads() {
        assertNotNull(application);
    }

    @Test
    void verifyIndexSites(){
        assertThat(serverPort, Matchers.containsString("8080"));
    }

}