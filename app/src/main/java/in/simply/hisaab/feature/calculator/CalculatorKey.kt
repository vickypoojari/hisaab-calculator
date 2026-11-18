package `in`.simply.hisaab.feature.calculator

/** The 20 keys of the 4 × 5 keypad on the design board, in reading order. */
enum class CalculatorKey(val label: String, val spoken: String, val kind: Kind) {
    CLEAR("C", "Clear", Kind.FUNCTION),
    BACKSPACE("⌫", "Backspace", Kind.FUNCTION),
    PERCENT("%", "Percent", Kind.FUNCTION),
    DIVIDE("÷", "Divide", Kind.OPERATOR),
    D7("7", "7", Kind.DIGIT),
    D8("8", "8", Kind.DIGIT),
    D9("9", "9", Kind.DIGIT),
    MULTIPLY("×", "Multiply", Kind.OPERATOR),
    D4("4", "4", Kind.DIGIT),
    D5("5", "5", Kind.DIGIT),
    D6("6", "6", Kind.DIGIT),
    MINUS("−", "Minus", Kind.OPERATOR),
    D1("1", "1", Kind.DIGIT),
    D2("2", "2", Kind.DIGIT),
    D3("3", "3", Kind.DIGIT),
    PLUS("+", "Plus", Kind.OPERATOR),
    DOUBLE_ZERO("00", "Double zero", Kind.DIGIT),
    D0("0", "0", Kind.DIGIT),
    DECIMAL(".", "Decimal point", Kind.DIGIT),
    EQUALS("=", "Equals", Kind.EQUALS);

    enum class Kind { DIGIT, OPERATOR, FUNCTION, EQUALS }
}
