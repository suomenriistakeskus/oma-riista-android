package fi.riista.mobile.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;

import com.google.android.material.radiobutton.MaterialRadioButton;

/**
 * Radio button which allows clearing selection by clicking it again.
 */
public class RadioButtonToggle extends MaterialRadioButton {

    private boolean mToggleEnabled = false;

    public RadioButtonToggle(Context context) {
        super(context);
    }

    public RadioButtonToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadioButtonToggle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setToggleEnabled(boolean enabled) {
        mToggleEnabled = enabled;
    }

    @Override
    public void toggle() {
        if (isChecked() && mToggleEnabled) {
            if (getParent() instanceof RadioGroup) {
                ((RadioGroup) getParent()).clearCheck();
            }
        } else {
            setChecked(true);
        }
    }
}
