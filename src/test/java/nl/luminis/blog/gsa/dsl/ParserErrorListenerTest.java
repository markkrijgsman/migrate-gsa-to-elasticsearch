package nl.luminis.blog.gsa.dsl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Test;

public class ParserErrorListenerTest {

    @Test(expected = ParseCancellationException.class)
    public void testSyntaxError() {
        new ParserErrorListener().syntaxError(null, createToken("query"), 1, 5, "error message", null);
    }

    private Token createToken(String query) {
        Token token = mock(Token.class);
        CharStream stream = mock(CharStream.class);

        when(token.getInputStream()).thenReturn(stream);
        when(stream.toString()).thenReturn(query);

        return token;
    }
}
