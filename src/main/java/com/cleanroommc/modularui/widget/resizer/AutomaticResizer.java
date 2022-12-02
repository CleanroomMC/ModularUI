package com.cleanroommc.modularui.widget.resizer;

import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.IResizer;

import java.util.ArrayList;
import java.util.List;

public abstract class AutomaticResizer extends BaseResizer {

    public IWidget parent;
    public int margin;
    public int padding;
    public int height;

    public AutomaticResizer(IWidget parent, int margin) {
        this.parent = parent;
        this.margin = margin;

        this.setup();
    }

    /* Standard properties */

    public AutomaticResizer padding(int padding) {
        this.padding = padding;

        return this;
    }

    public AutomaticResizer height(int height) {
        this.height = height;

        return this;
    }

    public void reset() {
        /* ¯\_(ツ)_/¯ */
    }

    /* Child management */

    public void setup() {
        for (IWidget child : this.parent.getChildren()) {
            //child.resizer(this.child(child));
        }
    }

    public IResizer child(IWidget element) {
        return new ChildResizer(this, element);
    }

    public List<ChildResizer> getResizers() {
        List<ChildResizer> resizers = new ArrayList<ChildResizer>();

        for (IWidget widget : this.parent.getChildren()) {
            if (widget.resizer() instanceof ChildResizer) {
                resizers.add((ChildResizer) widget.resizer());
            }
        }

        return resizers;
    }

    /* Miscellaneous */

    @Override
    public void add(IWidget parent, IWidget child) {
        // TODO
        /*if (child.ignored) {
            return;
        }*/

        //child.resizer(this.child(child));
    }

    @Override
    public void remove(IWidget parent, IWidget child) {
        /*if (child.ignored) {
            return;
        }*/

        /*IResizer resizer = child.resizer();

        if (resizer instanceof ChildResizer) {
            child.resizer(((ChildResizer) resizer).resizer);
        }*/
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getW() {
        return 0;
    }

    @Override
    public int getH() {
        return 0;
    }
}