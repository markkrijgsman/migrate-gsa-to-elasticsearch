package nl.luminis.blog.gsa.rest;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nl.luminis.blog.gsa.dto.MusicAlbum;
import nl.luminis.blog.gsa.service.SearchService;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MusicAlbum> search(@RequestParam(value = "q", required = false) String query,
                                   @RequestParam(value = "partialFields", required = false) String partialFields,
                                   @RequestParam(value = "start", required = false, defaultValue = "0") Integer from,
                                   @RequestParam(value = "num", required = false, defaultValue = "10") Integer size) throws IOException {
        return searchService.search(query, partialFields, from, size);
    }
}