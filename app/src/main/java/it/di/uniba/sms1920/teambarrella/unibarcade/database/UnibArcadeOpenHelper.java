package it.di.uniba.sms1920.teambarrella.unibarcade.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeContract.*;


public class UnibArcadeOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private Context context;

    private static final String DATABASE_NAME = "unibArcade.db";

    private static final String GAME_CREATE_TABLE = "CREATE TABLE " + GameEntry.TABLE_NAME + "("
            + GameEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GameEntry.NAME + " TEXT)";

    UnibArcadeOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GAME_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void deleteDb() {
        context.deleteDatabase(DATABASE_NAME);
    }
}
