package com.devteam.acceleration.jabber.db;

import android.provider.BaseColumns;

public final class JabberContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private JabberContract() {}

    /* Inner class that defines the table contents */
    public static class JabberEntry implements BaseColumns {
        public static final String TABLE_NAME = "JabberDB";
        public static final String COLUMN_NAME_DATE = "DATE";
        public static final String COLUMN_NAME_TEXT_MESSAGE = "TEXT_MESSAGE";
        public static final String COLUMN_NAME_PATH_TO_IMAGE = "PATH_TO_IMAGE";
        public static final String COLUMN_NAME_MESSAGE_TYPE = "MESSAGE_TYPE";
    }
}
