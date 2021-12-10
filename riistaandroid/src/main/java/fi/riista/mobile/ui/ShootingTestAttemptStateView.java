package fi.riista.mobile.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import fi.riista.mobile.R;

public class ShootingTestAttemptStateView extends ConstraintLayout {

    private String mTypeTextAttribute;

    private TextView mTypeText;
    private TextView mAttemptCount;
    private ImageView mStateImage;

    public ShootingTestAttemptStateView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShootingTestAttemptStateView);
        try {
            mTypeTextAttribute = a.getString(R.styleable.ShootingTestAttemptStateView_typeText);
        } finally {
            a.recycle();
        }

        initializeViews(context);
    }

    private void initializeViews(final Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_attempt_state_item, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTypeText = findViewById(R.id.title_short_text);
        mAttemptCount = findViewById(R.id.attempts_text);
        mStateImage = findViewById(R.id.pass_fail_image);

        setState(AttemptState.NONE, 0);
    }

    public void setState(final AttemptState state, final int attempts) {
//        mTypeText.setText(mTypeTextAttribute);

        mAttemptCount.setText(attempts > 0 ? String.valueOf(attempts) : "");

        setBackgroundColor(Color.TRANSPARENT);

        switch (state) {
            case PASS:
                setBackgroundResource(R.drawable.bg_rounded_primary);
                mStateImage.setImageResource(R.drawable.ic_result_pass);
                break;
            case FAIL:
                setBackgroundResource(R.drawable.bg_rounded_destructive);
                mStateImage.setImageResource(R.drawable.ic_result_fail);
                break;
            case INTENDED:
                setBackgroundResource(R.drawable.bg_rounded_intended);
                mStateImage.setImageDrawable(null);
                break;
            case NONE:
            default:
                setBackgroundResource(R.drawable.bg_rounded_not_selected);
                mStateImage.setImageDrawable(null);
                break;
        }
    }

    public void setState(@NonNull final AttemptState state, final int attempts, final String type) {
        mTypeText.setText(type);
        setState(state, attempts);
    }

    public String getText() {
        return mTypeTextAttribute;
    }

    public void setText(final String text) {
        mTypeTextAttribute = text;
        mTypeText.setText(text);

        mTypeText.invalidate();
        mTypeText.requestLayout();
    }

    public enum AttemptState {
        NONE,
        INTENDED,
        PASS,
        FAIL
    }
}