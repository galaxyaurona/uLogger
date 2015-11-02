package nghia.minh.tran.ulogger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Task7 extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task7);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new Task7Fragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task7, menu);
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
    public static class Task7Fragment extends Fragment implements GoogleApiClient.ConnectionCallbacks ,
            GoogleApiClient.OnConnectionFailedListener {
        MediaRecorder mediaRecorder;
        MediaPlayer mediaPlayer;
        boolean mStartRecording = false;
        Button recordAudioButton;
        String basepath = "";
        String currentFile = "";
        Boolean disablePlayback= true;
        Button playbackButton;
        Boolean mStartPlaying = false;
        String mFileName;
        File folder;
        TextView coordTextView;
        TextView descriptionTextView;
        ListView recordHistoryListView;
        ArrayList<MyRecordHistory> myRecordHistories;
        ArrayAdapter<MyRecordHistory> myRecordHistoryArrayAdapter;
        Date date;
        File file;
        File currentPlaybackFile;
        private String UUID;
        private GoogleApiClient mGoogleApiClient;

        public Task7Fragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_task7, container, false);
            Toast.makeText(getActivity(),"Welcome to Task 7",Toast.LENGTH_SHORT).show();
            recordAudioButton = (Button) rootView.findViewById(R.id.recordAudioButton);
            playbackButton = (Button) rootView.findViewById(R.id.playbackButton);
            recordHistoryListView = (ListView) rootView.findViewById(R.id.recordHistoryListView);
            coordTextView = (TextView) rootView.findViewById(R.id.recordHistoryCoordTextView);
            descriptionTextView = (TextView) rootView.findViewById(R.id.recordHistoryDescriptionTextView);

            mGoogleApiClient = new GoogleApiClient.Builder(rootView.getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            UUID = Settings.Secure.getString(getActivity().getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            String workingDirectory = "uLoggerTask7";
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
            mediaPlayer = new MediaPlayer();

            if (savedInstanceState != null){
                mFileName = savedInstanceState.getString("mFileName");
                currentPlaybackFile = new File(savedInstanceState.getString("currentPlaybackFile"));
                mStartRecording = savedInstanceState.getBoolean("mStartRecording");
                disablePlayback = savedInstanceState.getBoolean("disablePlayback");
                mStartPlaying = savedInstanceState.getBoolean("mStartPlaying");
            }
            recordAudioButton.setEnabled(mStartRecording || !mStartPlaying);
            playbackButton.setEnabled(false);
            myRecordHistories = new ArrayList<>();
            myRecordHistoryArrayAdapter = new ArrayAdapter<MyRecordHistory>(getActivity(),android.R.layout.simple_list_item_activated_1,android.R.id.text1,myRecordHistories);
            recordHistoryListView.setAdapter(myRecordHistoryArrayAdapter);
            recordHistoryListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            recordHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    recordHistoryListView.setItemChecked(i,true);
                    MyRecordHistory myRecordHistory = myRecordHistoryArrayAdapter.getItem(i);
                    HelperClass.showToast(getActivity(),myRecordHistory.filePath);
                    descriptionTextView.setText("Description: " + myRecordHistory.description);
                    coordTextView.setText("Coords: "+myRecordHistory.lat+","+myRecordHistory.lng);
                    playbackButton.setText("Play "+myRecordHistory.name);
                    currentPlaybackFile = new File(myRecordHistory.filePath);
                    playbackButton.setEnabled(true);
                }
            });
            scanMetaFile();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    MyRecordHistory myRecordHistory =myRecordHistoryArrayAdapter.getItem(recordHistoryListView.getCheckedItemPosition());
                    playbackButton.setText("Play "+myRecordHistory.name);

                    recordAudioButton.setEnabled(true);
                    mStartPlaying = false;
                    stopPlaying();
                }
            });




            playbackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mStartPlaying){
                        if (startPlaying()){
                            playbackButton.setText("Stop playing ");
                            recordHistoryListView.setEnabled(false);
                            recordAudioButton.setEnabled(false);
                            mStartPlaying = true;
                        }
                    }else{
                        stopPlaying();
                        MyRecordHistory myRecordHistory =myRecordHistoryArrayAdapter.getItem(recordHistoryListView.getCheckedItemPosition());
                        playbackButton.setText("Play "+myRecordHistory.name);
                        recordHistoryListView.setEnabled(true);
                        recordAudioButton.setEnabled(true);
                        mStartPlaying = false;
                    }
                }
            });


            recordAudioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mStartRecording) {
                        recordAudioButton.setText("Start recording");
                        recordHistoryListView.setEnabled(true);
                        stopRecording();
                        mStartRecording = false;
                        playbackButton.setEnabled(true);
                    } else {
                        String tempPath = startRecording();
                        if (!tempPath.equals("")) { // if successfully
                            currentFile = tempPath;
                            disablePlayback = true;
                            recordHistoryListView.setEnabled(false);
                            recordAudioButton.setText("Stop recording");
                            mStartRecording = true;
                            playbackButton.setEnabled(false);
                        }


                    }

                }
            });
            return rootView;
        }

        public String startRecording(){
            date = new Date();
            mFileName=  basepath +"/"+UUID+"_"+ date.getTime();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            file = new File(mFileName+".3pg");
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                return  mFileName; // return filename to play later
            } catch (IOException e) {
                Toast.makeText(getActivity(),"Error start recording IOException",Toast.LENGTH_SHORT).show();
                Log.e("MediaRecorder", "prepare() b4 recording failed");
                return  "";
            }

        }
        private void scanMetaFile(){

            File[] history = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.matches(UUID+"_\\w+.rcmeta");
                }
            });
            myRecordHistoryArrayAdapter.clear();
            for (File file:history){
                Long timestamp = Long.parseLong(file.getName().split("\\p{Punct}")[1]);
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String name = bufferedReader.readLine();
                    String description = bufferedReader.readLine();
                    String[] location = bufferedReader.readLine().split(",");
                    String filepath = bufferedReader.readLine();

                    MyRecordHistory myRecordHistory = new MyRecordHistory(name,description,location[0],location[1],filepath, new Date(timestamp));
                    myRecordHistoryArrayAdapter.add(myRecordHistory);


                } catch (Exception e) {
                    e.printStackTrace();
                }
                myRecordHistoryArrayAdapter.notifyDataSetChanged();

            }

        }
        private void showPrompt(){
            LayoutInflater innerInflater = getActivity().getLayoutInflater();

            final AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
            final View promptView = innerInflater.inflate(R.layout.dialog_layout, null);
            final Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


            builder.setView(promptView)
                    .setTitle("Save recording result")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            EditText descriptionEditText = (EditText)promptView.findViewById(R.id.descriptionEditText);
                            EditText nameEditText = (EditText)promptView.findViewById(R.id.nameEditText);
                            File meta = new File(mFileName+".rcmeta");
                            if (!meta.exists()) {
                                try {
                                    meta.createNewFile();
                                    String locationString = "0,0";
                                    PrintWriter printWriter = new PrintWriter(meta);
                                    if (mLastLocation==null){
                                        Toast.makeText(getActivity(),"Cannot find location, check your location service",Toast.LENGTH_SHORT).show();
                                        myRecordHistoryArrayAdapter.add(new MyRecordHistory(nameEditText.getText()+"",descriptionEditText.getText()+"","0","0",file.getAbsolutePath(),date));
                                    }else{
                                        locationString = mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
                                        myRecordHistoryArrayAdapter.add(new MyRecordHistory(nameEditText.getText()+"",descriptionEditText.getText()+"",mLastLocation.getLatitude()+"",mLastLocation.getLongitude()+"",file.getAbsolutePath(),date));
                                    }
                                    printWriter.println(nameEditText.getText());
                                    printWriter.println(descriptionEditText.getText());
                                    printWriter.println(locationString);
                                    printWriter.println(file.getAbsolutePath());

                                    printWriter.close();
                                }catch (IOException e) {
                                    Toast.makeText(getActivity(),"Error writing to storage, please check permission",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();

        }

        private void stopRecording() {
            mediaRecorder.stop();
            showPrompt();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        private void stopPlaying() {
            recordHistoryListView.setEnabled(true);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        private boolean startPlaying(){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    MyRecordHistory myRecordHistory =myRecordHistoryArrayAdapter.getItem(recordHistoryListView.getCheckedItemPosition());
                    playbackButton.setText("Play "+myRecordHistory.name);
                    recordAudioButton.setEnabled(true);
                    mStartPlaying = false;
                    stopPlaying();
                }
            });
            try {
                mediaPlayer.setDataSource(currentPlaybackFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                return true;
            } catch (IOException e) {
                Log.e("MediaPlayer", "media player on playing prepare() failed");
                return false;
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (currentPlaybackFile != null)
                outState.putString("currentPlaybackFile",currentPlaybackFile.getAbsolutePath());
            else
                outState.putString("currentPlaybackFile", "");

            if (mFileName !=null)
                outState.putString("mFileName",mFileName);
            else
                outState.putString("mFileName", "");

            outState.putBoolean("mStartPlaying",mStartPlaying);
            outState.putBoolean("mStartRecording",mStartRecording);
            outState.putBoolean("disablePlayback",disablePlayback);

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
        private class MyRecordHistory implements Parcelable{
            private String name;
            private String description;
            private String lat;
            private String lng;
            private String filePath;
            private Date recordDate;

            public MyRecordHistory(Parcel in){

            }
            private MyRecordHistory(String name, String description, String lat, String lng, String filePath, Date recordDate) {
                this.name = name;
                this.description = description;
                this.lat = lat;
                this.lng = lng;
                this.filePath = filePath;
                this.recordDate = recordDate;
            }

            @Override
            public String toString() {
                return name+" on "+ new SimpleDateFormat("dd/MM/yy 'at' HH:mm:ss a").format(recordDate);
            }

            public final Creator<MyRecordHistory> CREATOR
                    = new Creator<MyRecordHistory>() {
                public MyRecordHistory createFromParcel(Parcel in) {
                    return new MyRecordHistory(in);
                }

                public MyRecordHistory[] newArray(int size) {
                    return new MyRecordHistory[size];
                }
            };

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
