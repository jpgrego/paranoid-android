package com.jpgrego.paranoidandroid.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jpgrego.paranoidandroid.db.DatabaseHelper;

public final class NotificationActionHandler extends IntentService {

    public static final String ACTION_ADD_TRUSTED_AP = "add_trusted _AP";
    private final SQLiteDatabase db;

    public NotificationActionHandler() {
        super(NotificationActionHandler.class.getSimpleName());
        db = new DatabaseHelper(this).getWritableDatabase();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null) return;

        final String action = intent.getAction();

        if(action == null) return;

        switch(action) {
            case ACTION_ADD_TRUSTED_AP:
                //TODO: this isn't working apparently?
                Log.e(this.getClass().getSimpleName(), "YEAAAHH!!");
                break;
        }
    }
}
