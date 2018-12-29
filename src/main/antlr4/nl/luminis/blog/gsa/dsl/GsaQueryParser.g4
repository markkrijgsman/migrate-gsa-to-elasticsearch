parser grammar GsaQueryParser;

options { tokenVocab=GsaQueryLexer; }

/**
 * A query can contain plain values, a key-value pair, or several key-value pairs separated with spaces and optional OR clauses.
 * A key is either 'allintext' or 'inmeta'.
 * The value of a key-value pair is either free text or a range in the form of DOUBLE..DOUBLE
 *
 * Examples:
 *  value1 value2
 *  allintext: value
 *  allintext: value1 value2
 *  allintext: value1 OR value2
 *  allintext: value1 inmeta:key=value
 *  inmeta:key1=value1 OR inmeta:longitude_key:50.00..51.00
 */
query   : pair (OR? pair)*  #pairQuery
        | TEXT+             #freeTextQuery;

pair    : IN_META TEXT+                 #inmetaPair
        | ALL_IN_TEXT TEXT (OR? TEXT)*  #allintextPair;
