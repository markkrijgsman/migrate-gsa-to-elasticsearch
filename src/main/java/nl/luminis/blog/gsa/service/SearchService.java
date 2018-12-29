package nl.luminis.blog.gsa.service;

import static java.util.stream.Collectors.toList;
import static nl.luminis.blog.gsa.Constants.INDEX;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;
import nl.luminis.blog.gsa.dsl.SearchRequestParser;
import nl.luminis.blog.gsa.dto.MusicAlbum;
import nl.luminis.blog.gsa.util.JsonUtil;

@Slf4j
@Service
public class SearchService {

    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private final RestHighLevelClient client;
    private final SearchRequestParser searchRequestParser;

    @Autowired
    public SearchService(RestHighLevelClient client, SearchRequestParser searchRequestParser) {
        this.client = client;
        this.searchRequestParser = searchRequestParser;
    }

    public List<MusicAlbum> search(String query, String partialFields, int from, int size) throws IOException {
        QueryBuilder builder = searchRequestParser.parse(query, partialFields);
        SearchRequest request = createRequest(builder, from, size, INDEX);
        SearchResponse response = executeRequest(request);
        return handleResponse(response);
    }

    private SearchRequest createRequest(QueryBuilder queryBuilder, int from, int size, String... indices) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        return new SearchRequest(indices, searchSourceBuilder);
    }

    private SearchResponse executeRequest(SearchRequest request) throws IOException {
        log.debug("\nGET {}/_search\n{}", String.join(",", request.indices()), JsonUtil.pretty(request.source().toString()));
        return client.search(request, RequestOptions.DEFAULT);
    }

    private List<MusicAlbum> handleResponse(SearchResponse response) {
        return Arrays.stream(response.getHits().getHits())
                     .map(hit -> GSON.fromJson(hit.getSourceAsString(), MusicAlbum.class))
                     .collect(toList());
    }
}
