package com.cleanroommc.modularui.api.math;

import com.cleanroommc.modularui.ModularUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MathExpression {

    private static final List<Object> DEFAULT = Collections.singletonList(0);

    public static double parseMathExpression(String expr) {
        List<Object> parsed = buildParsedList(expr);
        if (parsed == DEFAULT || parsed.size() == 0) {
            return 0;
        }
        if (parsed.size() == 1) {
            Object value = parsed.get(0);
            return value instanceof Double ? (double) value : 0;
        }
        Double lastNum = null;
        for (int i = 0; i < parsed.size(); i++) {
            Object obj = parsed.get(i);
            if (lastNum == null && obj instanceof Double) {
                lastNum = (Double) obj;
                continue;
            }
            if (obj == Operator.POWER) {
                Double newNum = Math.pow(lastNum, (Double) parsed.get(i + 1));
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.add(i - 1, newNum);
                lastNum = newNum;
                i--;
                continue;
            }
            lastNum = null;
        }
        if (lastNum != null) {
            lastNum = null;
        }
        ModularUI.LOGGER.info("Parsed after pow {}", parsed);
        if (parsed.size() > 1) {
            for (int i = 0; i < parsed.size(); i++) {
                Object obj = parsed.get(i);
                if (lastNum == null && obj instanceof Double) {
                    lastNum = (Double) obj;
                    continue;
                }
                if (obj == Operator.MULTIPLY) {
                    Double newNum = lastNum * (Double) parsed.get(i + 1);
                    parsed.remove(i - 1);
                    parsed.remove(i - 1);
                    parsed.remove(i - 1);
                    parsed.add(i - 1, newNum);
                    lastNum = newNum;
                    i--;
                    continue;
                }
                if (obj == Operator.DIVIDE) {
                    Double newNum = lastNum / (Double) parsed.get(i + 1);
                    parsed.remove(i - 1);
                    parsed.remove(i - 1);
                    parsed.remove(i - 1);
                    parsed.add(i - 1, newNum);
                    lastNum = newNum;
                    i--;
                    continue;
                }
                lastNum = null;
            }
            if (lastNum != null) {
                lastNum = null;
            }
        }
        ModularUI.LOGGER.info("Parsed after dot {}", parsed);
        if (parsed.size() > 1) {
            for (int i = 0; i < parsed.size(); i++) {
                Object obj = parsed.get(i);
                if (lastNum == null && obj instanceof Double) {
                    lastNum = (Double) obj;
                    continue;
                }
                if (obj == Operator.PLUS) {
                    Double newNum = lastNum + (Double) parsed.get(i + 1);
                    parsed.remove(i - 1);
                    parsed.remove(i - 1);
                    parsed.remove(i - 1);
                    parsed.add(i - 1, newNum);
                    lastNum = newNum;
                    i--;
                    continue;
                }
                if (obj == Operator.MINUS) {
                    Double newNum = lastNum - (Double) parsed.get(i + 1);
                    parsed.remove(i - 1);
                    parsed.remove(i - 1);
                    parsed.remove(i - 1);
                    parsed.add(i - 1, newNum);
                    lastNum = newNum;
                    i--;
                    continue;
                }
                lastNum = null;
            }
        }
        ModularUI.LOGGER.info("Parsed after line {}", parsed);
        if (parsed.size() != 1) {
            throw new IllegalStateException("Calculated expr has more than 1 result. " + parsed);
        }
        return (Double) parsed.get(0);
    }

    public static List<Object> buildParsedList(String expr) {
        List<Object> parsed = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            switch (c) {
                case '+': {
                    if (builder.length() > 0) {
                        parsed.add(Double.parseDouble(builder.toString()));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.PLUS);
                    break;
                }
                case '-': {
                    if (builder.length() > 0) {
                        parsed.add(Double.parseDouble(builder.toString()));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.MINUS);
                    break;
                }
                case '*': {
                    if (builder.length() > 0) {
                        parsed.add(Double.parseDouble(builder.toString()));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.MULTIPLY);
                    break;
                }
                case '/': {
                    if (builder.length() > 0) {
                        parsed.add(Double.parseDouble(builder.toString()));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.DIVIDE);
                    break;
                }
                case '^': {
                    if (builder.length() > 0) {
                        parsed.add(Double.parseDouble(builder.toString()));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.POWER);
                    break;
                }
                default:
                    builder.append(c);
            }
        }
        if (builder.length() > 0) {
            parsed.add(Double.parseDouble(builder.toString()));
        }
        boolean shouldBeOperator = false;
        for (Object object : parsed) {
            if (shouldBeOperator) {
                if (!(object instanceof Operator)) {
                    return DEFAULT;
                }
                shouldBeOperator = false;
            } else {
                if (!(object instanceof Double)) {
                    return DEFAULT;
                }
                shouldBeOperator = true;
            }
        }
        while (parsed.get(parsed.size() - 1) instanceof Operator) {
            parsed.remove(parsed.size() - 1);
        }
        ModularUI.LOGGER.info("Parsed expr {}", parsed);
        return parsed;
    }

    public enum Operator {

        PLUS("+"), MINUS("-"), MULTIPLY("*"), DIVIDE("/"), POWER("^");
        public final String sign;

        Operator(String sign) {
            this.sign = sign;
        }

        @Override
        public String toString() {
            return sign;
        }
    }
}
