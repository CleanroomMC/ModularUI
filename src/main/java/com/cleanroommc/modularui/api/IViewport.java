package com.cleanroommc.modularui.api;

public interface IViewport {

    void apply(IViewportStack stack);

    void unapply(IViewportStack stack);
}