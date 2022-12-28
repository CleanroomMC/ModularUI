package com.cleanroommc.modularui.widgets.textfield;

/**
 * A non syncable, multiline text input widget. Meant for client only screens to edit large amounts of text.
 */
// TODO steal from Mclib
public class TextEditorWidget extends BaseTextFieldWidget<TextEditorWidget> {

    public TextEditorWidget() {
        this.handler.setMaxLines(10000);
    }
}
