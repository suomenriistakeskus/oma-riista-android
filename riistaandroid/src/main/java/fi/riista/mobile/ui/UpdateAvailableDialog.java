package fi.riista.mobile.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import fi.riista.mobile.R;

public class UpdateAvailableDialog {

    public static void show(final Context context, final String versionDownloadable, final int dialogIconResId) {
        try {
            final PackageManager packageManager = context.getPackageManager();

            try {
                // TODO Return value not used. Unsure whether this is called only for testing whether activity is
                //  still alive. In the outer try-catch block, there are three RuntimeExceptions that are prepared to be
                //  catched. Without further knowledge, each of them could possibly be raised within execution of the
                //  following line.
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(context.getPackageName(), 0));
            } catch (final PackageManager.NameNotFoundException ignored) {
            }

            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.update_available_title))
                    .setMessage(context.getString(R.string.update_available_message))
                    .setCancelable(true)
                    .setPositiveButton(context.getString(android.R.string.ok), (dialog, which) -> {
                        goToPlayStore(context);
                        dialog.cancel();
                    })
                    .setNegativeButton(context.getString(android.R.string.no), (dialog, which) -> dialog.cancel());

            if (dialogIconResId != 0) {
                alertDialogBuilder.setIcon(dialogIconResId);
            }

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        } catch (final NullPointerException | IllegalStateException | WindowManager.BadTokenException e) {
            // Activity already closed.
            e.printStackTrace();
        }
    }

    private static void goToPlayStore(final Context context) {
        final String appPackageName = context.getPackageName();

        try {
            goToPlayStore(context, Uri.parse("market://details?id=" + appPackageName));
        } catch (final ActivityNotFoundException e) {
            goToPlayStore(context, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
        }
    }

    private static void goToPlayStore(final Context context, final Uri uri) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}
