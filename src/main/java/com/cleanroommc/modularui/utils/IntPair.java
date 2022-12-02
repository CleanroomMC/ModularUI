package com.cleanroommc.modularui.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IntPair implements Comparable<IntPair>, Cloneable {

    public static final IntPair ZERO = new IntPair(0, 0);

    private int left, right;

    public IntPair(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public IntPair(IntPair intPair) {
        this(intPair.left, intPair.right);
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    void setLeft(int left) {
        this.left = left;
    }

    void setRight(int right) {
        this.right = right;
    }

    public IntPair copyAdd(int left, int right) {
        return new IntPair(this.left + left, this.right + right);
    }

    public IntPair copySubtract(int left, int right) {
        return new IntPair(this.left - left, this.right - right);
    }

    public IntPair copyMultiply(int left, int right) {
        return new IntPair(this.left * left, this.right * right);
    }

    public IntPair copyDivide(int left, int right) {
        return new IntPair(this.left / left, this.right / right);
    }

    public IntPair copy() {
        return new IntPair(this);
    }

    public Mut copyMutable() {
        return new Mut(this);
    }

    public IntPair toImmutable() {
        return this instanceof Mut ? copy() : this;
    }

    public Mut toMutable() {
        return this instanceof Mut ? (Mut) this : copyMutable();
    }

    public boolean isZero() {
        return left == 0 && right == 0;
    }

    @Override
    public String toString() {
        return "IntPair(" + left +
                ", " + right +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntPair intPair = (IntPair) o;
        return left == intPair.left && right == intPair.right;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public int compareTo(@NotNull IntPair o) {
        int r = Integer.compare(left, o.left);
        return r == 0 ? Integer.compare(right, o.right) : r;
    }

    public static class Mut extends IntPair {

        public Mut(int left, int right) {
            super(left, right);
        }

        public Mut(IntPair intPair) {
            this(intPair.left, intPair.right);
        }

        public Mut() {
            this(0, 0);
        }

        public void set(int left, int right) {
            setLeft(left);
            setRight(right);
        }

        public void setLeft(int left) {
            super.setLeft(left);
        }

        public void setRight(int right) {
            super.setRight(right);
        }

        public Mut add(int left, int right) {
            setLeft(getLeft() + left);
            setRight(getRight() + right);
            return this;
        }

        public Mut subtract(int left, int right) {
            setLeft(getLeft() - left);
            setRight(getRight() - right);
            return this;
        }

        public Mut multiply(int left, int right) {
            setLeft(getLeft() * left);
            setRight(getRight() * right);
            return this;
        }

        public Mut divide(int left, int right) {
            setLeft(getLeft() / left);
            setRight(getRight() / right);
            return this;
        }

        @Override
        public String toString() {
            return "IntPair.Mut(" + getLeft() +
                    ", " + getRight() +
                    ')';
        }
    }
}
