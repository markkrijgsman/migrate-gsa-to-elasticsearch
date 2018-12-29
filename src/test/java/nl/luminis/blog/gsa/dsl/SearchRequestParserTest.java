package nl.luminis.blog.gsa.dsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.luminis.blog.gsa.dsl.partialfields.PartialFieldsParser;
import nl.luminis.blog.gsa.dsl.query.QueryParser;

@RunWith(MockitoJUnitRunner.class)
public class SearchRequestParserTest {

    @InjectMocks
    private SearchRequestParser parser;
    @Mock
    private PartialFieldsParser partialFieldsParser;
    @Mock
    private QueryParser queryParser;

    @Test
    public void testparse() {
        QueryBuilder partialFieldsResult = mock(QueryBuilder.class);
        QueryBuilder queryResult = mock(QueryBuilder.class);

        when(partialFieldsParser.visit("(key:value)")).thenReturn(partialFieldsResult);
        when(queryParser.visit("allintext:value")).thenReturn(queryResult);

        QueryBuilder builder = parser.parse("allintext:value", "(key:value)");

        assertThat(builder).isEqualTo(
                boolQuery()
                        .must(partialFieldsResult)
                        .must(queryResult)
        );
    }

    @Test
    public void testEmptyPartialFieldsIsIgnored() {
        when(queryParser.visit(anyString())).thenReturn(mock(QueryBuilder.class));

        parser.parse("allintext:value", null);
        parser.parse("allintext:value", "");
        parser.parse("allintext:value", " ");

        verify(queryParser, times(3)).visit("allintext:value");
        verify(partialFieldsParser, times(0)).visit(anyString());
    }

    @Test
    public void testEmptyQueryIsIgnored() {
        when(partialFieldsParser.visit(anyString())).thenReturn(mock(QueryBuilder.class));

        parser.parse(null, "(key:value)");
        parser.parse("", "(key:value)");
        parser.parse(" ", "(key:value)");

        verify(queryParser, times(0)).visit(anyString());
        verify(partialFieldsParser, times(3)).visit("(key:value)");
    }
}
