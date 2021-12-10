package fi.riista.mobile.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;

import fi.riista.mobile.R;

/**
 * Radio button with centered compound button drawable.
 */
public class RadioButtonCenter extends RadioButtonToggle {

    Drawable mButtonDrawable;

    public RadioButtonCenter(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CompoundButton, 0, 0);
        //noinspection ResourceType
        mButtonDrawable = a.getDrawable(R.styleable.CompoundButton_android_button);
        setButtonDrawable(android.R.color.transparent);

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mButtonDrawable != null) {
            mButtonDrawable.setState(getDrawableState());
            final int verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
            final int height = mButtonDrawable.getIntrinsicHeight();

            int y = 0;

            switch (verticalGravity) {
                case Gravity.BOTTOM:
                    y = getHeight() - height;
                    break;
                case Gravity.CENTER_VERTICAL:
                    y = (getHeight() - height) / 2;
                    break;
            }

            int buttonWidth = mButtonDrawable.getIntrinsicWidth();
            int buttonLeft = (getWidth() - buttonWidth) / 2;
            mButtonDrawable.setBounds(buttonLeft, y, buttonLeft + buttonWidth, y + height);
            mButtonDrawable.draw(canvas);
        }
    }
}
