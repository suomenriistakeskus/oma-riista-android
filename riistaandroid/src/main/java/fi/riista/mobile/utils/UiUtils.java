package fi.riista.mobile.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import fi.riista.mobile.models.user.UserInfo;

public class UiUtils {

    public static int dipToPixels(final Context context, final int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }

    public static void setTopMargin(final View view, final int dip) {
        final LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.topMargin = UiUtils.dipToPixels(view.getContext(), dip);
        view.setLayoutParams(params);
    }

    public static boolean isSrvaVisible(final UserInfo userInfo) {
        return userInfo != null && userInfo.getEnableSrva();
    }

    public static void scrollToListviewBottom(final ListView listView) {
        listView.post(() -> listView.setSelection(listView.getCount() - 1));
    }
}
