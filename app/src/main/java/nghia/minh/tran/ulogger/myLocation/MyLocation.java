package nghia.minh.tran.ulogger.myLocation;

import java.util.Date;

/**
 * Created by Oguri on 5/10/2015.
 */
public class MyLocation {
    private long id;
    private String lat;
    private String lng;
    private Date date;
    private String name;
    private String description;

   public MyLocation(long id,String name,String description, String lat, String lng,Date date){
       this.id = id;
       this.name = name;
       this.lat = lat;
       this.lng = lng;
       this.description = description;
       this.date = date;
   }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {

        return "Id: "+id
              +", name: "+name
              +", description"+description
              +", lat: "+lat
              +", lng: "+lng
              +"\n";
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }
}
