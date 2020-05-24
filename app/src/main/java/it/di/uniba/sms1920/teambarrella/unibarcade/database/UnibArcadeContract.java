package it.di.uniba.sms1920.teambarrella.unibarcade.database;

import android.provider.BaseColumns;

public class UnibArcadeContract {

    UnibArcadeContract() {
    }

    public static final class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "game";
        public static final String ID = "id";
        public static final String NAME = "name";
    }
}
