package nghia.minh.tran.ulogger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nghia.minh.tran.ulogger.myLocation.MyLocation;
import nghia.minh.tran.ulogger.myLocation.MyLocationDAO;
import nghia.minh.tran.ulogger.place.MyPlace;
import nghia.minh.tran.ulogger.place.MyPlaceDAO;


public class Task2 extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new Task2Fragment())
                    .commit();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("LifeCycle","Restore activity");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("LifeCycle","save activity");
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (HelperClass.handleBack(this,id)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        HelperClass.onBackPressed(this);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class Task2Fragment extends Fragment
            implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            LocationListener {

        private GoogleApiClient mGoogleApiClient;
        private PlaceDetectionApi placeDetectionApi;
        private Marker previousLocationMarker;
        private Marker suggestedPlaceMarker;

        private Button recordCurrentLocationButton;
        private Button whereAmIButton;
        private CameraPosition cameraPosition;
        private ListView suggestedPlaceListView;
        private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm z");
        private MyPlaceDAO myPlaceDAO;
        private MyLocationDAO myLocationDAO;
        private ArrayList<MyPlace> suggestedPlaceArrayList;
        private ArrayAdapter<MyPlace> suggestedPlaceArrayAdapter;
        private final String C9HOST = "https://mpc-assignment-m18tran.c9.io/";

        SupportMapFragment supportMapFragment;
        GoogleMap mGoogleMap;
        AlertDialog dialog;
        RequestQueue requestQueue;
        LatLng lastLocation;
        private View rootView;

        public Task2Fragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_task2, container, false);
            // external component initialization
            mGoogleApiClient = new GoogleApiClient.Builder(rootView.getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            // Task2Logic up here
            requestQueue = Volley.newRequestQueue(rootView.getContext());
            myPlaceDAO = new MyPlaceDAO(rootView.getContext());
            myLocationDAO = new MyLocationDAO(rootView.getContext());

            // UI component initialization

            recordCurrentLocationButton = (Button) rootView.findViewById(R.id.recordCurrentLocationButton);
            suggestedPlaceListView = (ListView) rootView.findViewById(R.id.suggestedPlaceListView);
            whereAmIButton = (Button) rootView.findViewById(R.id.whereAmI);


            // handle screen rotation logic and re-entry from menu here
            if (savedInstanceState == null){
                supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
                mapComponentLogicRegister();
                suggestedPlaceArrayList = new ArrayList<>();
            }else{
                customOnRestoreInstanceState(savedInstanceState);
            }

            suggestedPlaceArrayAdapter = new ArrayAdapter<MyPlace>(getActivity().getApplicationContext(),android.R.layout.simple_list_item_1,android.R.id.text1,suggestedPlaceArrayList);
            suggestedPlaceListView.setAdapter(suggestedPlaceArrayAdapter);




            uiComponentLogicRegister();


            return rootView;
        }

        private void restoreMapState(CameraPosition cameraPosition){

            // this function restore map state
            mGoogleMap.setBuildingsEnabled(true);
            mGoogleMap.setIndoorEnabled(true);
            mGoogleMap.setMyLocationEnabled(true);
            //previousLocation should be set on this
            restoreHistoryMarker();

            if (cameraPosition!=null)
                mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            else{
                if (previousLocationMarker != null)
                    lastLocation = new LatLng(previousLocationMarker.getPosition().latitude, previousLocationMarker.getPosition().longitude);
                else
                    lastLocation = new LatLng(-33.8650, 151.2094);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 18));
            }

        }

        public void mapComponentLogicRegister(){

            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mGoogleMap = googleMap;
                    restoreMapState(null);
                    recordCurrentLocationButton.setEnabled(true);
                }
            });
        }


        public void uiComponentLogicRegister(){
            // this function handle the visible UI component logic
            recordCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getLastLocation(true);
                }
            });
            whereAmIButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getLastLocation(false);
                }
            });
            suggestedPlaceListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (suggestedPlaceMarker != null) {
                        suggestedPlaceMarker.remove();
                    }
                    MyPlace myPlace = suggestedPlaceArrayAdapter.getItem(i);
                    LatLng myPlaceLatLng = new LatLng(Double.parseDouble(myPlace.getLat()), Double.parseDouble(myPlace.getLng()));
                    suggestedPlaceMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .title(myPlace.getName())
                            .snippet(myPlace.getDescription() + "")
                            .position(myPlaceLatLng));

                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlaceLatLng,18));
                    suggestedPlaceMarker.showInfoWindow();
                }
            });
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

        @Override
        public void onConnected(Bundle bundle) {
            HelperClass.showToast(getActivity(),"Connected to google api");
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            HelperClass.showToast(getActivity(),"Fail to connect to google api");
        }
        public void getLastLocation(boolean addPrompt){
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                // always zoom to current location
                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                getNearbyPlaces(mLastLocation);
                if (addPrompt){
                    showPrompt(mLastLocation);
                }
            }
        }

        public void getNearbyPlaces(Location mLastLocation){
            suggestedPlaceArrayAdapter.clear();
            suggestedPlaceArrayAdapter.addAll(myPlaceDAO.getAllMyPlacesInRange(mLastLocation.getLatitude() + "", mLastLocation.getLongitude() + ""));
            HelperClass.showToast(getActivity(),suggestedPlaceArrayList.size()+" place found from cache");
            if (suggestedPlaceArrayList.size()==0){
                PendingResult<PlaceLikelihoodBuffer> pendingResults = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
                pendingResults.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                    @Override
                    public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {

                        for (PlaceLikelihood placeLikelihood : placeLikelihoods){
                            Place place = placeLikelihood.getPlace();
                            // cached magic happen here
                            myPlaceDAO.createMyPlace(place);
                            suggestedPlaceArrayAdapter.add(new MyPlace(place));
                        }
                        placeLikelihoods.release();
                        HelperClass.showToast(getActivity(),suggestedPlaceArrayList.size()+" place found from cache");
                    }
                });
            }
            suggestedPlaceArrayAdapter.notifyDataSetChanged();

        }


        public void showPrompt(final Location mLastLocation){
            LayoutInflater innerInflater = getActivity().getLayoutInflater();
            final View dialogView = innerInflater.inflate(R.layout.current_location_prompt, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());

            builder.setView(dialogView)
                    .setTitle("Location detail")
                    .setNeutralButton("Add", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            EditText locationNameEditText = (EditText) dialogView.findViewById(R.id.locationNameEditText);
                            EditText locationDescriptionEditText = (EditText) dialogView.findViewById(R.id.locationDescriptionEditText);
                            String locationName = locationNameEditText.getText().toString().trim();
                            String locationDescription = locationDescriptionEditText.getText().toString();
                            if (TextUtils.isEmpty(locationName)) {
                                HelperClass.showToast(getActivity(),"Name of location is empty, cannot recorded");
                            } else {
                                MyLocation newLocation = myLocationDAO.createLocation(locationName, locationDescription, mLastLocation.getLatitude() + "", mLastLocation.getLongitude() + "", new Date());
                                myPlaceDAO.createMyPlace(newLocation);
                                addLastLocationMarker(newLocation);
                                HelperClass.showToast(getActivity(),locationNameEditText.getText() + " " + locationDescriptionEditText.getText() + " " + mLastLocation.toString() + " " + new Date().toString());
                                dialog.dismiss();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            dialog= builder.create();

            dialog.show();
        }

        public void showInfo(Marker marker){
            LayoutInflater innerInflater = getActivity().getLayoutInflater();
            final View dialogView = innerInflater.inflate(R.layout.history_location_info_dialog, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
            // get location info from marker
            final String locationName = marker.getTitle().split("@")[0];
            final String locationDescription = marker.getSnippet();
            final String lat = marker.getPosition().latitude+"";
            final String lng = marker.getPosition().longitude+"";
            Date locationDate = new Date();
            try {
                locationDate = dateFormat.parse(marker.getTitle().split("@")[1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //assign to UI
            ((TextView) dialogView.findViewById(R.id.placeNameInfoTextView)).setText("Name: "+locationName);
            ((TextView) dialogView.findViewById(R.id.placeDescriptionInfoTextView)).setText("Description: "+locationDescription);
            ((TextView) dialogView.findViewById(R.id.placeCoordinateInfoTextView)).setText("Coordinate: "+lat+","+lng);
            ((TextView) dialogView.findViewById(R.id.placeDateInfoTextView)).setText(dateFormat.format(locationDate));


            ((Button) dialogView.findViewById(R.id.placeInfoCancelButton)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            // share to Clouđ Server
            final Date finalLocationDate = locationDate;
            ((Button) dialogView.findViewById(R.id.shareToC9Button)).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //initialize params
                    HashMap<String,String> params = new HashMap<String, String>();
                    params.put("appName","ULogger");
                    params.put("locationName",locationName);
                    params.put("locationDescription",locationDescription);
                    params.put("date",dateFormat.format(finalLocationDate));
                    params.put("lat",lat);
                    params.put("lng",lng);
                    callToBackend(params, C9HOST);
                    dialog.dismiss();
                }
            });
            // set view and create location
            builder.setView(dialogView)
                    .setTitle("Location History");


            dialog= builder.create();

            dialog.show();
        }
        public void restoreHistoryMarker(){
            List<MyLocation> logHistory = myLocationDAO.getAllMyLocations();
            HelperClass.showToast(getActivity(),logHistory.size()+" location from log history");
            for (MyLocation myLocation: logHistory){
                addLastLocationMarker(myLocation);
            }
        }
        public void callToBackend(HashMap<String,String> params,String url){
            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST,url,new JSONObject(params),new Response.Listener<JSONObject>(){
                @Override
                public void onResponse(JSONObject response) {
                    HelperClass.showToast(getActivity(),"Response from server:"+response.toString());
                }
            }, new Response.ErrorListener(){

                @Override
                public void onErrorResponse(VolleyError error) {
                    HelperClass.showToast(getActivity(),error.getMessage());
                }
            });
            requestQueue.add(postRequest);
        }

        public void addLastLocationMarker(MyLocation mLastLocation){
            if (previousLocationMarker != null)
                previousLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(275));

            previousLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .title(mLastLocation.getName() + "@" + dateFormat.format(mLastLocation.getDate()))
                    .snippet(mLastLocation.getDescription() + "")
                    .position(new LatLng(Double.parseDouble(mLastLocation.getLat()), Double.parseDouble(mLastLocation.getLng()))));
            previousLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
            mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    showInfo(marker);
                    HelperClass.showToast(getActivity(),marker.getTitle()+" "+marker.getSnippet()+marker.getPosition());
                    return false;
                }
            });
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            Log.d("LifeCycle","save instance state");
            if (dialog != null) {
                dialog.dismiss();
                //dialog = null;
            }
            getChildFragmentManager().putFragment(outState,"supportMapFragment",supportMapFragment);
            try{
                outState.putParcelable("CameraPos",mGoogleMap.getCameraPosition());
                outState.putParcelableArrayList("suggestedPlaceArrayList",suggestedPlaceArrayList);
            }catch (Exception e){
                e.printStackTrace();
            }

            super.onSaveInstanceState(outState);
        }

        public void customOnRestoreInstanceState(Bundle savedInstanceState){
            supportMapFragment = (SupportMapFragment) getChildFragmentManager().getFragment(savedInstanceState, "supportMapFragment");
            mGoogleMap = supportMapFragment.getMap();
            suggestedPlaceArrayList = savedInstanceState.getParcelableArrayList("suggestedPlaceArrayList");
            CameraPosition cameraPosition = (CameraPosition) savedInstanceState.getParcelable("CameraPos");
            restoreMapState(cameraPosition);
        }



        @Override
        public void onDestroy() {
            if (dialog != null) {
                dialog.dismiss();
            }
            Log.d("LifeCycle","destroy instance state");
            super.onDestroy();
        }

    }


}
