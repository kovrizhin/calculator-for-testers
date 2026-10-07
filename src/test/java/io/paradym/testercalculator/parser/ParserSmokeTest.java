package io.paradym.testercalculator.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParserSmokeTest {

    private final Parser parser = new Parser();

    @ParameterizedTest
    @CsvSource({
            "2+3*4, 14",
            "(1+2)*3, 9",
            "6+7+(3+4*5) * 3, 82",
            "2.5*2, 5",
            "-(2+3), -5"
    })
    void shouldEvaluate(String equation, double expected) {
        assertThat(parser.parse(equation).evaluate()).isEqualTo(expected);
    }

    @Test
    void shouldRejectDoubleOperator() {
        assertThatThrownBy(() -> parser.parse("2++3"))
                .isInstanceOf(EquationException.class);
    }

    @Test
    void shouldRejectUnknownCharacter() {
        assertThatThrownBy(() -> parser.parse("2^3"))
                .isInstanceOf(EquationException.class)
                .extracting("position")
                .isEqualTo(1);
    }
}
