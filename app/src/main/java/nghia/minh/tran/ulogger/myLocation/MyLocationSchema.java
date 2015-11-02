package nghia.minh.tran.ulogger.myLocation;

import android.provider.BaseColumns;

/**
 * Created by Oguri on 5/10/2015.
 */
public final class MyLocationSchema {
    public MyLocationSchema(){}
    public static abstract class MyLocationTable implements BaseColumns {
        public static final String TABLE_NAME = "MyLocation";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LNG = "lng";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_DATE = "date";
        public static final int DATABASE_VERSION = 1;

        private static final String TEXT_TYPE = " TEXT";
        private static final String REAL_TYPE = " REAL";
        private static final String INTEGER_TYPE = " INTEGER";
        private static final String COMMA_SEP = ",";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP
                + COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP
                + COLUMN_NAME_DATE + INTEGER_TYPE + COMMA_SEP
                + COLUMN_NAME_LAT + TEXT_TYPE + COMMA_SEP
                + COLUMN_NAME_LNG + TEXT_TYPE + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME+";";
    }
}
