package nghia.minh.tran.ulogger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Task4 extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task4);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new Task4Fragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task4, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class Task4Fragment extends Fragment implements
            GoogleApiClient.ConnectionCallbacks ,
            GoogleApiClient.OnConnectionFailedListener{
        WifiManager wifiManager;
        ListView scanResultListView;
        ListView resultHistoryListView;
        String basepath;
        File folder;
        private String UUID;
        private GoogleApiClient mGoogleApiClient;
        private Date date;
        private ArrayList<ScanResult> WAPs;
        private ArrayAdapter<String> scanResultArrayAdapter;
        private ArrayList<WifiScanHistory> historyScanResults;
        private Button saveResultButton;
        private AlertDialog dialog;
        private String chosenCoord="";
        private TextView coordTextView;
        private View rootView;
        public Task4Fragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_task4, container, false);
            UUID = Settings.Secure.getString(getActivity().getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            Button scanWifiButton = (Button) rootView.findViewById(R.id.scanButton);
            scanWifiButton.setText("Scan wifi");
            saveResultButton = (Button) rootView.findViewById(R.id.saveResultButton);
            scanResultListView = (ListView) rootView.findViewById(R.id.scanResultListView);
            resultHistoryListView = (ListView) rootView.findViewById(R.id.resultHistoryListView);
            coordTextView = (TextView) rootView.findViewById(R.id.placeCoordinateHistoryTextView);
            TextView resultTextView = (TextView) rootView.findViewById(R.id.resultTextView);
            resultTextView.setText("Wifi scan result history");
            saveResultButton.setEnabled(false);

            // back-end component
            mGoogleApiClient = new GoogleApiClient.Builder(rootView.getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();
            wifiManager = (WifiManager) rootView.getContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()){
                if(wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING){
                    wifiManager.setWifiEnabled(true);
                }
            }


            if (savedInstanceState!= null){
                WAPs = savedInstanceState.getParcelableArrayList("WAPs");
                chosenCoord = savedInstanceState.getString("chosenCoord");
                if (chosenCoord.equals("")){
                    scanResultListView.setBackgroundColor(Color.TRANSPARENT);
                    coordTextView.setVisibility(View.INVISIBLE);
                }else{
                    scanResultListView.setBackgroundColor(Color.parseColor("#BA68C8"));
                    coordTextView.setVisibility(View.VISIBLE);
                    coordTextView.setText(chosenCoord);
                }
            }else{
                WAPs = new ArrayList<>();
            }

            reassignListView(rootView);
            historyScanResults = new ArrayList<>();
            /// --- initialize basepath
            String workingDirectory = "uLoggerTask4";
            folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+workingDirectory);
            if (!folder.exists()){
                if (folder.mkdir()){
                    basepath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+workingDirectory;
                }else {
                    basepath = Environment.getExternalStorageDirectory().getAbsolutePath();
                }
            }else{
                basepath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+workingDirectory;
            }


            scanWifiButton.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    wifiManager.startScan();
                    saveResultButton.setEnabled(false);
                    WAPs = (ArrayList) wifiManager.getScanResults();
                    date = new Date();
                    Toast.makeText(getActivity(), WAPs.size() + "", Toast.LENGTH_SHORT).show();
                    scanResultListView.setBackgroundColor(Color.TRANSPARENT);
                    coordTextView.setVisibility(View.INVISIBLE);
                    saveResultButton.setEnabled(true);
                    reassignListView(rootView);
                    chosenCoord="";

                    resultHistoryListView.setItemChecked(resultHistoryListView.getCheckedItemPosition(),false);
                }
            });
            scanMetaFile();
            resultHistoryListView.setAdapter(new ArrayAdapter<WifiScanHistory>(rootView.getContext(), android.R.layout.simple_list_item_activated_1, android.R.id.text1, historyScanResults));
            resultHistoryListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            resultHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    WifiScanHistory wifiScanHistory = historyScanResults.get(i);
                    WAPs.clear();
                    WAPs.addAll(wifiScanHistory.getScanResults());
                    resultHistoryListView.setItemChecked(i, true);

                    chosenCoord="Coordinate: "+wifiScanHistory.getLat()+","+wifiScanHistory.getLng();
                    coordTextView.setText(chosenCoord);
                    coordTextView.setVisibility(View.VISIBLE);
                    scanResultArrayAdapter.clear();
                    scanResultArrayAdapter.addAll(wapsToListViewAdapter(WAPs));
                    scanResultArrayAdapter.notifyDataSetChanged();
                    scanResultListView.setBackgroundColor(Color.parseColor("#BA68C8"));
                    saveResultButton.setEnabled(false);
                }
            });
            saveResultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater innerInflater = getActivity().getLayoutInflater();

                    AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                    final View dialogView = innerInflater.inflate(R.layout.dialog_layout, null);

                    builder.setView(dialogView)
                            .setTitle("Save scan result")
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    String name = ((TextView) dialogView.findViewById(R.id.nameEditText)).getText()+"";
                                    String description = ((TextView) dialogView.findViewById(R.id.descriptionEditText)).getText()+"";
                                    saveToMetaFile(name, description);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });

                    dialog = builder.create();
                    dialog.show();

                }
            });
            return rootView;
        }
        private void reassignListView(View rootView){
            scanResultArrayAdapter = new ArrayAdapter<String>(rootView.getContext(),android.R.layout.simple_list_item_1,android.R.id.text1,wapsToListViewAdapter(WAPs));
            scanResultListView.setAdapter(scanResultArrayAdapter);
            scanResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ScanResult wap = WAPs.get(i);
                    showScanResultPrompt(wap);
                }
            });
        }
        private ArrayList<String> wapsToListViewAdapter(ArrayList<ScanResult> waps){
            ArrayList<String> result = new ArrayList<String>();
            for (ScanResult wap:waps){
                result.add(wap.SSID+":  "+wap.level+" dB");
            }
            return result;
        }
        private void showScanResultPrompt(ScanResult wap){
            LayoutInflater innerInflater = getActivity().getLayoutInflater();

            AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
            final View dialogView = innerInflater.inflate(R.layout.history_wifi_scan_result, null);
            ((TextView) dialogView.findViewById(R.id.ssidTextView)).setText(wap.SSID);
            ((TextView) dialogView.findViewById(R.id.bssidTextView)).setText(wap.BSSID);
            ((TextView) dialogView.findViewById(R.id.capabilitiesTextView)).setText(wap.capabilities);
            ((TextView) dialogView.findViewById(R.id.levelTextView)).setText(wap.level+" dB");
            ((TextView) dialogView.findViewById(R.id.frequencyInfoTextView)).setText(wap.frequency+" Hz");
            builder.setView(dialogView)
                    .setTitle("Save scan result")
                    .setNegativeButton("Return", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            dialog = builder.create();
            dialog.show();
        }

        private void saveToMetaFile(String name,String description){
            File file = new File(basepath,UUID+"_"+date.getTime()+".wimeta");
            if (!file.exists()){
                try {
                    file.createNewFile();
                    PrintWriter printWriter = new PrintWriter(file);
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    String locationString = "0,0";
                    if (mLastLocation==null){
                        Toast.makeText(getActivity(),"Cannot find location, check your location service",Toast.LENGTH_SHORT).show();
                    }else{
                        locationString = mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
                        Toast.makeText(getActivity(),locationString,Toast.LENGTH_SHORT).show();
                    }
                    printWriter.println(name);
                    printWriter.println(description);
                    printWriter.println(locationString);
                    String arrayGson = new Gson().toJson(WAPs);
                    historyScanResults.add(new WifiScanHistory(name,description,date,mLastLocation.getLatitude()+"",mLastLocation.getLongitude()+"",WAPs));

                    printWriter.println(arrayGson);
                    printWriter.close();
                    saveResultButton.setEnabled(false);
                } catch (IOException e) {
                    Toast.makeText(getActivity(),"Error writing to storage, please check permission",Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void scanMetaFile(){
            Type type = new TypeToken<ArrayList<ScanResult>>() {}.getType();
            File[] history = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.matches(UUID+"_\\w+.wimeta");
                }
            });
            for (File file:history){
                String timestamp = file.getName().split("\\p{Punct}")[1];
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String name = bufferedReader.readLine();
                    String description = bufferedReader.readLine();
                    String[] location = bufferedReader.readLine().split(",");
                    String scanResultString = bufferedReader.readLine();
                    ArrayList<ScanResult> scanResultsFromFile = new Gson().fromJson(scanResultString,type);
                    WifiScanHistory historyScanResult = new WifiScanHistory(name,description,new Date(Long.parseLong(timestamp)),location[0],location[1],scanResultsFromFile);
                    historyScanResults.add(historyScanResult);

                    Log.d("History", historyScanResult.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("File", timestamp);
                Log.d("FileNameAsDate",new Date(Long.parseLong(timestamp)).toString());
            }

        }

        @Override
        public void onConnected(Bundle bundle) {

        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (dialog != null){
                dialog.dismiss();
            }
            outState.putString("chosenCoord",chosenCoord);
            outState.putParcelableArrayList("WAPs", WAPs);
            super.onSaveInstanceState(outState);
        }

        private class WifiScanHistory implements Parcelable {
            String name;
            String description;
            String lat;
            String lng;
            ArrayList<ScanResult> scanResults;
            Date scanDate;

            public WifiScanHistory(String name,String description,Date scanDate, String lat, String lng, ArrayList<ScanResult> scanResults){
                this.name = name;
                this.description = description;
                this.lat = lat;
                this.lng = lng;
                this.scanResults = scanResults;
                this.scanDate = scanDate;
            }

            public String getName() {
                return name;
            }

            public String getDescription() {
                return description;
            }

            public String getLng() {
                return lng;
            }

            public String getLat() {
                return lat;
            }
            public ArrayList<ScanResult> getScanResults() {
                return scanResults;
            }

            public Date getScanDate() {
                return scanDate;
            }

            @Override
            public String toString() {
                return name +" on "+new SimpleDateFormat("dd/MM/yy 'at' HH:mm:ss a").format(scanDate);

            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel parcel, int i) {

            }
        }



    }
}
