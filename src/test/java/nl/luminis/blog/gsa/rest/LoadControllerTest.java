package nl.luminis.blog.gsa.rest;

import static nl.luminis.blog.gsa.Constants.INDEX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.luminis.blog.gsa.rest.LoadController;
import nl.luminis.blog.gsa.service.BulkRequestService;

@RunWith(MockitoJUnitRunner.class)
public class LoadControllerTest {

    @InjectMocks
    private LoadController loadController;
    @Mock
    private BulkRequestService bulkRequestService;
    @Captor
    private ArgumentCaptor<List<String>> captor;

    @Test
    public void testLoadDocuments() throws IOException {
        loadController.loadDocuments();

        verify(bulkRequestService, times(1)).executeBulkrequest(eq(INDEX), captor.capture());
        assertThat(captor.getValue()).hasSize(500);
    }
}
