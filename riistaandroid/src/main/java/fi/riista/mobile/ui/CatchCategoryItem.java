package fi.riista.mobile.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import fi.riista.mobile.R;

public class CatchCategoryItem extends LinearLayout {
    private Context mContext = null;

    public CatchCategoryItem(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CatchCategoryItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public CatchCategoryItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.view_catch_category_item, this);
    }

    public void setContents(int catchAmount, String categoryName) {
        TextView amountView = (TextView) findViewById(R.id.category_catch_amount);
        amountView.setText(Integer.valueOf(catchAmount).toString());
        TextView nameView = (TextView) findViewById(R.id.category_name);
        nameView.setText(categoryName);
    }
}
