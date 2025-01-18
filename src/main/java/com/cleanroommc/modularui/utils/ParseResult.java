package com.cleanroommc.modularui.utils;

import org.jetbrains.annotations.NotNull;

public class ParseResult {

    private final double result;
    private final String error;

    public static ParseResult success(double result) {
        return new ParseResult(result, null);
    }

    public static ParseResult failure(@NotNull String error) {
        return failure(Double.NaN, error);
    }

    public static ParseResult failure(double value, @NotNull String error) {
        return new ParseResult(value, error);
    }

    private ParseResult(double result, String error) {
        this.result = result;
        this.error = error;
    }

    public boolean isSuccess() {
        return this.error == null;
    }

    public boolean isFailure() {
        return this.error != null;
    }

    public boolean hasValue() {
        return !Double.isNaN(this.result);
    }

    public double getResult() {
        return result;
    }

    public String getError() {
        return error;
    }
}
