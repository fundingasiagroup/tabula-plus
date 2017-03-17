package com.fs.tabulaplus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 28/12/16.
 */
public class NormalizedRow {
    public final List<String> texts;
    private final int STANDARDISED_LENGTH = 30;

    public NormalizedRow(List<String> texts) {
        this.texts = texts;
    }

    public String get(int column) {
        return texts.get(column);
    }

    public int size() {
        return texts.size();
    }

    @Override
    public String toString() {
        if (texts == null) {
            return "";
        }
        return String.format("%s", String.join("  |  ", texts));
    }

    public String toTabularString() {
        if (texts == null) {
            return "";
        }

        ArrayList<String> formatedTexts = new ArrayList<>();
        for (String text : texts) {
            formatedTexts.add(trimOrPadStringToFixedLength(text, STANDARDISED_LENGTH));
        }

        return String.format("%s", String.join("|", formatedTexts));
    }

    public String toSimpleString() {
        for (int i=0; i<this.texts.size(); i++) {
            this.texts.set(i, this.texts.get(i).trim());
        }
        return String.join(" ", this.texts);
    }

    private String trimOrPadStringToFixedLength(String text, int length) {
        if (text.length() > length)
            return text.substring(0, length);

        String new_text = text;

        for (int i=0; i < length - text.length() - 1; i = i + 2) {
            new_text = " " + new_text + " ";
        }

        if (new_text.length() == length - 1) {
            new_text = new_text + " ";
        }

        return new_text;
    }
}
