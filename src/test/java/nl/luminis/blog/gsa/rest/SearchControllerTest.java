package nl.luminis.blog.gsa.rest;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.luminis.blog.gsa.service.SearchService;

@RunWith(MockitoJUnitRunner.class)
public class SearchControllerTest {

    @InjectMocks
    private SearchController searchController;
    @Mock
    private SearchService searchService;

    @Test
    public void testSearch() throws IOException {
        searchController.search("value", "(key:value)", 0, 10);

        verify(searchService, times(1)).search("value", "(key:value)", 0, 10);
    }
}
