package com.cleanroommc.modularui.common.widget.textfield;

/**
 * A non syncable, multiline text input widget. Meant for client only screens to edit large amounts of text.
 */
public class TextEditorWidget extends BaseTextFieldWidget {

    public TextEditorWidget() {
        this.handler.setMaxLines(10000);
    }
}
