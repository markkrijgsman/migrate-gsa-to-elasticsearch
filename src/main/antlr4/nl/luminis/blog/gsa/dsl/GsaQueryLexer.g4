lexer grammar GsaQueryLexer;

// GSA keywords
ALL_IN_TEXT : 'allintext:';
IN_META     : 'inmeta:';

OR          : 'OR';

/**
 * TEXT is any stream of characters up until the first occurring separating character.
 *
 * Examples:
 *  word
 *  word & word
 */
TEXT        : ~(' '|'='|':'|'|'|'('|')')+;

/**
 * These characters are excluded from the TEXT token to make sure we have proper boundaries to the token.
 * Without these, the TEXT token would always be equal to the whole input because it would be the longest possible token.
 * Since we exclude them from the TEXT token we need to ignore them here.
 */
WHITESPACE  : [ \t\r\n]+ -> skip;
IGNORED     : [=:|()]+ -> skip;
