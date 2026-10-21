package com.spe.calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorTest {

    private final Calculator calc = new Calculator();

    @Test
    void addReturnsSum() {
        assertEquals(5.0, calc.add(2, 3));
    }

    @Test
    void addHandlesNegatives() {
        assertEquals(-1.0, calc.add(2, -3));
    }

    @Test
    void subtractReturnsDifference() {
        assertEquals(-1.0, calc.subtract(2, 3));
    }

    @Test
    void multiplyReturnsProduct() {
        assertEquals(6.0, calc.multiply(2, 3));
    }

    @Test
    void multiplyByZeroIsZero() {
        assertEquals(0.0, calc.multiply(5, 0));
    }

    @Test
    void divideReturnsQuotient() {
        assertEquals(2.5, calc.divide(5, 2));
    }

    @Test
    void divideByZeroThrows() {
        ArithmeticException ex = assertThrows(ArithmeticException.class, () -> calc.divide(10, 0));
        assertEquals("Division by zero is not allowed", ex.getMessage());
    }
}
