package nl.luminis.blog.gsa.dsl.partialfields;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.elasticsearch.index.query.MatchNoneQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import nl.luminis.blog.gsa.dsl.GsaPartialFieldsLexer;
import nl.luminis.blog.gsa.dsl.GsaPartialFieldsParser;
import nl.luminis.blog.gsa.dsl.ParserErrorListener;

@Slf4j
@Service
public class PartialFieldsParser {

    private final PartialFieldsVisitor visitor;

    @Autowired
    public PartialFieldsParser(PartialFieldsVisitor visitor) {
        this.visitor = visitor;
    }

    public QueryBuilder visit(String searchQuery) {
        try {
            GsaPartialFieldsParser.QueryContext query = createQueryContext(searchQuery);
            QueryBuilder builder = visitor.visit(query);
            return (builder == null) ? new MatchNoneQueryBuilder() : builder;
        } catch (ParseCancellationException ex) {
            log.error("An error occurred while parsing the query \"{}\":", searchQuery, ex);
            return new MatchNoneQueryBuilder();
        }
    }

    private GsaPartialFieldsParser.QueryContext createQueryContext(String query) {
        GsaPartialFieldsLexer lexer = new GsaPartialFieldsLexer(CharStreams.fromString(query));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ParserErrorListener());

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);

        GsaPartialFieldsParser parser = new GsaPartialFieldsParser(commonTokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(new ParserErrorListener());

        return parser.query();
    }
}
