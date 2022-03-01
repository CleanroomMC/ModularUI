package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.common.internal.UIBuildContext;

public interface ITileWithModularUI {

    ModularWindow createWindow(UIBuildContext buildContext);
}
