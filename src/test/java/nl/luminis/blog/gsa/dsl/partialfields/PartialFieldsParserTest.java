package nl.luminis.blog.gsa.dsl.partialfields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchNoneQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.luminis.blog.gsa.dsl.BooleanConverter;

@RunWith(MockitoJUnitRunner.class)
public class PartialFieldsParserTest {

    private static final String ID = "351046";
    private static final String ALBUM = "Pet Sounds";
    private static final String INFORMATION = "Additional album information";

    private PartialFieldsParser parser;

    @Mock
    private BooleanConverter booleanConverter;

    @Before
    public void setUp() {
        PartialFieldsVisitor visitor = new PartialFieldsVisitor(booleanConverter);
        parser = new PartialFieldsParser(visitor);

        // The default behaviour of the converter should be to return the argument untouched.
        when(booleanConverter.convert(anyString())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    public void testNormalPair() {
        QueryBuilder builder = parser.visit("(id:" + ID + ")");

        assertThat(builder).isEqualTo(
                new MatchQueryBuilder("id", ID).operator(Operator.AND));
    }

    @Test
    public void testExcludedPair() {
        QueryBuilder builder = parser.visit("(-id:" + ID + ")");

        assertThat(builder).isEqualTo(
                new BoolQueryBuilder().mustNot(new MatchQueryBuilder("id", ID)));
    }

    @Test
    public void testDashCasedValue() {
        QueryBuilder builder = parser.visit("(information:description-with-dashes)");

        assertThat(builder).isEqualTo(
                new MatchQueryBuilder("information", "description-with-dashes").operator(Operator.AND));
    }

    @Test
    public void testAndPairs() {
        QueryBuilder builder = parser.visit("(id:" + ID + ").(album:" + ALBUM + ")");

        assertThat(builder).isEqualTo(
                boolQuery()
                        .must(new MatchQueryBuilder("id", ID).operator(Operator.AND))
                        .must(new MatchQueryBuilder("album", ALBUM).operator(Operator.AND))
        );
    }

    @Test
    public void testOrPairs() {
        QueryBuilder builder = parser.visit("(id:" + ID + ")|(album:" + ALBUM + ")");

        assertThat(builder).isEqualTo(
                boolQuery()
                        .should(new MatchQueryBuilder("id", ID).operator(Operator.AND))
                        .should(new MatchQueryBuilder("album", ALBUM).operator(Operator.AND))
                        .minimumShouldMatch(1)
        );
    }

    @Test
    public void testAndOrPairs() {
        QueryBuilder builder = parser.visit("(id:" + ID + ").((album:" + ALBUM + ")|(information:" + INFORMATION + "))");
        assertThat(builder).isEqualTo(
                boolQuery()
                        .must(boolQuery()
                                      .should(new MatchQueryBuilder("album", ALBUM).operator(Operator.AND))
                                      .should(new MatchQueryBuilder("information", INFORMATION).operator(Operator.AND))
                                      .minimumShouldMatch(1))
                        .must(new MatchQueryBuilder("id", ID).operator(Operator.AND))
        );
    }

    @Test
    public void testRedundantlyNestedPair() {
        QueryBuilder builder = parser.visit("((id:" + ID + "))");
        assertThat(builder).isEqualTo(
                new MatchQueryBuilder("id", ID).operator(Operator.AND));
    }

    @Test
    public void testRedundantlyNestedQuery() {
        QueryBuilder builder = parser.visit("((id:" + ID + ")|(album:" + ALBUM + "))");
        assertThat(builder).isEqualTo(
                boolQuery()
                        .should(new MatchQueryBuilder("id", ID).operator(Operator.AND))
                        .should(new MatchQueryBuilder("album", ALBUM).operator(Operator.AND))
                        .minimumShouldMatch(1)
        );
    }

    @Test
    public void testExtendedUnicodeCharacters() {
        String diacritic = "à";
        String nonLetterTerm = "[value]";
        String nonLetterTermInDSL = "An example: a value";
        String highUnicodeCharacter = "ـش\u200E";

        for (String term : Arrays.asList(diacritic, nonLetterTerm, nonLetterTermInDSL, highUnicodeCharacter)) {
            QueryBuilder builder = parser.visit("(album:" + term + ")");
            assertThat(builder).isEqualTo(new MatchQueryBuilder("album", term).operator(Operator.AND));
        }
    }

    @Test
    public void testQueryWithoutParentheses() {
        QueryBuilder builder = parser.visit("invalid-query");
        assertThat(builder).isEqualTo(new MatchNoneQueryBuilder());
    }

    @Test
    public void testQueryWithoutValue() {
        QueryBuilder builder = parser.visit("(invalid-key:)");
        assertThat(builder).isEqualTo(new MatchNoneQueryBuilder());
    }

    @Test
    public void testQueryUnknownField() {
        QueryBuilder builder = parser.visit("(unknown_key:value)");
        assertThat(builder).isEqualTo(
                new MatchQueryBuilder("unknown_key", "value").operator(Operator.AND));
    }

    @Test
    public void testKeywordQuery() {
        QueryBuilder builder = parser.visit("(album.keyword:" + ALBUM + ")");
        assertThat(builder).isEqualTo(
                new MatchQueryBuilder("album.keyword", ALBUM).operator(Operator.AND));
    }

    @Test
    public void testQueryValueWithColon() {
        QueryBuilder builder = parser.visit("(album:Album title: subtitle)");
        assertThat(builder).isEqualTo(
                new MatchQueryBuilder("album", "Album title: subtitle").operator(Operator.AND));
    }
}
