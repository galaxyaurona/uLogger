package nghia.minh.tran.ulogger;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;


/**
 * An activity representing a list of Tasks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,

 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TaskListFragment} and the item details

 * <p/>
 * This activity also implements the required
 * {@link TaskListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class TaskListActivity extends FragmentActivity
        implements TaskListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Task2.Task2Fragment task2Fragment;
    private Welcome.WelcomeFragment welcomeFragment;
    private Fragment fragment; // variable point to current fragment
    private ToBeImplement.PlaceholderFragment placeholderFragment;
    private Task4.Task4Fragment task4Fragment;
    private Task5.Task5Fragment task5Fragment;
    private Task6.Task6Fragment task6Fragment;
    private Task7.Task7Fragment task7Fragment;
    private Task10.Task10Fragment task10Fragment;
    private final  String[] tasks = {"Task 02: Location","Task 04: Wifi",
            "Task 05: Bluetooth","Task 06: Picture","Task 07: Audio","Task 09: Indoor positions"};
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        if (findViewById(R.id.task_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            // instantiate static fragment for 2 pane layout
            if (savedInstanceState == null){
                task2Fragment = new Task2.Task2Fragment();
                welcomeFragment = new Welcome.WelcomeFragment();
                placeholderFragment = new ToBeImplement.PlaceholderFragment();
                task4Fragment = new Task4.Task4Fragment();
                task5Fragment = new Task5.Task5Fragment();
                task6Fragment = new Task6.Task6Fragment();
                task7Fragment = new Task7.Task7Fragment();
                //task10Fragment = new Task10.Task10Fragment();
                fragment = welcomeFragment;
                initializeFragmentOrder();
                reoderFragment();
            }





            // initialize first order fragment order otherwise overlap


        }

        createListView();


    }
    private void initializeFragmentOrder() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.task_detail_container, task2Fragment)
                .hide(task2Fragment)
                .add(R.id.task_detail_container, task4Fragment)
                .hide(task4Fragment)
                .add(R.id.task_detail_container, task5Fragment)
                .hide(task5Fragment)
                .add(R.id.task_detail_container, task6Fragment)
                .hide(task6Fragment)
                .add(R.id.task_detail_container, task7Fragment)
                .hide(task7Fragment)
                //.add(R.id.task_detail_container, task10Fragment)
                //.hide(task10Fragment)
                .add(R.id.task_detail_container, placeholderFragment)
                .hide(placeholderFragment)
                .add(R.id.task_detail_container, welcomeFragment)
                .hide(welcomeFragment)
                .commit();
    }

    private void reoderFragment(){
        getSupportFragmentManager()
                .beginTransaction()
                .hide(welcomeFragment)
                .hide(placeholderFragment)
                .hide(task2Fragment)
                .hide(task4Fragment)
                .hide(task5Fragment)
                .hide(task6Fragment)
                .hide(task7Fragment)
                //.hide(task10Fragment)
                .show(fragment)
                .commit();
    }
    private void hideFragment(){
        getSupportFragmentManager()
                .beginTransaction()
                .hide(fragment)
                .hide(placeholderFragment)
                .hide(task2Fragment)
                .hide(task4Fragment)
                .hide(task5Fragment)
                .hide(task6Fragment)
                .hide(task7Fragment)
                //.hide(task10Fragment)
                .commit();
    }

    private void createListView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        TaskListFragment listFragment = (TaskListFragment) fragmentManager.findFragmentByTag("TaskList");
        if (listFragment == null) {

            listFragment = new TaskListFragment();
            Bundle arguments = new Bundle();
            // send task list to the fragment to display
            arguments.putStringArray("tasks", tasks);
            //set list fragment
            listFragment.setArguments(arguments);
            // make the listfragment, inflate with tag for retreival
            fragmentManager.beginTransaction().replace(R.id.task_list, listFragment, "TaskList").commit();

        }
    }
    /**
     * Callback method from {@link TaskListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        String taskId = id.substring(5,7); // take out the real string
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            hideFragment();

            switch (taskId) {
                case "02":Log.d("Task","Task 2");fragment = task2Fragment ;break;
                case "04":Log.d("Task","Task 4");fragment = task4Fragment ;break;
                case "05":Log.d("Task","Task 4");fragment = task5Fragment ;break;
                case "06":Log.d("Task","Task 4");fragment = task6Fragment ;break;
                case "07":Log.d("Task","Task 4");fragment = task7Fragment ;break;
                //case "09":Log.d("Task","Task 4");fragment = task10Fragment ;break;
                //case "04":Log.d("Task","Task 4");fragment = task4Fragment ;break;
                default: fragment = placeholderFragment;
            }
            // call this function to reorder fragment instead of destroy fragment , save state
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(fragment)
                    .commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent;
            switch (taskId) {
                case "02":
                    detailIntent = new Intent(this, Task2.class);
                    break;
                case "04":
                    detailIntent = new Intent(this, Task4.class);
                    break;
                case "05":
                    detailIntent = new Intent(this, Task5.class);
                    break;
                case "06":
                    detailIntent = new Intent(this, Task6.class);
                    break;
                case "07":
                    detailIntent = new Intent(this, Task7.class);
                    break;
                /*case "09":
                    detailIntent = new Intent(this, Task10.class);
                    break;*/
                default: detailIntent = new Intent(this, ToBeImplement.class);
            }
            // just reorder to front, don't create new activity if don't need
            detailIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            // start activity with flag
            startActivity(detailIntent);
        }

    }
    //This method to persist the fragment on orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (mTwoPane) {
            // if fragment hasn't been select, then point it to welcome screen
            if (fragment==null)
                fragment = welcomeFragment;
            // saving fragment to retreive later
            getSupportFragmentManager().putFragment(outState,"welcomeFragment",welcomeFragment);
            getSupportFragmentManager().putFragment(outState,"task2Fragment",task2Fragment);
            getSupportFragmentManager().putFragment(outState,"task4Fragment",task4Fragment);
            getSupportFragmentManager().putFragment(outState,"task5Fragment",task5Fragment);
            getSupportFragmentManager().putFragment(outState,"task6Fragment",task6Fragment);
            getSupportFragmentManager().putFragment(outState,"task7Fragment",task7Fragment);
            //getSupportFragmentManager().putFragment(outState,"task10Fragment",task10Fragment);
            getSupportFragmentManager().putFragment(outState,"placeholderFragment",placeholderFragment);
            getSupportFragmentManager().putFragment(outState,"fragment",fragment);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        if (mTwoPane){
            welcomeFragment = (Welcome.WelcomeFragment) getSupportFragmentManager().getFragment(savedInstanceState,"welcomeFragment");
            task2Fragment = (Task2.Task2Fragment) getSupportFragmentManager().getFragment(savedInstanceState,"task2Fragment");
            task4Fragment = (Task4.Task4Fragment) getSupportFragmentManager().getFragment(savedInstanceState,"task4Fragment");
            task5Fragment = (Task5.Task5Fragment) getSupportFragmentManager().getFragment(savedInstanceState,"task5Fragment");
            task6Fragment = (Task6.Task6Fragment) getSupportFragmentManager().getFragment(savedInstanceState,"task6Fragment");
            task7Fragment = (Task7.Task7Fragment) getSupportFragmentManager().getFragment(savedInstanceState,"task7Fragment");
            //task10Fragment = (Task10.Task10Fragment) getSupportFragmentManager().getFragment(savedInstanceState,"task10Fragment");
            placeholderFragment = (ToBeImplement.PlaceholderFragment) getSupportFragmentManager().getFragment(savedInstanceState,"placeholderFragment");
            fragment = getSupportFragmentManager().getFragment(savedInstanceState,"fragment");
            reoderFragment();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
