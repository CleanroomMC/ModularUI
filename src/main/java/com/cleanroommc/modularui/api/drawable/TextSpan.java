package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.ArrayUtils;

public class TextSpan implements IDrawable {

    private Text[] texts;

    public TextSpan(Text... texts) {
        this.texts = texts;
    }

    public TextSpan addText(Text... texts) {
        this.texts = ArrayUtils.addAll(this.texts, texts);
        return this;
    }

    public TextSpan addText(ITextComponent textComponent) {
        return addText(Text.of(textComponent));
    }

    @Override
    public void draw(Pos2d pos, Size size, float partialTicks) {
        TextRenderer.drawString(Text.getFormatted(texts), pos, 0x212121, size.width);
    }

    public Text[] getTexts() {
        return texts;
    }
}
