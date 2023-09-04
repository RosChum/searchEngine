package searchengine.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private SiteRepository siteRepository;
    @MockBean
    private PageRepository pageRepository;
    @MockBean
    private LemmaRepository lemmaRepository;
    @MockBean
    private IndexRepository indexSearchRepository;

    @Autowired
    public ApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }


    @Test
    public void testGetStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics"))
                .andDo(print()).andExpect(status()
                        .isOk()).andExpect(content()
                        .string(containsString("")));
    }

    @Test
    public void testStartIndexing() throws Exception {
        mockMvc.perform(get("/api/startIndexing")).andDo(print()).andExpect(status()
                .isOk()).andExpect(content()
                .string(containsString("")));
    }

    @Test
    public void testStopIndexing() throws Exception {
        mockMvc.perform(get("/api/stopIndexing")).andDo(print()).andExpect(status()
                .isOk()).andExpect(content()
                .string(containsString("")));
    }

    @Test
    public void testIndexPage() throws Exception {
        mockMvc.perform(post("/api/indexPage?url={url}", "")).andDo(print()).andExpect(status()
                .isOk()).andExpect(content()
                .string(containsString("")));
    }


    @Test
    public void testSearch() throws Exception {
        mockMvc.perform(get("/api/search?query={query}&limit={limit}&offset={offset}", "", 10, 0)).andDo(print()).andExpect(status()
                .isOk()).andExpect(content()
                .string(containsString("")));
        System.out.println("------------" + "\n");

        mockMvc.perform(get("/api/search?query={query}&limit={limit}&offset={offset}", "театр", 10, 0)).andDo(print()).andExpect(status()
                .isOk()).andExpect(content()
                .string(containsString("")));

    }


}