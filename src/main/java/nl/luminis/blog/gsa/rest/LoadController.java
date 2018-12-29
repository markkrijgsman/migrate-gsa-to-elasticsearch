package nl.luminis.blog.gsa.rest;

import static java.util.stream.Collectors.toList;
import static nl.luminis.blog.gsa.Constants.INDEX;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import nl.luminis.blog.gsa.service.BulkRequestService;

@Slf4j
@RestController
@RequestMapping("/load")
public class LoadController {

    private static final String DOCUMENTS = "/rolling500.json";
    private final BulkRequestService bulkRequestService;

    @Autowired
    public LoadController(BulkRequestService bulkRequestService) {
        this.bulkRequestService = bulkRequestService;
    }

    @GetMapping
    public void loadDocuments() throws IOException {
        List<String> documents = loadResource(DOCUMENTS);
        bulkRequestService.executeBulkrequest(INDEX, documents);
        log.info("Finished loading {} documents in index {}", documents.size(), INDEX);
    }

    private List<String> loadResource(String resource) throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream(resource)) {
            return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(toList());
        }
    }
}