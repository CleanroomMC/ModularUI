package com.cleanroommc.modularui.utils;

import com.ezylang.evalex.Expression;

import java.math.BigDecimal;

public enum SIPrefix {

    Infinite('∞', Integer.MAX_VALUE),
    Quetta('Q', 30),
    Ronna('R', 27),
    Yotta('Y', 24),
    Zetta('Z', 21),
    Exa('X', 18), // this should actually be E, but this clashes with euler's number e = 2.71...
    Peta('P', 15),
    Tera('T', 12),
    Giga('G', 9),
    Mega('M', 6),
    Kilo('k', 3),
    One(Character.MIN_VALUE, 0),
    Milli('m', -3),
    Micro('µ', -6),
    Nano('n', -9),
    Pico('p', -12),
    Femto('f', -15),
    Atto('a', -18),
    Zepto('z', -21),
    Yocto('y', -24),
    Ronto('r', -27),
    Quecto('q', -30),
    Infinitesimal('∞', Integer.MIN_VALUE);

    public final char symbol;
    public final String stringSymbol;
    public final double factor;
    public final double oneOverFactor;
    public final BigDecimal bigFactor;
    public final BigDecimal bigOneOverFactor;
    public final boolean infiniteLike;

    SIPrefix(char symbol, int powerOfTen) {
        this.symbol = symbol;
        this.stringSymbol = symbol != Character.MIN_VALUE ? Character.toString(symbol) : "";
        this.infiniteLike = powerOfTen == Integer.MAX_VALUE || powerOfTen == Integer.MIN_VALUE;
        if (this.infiniteLike) {
            this.factor = Double.MAX_VALUE;
            this.bigFactor = new BigDecimal(Double.MAX_VALUE);
        } else {
            this.factor = Math.pow(10, powerOfTen);
            this.bigFactor = new BigDecimal(this.factor);
        }
        this.oneOverFactor = 1 / this.factor;
        this.bigOneOverFactor = new BigDecimal(this.oneOverFactor);
    }

    public boolean isOne() {
        return this == One;
    }

    public void addToExpression(Expression e) {
        e.with(String.valueOf(this.symbol), this.factor);
    }

    public static final SIPrefix[] VALUES = values();
    public static final SIPrefix[] HIGH = new SIPrefix[values().length / 2];
    public static final SIPrefix[] LOW = new SIPrefix[values().length / 2];

    static {
        SIPrefix[] values = values();
        for (int i = 0; i < HIGH.length; i++) {
            HIGH[i] = values[HIGH.length - 1 - i];
            LOW[i] = values[HIGH.length + 1 + i];
        }
    }

    public static void addAllToExpression(Expression e) {
        for (SIPrefix siPrefix : VALUES) {
            siPrefix.addToExpression(e);
        }
    }
}
