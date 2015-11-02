package nghia.minh.tran.ulogger.place;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.List;

import nghia.minh.tran.ulogger.myLocation.MyLocation;

/**
 * Created by Oguri on 5/10/2015.
 */
public class MyPlaceDAO {
    private SQLiteDatabase database;
    private MyPlaceSQLiteHelper dbHelper;
    private String[] allColumns = {"rowid AS _id",MyPlaceSchema.MyPlaceTable.COLUMN_NAME_ID,MyPlaceSchema.MyPlaceTable.COLUMN_NAME_NAME, MyPlaceSchema.MyPlaceTable.COLUMN_NAME_DESCRIPTION, MyPlaceSchema.MyPlaceTable.COLUMN_NAME_LAT, MyPlaceSchema.MyPlaceTable.COLUMN_NAME_LNG};

    public MyPlaceDAO(Context context){
        dbHelper = new MyPlaceSQLiteHelper(context);
    }

    public void open() {database = dbHelper.getWritableDatabase();}
    public void close() {dbHelper.close();}

    public MyPlace createMyPlace(Place place){
        if (findPlaceById(place)==null) {
            open();
            String UUID = place.getId();
            String name = place.getName() + "";
            String address = place.getAddress() + "";
            String lat = place.getLatLng().latitude + "";
            String lng = place.getLatLng().longitude + "";

            ContentValues values = new ContentValues();
            values.put(MyPlaceSchema.MyPlaceTable.COLUMN_NAME_ID, UUID);
            values.put(MyPlaceSchema.MyPlaceTable.COLUMN_NAME_NAME, name);
            values.put(MyPlaceSchema.MyPlaceTable.COLUMN_NAME_DESCRIPTION , address);
            values.put(MyPlaceSchema.MyPlaceTable.COLUMN_NAME_LAT, lat);
            values.put(MyPlaceSchema.MyPlaceTable.COLUMN_NAME_LNG, lng);

            database.insert(MyPlaceSchema.MyPlaceTable.TABLE_NAME, null, values);
            Log.d("Database","Adding location to database"+values.toString());
            return new MyPlace(place);
        }else{
            return null;
        }
    }
    public MyPlace createMyPlace(MyLocation myLocation){
       open();
       MyPlace tempPlace = new MyPlace(myLocation);

        ContentValues values = new ContentValues();
        values.put(MyPlaceSchema.MyPlaceTable.COLUMN_NAME_ID,myLocation.getId());
        values.put(MyPlaceSchema.MyPlaceTable.COLUMN_NAME_NAME, myLocation.getName());
        values.put(MyPlaceSchema.MyPlaceTable.COLUMN_NAME_DESCRIPTION , myLocation.getDescription());
        values.put(MyPlaceSchema.MyPlaceTable.COLUMN_NAME_LAT, myLocation.getLat());
        values.put(MyPlaceSchema.MyPlaceTable.COLUMN_NAME_LNG, myLocation.getLng());
        database.insert(MyPlaceSchema.MyPlaceTable.TABLE_NAME, null, values);
        return tempPlace;
    }

    public MyPlace findPlaceById(Place place){
        Cursor cursor = database.query(MyPlaceSchema.MyPlaceTable.TABLE_NAME,allColumns,MyPlaceSchema.MyPlaceTable.COLUMN_NAME_ID+" = ?",new String[]{place.getId()},null,null,null);
        if (cursor.isAfterLast())
          return null;
        else{
            cursor.moveToFirst();
            return cursorToMyPlace(cursor);
        }

    };
    public void deleteMyPlace(MyPlace myPlace){
        String id = myPlace.getGoogleId();
        database.delete(MyPlaceSchema.MyPlaceTable.TABLE_NAME, MyPlaceSchema.MyPlaceTable.COLUMN_NAME_ID+"="+id,null);
    }

    public List<MyPlace> getAllMyPlaces(){
        open();
        List<MyPlace> myPlaces = new ArrayList<>();
        Cursor cursor = database.query(MyPlaceSchema.MyPlaceTable.TABLE_NAME,allColumns,null,null,null,null,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            MyPlace myPlace = cursorToMyPlace(cursor);
            myPlaces.add(myPlace);
            cursor.moveToNext();
        }
        cursor.close();
        return myPlaces;
    }

    public ArrayList<MyPlace> getAllMyPlacesInRange(String lat,String lng){
        List<MyPlace> allPlaces = getAllMyPlaces();
        ArrayList<MyPlace> inRangePlaces = new ArrayList<>();
        for (MyPlace place: allPlaces){
            if (place.inRange(lat, lng))
                inRangePlaces.add(place);
        }
        return inRangePlaces;
    }

    private MyPlace cursorToMyPlace(Cursor cursor){// first of cursor is row id
      MyPlace myPlace = new MyPlace(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5));
      return myPlace;
    };

}
