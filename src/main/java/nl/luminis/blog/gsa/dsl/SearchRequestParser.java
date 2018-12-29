package nl.luminis.blog.gsa.dsl;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchNoneQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;

import nl.luminis.blog.gsa.dsl.partialfields.PartialFieldsParser;
import nl.luminis.blog.gsa.dsl.query.QueryParser;

@Service
public class SearchRequestParser {

    private final PartialFieldsParser partialFieldsParser;
    private final QueryParser queryParser;

    public SearchRequestParser(PartialFieldsParser partialFieldsParser, QueryParser queryParser) {
        this.partialFieldsParser = partialFieldsParser;
        this.queryParser = queryParser;
    }

    public QueryBuilder parse(String query, String partialFields) {
        if (isEmptySearch(query, partialFields)) {
            return new MatchNoneQueryBuilder();
        }

        BoolQueryBuilder result = boolQuery();
        if (isNotBlank(partialFields)) {
            result.must(partialFieldsParser.visit(partialFields));
        }
        if (isNotBlank(query)) {
            result.must(queryParser.visit(query));
        }
        return result;
    }

    private boolean isEmptySearch(String query, String partialFields) {
        return isBlank(query) && isBlank(partialFields);
    }
}
