package nl.luminis.blog.gsa.dsl.query;

import static java.util.stream.Collectors.joining;
import static nl.luminis.blog.gsa.dto.MusicAlbum.SEARCHABLE_FIELDS;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.luminis.blog.gsa.dsl.BooleanConverter;
import nl.luminis.blog.gsa.dsl.GsaQueryParser;
import nl.luminis.blog.gsa.dsl.GsaQueryParserBaseVisitor;

@Service
public class QueryVisitor extends GsaQueryParserBaseVisitor<QueryBuilder> {

    private static final String TOKEN_OR = "OR";
    private static final String RANGE_REGEX = "\\.\\.";
    private static final String DOUBLE_REGEX = "-?\\d+\\.\\d+";

    private final BooleanConverter booleanConverter;

    @Autowired
    public QueryVisitor(BooleanConverter booleanConverter) {
        this.booleanConverter = booleanConverter;
    }

    @Override
    public QueryBuilder visitFreeTextQuery(GsaQueryParser.FreeTextQueryContext ctx) {
        return createFreeTextQuery(concatenateValues(ctx.TEXT()), Operator.AND);
    }

    @Override
    public QueryBuilder visitPairQuery(GsaQueryParser.PairQueryContext ctx) {
        BoolQueryBuilder result = new BoolQueryBuilder();
        ctx.pair().forEach(pair -> {
            QueryBuilder builder = visit(pair);
            if (hasOrClause(ctx, pair)) {
                result.should(builder);
                result.minimumShouldMatch(1);
            } else {
                result.must(builder);
            }
        });
        return result;
    }

    @Override
    public QueryBuilder visitInmetaPair(GsaQueryParser.InmetaPairContext ctx) {
        String key = getInmetaKey(ctx.TEXT());
        String value = getInmetaValue(ctx.TEXT());

        if (value.matches(DOUBLE_REGEX + RANGE_REGEX + DOUBLE_REGEX)) {
            String[] range = value.split(RANGE_REGEX);
            return new RangeQueryBuilder(key).gte(range[0]).lte(range[1]);
        } else {
            return new MatchQueryBuilder(key, value);
        }
    }

    @Override
    public QueryBuilder visitAllintextPair(GsaQueryParser.AllintextPairContext ctx) {
        String value = concatenateValues(ctx.TEXT());
        if (!ctx.OR().isEmpty()) {
            return createFreeTextQuery(value, Operator.OR).minimumShouldMatch("1");
        } else {
            return createFreeTextQuery(value, Operator.AND);
        }
    }

    private MultiMatchQueryBuilder createFreeTextQuery(String value, Operator operator) {
        return new MultiMatchQueryBuilder(value, SEARCHABLE_FIELDS)
                .operator(operator)
                .lenient(true);
    }

    private String concatenateValues(List<TerminalNode> textNodes) {
        return textNodes.stream().map(ParseTree::getText).collect(joining(" "));
    }

    private String getInmetaKey(List<TerminalNode> textNodes) {
        return textNodes.get(0).getText().toLowerCase();
    }

    private String getInmetaValue(List<TerminalNode> textNodes) {
        textNodes.remove(0);
        String value = concatenateValues(textNodes);
        return booleanConverter.convert(value);
    }

    // Pairs may be separated by OR clauses. Check the children to the left and to the right of the current pair to see if this is the case.
    private boolean hasOrClause(GsaQueryParser.QueryContext ctx, GsaQueryParser.PairContext pair) {
        int currentPairIndex = ctx.children.indexOf(pair);
        int offset = 1;

        boolean orTokenBeforeCurrentPair = currentPairIndex - offset >= 0 && TOKEN_OR.equals(ctx.children.get(currentPairIndex - offset).getText());
        boolean orTokenAfterCurrentPair = currentPairIndex + offset < ctx.children.size() && TOKEN_OR.equals(ctx.children.get(currentPairIndex + offset).getText());

        return orTokenBeforeCurrentPair || orTokenAfterCurrentPair;
    }
}
