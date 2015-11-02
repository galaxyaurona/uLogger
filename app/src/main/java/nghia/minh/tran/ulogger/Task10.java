package nghia.minh.tran.ulogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class Task10 extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task10);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new Task10Fragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task10, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class Task10Fragment extends Fragment {
        private WifiManager wifiManager;
        private ListView wifiFingerPrintListView;
        public Task10Fragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_task10, container, false);
            wifiFingerPrintListView = (ListView) rootView.findViewById(R.id.wifiFingerPrintListView);
            wifiManager = (WifiManager) rootView.getContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()){
                if(wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING){
                    wifiManager.setWifiEnabled(true);
                }
            }
            wifiManager.startScan();
            BroadcastReceiver onWifiScanEnable = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    HelperClass.showToast(getActivity(), "Updated wifi access point");
                    ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,android.R.id.text1,new ArrayList<String>());
                    ArrayList<ScanResult> scanResultArrayList = (ArrayList<ScanResult>) wifiManager.getScanResults();
                    for (ScanResult scanResult: scanResultArrayList){
                        stringArrayAdapter.add(scanResult.BSSID+":"+scanResult.level+" dB");
                    }
                    wifiFingerPrintListView.setAdapter(stringArrayAdapter);
                }
            };
            rootView.getContext().registerReceiver(onWifiScanEnable,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            return rootView;
        }

    }
}
