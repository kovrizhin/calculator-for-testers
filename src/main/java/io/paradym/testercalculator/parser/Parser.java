package io.paradym.testercalculator.parser;

import io.paradym.testercalculator.parser.Token.TokenType;

import java.util.List;

public class Parser {

    private final Tokenizer tokenizer = new Tokenizer();
    private List<Token> tokens;
    private int index;

    public Expr parse(String input) {
        tokens = tokenizer.tokenize(input);
        index = 0;
        if (peek().type() == TokenType.END) {
            throw new EquationException("Equation is empty", 0);
        }
        Expr expr = parseExpression();
        if (peek().type() != TokenType.END) {
            throw new EquationException("Unexpected token '" + peek().text() + "'", peek().position());
        }
        return expr;
    }

    private Expr parseExpression() {
        Expr left = parseTerm();
        while (true) {
            Token token = peek();
            if (token.type() == TokenType.PLUS) {
                next();
                if (peek().type() == TokenType.END) {
                    return left;
                }
                left = new Expr.Binary('+', left, parseTerm());
            } else if (token.type() == TokenType.MINUS) {
                next();
                if (peek().type() == TokenType.END) {
                    return left;
                }
                return new Expr.Binary('-', left, parseExpression());
            } else {
                return left;
            }
        }
    }

    private Expr parseTerm() {
        Expr left = parseFactor();
        while (true) {
            Token token = peek();
            if (token.type() == TokenType.STAR) {
                next();
                if (peek().type() == TokenType.END) {
                    return left;
                }
                left = new Expr.Binary('*', left, parseFactor());
            } else if (token.type() == TokenType.SLASH) {
                next();
                if (peek().type() == TokenType.END) {
                    return left;
                }
                left = new Expr.Binary('/', left, parseFactor());
            } else {
                return left;
            }
        }
    }

    private Expr parseFactor() {
        Token token = peek();
        switch (token.type()) {
            case NUMBER -> {
                next();
                return new Expr.Num(Double.parseDouble(token.text()));
            }
            case MINUS -> {
                next();
                return new Expr.Negate(parseFactor());
            }
            case LPAREN -> {
                next();
                Expr inner = parseExpression();
                if (peek().type() != TokenType.RPAREN) {
                    throw new EquationException("Expected ')'", peek().position());
                }
                next();
                return inner;
            }
            case END -> throw new EquationException("Unexpected end of equation", token.position());
            default -> throw new EquationException("Unexpected token '" + token.text() + "'", token.position());
        }
    }

    private Token peek() {
        return tokens.get(index);
    }

    private void next() {
        index++;
    }
}
