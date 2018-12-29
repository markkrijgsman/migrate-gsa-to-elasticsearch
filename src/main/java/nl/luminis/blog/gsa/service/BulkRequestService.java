package nl.luminis.blog.gsa.service;

import static nl.luminis.blog.gsa.Constants.INDEX_TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BulkRequestService {

    private final RestHighLevelClient client;

    @Autowired
    public BulkRequestService(RestHighLevelClient client) {
        this.client = client;
    }

    public void executeBulkrequest(String index, List<String> documents) throws IOException {
        BulkRequest request = new BulkRequest();

        for (String document : documents) {
            IndexRequest indexRequest = new IndexRequest(index, INDEX_TYPE).source(document, XContentType.JSON);
            request.add(indexRequest);
        }
        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        handleResponse(response);
    }

    private void handleResponse(BulkResponse response) {
        List<String> corruptDocuments = new ArrayList<>();
        if (response.hasFailures()) {
            for (BulkItemResponse bulkItemResponse : response.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    corruptDocuments.add(bulkItemResponse.getId() + " " + bulkItemResponse.getFailureMessage());
                }
            }
            throw new ElasticsearchException("Failed to index one or more documents: {}", corruptDocuments);
        }
    }
}
