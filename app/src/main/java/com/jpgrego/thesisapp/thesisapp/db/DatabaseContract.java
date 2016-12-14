package com.jpgrego.thesisapp.thesisapp.db;

import android.provider.BaseColumns;

/**
 * Created by jpgrego on 14/12/16.
 */

final class DatabaseContract {

    //private static final String TEXT_TYPE       = " TEXT";
    private static final String INTEGER_TYPE    = " INTEGER";
    private static final String COMMA_SEP       = ",";

    // enforce noninstantiability
    private DatabaseContract() {
        throw new AssertionError();
    }

    static class CellEntry implements BaseColumns {
        private static final String TABLE_NAME = "cells";
        private static final String MCC_COLUMN = "mcc";
        private static final String MNC_COLUMN = "mnc";
        private static final String CID_COLUMN = "cid";
        private static final String LAC_COLUMN = "lac";
        private static final String PSC_COLUMN = "psc";
        private static final String DBM_COLUMN = "dbm";

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        MCC_COLUMN + INTEGER_TYPE + COMMA_SEP +
                        MNC_COLUMN + INTEGER_TYPE + COMMA_SEP +
                        CID_COLUMN + INTEGER_TYPE + COMMA_SEP +
                        LAC_COLUMN + INTEGER_TYPE + COMMA_SEP +
                        PSC_COLUMN + INTEGER_TYPE + COMMA_SEP +
                        DBM_COLUMN + INTEGER_TYPE + " )";

        static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

    }
}
