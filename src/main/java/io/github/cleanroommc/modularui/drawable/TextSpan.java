package io.github.cleanroommc.modularui.drawable;

import io.github.cleanroommc.modularui.api.math.Pos2d;
import io.github.cleanroommc.modularui.api.math.Size;

import java.util.ArrayList;
import java.util.List;

public class TextSpan implements IDrawable {

    private final List<Text> texts = new ArrayList<>();

    public TextSpan(Text... texts) {
        addText(texts);
    }

    public TextSpan addText(Text... texts) {
        for (Text text : texts) {
            if(text != null)  {
                this.texts.add(text);
            }
        }
        return this;
    }

    @Override
    public void draw(Pos2d pos, Size size, float partialTicks) {
        Text.drawText(pos, (int) size.width, texts);
    }
}
