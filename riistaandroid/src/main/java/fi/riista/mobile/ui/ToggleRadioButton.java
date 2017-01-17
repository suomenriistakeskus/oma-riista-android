package fi.riista.mobile.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.widget.RadioGroup;

/**
 * Radio button which allows clearing selection by clicking it again.
 */
public class ToggleRadioButton extends AppCompatRadioButton {

    private boolean mIsUncheckable = false;

    public ToggleRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setUncheckable(boolean enabled) {
        mIsUncheckable = enabled;
    }

    @Override
    public void toggle() {
        if (isChecked() && mIsUncheckable) {
            if (getParent() instanceof RadioGroup) {
                ((RadioGroup) getParent()).clearCheck();
            }
        } else {
            setChecked(true);
        }
    }
}
