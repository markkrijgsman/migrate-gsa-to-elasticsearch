parser grammar GsaPartialFieldsParser;

options { tokenVocab=GsaPartialFieldsLexer; }

/**
 * A query is either one (key:value) pair or several (key:value) pairs compounded using brackets and logical operators.
 *
 * Examples:
 *  (key:value)
 *  (key1:value1).(key2:value2)
 *  (key1:value1).((key2:value2)|(key3:value3))
 */
query       : pair
            | subQuery;

// Iterate all possible combinations of pairs and subqueries.
subQuery    : LEFTBRACKET subQuery RIGHTBRACKET
            | pair (AND pair)+
            | pair (OR pair)+
            | subQuery (AND subQuery)+
            | subQuery (OR subQuery)+
            | subQuery AND pair
            | subQuery OR pair
            | pair AND subQuery
            | pair OR subQuery;

/**
 * A pair is always a key/value pair enclosed by brackets, separated by a colon. The key may be negated to indicate exclusion.
 * Note that the colon is part of the value token to make token recognition easier.
 *
 * Examples:
 *  (key:value)
 *  (-key:value)
 *  ((key:value))
 */
pair        : LEFTBRACKET KEYWORD VALUE RIGHTBRACKET        #inclusionPair
            | LEFTBRACKET NOT KEYWORD VALUE RIGHTBRACKET    #exclusionPair
            | LEFTBRACKET pair RIGHTBRACKET                 #nestedPair;
