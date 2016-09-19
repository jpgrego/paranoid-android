package com.jpgrego.thesisapp.thesisapp.activities;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.jpgrego.thesisapp.thesisapp.R;
import com.jpgrego.thesisapp.thesisapp.fragments.WifiAndCellFragment;
import com.jpgrego.thesisapp.thesisapp.listeners.CellInfoListener;
import com.jpgrego.thesisapp.thesisapp.listeners.WifiInfoReceiver;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Process;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private static final String THESIS_DIR_NAME = "ThesisApp";
    private static final String LOG_FILE_NAME = "THESIS_APP_LOG.TXT";

    private CellInfoListener cellInfoListener;
    private WifiInfoReceiver wifiInfoReceiver;
    private TelephonyManager telephonyManager;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    //private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final ViewPager mViewPager;
        final WifiManager wifiManager;

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                final File thesisAppDir, logFile;
                final FileWriter fileWriter;
                final Process logProcess;
                BufferedWriter bufferedWriter = null;
                InputStreamReader inputStreamReader = null;
                char[] buffer = new char[10000];
                int readBytes;

                try {

                    Log.e(thread.getName(), Arrays.toString(ex.getStackTrace()));

                    thesisAppDir = new File(Environment.getExternalStorageDirectory()
                            + "/" + THESIS_DIR_NAME + "/");
                    logFile = new File(thesisAppDir + "/" + LOG_FILE_NAME);


                    if(!thesisAppDir.exists()) {
                        if(!thesisAppDir.mkdir()) {
                            throw new IOException("Unable to create ThesisApp dir");
                        }
                    } else if(!thesisAppDir.isDirectory()) {
                        throw new IOException("A file named ThesisApp already exists");
                    }

                    if(!logFile.exists()) {
                        if(logFile.createNewFile()) {
                            throw new IOException("Unable to create log file");
                        }
                    } else if(!logFile.isFile()) {
                        throw new IOException(LOG_FILE_NAME + " already exists");
                    }

                    fileWriter = new FileWriter(logFile);
                    bufferedWriter = new BufferedWriter(fileWriter);
                    logProcess = Runtime.getRuntime().exec("logcat -t 1000 -v time");
                    inputStreamReader = new InputStreamReader(logProcess.getInputStream());

                    while((readBytes = inputStreamReader.read(buffer, 0, buffer.length)) > -1) {
                        bufferedWriter.write(buffer, 0, readBytes);
                    }

                    bufferedWriter.close();
                    inputStreamReader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if(inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.exit(1);
            }
        });


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        cellInfoListener = new CellInfoListener(telephonyManager);
        wifiInfoReceiver = new WifiInfoReceiver(wifiManager);
        mTitle = getTitle();
        mViewPager = (ViewPager) findViewById(R.id.pager);

        if(mViewPager != null) {
            mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        }

        telephonyManager.listen(cellInfoListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        registerReceiver(wifiInfoReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(wifiInfoReceiver,
                new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

        /*
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        */
        //mRefreshHandler.postDelayed(mUpdateCellData, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        telephonyManager.listen(cellInfoListener, PhoneStateListener.LISTEN_NONE);
        unregisterReceiver(wifiInfoReceiver);
    }

    /*
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager;
        Fragment fragment;

        fragmentManager = getSupportFragmentManager();

        mRefreshHandler.removeCallbacksAndMessages(null);
        switch(position) {
            default:
            case 0:
                fragment = new WifiAndCellFragment();
                registeredCellFragment = (WifiAndCellFragment) fragment;
                mRefreshHandler.postDelayed(mUpdateCellData, 1000);
                break;
            case 1:
                fragment = new TestFragment2();
                break;
        }


        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }
    */

    public void restoreActionBar() {
        /*
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
        */
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        */
        return super.onCreateOptionsMenu(menu);
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

    public CellInfoListener getCellInfoListener() {
        return cellInfoListener;
    }

    public WifiInfoReceiver getWifiInfoReceiver() {
        return wifiInfoReceiver;
    }


/*
    *//**
     * A placeholder fragment containing a simple view.
     *//*
    public static class PlaceholderFragment extends Fragment {
        *//**
         * The fragment argument representing the section number for this
         * fragment.
         *//*
        private static final String ARG_SECTION_NUMBER = "section_number";

        *//**
         * Returns a new instance of this fragment for the given section
         * number.
         *//*
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }*/

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new WifiAndCellFragment();

                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    }
}
