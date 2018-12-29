lexer grammar GsaPartialFieldsLexer;

// Logical operators.
AND         : '.';
OR          : '|';
NOT         : '-';

/**
 * A KEYWORD is a concatenation of one or more alphanumeric characters. Possibly separated by, but not starting with, dashes and dots.
 *
 * Examples:
 *  word
 *  word0
 *  0000
 *  dash-cased-word
 *  word.keyword
 */
KEYWORD     : [A-z0-9]([A-z0-9]|'-'|'.')*;
/**
 * A VALUE is any stream of characters starting with a colon (':') up until the first occurring ')'.
 * The colon is included in this token because it is the only way to reliably distinguish a VALUE token from a KEYWORD token.
 * The ~ operator negates the character set to include anything but ')'.
 *
 * Examples:
 *  :word
 *  :word & word
 */
VALUE       : SEPARATOR~(')')+;

SEPARATOR   : [:];
LEFTBRACKET : [(];
RIGHTBRACKET: [)];
WHITESPACE  : [\t\r\n]+ -> skip;
