package edu.niu.z1891607.aggrss;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "rss.db";
    private static final int DATABASE_VERSION = 1;
    public static final String FEED_TABLE = "feed_table";

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + FEED_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TITLE TEXT, URL TEXT, ENABLED INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + FEED_TABLE);
        onCreate(db);
    }

    public void insertFeed(Feed feed) {
        SQLiteDatabase db = getWritableDatabase();
        int feedEnabled = feed.isEnabled() ? 1 : 0;
        db.execSQL("insert into " + FEED_TABLE + " values(null, '"
                + feed.getTitle() + "', '" + feed.getUrl() + "', '" + feedEnabled + "')");

        String query = "select *  from " + FEED_TABLE;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToLast();
        feed.setId(Integer.parseInt(cursor.getString(0)));

        cursor.close();
        db.close();
    }

    public void deleteFeedById(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + FEED_TABLE + " where ID = " + id);
        db.close();
    }

    public void updateFeedById(int id, boolean enabled) {
        SQLiteDatabase db = getWritableDatabase();

        int feedEnabled = enabled ? 1 : 0;
        db.execSQL("update " + FEED_TABLE + " set ENABLED = '" + feedEnabled + "' where ID = " +
                id);
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
