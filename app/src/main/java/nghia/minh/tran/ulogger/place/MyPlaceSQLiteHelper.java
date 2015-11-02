package nghia.minh.tran.ulogger.place;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Oguri on 5/10/2015.
 */
public class MyPlaceSQLiteHelper extends SQLiteOpenHelper {

    public MyPlaceSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }
    public MyPlaceSQLiteHelper(Context context){
        super(context, MyPlaceSchema.MyPlaceTable.TABLE_NAME,null, MyPlaceSchema.MyPlaceTable.DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(MyPlaceSchema.MyPlaceTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(MyPlaceSchema.MyPlaceTable.SQL_DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
