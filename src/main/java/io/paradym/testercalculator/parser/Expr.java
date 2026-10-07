package io.paradym.testercalculator.parser;

public sealed interface Expr {

    double evaluate();

    record Num(double value) implements Expr {
        @Override
        public double evaluate() {
            return value;
        }
    }

    record Negate(Expr operand) implements Expr {
        @Override
        public double evaluate() {
            return -operand.evaluate();
        }
    }

    record Binary(char operator, Expr left, Expr right) implements Expr {
        @Override
        public double evaluate() {
            double l = left.evaluate();
            double r = right.evaluate();
            return switch (operator) {
                case '+' -> l + r;
                case '-' -> l - r;
                case '*' -> l * r;
                case '/' -> divide(l, r);
                default -> throw new IllegalStateException("Unknown operator " + operator);
            };
        }

        private static double divide(double l, double r) {
            double result = l / r;
            if (r != 0 && l == Math.rint(l) && r == Math.rint(r)) {
                return (double) (long) result;
            }
            return result;
        }
    }
}
