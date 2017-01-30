package com.jpgrego.thesisapp.thesisapp.activities;

import android.content.Intent;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.jpgrego.thesisapp.thesisapp.R;
import com.jpgrego.thesisapp.thesisapp.fragments.MapFragment;
import com.jpgrego.thesisapp.thesisapp.fragments.SensorsFragment;
import com.jpgrego.thesisapp.thesisapp.fragments.WifiAndCellFragment;
import com.jpgrego.thesisapp.thesisapp.services.DataService;

import org.osmdroid.config.Configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Process;
import java.util.Arrays;


public final class MainActivity extends AppCompatActivity {

    private static final String THESIS_DIR_NAME = "ThesisApp";
    private static final String LOG_FILE_NAME = "THESIS_APP_LOG.TXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, DataService.class));
        //Thread.setDefaultUncaughtExceptionHandler(new ThesisAppExceptionHandler());
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            final Fragment wifiAndCellFragment = new WifiAndCellFragment();
            //wifiAndCellFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, wifiAndCellFragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int menuItemID;
        final FragmentManager fragmentManager;
        final Fragment fragment;

        menuItemID = item.getItemId();
        fragmentManager = getSupportFragmentManager();

        switch (menuItemID) {
            case R.id.action_radio:
                fragment = new WifiAndCellFragment();
                break;
            case R.id.action_sensors:
                fragment = new SensorsFragment();
                break;
            case R.id.action_map:
                fragment = new MapFragment();
                break;
            default:
                fragment = null;
        }

        if (fragment != null) {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }

        return super.onOptionsItemSelected(item);
    }

    private class ThesisAppExceptionHandler implements Thread.UncaughtExceptionHandler {
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


                if (!thesisAppDir.exists()) {
                    if (!thesisAppDir.mkdir()) {
                        throw new IOException("Unable to create ThesisApp dir");
                    }
                } else if (!thesisAppDir.isDirectory()) {
                    throw new IOException("A file named ThesisApp already exists");
                }

                if (!logFile.exists()) {
                    if (!logFile.createNewFile()) {
                        throw new IOException("Unable to create log file");
                    }
                } else if (!logFile.isFile()) {
                    throw new IOException(LOG_FILE_NAME + " already exists");
                }

                fileWriter = new FileWriter(logFile);
                bufferedWriter = new BufferedWriter(fileWriter);
                logProcess = Runtime.getRuntime().exec("logcat -t 1000 -v time");
                inputStreamReader = new InputStreamReader(logProcess.getInputStream());

                while ((readBytes = inputStreamReader.read(buffer, 0, buffer.length)) > -1) {
                    bufferedWriter.write(buffer, 0, readBytes);
                }

                bufferedWriter.close();
                inputStreamReader.close();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            System.exit(1);
        }
    }
}
