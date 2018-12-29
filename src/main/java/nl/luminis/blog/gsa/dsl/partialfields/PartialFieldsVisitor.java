package nl.luminis.blog.gsa.dsl.partialfields;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.luminis.blog.gsa.dsl.BooleanConverter;
import nl.luminis.blog.gsa.dsl.GsaPartialFieldsParser;
import nl.luminis.blog.gsa.dsl.GsaPartialFieldsParserBaseVisitor;

@Service
public class PartialFieldsVisitor extends GsaPartialFieldsParserBaseVisitor<QueryBuilder> {

    private final BooleanConverter booleanConverter;

    @Autowired
    public PartialFieldsVisitor(BooleanConverter booleanConverter) {
        this.booleanConverter = booleanConverter;
    }

    @Override
    public QueryBuilder visitSubQuery(GsaPartialFieldsParser.SubQueryContext ctx) {
        BoolQueryBuilder result = new BoolQueryBuilder();
        List<QueryBuilder> builders = new ArrayList<>();

        // Unwrap nested subqueries
        if (ctx.subQuery() != null) {
            ctx.subQuery().stream().map(this::visit).filter(Objects::nonNull).forEach(builders::add);
        }

        if (ctx.pair() != null) {
            ctx.pair().stream().map(this::visit).filter(Objects::nonNull).forEach(builders::add);
        }

        // Prevent the addition of unnecessary boolean query clauses.
        if (builders.isEmpty()) {
            return null;
        } else if (builders.size() == 1) {
            return builders.get(0);
        } else {
            if (!ctx.AND().isEmpty()) {
                builders.forEach(((BoolQueryBuilder) result)::must);
            }
            if (!ctx.OR().isEmpty()) {
                builders.forEach(((BoolQueryBuilder) result)::should);
                result.minimumShouldMatch(1);
            }
            return result;
        }
    }

    @Override
    public QueryBuilder visitInclusionPair(GsaPartialFieldsParser.InclusionPairContext ctx) {
        return createQuery(ctx.KEYWORD().getText(), ctx.VALUE().getText(), false);
    }

    @Override
    public QueryBuilder visitExclusionPair(GsaPartialFieldsParser.ExclusionPairContext ctx) {
        return createQuery(ctx.KEYWORD().getText(), ctx.VALUE().getText(), true);
    }

    @Override
    public QueryBuilder visitNestedPair(GsaPartialFieldsParser.NestedPairContext ctx) {
        return visit(ctx.pair());
    }

    private QueryBuilder createQuery(String key, String value, boolean isExcluded) {
        // For the sake of token recognition, we include the separator ':' in the value and need to strip it off here.
        value = value.substring(1);
        String sanitizedValue = booleanConverter.convert(value);

        if (isExcluded) {
            return getExclusionQuery(key, sanitizedValue);
        } else {
            return getInclusionQuery(key, sanitizedValue);
        }
    }

    private QueryBuilder getExclusionQuery(String key, String value) {
        return new BoolQueryBuilder().mustNot(new MatchQueryBuilder(key, value));
    }

    private QueryBuilder getInclusionQuery(String key, String value) {
        return new MatchQueryBuilder(key, value).operator(Operator.AND);
    }
}

