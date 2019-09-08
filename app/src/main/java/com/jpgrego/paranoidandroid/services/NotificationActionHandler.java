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
    public static final String ACTION_ADD_TRUSTED_USB_DEVICE = "add_trusted_USB_device";

    public static final String EXTRA_NOT_ID = "notificationId";
    public static final String EXTRA_BSSID = "bssid";
    public static final String EXTRA_VENDOR_ID = "vendorID";
    public static final String EXTRA_PRODUCT_ID = "productID";


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

        final int notificationId = intent.getIntExtra(EXTRA_NOT_ID, -1);
        final ContentValues cv = new ContentValues();

        switch(action) {
            case ACTION_ADD_TRUSTED_AP:
                final String bssid = intent.getStringExtra(EXTRA_BSSID);
                cv.put(DatabaseContract.WifiAPEntry.TRUSTED_COLUMN, true);
                db.update(DatabaseContract.WifiAPEntry.TABLE_NAME, cv,
                        DatabaseContract.WifiAPEntry.BSSID_COLUMN + "=?",
                        new String[]{bssid});
                break;
            case ACTION_ADD_TRUSTED_USB_DEVICE:
                final String vendorID = intent.getStringExtra(EXTRA_VENDOR_ID);
                final String productID = intent.getStringExtra(EXTRA_PRODUCT_ID);
                cv.put(DatabaseContract.TrustedUSBDeviceEntry.VENDORID_COLUMN, vendorID);
                cv.put(DatabaseContract.TrustedUSBDeviceEntry.PRODUCTID_COLUMN, productID);
                db.insertWithOnConflict(
                        DatabaseContract.TrustedUSBDeviceEntry.TABLE_NAME, null,
                        cv, SQLiteDatabase.CONFLICT_IGNORE);
            default:
                // do nothing;
        }

        if(notificationManager != null) notificationManager.cancel(notificationId);

        db.close();
    }
}
