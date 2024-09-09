package com.cleanroommc.modularui.utils.math;

import com.cleanroommc.modularui.api.IMathValue;
import com.cleanroommc.modularui.utils.math.functions.Function;
import com.cleanroommc.modularui.utils.math.functions.classic.*;
import com.cleanroommc.modularui.utils.math.functions.limit.Clamp;
import com.cleanroommc.modularui.utils.math.functions.limit.Max;
import com.cleanroommc.modularui.utils.math.functions.limit.Min;
import com.cleanroommc.modularui.utils.math.functions.rounding.Ceil;
import com.cleanroommc.modularui.utils.math.functions.rounding.Floor;
import com.cleanroommc.modularui.utils.math.functions.rounding.Round;
import com.cleanroommc.modularui.utils.math.functions.rounding.Trunc;
import com.cleanroommc.modularui.utils.math.functions.string.StringContains;
import com.cleanroommc.modularui.utils.math.functions.string.StringEndsWith;
import com.cleanroommc.modularui.utils.math.functions.string.StringStartsWith;
import com.cleanroommc.modularui.utils.math.functions.trig.*;
import com.cleanroommc.modularui.utils.math.functions.utility.*;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Math builder
 * <p>
 * This class is responsible for parsing math expressions provided by
 * user in a string to an {@link IMathValue} which can be used to compute
 * some value dynamically using different math operators, variables and
 * functions.
 * <p>
 * It works by first breaking down given string into a list of tokens
 * and then putting them together in a binary tree-like {@link IMathValue}.
 * <p>
 * TODO: maybe implement constant pool (to reuse same values)?
 * TODO: maybe pre-compute constant expressions?
 */
public class MathBuilder {

    public static final Pattern DECIMAL_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?([eE]-?\\d+)?");

    public static final MathBuilder INSTANCE = new MathBuilder();

    /**
     * Named variables that can be used in math expression by this
     * builder
     */
    public final Map<String, Variable> variables = new Object2ObjectOpenHashMap<>();

    /**
     * Map of functions which can be used in the math expressions
     */
    public final Map<String, Class<? extends Function>> functions = new Object2ObjectOpenHashMap<>();

    /**
     * Whether math expression parser should be strict about which characters
     * can be used within math expressions
     */
    protected boolean strict = true;

    public MathBuilder() {
        /* Some default values */
        this.register(new Variable("PI", Math.PI));
        this.register(new Variable("NAPIER", Math.E));

        /* Rounding functions */
        this.functions.put("floor", Floor.class);
        this.functions.put("round", Round.class);
        this.functions.put("ceil", Ceil.class);
        this.functions.put("trunc", Trunc.class);

        /* Selection and limit functions */
        this.functions.put("clamp", Clamp.class);
        this.functions.put("max", Max.class);
        this.functions.put("min", Min.class);

        /* Classical functions */
        this.functions.put("abs", Abs.class);
        this.functions.put("exp", Exp.class);
        this.functions.put("ln", Ln.class);
        this.functions.put("sqrt", Sqrt.class);
        this.functions.put("mod", Mod.class);
        this.functions.put("pow", Pow.class);

        /* Trig functions */
        this.functions.put("cos", Cos.class);
        this.functions.put("sin", Sin.class);
        this.functions.put("acos", Acos.class);
        this.functions.put("asin", Asin.class);
        this.functions.put("atan", Atan.class);
        this.functions.put("atan2", Atan2.class);

        /* Utility functions */
        this.functions.put("lerp", Lerp.class);
        this.functions.put("lerprotate", LerpRotate.class);
        this.functions.put("random", Random.class);
        this.functions.put("randomi", RandomInteger.class);
        this.functions.put("roll", DieRoll.class);
        this.functions.put("rolli", DieRollInteger.class);
        this.functions.put("hermite", HermiteBlend.class);

        /* String functions */
        this.functions.put("str_contains", StringContains.class);
        this.functions.put("str_starts", StringStartsWith.class);
        this.functions.put("str_ends", StringEndsWith.class);
    }

    public MathBuilder lenient() {
        this.strict = false;

        return this;
    }

    /**
     * Register a variable
     */
    public void register(Variable variable) {
        this.variables.put(variable.getName(), variable);
    }

    /**
     * Parse given math expression into a {@link IMathValue} which can be
     * used to execute math.
     */
    public IMathValue parse(String expression) throws ParseException {
        return this.parseSymbols(this.breakdownChars(this.breakdown(expression)));
    }

    /**
     * Breakdown an expression
     */
    public String[] breakdown(String expression) throws ParseException {
        /* If given string have illegal characters, then it can't be parsed */
        if (this.strict && !expression.matches("^[\\w\\s_+-/*%^&|<>=!?:.,()\"'@~\\[\\]]+$")) {
            throw new ParseException("Given expression '" + expression + "' contains illegal characters!");
        }

        String[] chars = expression.split("(?!^)");

        int left = 0;
        int right = 0;

        for (String s : chars) {
            if (s.equals("(")) {
                left++;
            } else if (s.equals(")")) {
                right++;
            }
        }

        /* Amount of left and right brackets should be the same */
        if (left != right) {
            // TODO: auto pre- or append ( or )
            throw new ParseException("Given expression '" + expression + "' has more uneven amount of parenthesis, there are " + left + " open and " + right + " closed!");
        }

        return chars;
    }

    /**
     * Breakdown characters into a list of math expression symbols.
     */
    public List<Object> breakdownChars(String[] chars) {
        List<Object> symbols = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        int len = chars.length;
        boolean string = false;

        for (int i = 0; i < len; i++) {
            String s = chars[i];
            boolean longOperator = i < chars.length - 1 && this.isOperator(s + chars[i + 1]);

            if (s.equals("\"")) {
                string = !string;
            }

            if (string) {
                buffer.append(s);
            } else if (this.isOperator(s) || longOperator || s.equals(",")) {
                /* Taking care of a special case of using minus sign to
                 * invert the positive value */
                if (s.equals("-")) {
                    int size = symbols.size();

                    boolean isEmpty = buffer.toString().trim().isEmpty();
                    boolean isFirst = size == 0 && isEmpty;
                    boolean isOperatorBehind = size > 0 && (this.isOperator(symbols.get(size - 1)) || symbols.get(size - 1).equals(",")) && isEmpty;

                    if (isFirst || isOperatorBehind) {
                        buffer.append(s);

                        continue;
                    }
                }

                /* Push buffer and operator */
                if (buffer.length() > 0) {
                    symbols.add(buffer.toString());
                    buffer = new StringBuilder();
                }

                if (longOperator) {
                    symbols.add(s + chars[i + 1]);
                    i += 1;
                } else {
                    symbols.add(s);
                }
            } else if (s.equals("(")) {
                /* Push a list of symbols */
                if (buffer.length() > 0) {
                    symbols.add(buffer.toString());
                    buffer = new StringBuilder();
                }

                int counter = 1;

                for (int j = i + 1; j < len; j++) {
                    String c = chars[j];

                    if (c.equals("(")) {
                        counter++;
                    } else if (c.equals(")")) {
                        counter--;
                    }

                    if (counter == 0) {
                        symbols.add(this.breakdownChars(buffer.toString().split("(?!^)")));

                        i = j;
                        buffer = new StringBuilder();

                        break;
                    } else {
                        buffer.append(c);
                    }
                }
            } else {
                /* Accumulate the buffer */
                buffer.append(s);
            }
        }

        if (buffer.length() > 0) {
            symbols.add(buffer.toString());
        }

        return this.trimSymbols(symbols);
    }

    /**
     * Trims spaces from individual symbols
     */
    private List<Object> trimSymbols(List<?> symbols) {
        List<Object> newSymbols = new ArrayList<>();

        for (Object value : symbols) {
            if (value instanceof String string) {
                string = string.trim();

                if (!string.isEmpty()) {
                    newSymbols.add(string);
                }
            } else {
                newSymbols.add(this.trimSymbols((List<?>) value));
            }
        }

        return newSymbols;
    }

    /**
     * Parse symbols
     * <p>
     * This function is the most important part of this class. It's
     * responsible for turning list of symbols into {@link IMathValue}. This
     * is done by constructing a binary tree-like {@link IMathValue} based on
     * {@link Operator} class.
     * <p>
     * However, beside parsing operations, it's also can return one or
     * two item sized symbol lists.
     */
    public IMathValue parseSymbols(List<?> symbols) throws ParseException {
        IMathValue ternary = this.tryTernary(symbols);

        if (ternary != null) {
            return ternary;
        }

        int size = symbols.size();

        /* Constant, variable or group (parenthesis) */
        if (size == 1) {
            return this.valueFromObject(symbols.get(0));
        }

        /* Function */
        if (size == 2) {
            Object first = symbols.get(0);
            Object second = symbols.get(1);

            if ((this.isVariable(first) || first.equals("-")) && second instanceof List<?> list) {
                return this.createFunction((String) first, list);
            }
        }

        /* Any other math expression */
        int lastOp = this.seekLastOperator(symbols);
        int op = lastOp;

        while (op != -1) {
            int leftOp = this.seekLastOperator(symbols, op - 1);

            if (leftOp != -1) {
                Operation left = this.operationForOperator((String) symbols.get(leftOp));
                Operation right = this.operationForOperator((String) symbols.get(op));

                if (right.value > left.value) {
                    IMathValue leftValue = this.parseSymbols(symbols.subList(0, leftOp));
                    IMathValue rightValue = this.parseSymbols(symbols.subList(leftOp + 1, size));

                    return new Operator(left, leftValue, rightValue);
                } else if (left.value > right.value) {
                    Operation initial = this.operationForOperator((String) symbols.get(lastOp));

                    if (initial.value < left.value) {
                        IMathValue leftValue = this.parseSymbols(symbols.subList(0, lastOp));
                        IMathValue rightValue = this.parseSymbols(symbols.subList(lastOp + 1, size));

                        return new Operator(initial, leftValue, rightValue);
                    }

                    IMathValue leftValue = this.parseSymbols(symbols.subList(0, op));
                    IMathValue rightValue = this.parseSymbols(symbols.subList(op + 1, size));

                    return new Operator(right, leftValue, rightValue);
                }
            }

            op = leftOp;
        }

        Operation operation = this.operationForOperator((String) symbols.get(lastOp));

        return new Operator(operation, this.parseSymbols(symbols.subList(0, lastOp)), this.parseSymbols(symbols.subList(lastOp + 1, size)));
    }

    protected int seekLastOperator(List<?> symbols) {
        return this.seekLastOperator(symbols, symbols.size() - 1);
    }

    /**
     * Find the index of the first operator
     */
    protected int seekLastOperator(List<?> symbols, int offset) {
        for (int i = offset; i >= 0; i--) {
            Object o = symbols.get(i);

            if (this.isOperator(o)) {
                /* - before a group isn't considered an operator per se */
                if (o.equals("-")) {
                    Object next = i < symbols.size() - 1 ? symbols.get(i + 1) : null;
                    Object prev = i > 0 ? symbols.get(i - 1) : null;

                    if (next instanceof List && (this.isOperator(prev) || prev == null)) {
                        continue;
                    }
                }

                return i;
            }
        }

        return -1;
    }

    /**
     * Try parsing a ternary expression
     * <p>
     * From what we know, with ternary expressions, we should have only one ? and :,
     * and some elements from beginning till ?, in between ? and :, and also some
     * remaining elements after :.
     */
    protected IMathValue tryTernary(List<?> symbols) throws ParseException {
        int question = -1;
        int questions = 0;
        int colon = -1;
        int colons = 0;
        int size = symbols.size();

        for (int i = 0; i < size; i++) {
            Object object = symbols.get(i);

            if (object instanceof String) {
                if (object.equals("?")) {
                    if (question == -1) {
                        question = i;
                    }

                    questions++;
                } else if (object.equals(":")) {
                    if (colons + 1 == questions && colon == -1) {
                        colon = i;
                    }

                    colons++;
                }
            }
        }

        if (questions == colons && question > 0 && question + 1 < colon && colon < size - 1) {
            return new Ternary(
                    this.parseSymbols(symbols.subList(0, question)),
                    this.parseSymbols(symbols.subList(question + 1, colon)),
                    this.parseSymbols(symbols.subList(colon + 1, size))
            );
        }

        return null;
    }

    /**
     * Create a function value
     * <p>
     * This method in comparison to {@link #valueFromObject(Object)}
     * needs the name of the function and list of args (which can't be
     * stored in one object).
     * <p>
     * This method will construct {@link IMathValue}s from list of args
     * mixed with operators, groups, values and commas. And then plug it
     * in to a class constructor with given name.
     */
    protected IMathValue createFunction(String first, List<?> args) throws ParseException {
        /* Handle special cases with negation */
        if (first.equals("!")) {
            return new Negate(this.parseSymbols(args));
        }

        if (first.startsWith("!") && first.length() > 1) {
            return new Negate(this.createFunction(first.substring(1), args));
        }

        /* Handle inversion of the value */
        if (first.equals("-")) {
            return new Negative(new Group(this.parseSymbols(args)));
        }

        if (first.startsWith("-") && first.length() > 1) {
            return new Negative(this.createFunction(first.substring(1), args));
        }

        if (!this.functions.containsKey(first)) {
            throw new ParseException("Function '" + first + "' couldn't be found!");
        }

        List<IMathValue> values = new ArrayList<>();
        List<Object> buffer = new ArrayList<>();

        for (Object o : args) {
            if (o.equals(",")) {
                values.add(this.parseSymbols(buffer));
                buffer.clear();
            } else {
                buffer.add(o);
            }
        }

        if (!buffer.isEmpty()) {
            values.add(this.parseSymbols(buffer));
        }

        Class<? extends Function> function = this.functions.get(first);
        Constructor<? extends Function> ctor;
        try {
            ctor = function.getConstructor(IMathValue[].class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            return ctor.newInstance(values.toArray(new IMathValue[0]), first);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get value from an object.
     * <p>
     * This method is responsible for creating different sort of values
     * based on the input object. It can create constants, variables and
     * groups.
     */
    @SuppressWarnings("unchecked")
    public IMathValue valueFromObject(Object object) throws ParseException {
        if (object instanceof String symbol) {

            /* Variable and constant negation */
            if (symbol.startsWith("!")) {
                return new Negate(this.valueFromObject(symbol.substring(1)));
            }

            if (symbol.startsWith("\"") && symbol.endsWith("\"")) {
                return new Constant(symbol.substring(1, symbol.length() - 1));
            }

            symbol = trimThousandSeparator(symbol);
            Double triedNumber = tryParseNumber(symbol);

            if (triedNumber != null) {
                return new Constant(triedNumber);
            } else if (this.isVariable(symbol)) {
                /* Need to account for a negative value variable */
                if (symbol.startsWith("-")) {
                    symbol = symbol.substring(1);
                    Variable value = this.getVariable(symbol);

                    if (value != null) {
                        return new Negative(value);
                    }
                } else {
                    IMathValue value = this.getVariable(symbol);

                    /* Avoid NPE */
                    if (value != null) {
                        return value;
                    }
                }
            }
        } else if (object instanceof List<?> list) {
            return new Group(this.parseSymbols(list));
        }

        throw new ParseException("Given object couldn't be converted to value! " + object);
    }

    protected String trimThousandSeparator(String symbol) {
        return symbol.replace(" ", "")
                .replace("\u202F", "") // French locale
                .replace("_", "");
    }

    protected Double tryParseNumber(String symbol) throws ParseException {
        final StringBuilder buffer = new StringBuilder();
        final char[] characters = symbol.toCharArray();
        Double leftNumber = null;
        for (int i = 0; i < characters.length; i++) {
            char c = characters[i];
            double multiplier = switch (c) {
                case 'k', 'K' -> 1_000;
                case 'm', 'M' -> 1_000_000;
                case 'g', 'G', 'b', 'B' -> 1_000_000_000;
                case 't', 'T' -> 1_000_000_000_000L;
                case 's', 'S' -> 64;
                case 'i', 'I' -> 144;
                default -> 0;
            };
            final boolean isENotation = c == 'e' || c == 'E';
            if (multiplier == 0 && !isENotation) {
                // Not a suffix nor E notation
                if (leftNumber != null) {
                    // Something like 2k5 cannot be parsed
                    throw new ParseException(String.format("Symbol %s cannot be parsed!", symbol));
                }
                buffer.append(c);
            } else {
                if (leftNumber == null) {
                    // We haven't seen number so far, so try parse it
                    final String buffered = buffer.toString();
                    try {
                        leftNumber = Double.parseDouble(buffered);
                    } catch (NumberFormatException ignored) {
                        // Don't throw error here, as it could be variable name
                        return null;
                    }
                }
                if (isENotation) {
                    final String rightString = symbol.substring(i + 1);
                    final double rightNumber;
                    try {
                        rightNumber = Double.parseDouble(rightString);
                    } catch (NumberFormatException ignored) {
                        // Left number is guaranteed to be nonnull here, and we don't have variable like 4ER
                        throw new ParseException(String.format("Symbol %s cannot be parsed!", symbol));
                    }
                    return leftNumber * Math.pow(10, rightNumber);
                } else {
                    // Continue parsing to allow 2kk == 2000k for example
                    leftNumber *= multiplier;
                }
            }
        }
        if (leftNumber != null) {
            return leftNumber;
        }
        try {
            return Double.parseDouble(symbol);
        } catch (NumberFormatException ignored) {
            throw new ParseException(String.format("Symbol %s cannot be parsed!", symbol));
        }
    }

    /**
     * Get variable
     */
    @Nullable
    protected Variable getVariable(String name) {
        return this.variables.get(name);
    }

    /**
     * Get operation for given operator strings
     */
    protected Operation operationForOperator(String op) throws ParseException {
        for (Operation operation : Operation.values()) {
            if (operation.sign.equals(op)) {
                return operation;
            }
        }

        throw new ParseException("There is no such operator '" + op + "'!");
    }

    /**
     * Whether given object is a variable
     */
    protected boolean isVariable(Object o) {
        return o instanceof String string && !this.isDecimal(string) && !this.isOperator(string);
    }

    protected boolean isOperator(Object o) {
        return o instanceof String string && this.isOperator(string);
    }

    /**
     * Whether string is an operator
     */
    protected boolean isOperator(String s) {
        return Operation.OPERATORS.contains(s) || s.equals("?") || s.equals(":");
    }

    /**
     * Whether string is numeric (including whether it's a floating
     * number)
     */
    protected boolean isDecimal(String s) {
        return DECIMAL_PATTERN.matcher(s).matches();
    }
    
    public static class ParseException extends Exception {

        public ParseException(String message) {
            super(message);
        }
    }
}