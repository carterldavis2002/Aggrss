package com.carterldavis2002.aggrss;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "rss.db";
    private static final int DATABASE_VERSION = 1;

    private static final String FEED_TABLE = "feed_table";
    private static final String FEED_ID = "ID";
    private static final String FEED_TITLE = "TITLE";
    private static final String FEED_URL = "URL";
    private static final String FEED_ENABLED = "ENABLED";


    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL("create table " + FEED_TABLE + " (" + FEED_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + FEED_TITLE + " TEXT, " + FEED_URL +
                " TEXT, " + FEED_ENABLED + " INTEGER)");
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + FEED_TABLE);
        onCreate(db);
    }

    public void insertFeed(@NonNull Feed feed) {
        SQLiteDatabase db = getWritableDatabase();
        int feedEnabled = feed.isEnabled() ? 1 : 0;
        String feedTitle = feed.getTitle().replace("'", "''");
        String feedUrl = feed.getUrl().replace("'", "''");
        db.execSQL("insert into " + FEED_TABLE + " values(null, '"
                + feedTitle + "', '" + feedUrl + "', '" + feedEnabled + "')");

        String query = "select *  from " + FEED_TABLE;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToLast();
        feed.setId(Integer.parseInt(cursor.getString(0)));

        cursor.close();
        db.close();
    }

    public void deleteFeedById(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + FEED_TABLE + " where " + FEED_ID + " = " + id);
        db.close();
    }

    public void updateFeedById(int id, boolean enabled) {
        SQLiteDatabase db = getWritableDatabase();

        int feedEnabled = enabled ? 1 : 0;
        db.execSQL("update " + FEED_TABLE + " set " + FEED_ENABLED + " = '" + feedEnabled +
                "' where " + FEED_ID + " = " + id);
        db.close();
    }

    public ArrayList<Feed> selectAllFeeds() {
        String query = "select * from " + FEED_TABLE;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        ArrayList<Feed> feeds = new ArrayList<>();
        while(cursor.moveToNext()) {
            Feed currentFeed = new Feed(cursor.getString(1), cursor.getString(2));
            currentFeed.setId(Integer.parseInt(cursor.getString(0)));
            if(Integer.parseInt(cursor.getString(3)) == 0)
                currentFeed.toggleEnabled();

            feeds.add(currentFeed);
        }
        cursor.close();
        db.close();

        return feeds;
    }
}