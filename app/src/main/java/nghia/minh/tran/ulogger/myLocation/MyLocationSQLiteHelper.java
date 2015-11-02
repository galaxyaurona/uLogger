package nghia.minh.tran.ulogger.myLocation;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Oguri on 5/10/2015.
 */
public class MyLocationSQLiteHelper extends SQLiteOpenHelper {

    public MyLocationSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }
    public MyLocationSQLiteHelper(Context context){
        super(context, MyLocationSchema.MyLocationTable.TABLE_NAME,null, MyLocationSchema.MyLocationTable.DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(MyLocationSchema.MyLocationTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(MyLocationSchema.MyLocationTable.SQL_DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
