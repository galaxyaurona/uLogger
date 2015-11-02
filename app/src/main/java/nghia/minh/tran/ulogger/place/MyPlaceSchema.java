package nghia.minh.tran.ulogger.place;

import android.provider.BaseColumns;

/**
 * Created by Oguri on 5/10/2015.
 */
public final class MyPlaceSchema {
    public MyPlaceSchema(){}
    public static abstract class MyPlaceTable implements BaseColumns {
        public static final String TABLE_NAME = "MyPlace";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LNG = "lng";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME ="name";
        public static final String COLUMN_NAME_DESCRIPTION = "description";

        public static final int DATABASE_VERSION = 1;

        private static final String TEXT_TYPE = " TEXT";
        private static final String REAL_TYPE = " REAL";
        private static final String COMMA_SEP = ",";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_NAME_ID + " STRING PRIMARY KEY ,"
                + COLUMN_NAME_NAME + TEXT_TYPE+ COMMA_SEP
                + COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP
                + COLUMN_NAME_LAT + TEXT_TYPE + COMMA_SEP
                + COLUMN_NAME_LNG + TEXT_TYPE + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME+";";
    }
}
