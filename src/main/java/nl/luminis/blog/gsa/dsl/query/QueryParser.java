package nl.luminis.blog.gsa.dsl.query;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.elasticsearch.index.query.MatchNoneQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import nl.luminis.blog.gsa.dsl.GsaQueryLexer;
import nl.luminis.blog.gsa.dsl.GsaQueryParser;
import nl.luminis.blog.gsa.dsl.ParserErrorListener;

@Slf4j
@Service
public class QueryParser {

    private QueryVisitor visitor;

    @Autowired
    public QueryParser(QueryVisitor visitor) {
        this.visitor = visitor;
    }

    public QueryBuilder visit(String searchQuery) {
        try {
            GsaQueryParser.QueryContext query = createQueryContext(searchQuery);
            return visitor.visit(query);
        } catch (ParseCancellationException ex) {
            log.error("An error occurred while parsing the query \"{}\":", searchQuery, ex);
            return new MatchNoneQueryBuilder();
        }
    }

    private GsaQueryParser.QueryContext createQueryContext(String query) {
        query = query.trim();
        GsaQueryLexer lexer = new GsaQueryLexer(CharStreams.fromString(query));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ParserErrorListener());

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);

        GsaQueryParser parser = new GsaQueryParser(commonTokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(new ParserErrorListener());

        return parser.query();
    }
}
