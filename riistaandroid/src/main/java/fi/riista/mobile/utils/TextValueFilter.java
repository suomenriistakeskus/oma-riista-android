package fi.riista.mobile.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class TextValueFilter implements InputFilter {

    private float mValue;

    public TextValueFilter(float value) {
        mValue = value;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        StringBuilder builder = new StringBuilder(dest);
        builder.replace(dstart, dend, source.subSequence(start, end).toString());

        try {
            float value = Float.parseFloat(builder.toString());
            if (value < 0.0f || value > mValue) {
                //Invalid value
                return "";
            }
        } catch (NumberFormatException e) {
            //Allow
        }
        return null;
    }
}
