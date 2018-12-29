package nl.luminis.blog.gsa.dsl;

import org.springframework.stereotype.Service;

// The GSA works with 1 and 0 rather than true and false, map it to values that Elasticsearch can work with.
@Service
public class BooleanConverter {

    public String convert(String input) {
        switch (input) {
            case "1":
                return "true";
            case "0":
                return "false";
            default:
                return input;
        }
    }
}
