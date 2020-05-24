package it.di.uniba.sms1920.teambarrella.unibarcade.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeContract.*;

public class UnibArcadeDBAdapter {

    private static final String SELECT = "SELECT ";
    private static final String FROM = " FROM ";
    private static final String WHERE = " WHERE ";

    //Query for retrieving game id
    private static final String GET_GAME_ID = SELECT + GameEntry.ID + FROM + GameEntry.TABLE_NAME + WHERE + GameEntry.NAME + "=?";

    //Query for game table row
    private static final String GET_ROW_TABLE = SELECT + "*" + FROM + GameEntry.TABLE_NAME;

    //instance of Open Helper
    private UnibArcadeOpenHelper openHelper;

    public UnibArcadeDBAdapter(Context context) {
        openHelper = new UnibArcadeOpenHelper(context);
    }

    //insert game data on local db
    public void insertGameData(Context context) {
        //get resources from array string in strings.xml
        String[] arrayGameName = context.getResources().getStringArray(R.array.GameList);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        //if is empty add values of games
        if(isTableEmpty()) {
            for (int i = 0; i < arrayGameName.length; i++) {
                ContentValues values = new ContentValues();
                values.put(GameEntry.NAME, arrayGameName[i]);
                long newRow = db.insert(GameEntry.TABLE_NAME, null, values);
            }
        }
    }

    //retrieve game id from local db
    public String retrieveGameId(String name) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(GET_GAME_ID,  new String[]{name});
        cursor.moveToFirst();
        return cursor.getString(0);
    }

    //check if table is empty
    public boolean isTableEmpty() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(GET_ROW_TABLE, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            return false;
        } else {
            return true;
        }
    }
}
