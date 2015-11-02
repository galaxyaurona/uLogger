package nghia.minh.tran.ulogger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

/**
 * Created by Oguri on 16/10/2015.
 */
public abstract class HelperClass {
    public static boolean handleBack(Activity activity, int id){
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // NavUtils.navigateUpTo(this, new Intent(this, TaskListActivity.class));
            //moveTaskToBack(true);
            Intent intent = new Intent(activity, TaskListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
            return true;
        }
        return false;
    };
    public static void onBackPressed(Activity activity){
        Intent intent = new Intent(activity, TaskListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    };
    public static void showToast(Activity activity,String message){
        if (activity != null){
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
    };
    public static String createWorkingDirectory(String workingDirectory){
        String basepath = "";
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+workingDirectory);
        if (!folder.exists()){
            if (folder.mkdir()){
                basepath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+workingDirectory;
            }else {
                basepath = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }else{
            basepath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+workingDirectory;
        }
        return basepath;
    };
}
