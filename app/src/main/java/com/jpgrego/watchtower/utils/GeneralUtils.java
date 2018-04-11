package com.jpgrego.watchtower.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.jpgrego.watchtower.activities.WifiAndCells;

/**
 * Created by jpgrego on 7/6/17.
 */

public final class GeneralUtils {
    public static void generateBigTextNotification(final Context context,
                                                   final NotificationCompat.Builder notBuilder,
                                                   final int notificationID, final String title,
                                                   final String bigText) {

        final Intent resultIntent = new Intent(context, WifiAndCells.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        notBuilder.setContentIntent(PendingIntent.getActivity(context,
                notificationID, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        notBuilder.setTicker(title);
        notBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(bigText));
        notBuilder.setContentText(bigText);

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, notBuilder.build());
    }

    public static String toHexString(final int num) {
        return Integer.toHexString(0x10000 | num).substring(1).toUpperCase();
    }
}
