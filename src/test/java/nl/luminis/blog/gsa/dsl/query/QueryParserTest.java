package nl.luminis.blog.gsa.dsl.query;

import static nl.luminis.blog.gsa.dto.MusicAlbum.SEARCHABLE_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.Operator.AND;
import static org.elasticsearch.index.query.Operator.OR;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.luminis.blog.gsa.dsl.BooleanConverter;

@RunWith(MockitoJUnitRunner.class)
public class QueryParserTest {

    private static final String ID = "351046";
    private static final String ALBUM = "Pet Sounds";
    private static final String INFORMATION = "Additional album information";

    private QueryParser parser;

    @Mock
    private BooleanConverter booleanConverter;

    @Before
    public void setUp() {
        QueryVisitor visitor = new QueryVisitor(booleanConverter);
        parser = new QueryParser(visitor);

        // The default behaviour of the converter should be to return the argument untouched.
        when(booleanConverter.convert(anyString())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    public void testSimpleValueQuery() {
        QueryBuilder builder = parser.visit("value");
        assertThat(builder).isEqualTo(freeTextQuery("value"));
    }

    @Test
    public void testSimpleNumericQuery() {
        QueryBuilder builder = parser.visit("69346054");
        assertThat(builder).isEqualTo(freeTextQuery("69346054"));
    }

    @Test
    public void testSimpleValueQueryWithNonLetterCharacter() {
        QueryBuilder builder = parser.visit("value.value");
        assertThat(builder).isEqualTo(freeTextQuery("value.value"));
    }

    @Test
    public void testTrailingWhitespaceQuery() {
        QueryBuilder builder = parser.visit("value   ");
        assertThat(builder).isEqualTo(freeTextQuery("value"));
    }

    @Test
    public void testDoubleValueQuery() {
        QueryBuilder builder = parser.visit("value1 value2");
        assertThat(builder).isEqualTo(freeTextQuery("value1 value2"));
    }

    @Test
    public void testDoubleValueQueryWithNonLetterCharacter() {
        QueryBuilder builder = parser.visit("value1 value2 .");
        assertThat(builder).isEqualTo(freeTextQuery("value1 value2 ."));
    }

    @Test
    public void testAllInTextSingleValueQuery() {
        QueryBuilder builder = parser.visit("allintext:value");
        assertThat(builder).isEqualTo(boolQuery().must(freeTextQuery("value")));
    }

    @Test
    public void testAllInTextDoubleValueQuery() {
        QueryBuilder builder = parser.visit("allintext:value1 value2");
        assertThat(builder).isEqualTo(boolQuery().must(freeTextQuery("value1 value2")));
    }

    @Test
    public void testAllInTextDoubleKeyQuery() {
        QueryBuilder builder = parser.visit("allintext:value1 allintext:value2");
        assertThat(builder).isEqualTo(
                boolQuery()
                        .must(freeTextQuery("value1"))
                        .must(freeTextQuery("value2"))
        );
    }

    @Test
    public void testAllInTextTripleValueQuery() {
        QueryBuilder builder = parser.visit("allintext:value1 value2 value3");
        assertThat(builder).isEqualTo(boolQuery().must(freeTextQuery("value1 value2 value3")));
    }

    @Test
    public void testSingleSpacedAllInTextQuery() {
        QueryBuilder builder = parser.visit("allintext: value");
        assertThat(builder).isEqualTo(boolQuery().must(freeTextQuery("value")));
    }

    @Test
    public void testDoubleSpacedAllInTextQuery() {
        QueryBuilder builder = parser.visit("allintext:  value");
        assertThat(builder).isEqualTo(boolQuery().must(freeTextQuery("value")));
    }

    @Test
    public void testTrailingSpaceAllInTextQuery() {
        QueryBuilder builder = parser.visit("allintext: value ");
        assertThat(builder).isEqualTo(boolQuery().must(freeTextQuery("value")));
    }

    @Test
    public void testInMetaSingleValueQuery() {
        QueryBuilder builder = parser.visit("inmeta:id=" + ID);
        assertThat(builder).isEqualTo(
                boolQuery()
                        .must(new MatchQueryBuilder("id", ID)));
    }

    @Test
    public void testInMetaDoubleValueQuery() {
        QueryBuilder builder = parser.visit("inmeta:id=" + ID + " inmeta:album=" + ALBUM);
        assertThat(builder).isEqualTo(
                boolQuery()
                        .must(new MatchQueryBuilder("id", ID))
                        .must(new MatchQueryBuilder("album", ALBUM)));
    }

    @Test
    public void testInMetaTripleValueQuery() {
        QueryBuilder builder = parser.visit("inmeta:id=" + ID + " inmeta:album=" + ALBUM + " inmeta:information=" + INFORMATION);
        assertThat(builder).isEqualTo(
                boolQuery()
                        .must(new MatchQueryBuilder("id", ID))
                        .must(new MatchQueryBuilder("album", ALBUM))
                        .must(new MatchQueryBuilder("information", INFORMATION)));
    }

    @Test
    public void testInvalidInMetaValue() {
        QueryBuilder builder = parser.visit("inmeta:invalid-key");
        assertThat(builder).isEqualTo(
                boolQuery()
                        .must(new MatchQueryBuilder("invalid-key", "")));
    }

    @Test
    public void testAllInTextAndInMetaQuery() {
        QueryBuilder builder = parser.visit("allintext:" + ALBUM + " inmeta:id=" + ID);
        assertThat(builder).isEqualTo(
                boolQuery()
                        .must(freeTextQuery(ALBUM))
                        .must(new MatchQueryBuilder("id", ID))
        );
    }

    @Test
    public void testExtendedUnicodeCharacters() {
        String diacritic = "à";
        String nonLetterTerm = "[value]";
        String highUnicodeCharacter = "ـش\u200E";

        for (String term : Arrays.asList(diacritic, nonLetterTerm, highUnicodeCharacter)) {
            QueryBuilder builder = parser.visit(term);
            assertThat(builder).isEqualTo(freeTextQuery(term));
        }
    }

    @Test
    public void testValueWithEquals() {
        QueryBuilder builder = parser.visit("notAKeyWord=someOtherValue");
        assertThat(builder).isEqualTo(freeTextQuery("notAKeyWord someOtherValue"));
    }

    @Test
    public void testValueWithColon() {
        QueryBuilder builder = parser.visit("notAKeyWord:someOtherValue");
        assertThat(builder).isEqualTo(freeTextQuery("notAKeyWord someOtherValue"));
    }

    @Test
    public void testQueryValueWithColon() {
        QueryBuilder builder = parser.visit("allintext:text:more text");
        assertThat(builder).isEqualTo(boolQuery().must(freeTextQuery("text more text")));
    }

    @Test
    public void testQueryValueWithParentheses() {
        QueryBuilder builder = parser.visit("allintext:title of album (subtitle)");
        assertThat(builder).isEqualTo(boolQuery().must(freeTextQuery("title of album subtitle")));
    }

    @Test
    public void testOrAllInTextQuery() {
        QueryBuilder builder = parser.visit("allintext:Elton John OR allintext:The Beach Boys");
        assertThat(builder).isEqualTo(
                boolQuery()
                        .should(freeTextQuery("Elton John"))
                        .should(freeTextQuery("The Beach Boys"))
                        .minimumShouldMatch(1)
        );
    }

    @Test
    public void testOrInMetaQuery() {
        QueryBuilder builder = parser.visit("inmeta:id=" + ID + " OR inmeta:album=" + ALBUM);
        assertThat(builder).isEqualTo(
                boolQuery()
                        .should(new MatchQueryBuilder("id", ID))
                        .should(new MatchQueryBuilder("album", ALBUM))
                        .minimumShouldMatch(1));
    }

    @Test
    public void testMultipleOrAllInTextQuery() {
        QueryBuilder builder = parser.visit("allintext:Elton John OR The Beach Boys");
        assertThat(builder).isEqualTo(
                boolQuery().must(
                        new MultiMatchQueryBuilder("Elton John The Beach Boys", SEARCHABLE_FIELDS)
                                .operator(OR)
                                .minimumShouldMatch("1")
                                .lenient(true)));
    }

    @Test
    public void testNestedOrInMetaQuery() {
        QueryBuilder builder = parser.visit("(inmeta:id=" + ID + " OR inmeta:album=" + ALBUM + ")");
        assertThat(builder).isEqualTo(
                boolQuery()
                        .should(new MatchQueryBuilder("id", ID))
                        .should(new MatchQueryBuilder("album", ALBUM))
                        .minimumShouldMatch(1));
    }

    @Test
    public void testNestedOrInMetaQueryWithExtraClause() {
        BoolQueryBuilder expectedQuery = boolQuery()
                .must(new MatchQueryBuilder("id", ID))
                .should(new MatchQueryBuilder("information", INFORMATION))
                .should(new MatchQueryBuilder("album", ALBUM))
                .minimumShouldMatch(1);

        QueryBuilder pairAndSubQueryBuilder = parser.visit("inmeta:id=" + ID + " (inmeta:information=" + INFORMATION + " OR inmeta:album=" + ALBUM + ")");
        assertThat(pairAndSubQueryBuilder).isEqualTo(expectedQuery);

        QueryBuilder subQueryAndPairBuilder = parser.visit("(inmeta:information=" + INFORMATION + " OR inmeta:album=" + ALBUM + ") inmeta:id=" + ID);
        assertThat(subQueryAndPairBuilder).isEqualTo(expectedQuery);
    }

    @Test
    public void testPipedQuery() {
        QueryBuilder builder = parser.visit("Elton John|The Beach Boys");
        assertThat(builder).isEqualTo(freeTextQuery("Elton John The Beach Boys"));
    }

    @Test
    public void testRangeQuery() {
        QueryBuilder builder = parser.visit("inmeta:latitude:52.081719..53.081719 inmeta:longitude:4.889763..5.889763");
        assertThat(builder).isEqualTo(
                boolQuery()
                        .must(new RangeQueryBuilder("latitude").gte("52.081719").lte("53.081719"))
                        .must(new RangeQueryBuilder("longitude").gte("4.889763").lte("5.889763")));
    }

    private MultiMatchQueryBuilder freeTextQuery(String terms) {
        return new MultiMatchQueryBuilder(terms, SEARCHABLE_FIELDS).operator(AND).lenient(true);
    }
}
