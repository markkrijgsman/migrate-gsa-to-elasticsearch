package nl.luminis.blog.gsa.dsl;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class ParserErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        String message;
        if (offendingSymbol != null) {
            Token token = (Token) offendingSymbol;
            String query = token.getInputStream().toString();
            message = String.format("Error occurred at line %s:%s in query \"%s\" (%s)", line, charPositionInLine, query, msg);
        } else {
            message = String.format("Error occurred at line %s:%s (%s)", line, charPositionInLine, msg);
        }
        throw new ParseCancellationException(message, e);
    }
}
