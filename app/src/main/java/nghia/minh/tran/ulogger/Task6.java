package nghia.minh.tran.ulogger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Task6 extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task6);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new Task6Fragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task6, menu);
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
        if (HelperClass.handleBack(this, id)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class Task6Fragment extends Fragment implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {
        CameraManager cameraManager;
        final int TAKE_PICTURE = 1;
        File file;
        private View rootView;
        String basepath;
        File folder;
        private String UUID;
        private GoogleApiClient mGoogleApiClient;
        Camera mCamera;
        ImageView imageView;
        private Date date;
        private AlertDialog dialog;
        private ListView listView;
        private ArrayList<MyImageHistory> myImageHistories;
        private ArrayAdapter<MyImageHistory> myImageHistoryArrayAdapter;
        public Task6Fragment() {
        }
        private boolean newImage= false;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_task6, container, false);

            Button takePictureButton = (Button) rootView.findViewById(R.id.takePictureButton);

            listView = (ListView) rootView.findViewById(R.id.imageHistoryListView);

            UUID = Settings.Secure.getString(getActivity().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            takePictureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    date = new Date();
                    file = new File(basepath, date.getTime() + ".jpg");
                    Uri outputFileUri = Uri.fromFile(file);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // use this specific intent specification
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri); // specify the file in which to put the captured image
                    startActivityForResult(intent, TAKE_PICTURE);
                }
            });

            mGoogleApiClient = new GoogleApiClient.Builder(rootView.getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();

            String workingDirectory = "uLoggerTask6";
            folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + workingDirectory);
            if (!folder.exists()) {
                if (folder.mkdir()) {
                    basepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + workingDirectory;
                } else {
                    basepath = Environment.getExternalStorageDirectory().getAbsolutePath();
                }
            } else {
                basepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + workingDirectory;
            }
            if (savedInstanceState != null){
                file = new File(savedInstanceState.getString("file"));
                myImageHistories = savedInstanceState.getParcelableArrayList("myImageHistories");
                if (!file.getAbsolutePath().equals("/"))
                    setImageView();
                date = new Date(savedInstanceState.getLong("date"));
            }else{
                file = new File("");
                date = new Date();
                myImageHistories = new ArrayList<>();
            }

            myImageHistoryArrayAdapter = new ArrayAdapter<MyImageHistory>(getActivity(),android.R.layout.simple_list_item_1,android.R.id.text1,myImageHistories);
            listView.setAdapter(myImageHistoryArrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    MyImageHistory myImageHistory = myImageHistories.get(i);

                    LayoutInflater innerInflater = getActivity().getLayoutInflater();

                    AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                    final View dialogView = innerInflater.inflate(R.layout.history_image_prompt, null);
                    ((ImageView) dialogView.findViewById(R.id.imageHistoryView)).setImageURI(Uri.fromFile(new File(myImageHistory.getFilePath())));
                    ((TextView)dialogView.findViewById(R.id.imageDescriptionTextView)).setText("Description: "+myImageHistory.getDescription());
                    ((TextView)dialogView.findViewById(R.id.imageCoordinateTextView)).setText("Coord: "+myImageHistory.getLat()+","+myImageHistory.getLng());
                    builder.setView(dialogView)
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
            scanMetaFile();
            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == TAKE_PICTURE) {
                if (resultCode == RESULT_OK) {
                    // assume there is image view in this example, with id “imageView1”
                    LayoutInflater innerInflater = getActivity().getLayoutInflater();

                    AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                    final View dialogView = innerInflater.inflate(R.layout.dialog_layout, null);
                    builder.setView(dialogView)
                            .setTitle("Save captured image")
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
                                    dialog.cancel();
                                }
                            });
                    setImageView();
                    dialog = builder.create();
                    dialog.show();
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            if (file.exists())
                                file.delete();
                        }
                    });
                }
            }
        }

        private void scanMetaFile(){

            File[] history = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.matches(UUID+"_\\w+.immeta");
                }
            });
            myImageHistoryArrayAdapter.clear();
            for (File file:history){
                Long timestamp = Long.parseLong(file.getName().split("\\p{Punct}")[1]);
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String name = bufferedReader.readLine();
                    String description = bufferedReader.readLine();
                    String[] location = bufferedReader.readLine().split(",");
                    String filepath = bufferedReader.readLine();

                    MyImageHistory myImageHistory = new MyImageHistory(name,description,location[0],location[1],filepath, new Date(timestamp));
                    myImageHistoryArrayAdapter.add(myImageHistory);


                } catch (Exception e) {
                    e.printStackTrace();
                }
                myImageHistoryArrayAdapter.notifyDataSetChanged();

            }

        }

        private void saveToMetaFile(String name,String description){

            File metaFile = new File(basepath,UUID+"_"+date.getTime()+".immeta");
            if (!metaFile.exists()){
                try {
                    metaFile.createNewFile();
                    PrintWriter printWriter = new PrintWriter(metaFile);
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    String locationString = "0,0";
                    if (mLastLocation==null){
                        Toast.makeText(getActivity(), "Cannot find location, check your location service", Toast.LENGTH_SHORT).show();

                    }else{
                        locationString = mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
                        Toast.makeText(getActivity(),locationString,Toast.LENGTH_SHORT).show();
                    }
                    printWriter.println(name);
                    printWriter.println(description);
                    printWriter.println(locationString);
                    printWriter.println(file.getAbsolutePath());
                    printWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),"Error writing to storage, please check permission",Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void setImageView(){
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
            if (dialog != null)
                dialog.dismiss();
            Log.d("LifeCycle","Saved instance "+file.getAbsolutePath());
            outState.putString("file", file.getAbsolutePath());
            outState.putParcelableArrayList("myImageHistories", myImageHistories);
            outState.putLong("date",date.getTime());
            super.onSaveInstanceState(outState);
        }

        private class MyImageHistory implements Parcelable{
            private String name;
            private String description;
            private String lat;
            private String lng;
            private String filePath;
            private Date takenDate;

            private MyImageHistory(String name, String description, String lat, String lng, String filePath, Date takenDate) {
                this.name = name;
                this.description = description;
                this.lat = lat;
                this.lng = lng;
                this.filePath = filePath;
                this.takenDate = takenDate;
            }
            public MyImageHistory(Parcel in){
                String[] data = new String[5];

                in.readStringArray(data);
                this.name = data[0];
                this.description = data[1];
                this.lat = data[2];
                this.lng = data[3];
                this.filePath = data[4];
                this.takenDate = (Date) in.readSerializable();
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

            public String getFilePath() {
                return filePath;
            }

            public String getLat() {
                return lat;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public String toString() {
                return name+" on "+ new SimpleDateFormat("dd/MM/yy 'at' HH:mm:ss a").format(takenDate);
            }

            @Override
            public void writeToParcel(Parcel parcel, int i) {
                parcel.writeStringArray(new String[] {this.name,
                        this.description,
                        this.lat,
                        this.lng,
                        this.filePath});
                parcel.writeSerializable(takenDate);
            }
            public final Creator<MyImageHistory> CREATOR
                    = new Creator<MyImageHistory>() {
                public MyImageHistory createFromParcel(Parcel in) {
                    return new MyImageHistory(in);
                }

                public MyImageHistory[] newArray(int size) {
                    return new MyImageHistory[size];
                }
            };
        }
    }
}
