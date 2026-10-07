package io.paradym.testercalculator.parser;

import io.paradym.testercalculator.parser.Token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (Character.isDigit(c) || c == '.') {
                int start = i;
                boolean dotSeen = false;
                while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    if (input.charAt(i) == '.') {
                        if (dotSeen) {
                            throw new EquationException("Malformed number", i);
                        }
                        dotSeen = true;
                    }
                    i++;
                }
                String text = input.substring(start, i);
                if (text.equals(".")) {
                    throw new EquationException("Malformed number", start);
                }
                tokens.add(new Token(TokenType.NUMBER, text, start));
                continue;
            }
            TokenType type = switch (c) {
                case '+' -> TokenType.PLUS;
                case '-' -> TokenType.MINUS;
                case '*' -> TokenType.STAR;
                case '/' -> TokenType.SLASH;
                case '(' -> TokenType.LPAREN;
                case ')' -> TokenType.RPAREN;
                default -> throw new EquationException("Unexpected character '" + c + "'", i);
            };
            tokens.add(new Token(type, String.valueOf(c), i));
            i++;
        }
        tokens.add(new Token(TokenType.END, "", input.length()));
        return tokens;
    }
}
