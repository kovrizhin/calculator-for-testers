package io.paradym.testercalculator.parser;

public record Token(TokenType type, String text, int position) {

    public enum TokenType {
        NUMBER, PLUS, MINUS, STAR, SLASH, LPAREN, RPAREN, END
    }
}
