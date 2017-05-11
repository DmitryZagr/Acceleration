package com.devteam.acceleration.jabber.executors;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static com.devteam.acceleration.jabber.db.JabberContract.JabberEntry;

import com.devteam.acceleration.jabber.db.JabberDbHelper;
import com.devteam.acceleration.ui.MessageData;

import org.jivesoftware.smack.packet.Message;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * Created by admin on 10.05.17.
 */

public class JabberDB {

    private static JabberDB INSTANCE = new JabberDB();

    private CallbackDB callbackDB;
    private final Executor executor = Executors.newCachedThreadPool();

    private static final String[] projection = {
            JabberEntry._ID,
            JabberEntry.COLUMN_NAME_DATE,
            JabberEntry.COLUMN_NAME_TEXT_MESSAGE,
            JabberEntry.COLUMN_NAME_PATH_TO_IMAGE,
            JabberEntry.COLUMN_NAME_MESSAGE_TYPE
    };

    public interface CallbackDB {
        void onCallbackDb(Exception error);
    }

    private JabberDB() {}

    public static JabberDB getInstance() {
        return INSTANCE;
    }


    public void saveMessage(final SQLiteDatabase db, final MessageData.MessageModel messageModel, final int messageType) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(JabberEntry.COLUMN_NAME_DATE, messageModel.getTime().toString());
                values.put(JabberEntry.COLUMN_NAME_TEXT_MESSAGE, messageModel.getContent());
                values.put(JabberEntry.COLUMN_NAME_PATH_TO_IMAGE, messageModel.getURL());
                values.put(JabberEntry.COLUMN_NAME_MESSAGE_TYPE, messageType);

                long newRowId = db.insert(JabberEntry.TABLE_NAME, null, values);
            }
        });
    }

    public void getHistory(final SQLiteDatabase db) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = db.query(JabberEntry.TABLE_NAME, projection, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    MessageData.MessageModel messageModel = new MessageData.MessageModel(
                            Integer.toString(cursor.getInt(cursor.getColumnIndex(JabberEntry._ID))),
                            cursor.getString(cursor.getColumnIndex(JabberEntry.COLUMN_NAME_TEXT_MESSAGE)),
                            cursor.getInt(cursor.getColumnIndex(JabberEntry.COLUMN_NAME_MESSAGE_TYPE)),
                            cursor.getString(cursor.getColumnIndex(JabberEntry.COLUMN_NAME_PATH_TO_IMAGE)),
                            cursor.getString(cursor.getColumnIndex(JabberEntry.COLUMN_NAME_DATE)));
                    MessageData.items.add(messageModel);
                    notifyUI(null);
                }
                cursor.close();
            }
        });
    }


    public void removeOldMessages(final SQLiteDatabase db, final int count) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String sql = "DELETE FROM " + JabberEntry.TABLE_NAME + " WHERE " + JabberEntry._ID
                        + " IN ( SELECT " + JabberEntry._ID + " FROM " + JabberEntry.TABLE_NAME
                        + " DESC LIMIT " + count;
                db.execSQL(sql);
            }
        });
    }


    private void notifyUI(final Exception e) {
        Ui.run(new Runnable() {
            @Override
            public void run() {
                if (callbackDB != null)
                    callbackDB.onCallbackDb(e);
            }
        });
    }


}
