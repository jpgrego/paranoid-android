package com.jpgrego.watchtower.db;

import android.provider.BaseColumns;

/**
 * Created by jpgrego on 14/12/16.
 */

public final class DatabaseContract {

    private static final String TEXT_TYPE       = " TEXT";
    private static final String INTEGER_TYPE    = " INTEGER";
    private static final String PRIMARY_KEY_CONSTRAINT = " PRIMARY KEY";
    private static final String UNIQUE_CONSTRAINT = " UNIQUE";
    private static final String COMMA_SEP       = ",";

    // enforce noninstantiability
    private DatabaseContract() {
        throw new AssertionError();
    }

    public static class CellEntry implements BaseColumns {
        public static final String TABLE_NAME = "cells";
        public static final String CID_COLUMN = "cid";
        public static final String MCC_COLUMN = "mcc";
        public static final String MNC_COLUMN = "mnc";
        public static final String LAC_COLUMN = "lac";
        public static final String PSC_COLUMN = "psc";

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + INTEGER_TYPE + PRIMARY_KEY_CONSTRAINT + COMMA_SEP +
                        CID_COLUMN + INTEGER_TYPE + UNIQUE_CONSTRAINT + COMMA_SEP +
                        MCC_COLUMN + INTEGER_TYPE + COMMA_SEP +
                        MNC_COLUMN + INTEGER_TYPE + COMMA_SEP +
                        LAC_COLUMN + INTEGER_TYPE + COMMA_SEP +
                        PSC_COLUMN + INTEGER_TYPE + " )";

        static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class WifiAPEntry implements BaseColumns {
        public static final String TABLE_NAME = "wifi";
        public static final String BSSID_COLUMN = "bssid";
        public static final String SSID_COLUMN = "ssid";
        public static final String CHANNEL_COLUMN = "channel";
        public static final String SECURITY = "security";

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + INTEGER_TYPE + PRIMARY_KEY_CONSTRAINT + COMMA_SEP +
                        BSSID_COLUMN + TEXT_TYPE + UNIQUE_CONSTRAINT + COMMA_SEP +
                        SSID_COLUMN + TEXT_TYPE + COMMA_SEP +
                        CHANNEL_COLUMN + INTEGER_TYPE + COMMA_SEP +
                        SECURITY + TEXT_TYPE + " )";

        static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class BluetoothEntry implements BaseColumns {
        public static final String TABLE_NAME = "bluetooth";
        public static final String NAME_COLUMN = "name";
        public static final String ADDRESS_COLUMN = "address";
        public static final String TYPE_COLUMN = "type";

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY_CONSTRAINT + COMMA_SEP +
                ADDRESS_COLUMN + TEXT_TYPE + UNIQUE_CONSTRAINT + COMMA_SEP +
                NAME_COLUMN + TEXT_TYPE + COMMA_SEP +
                TYPE_COLUMN + INTEGER_TYPE + " )";

        static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
