package fi.riista.mobile.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

public class ResourceProvider {
    private Context mContext;

    public ResourceProvider(Context mContext) {
        this.mContext = mContext;
    }

    public String getString(int resId) {
        return mContext.getString(resId);
    }

    public String getString(int resId, String value) {
        return mContext.getString(resId, value);
    }

    public int getColor(int resId) {
        return ContextCompat.getColor(mContext, resId);
    }

    public Drawable getDrawable(int resId) {
        return ContextCompat.getDrawable(mContext, resId);
    }
}
