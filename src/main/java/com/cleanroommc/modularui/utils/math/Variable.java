package com.cleanroommc.modularui.utils.math;

/**
 * Variable class
 * <p>
 * This class is responsible for providing a mutable {@link IValue}
 * which can be modifier during runtime and still getting referenced in
 * the expressions parsed by {@link MathBuilder}.
 * <p>
 * But in practice, it's simply returns stored value and provides a
 * method to modify it.
 */
public class Variable extends Constant {

    private final String name;

    public Variable(String name, double value) {
        super(value);

        this.name = name;
    }

    public Variable(String name, String value) {
        super(value);

        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}