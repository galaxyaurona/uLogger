package nghia.minh.tran.ulogger.place;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;

import nghia.minh.tran.ulogger.myLocation.MyLocation;

/**
 * Created by Oguri on 12/10/2015.
 */
public class MyPlace implements Parcelable {
    private String googleId;
    private String lat;
    private String lng;
    private String description;
    private String name;

    private static final double RADIUS = 500.0;
    private static final int EARTH_RADIUS = 6371000;
    public MyPlace(Parcel in) {
        String[] data = new String[5];

        in.readStringArray(data);
        this.googleId = data[0];
        this.name = data[1];
        this.description = data[2];
        this.lat = data[3];
        this.lng = data[4];

    }

    public MyPlace(Place place){
        this.googleId = place.getId();
        this.name = place.getName().toString();
        this.lat = place.getLatLng().latitude+"";
        this.lng = place.getLatLng().longitude+"";
        this.description = place.getAddress().toString();

    }
    public MyPlace(MyLocation myLocation){
        this.googleId = myLocation.getId()+"";
        this.name = myLocation.getName();
        this.lat = myLocation.getLat();
        this.lng = myLocation.getLng();
        this.description = myLocation.getDescription();
    }
    public MyPlace(String id,String name,String address,String lat,String lng){
        this.googleId = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.description = address;

    }
    public String getLng() {
        return lng;
    }

    public String getLat() {
        return lat;
    }

    public String getDescription() {
        return description;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getName() {
        return name;
    }
    public String toString() {return name;}
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {this.googleId,
                this.name,
                this.description,
                this.lat,
                this.lng});
    }
    //TODO: fine tuning this
    public boolean inRange(String lat,String lng){
       double latDb = Double.parseDouble(lat);
       double lngDb = Double.parseDouble(lng);
       try{
           android.location.Location tempLoc = new android.location.Location("");
           tempLoc.setLatitude(Double.parseDouble(this.lat));
           tempLoc.setLongitude(Double.parseDouble(this.lng));

           android.location.Location dest = new android.location.Location("");
           dest.setLatitude(latDb);
           dest.setLongitude(lngDb);
           double distance = tempLoc.distanceTo(dest);

           return distance <= RADIUS;
       }catch (Exception e){

          return false;
       }

    }

    public static final Creator<MyPlace> CREATOR
            = new Creator<MyPlace>() {
        public MyPlace createFromParcel(Parcel in) {
            return new MyPlace(in);
        }

        public MyPlace[] newArray(int size) {
            return new MyPlace[size];
        }
    };
}