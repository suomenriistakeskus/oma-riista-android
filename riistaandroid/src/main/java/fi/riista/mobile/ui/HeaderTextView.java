package fi.riista.mobile.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Locale;

import fi.riista.mobile.R;
import fi.vincit.androidutilslib.graphics.UnitConverter;

/**
 * Custom TextView element with uppercase text and bottom border
 */
public class HeaderTextView extends TextView {

    private Locale mLocale = null;

    public HeaderTextView(Context context) {
        super(context);
    }

    public HeaderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        setTextColor(getResources().getColor(R.color.header_text_color));
        setTypeface(null, Typeface.BOLD);
        setBackgroundResource(R.drawable.headerborder);
        setLeftPadding(5);
        mLocale = new Locale("fi");
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (mLocale == null) {
            init();
        }
        super.setText(text.toString().toUpperCase(mLocale), type);
    }

    public void setLeftPadding(int dip) {
        setPadding((int) UnitConverter.dipToPx(getContext(), dip), 0, 0, 0);
    }

    public void setRightPadding(int dip) {
        setPadding(0, 0, (int) UnitConverter.dipToPx(getContext(), dip), 0);
    }
}
