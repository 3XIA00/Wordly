package edu.fandm.yshen.wordly;

import android.text.InputFilter;
import android.text.Spanned;

public class EnglishWordsInputFilter implements InputFilter {
    private static final String ENGLISH_WORD_PATTERN = "^[a-z]*$";

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        // Check each character being entered
        for (int i = start; i < end; i++) {
            // If the character is not a letter, prevent it from being entered
            if (!Character.toString(source.charAt(i)).matches(ENGLISH_WORD_PATTERN)) {
                return "";
            }
        }
        return null; // Accept the input
    }
}
