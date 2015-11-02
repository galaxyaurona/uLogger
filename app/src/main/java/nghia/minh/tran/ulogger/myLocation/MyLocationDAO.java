package nghia.minh.tran.ulogger.myLocation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Oguri on 5/10/2015.
 */
public class MyLocationDAO {
    private SQLiteDatabase database;
    private MyLocationSQLiteHelper dbHelper;
    private String[] allColumns = {MyLocationSchema.MyLocationTable._ID,
            MyLocationSchema.MyLocationTable.COLUMN_NAME_NAME,
            MyLocationSchema.MyLocationTable.COLUMN_NAME_DESCRIPTION,
            MyLocationSchema.MyLocationTable.COLUMN_NAME_LAT,
            MyLocationSchema.MyLocationTable.COLUMN_NAME_LNG,
            MyLocationSchema.MyLocationTable.COLUMN_NAME_DATE};

    public MyLocationDAO(Context context){
        dbHelper = new MyLocationSQLiteHelper(context);
    }

    public void open() {database = dbHelper.getWritableDatabase();}
    public void close() {dbHelper.close();}

    public MyLocation createLocation(String name,String description,String lat,String lng,Date date){
        open();
        Long valueOfDate = date.getTime();
        ContentValues values = new ContentValues();
        values.put(MyLocationSchema.MyLocationTable.COLUMN_NAME_NAME,name);
        values.put(MyLocationSchema.MyLocationTable.COLUMN_NAME_DESCRIPTION,description);
        values.put(MyLocationSchema.MyLocationTable.COLUMN_NAME_LAT,lat);
        values.put(MyLocationSchema.MyLocationTable.COLUMN_NAME_LNG,lng);
        values.put(MyLocationSchema.MyLocationTable.COLUMN_NAME_DATE,valueOfDate);
        long insertedId = database.insert(MyLocationSchema.MyLocationTable.TABLE_NAME,null,values);
        return new MyLocation(insertedId,name,description,lat,lng,date);
    }

    public void deleteLocation(MyLocation myLocation){
        long id = myLocation.getId();
        database.delete(MyLocationSchema.MyLocationTable.TABLE_NAME, MyLocationSchema.MyLocationTable._ID+" = "+id,null);
    }

    public List<MyLocation> getAllMyLocations(){
        open();
        List<MyLocation> myLocations = new ArrayList<>();
        Cursor cursor = database.query(MyLocationSchema.MyLocationTable.TABLE_NAME,allColumns,null,null,null,null,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            MyLocation myLocation = cursorToLocation(cursor);
            myLocations.add(myLocation);
            cursor.moveToNext();
        }
        cursor.close();

        return myLocations;
    }

    private MyLocation cursorToLocation(Cursor cursor){
        Long valueOfDate = cursor.getLong(5);
      MyLocation myLocation = new MyLocation(cursor.getLong(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),new Date(valueOfDate));
        return myLocation;
    };

}
