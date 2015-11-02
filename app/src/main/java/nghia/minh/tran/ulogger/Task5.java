package nghia.minh.tran.ulogger;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.BleApi;
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
import java.util.List;


public class Task5 extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task5);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new Task5Fragment())
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task5, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        HelperClass.onBackPressed(this);
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
    public static class Task5Fragment extends Fragment implements
            GoogleApiClient.ConnectionCallbacks ,
            GoogleApiClient.OnConnectionFailedListener{

        private static final int ENABLE_BLUETOOTH = 1;
        ListView scanResultListView;
        BluetoothManager bluetoothManager;
        BluetoothAdapter bluetoothAdapter;
        BluetoothLeScanner bluetoothLeScanner;
        ArrayList<String> devicesName = new ArrayList<>();
        ArrayList<BluetoothDevice> devices = new ArrayList<>();
        String basepath;
        File folder;
        ListView resultHistoryListView;
        BroadcastReceiver finishDiscovering;
        BroadcastReceiver discoveryResult;
        BroadcastReceiver startDiscovering;
        private ArrayAdapter<BlueToothScanResult> historyArrayAdapter;
        private String chosenCoord="";
        private TextView coordTextView;
        private AlertDialog dialog;
        private Button saveResultButton;
        private GoogleApiClient mGoogleApiClient;
        private String UUID;
        private Date date;
        private ArrayList<BluetoothObject> BTDs = new ArrayList<>();
        private ArrayAdapter<ScanResult> scanResultArayAdapter;
        private ArrayList<BlueToothScanResult> historyScanResults;
        //private ArrayList<WifiScanResult> historyScanResults;
        private View rootView;
        public Task5Fragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_task4, container, false);
            final Button scanBluetoothButton = (Button) rootView.findViewById(R.id.scanButton);

            historyScanResults = new ArrayList<>();

            saveResultButton = (Button) rootView.findViewById(R.id.saveResultButton);
            mGoogleApiClient = new GoogleApiClient.Builder(rootView.getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            saveResultButton.setEnabled(false);
            String workingDirectory = "uLoggerTask5";
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
            coordTextView = (TextView) rootView.findViewById(R.id.placeCoordinateHistoryTextView);
            scanResultListView = (ListView) rootView.findViewById(R.id.scanResultListView);
            if (savedInstanceState != null){
                BTDs = savedInstanceState.getParcelableArrayList("BTDs");
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
                BTDs = new ArrayList<>();
                scanBluetoothButton.setText("Start scanning bluetooth");
            }

            UUID = Settings.Secure.getString(getActivity().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
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

                    AlertDialog dialog = builder.create();
                    dialog.show();


                    saveResultButton.setEnabled(false);
                }
            });

            TextView resultTextView = (TextView) rootView.findViewById(R.id.resultTextView);
            resultTextView.setText("Bluetooth scan result history");

            bluetoothManager = (BluetoothManager) rootView.getContext().getSystemService(BLUETOOTH_SERVICE);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (!bluetoothAdapter.isEnabled()){
                if (!(bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON))
                    bluetoothAdapter.enable();
            };
            final ArrayAdapter<BluetoothObject> arrayAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,android.R.id.text1,BTDs);
            scanResultListView.setAdapter(arrayAdapter);
            scanResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    BluetoothObject bluetoothObject = arrayAdapter.getItem(i);
                    showScanResultPrompt(bluetoothObject);
                }
            });
            resultHistoryListView = (ListView) rootView.findViewById(R.id.resultHistoryListView);
            historyScanResults = new ArrayList<>();
            historyArrayAdapter = new ArrayAdapter<BlueToothScanResult>(rootView.getContext(),android.R.layout.simple_list_item_1,android.R.id.text1,historyScanResults);
            resultHistoryListView.setAdapter(historyArrayAdapter);
            resultHistoryListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            resultHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    BlueToothScanResult blueToothScanResult = historyScanResults.get(i);
                    BTDs.clear();
                    BTDs.addAll(blueToothScanResult.getScanResults());
                    chosenCoord="Coordinate: "+blueToothScanResult.getLat()+","+blueToothScanResult.getLng();
                    coordTextView.setText(chosenCoord);
                    arrayAdapter.clear();
                    arrayAdapter.addAll(blueToothScanResult.getScanResults());
                    arrayAdapter.notifyDataSetChanged();
                    scanResultListView.setBackgroundColor(Color.parseColor("#BA68C8"));
                    coordTextView.setVisibility(View.VISIBLE);
                }
            });
            scanMetaFile();
            discoveryResult = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String remoteDeviceName = intent
                            .getStringExtra(BluetoothDevice.EXTRA_NAME);
                    BluetoothDevice remoteDevice = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d("BluetoothClientActivity", "Discovered " + devices);

                    if (remoteDeviceName != null){

                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        //BTDs.add(intent)

                        arrayAdapter.add(new BluetoothObject(device));
                        arrayAdapter.notifyDataSetChanged();
                    }

                }
            };

            finishDiscovering = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    scanBluetoothButton.setText("Start scanning bluetooth");
                    saveResultButton.setEnabled(true);
                    resultHistoryListView.setEnabled(true);
                };
            };
            rootView.getContext().registerReceiver(finishDiscovering,new IntentFilter(
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
            startDiscovering = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    scanBluetoothButton.setText("Stop scanning bluetooth");
                    chosenCoord = "";
                    scanResultListView.setBackgroundColor(Color.TRANSPARENT);
                    coordTextView.setVisibility(View.INVISIBLE);
                    saveResultButton.setEnabled(false);
                    resultHistoryListView.setEnabled(false);
                };
            };
            rootView.getContext().registerReceiver(startDiscovering,new IntentFilter(
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED));


            scanBluetoothButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (bluetoothAdapter.isDiscovering()){
                        Toast.makeText(rootView.getContext(),"Cancel discovering",Toast.LENGTH_LONG).show();
                        bluetoothAdapter.cancelDiscovery();
                        resultHistoryListView.setEnabled(false);
                    }else{
                        Toast.makeText(rootView.getContext(),"Start discovering",Toast.LENGTH_LONG).show();
                        arrayAdapter.clear();
                        arrayAdapter.notifyDataSetChanged();
                        bluetoothAdapter.startDiscovery();
                        view.getRootView().getContext().registerReceiver(discoveryResult,new IntentFilter(
                                BluetoothDevice.ACTION_FOUND));
                        resultHistoryListView.setEnabled(true);

                    }


                }
            });

            return rootView;
        }
        private void showScanResultPrompt(BluetoothObject bluetoothObject){
            LayoutInflater innerInflater = getActivity().getLayoutInflater();

            AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
            final View dialogView = innerInflater.inflate(R.layout.history_bluetooth_scan_result, null);
            ((TextView) dialogView.findViewById(R.id.bluetoothAddressTextView)).setText(bluetoothObject.getAddress());
            ((TextView) dialogView.findViewById(R.id.bluetoothClassTextView)).setText(bluetoothObject.getDeviceClass());
            ((TextView) dialogView.findViewById(R.id.bluetoothNameTextView)).setText(bluetoothObject.getName());
            ((TextView) dialogView.findViewById(R.id.bluetoothTypeTextView)).setText(bluetoothObject.getType());
            builder.setView(dialogView)
                    .setTitle("Save scan result")
                    .setNegativeButton("Return", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            dialog = builder.create();
            dialog.show();
        }

        @Override
        public void onPause() {
            /*try{
                getActivity().unregisterReceiver(finishDiscovering);
                getActivity().unregisterReceiver(startDiscovering);
                getActivity().unregisterReceiver(discoveryResult);
            }catch (IllegalArgumentException e){
                Log.d("Broadcast receiver","Illegal argument");
            }*/

            super.onPause();
        }

        private void saveToMetaFile(String name,String description){
            date = new Date();
            File file = new File(basepath,UUID+"_"+date.getTime()+".blumeta");
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
                    historyScanResults.add(new BlueToothScanResult(name, description, date, mLastLocation.getLatitude() + "", mLastLocation.getLongitude() + "", BTDs));
                    historyArrayAdapter.notifyDataSetChanged();
                    String arrayGson = new Gson().toJson(BTDs);
                    printWriter.println(arrayGson);
                    printWriter.close();
                    saveResultButton.setEnabled(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),"Error writing to storage, please check permission",Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void scanMetaFile(){
            Type type = new TypeToken<ArrayList<BluetoothObject>>() {}.getType();
            File[] history = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.matches(UUID+"_\\w+.blumeta");
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
                    ArrayList<BluetoothObject> scanResultsFromFile = new Gson().fromJson(scanResultString,type);
                    BlueToothScanResult historyScanResult = new BlueToothScanResult(name,description,new Date(Long.parseLong(timestamp)),location[0],location[1],scanResultsFromFile);
                    historyScanResults.add(historyScanResult);

                    Log.d("History",historyScanResult.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("File", timestamp);
                Log.d("FileNameAsDate",new Date(Long.parseLong(timestamp)).toString());
            }

        }
        private class BluetoothObject implements Parcelable {
            private String name;
            private String address;
            private String type;
            private String deviceClass;

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            public BluetoothObject(BluetoothDevice device){
                this.name = device.getName();
                this.address = device.getAddress();
                this.type = deviceTypeTranslator(device.getType());

                this.deviceClass = deviceClassTranslator(device.getBluetoothClass());
            }

            public BluetoothObject(Parcel in){
                String[] data = new String[5];

                in.readStringArray(data);
                this.name = data[0];
                this.address = data[1];
                this.deviceClass = data[2];
                this.type = data[3];
            }

            public String getName() {
                return name;
            }

            public String getAddress() {
                return address;
            }

            public String getType() {
                return type;
            }

            public String getDeviceClass() {
                return deviceClass;
            }



            private String deviceTypeTranslator(int deviceType) {
                switch (deviceType){
                    case BluetoothDevice.DEVICE_TYPE_CLASSIC: return "Classic";
                    case BluetoothDevice.DEVICE_TYPE_DUAL: return "Dual";
                    case BluetoothDevice.DEVICE_TYPE_LE: return "Low energy";
                    default: return "Unknown";
                }

            }

            ;
            public String deviceClassTranslator(BluetoothClass bluetoothClass){
                switch (bluetoothClass.getMajorDeviceClass()){
                    case BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER:
                    case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                    case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                    case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES:
                    case BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO:
                    case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
                    case BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE:
                    case BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO:
                    case BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX:
                    case BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED:
                    case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CAMERA:
                    case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                        return "Multimedia";
                    case BluetoothClass.Device.COMPUTER_DESKTOP:
                    case BluetoothClass.Device.COMPUTER_LAPTOP:
                    case BluetoothClass.Device.COMPUTER_SERVER:
                    case BluetoothClass.Device.COMPUTER_UNCATEGORIZED:
                    case BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA:
                    case BluetoothClass.Device.COMPUTER_WEARABLE:

                        return "Computer";
                    case BluetoothClass.Device.PHONE_SMART:
                    case BluetoothClass.Device.PHONE_CELLULAR:
                    case BluetoothClass.Device.PHONE_UNCATEGORIZED:
                        return "Phone";
                    default:return "Unknown";
                }

            }

            @Override
            public String toString() {
                return name+":"+deviceClass;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel parcel, int i) {
                parcel.writeStringArray(new String[] {this.name,
                        this.address,
                        this.deviceClass,
                        this.type});
            }
            public final Creator<BluetoothObject> CREATOR
                    = new Creator<BluetoothObject>() {
                public BluetoothObject createFromParcel(Parcel in) {
                    return new BluetoothObject(in);
                }

                public BluetoothObject[] newArray(int size) {
                    return new BluetoothObject[size];
                }
            };
        }
        private class BlueToothScanResult{
            String name;
            String description;
            String lat;
            String lng;
            ArrayList<BluetoothObject> scanResults;
            Date scanDate;
            public BlueToothScanResult(String name, String description,Date scanDate,String lat,String lng,ArrayList<BluetoothObject> scanResults){
                this.name = name;
                this.description = description;
                this.lat = lat;
                this.lng = lng;
                this.scanResults = scanResults;
                this.scanDate = scanDate;
            }

            public String getLng() {
                return lng;
            }

            public String getLat() {
                return lat;
            }
            public ArrayList<BluetoothObject> getScanResults() {
                return scanResults;
            }

            public Date getScanDate() {
                return scanDate;
            }

            @Override
            public String toString() {
                return name+" on "+ new SimpleDateFormat("dd/MM/yy 'at' HH:mm:ss a").format(scanDate);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (dialog != null){
                dialog.dismiss();
            }
            outState.putString("chosenCoord",chosenCoord);
            outState.putParcelableArrayList("BTDs", BTDs);
            super.onSaveInstanceState(outState);
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

    }
}
