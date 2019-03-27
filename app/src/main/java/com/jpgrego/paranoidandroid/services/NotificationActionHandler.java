package com.jpgrego.paranoidandroid.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.jpgrego.paranoidandroid.db.DatabaseContract;
import com.jpgrego.paranoidandroid.db.DatabaseHelper;

public final class NotificationActionHandler extends IntentService {

    public static final String ACTION_ADD_TRUSTED_AP = "add_trusted_AP";

    public static final String EXTRA_NOT_ID = "notificationId";
    public static final String EXTRA_BSSID = "bssid";


    public NotificationActionHandler() {
        super(NotificationActionHandler.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null) return;

        final String action = intent.getAction();

        if(action == null) return;

        final SQLiteDatabase db =
                new DatabaseHelper(this.getApplicationContext()).getWritableDatabase();
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        switch(action) {
            case ACTION_ADD_TRUSTED_AP:
                final int notificationId = intent.getIntExtra(EXTRA_NOT_ID, -1);
                final String bssid = intent.getStringExtra(EXTRA_BSSID);
                final ContentValues cv = new ContentValues();
                cv.put(DatabaseContract.WifiAPEntry.TRUSTED_COLUMN, true);

                db.update(DatabaseContract.WifiAPEntry.TABLE_NAME, cv,
                        DatabaseContract.WifiAPEntry.BSSID_COLUMN + "=?",
                        new String[]{bssid});

                if(notificationManager != null) notificationManager.cancel(notificationId);
                break;
            default:
                // do nothing;
        }

        db.close();
    }
}
