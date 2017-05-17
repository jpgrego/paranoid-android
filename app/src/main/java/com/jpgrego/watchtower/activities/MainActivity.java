package com.jpgrego.watchtower.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.fragments.AppTrafficFragment;
import com.jpgrego.watchtower.fragments.MapFragment;
import com.jpgrego.watchtower.fragments.SensorsFragment;
import com.jpgrego.watchtower.fragments.WifiAndCellFragment;
import com.jpgrego.watchtower.listeners.USBEventsReceiver;
import com.jpgrego.watchtower.services.DataService;

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
        new USBEventsReceiver(this);

        if(savedInstanceState == null) {
            final Fragment wifiAndCellFragment = new WifiAndCellFragment();
            //wifiAndCellFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, wifiAndCellFragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final LayoutInflater inflator = getLayoutInflater();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);

            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);

            final View customView = inflator.inflate(R.layout.tabbed_menu, null);
            actionBar.setCustomView(customView, new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return true;
        } else {
            return false;
        }
    }

    public void actionBarButtonClicked(View view) {
        final int id = view.getId();
        final Fragment fragment;
        final FragmentManager fragmentManager = getSupportFragmentManager();

        switch(id) {
            case R.id.action_radio:
                fragment = new WifiAndCellFragment();
                break;
            case R.id.action_traffic:
                fragment = new AppTrafficFragment();
                break;
            case R.id.action_sensors:
                fragment = new SensorsFragment();
                break;
            case R.id.action_location:
                fragment = new MapFragment();
                break;
            default:
                fragment = null;
        }

        if (fragment != null) {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
    }

    public void appTrafficRowClick(View view) {
        final Dialog dialog = new Dialog(this);
        final TextView appUidView = (TextView) view.findViewById(R.id.app_uid);
        final TextView appNameView = (TextView) view.findViewById(R.id.app_name);
        final TextView appPackageNameView = (TextView) view.findViewById(R.id.app_package_name);
        final TextView transmittedBytesView = (TextView) view.findViewById(R.id.transmitted_mbytes);
        final TextView receivedBytesView = (TextView) view.findViewById(R.id.received_mbytes);
        final TextView transmittedPackagesView =
                (TextView) view.findViewById(R.id.transmitted_packages);
        final TextView receivedPackagesView = (TextView) view.findViewById(R.id.received_packages);

        final View detailsView = View.inflate(this, R.layout.app_traffic_details, null);

        final CharSequence packageName = appPackageNameView.getText();
        final CharSequence uid = appUidView.getText();
        final CharSequence transmittedBytes = transmittedBytesView.getText();
        final CharSequence receivedBytes = receivedBytesView.getText();
        final CharSequence transmittedPackages = transmittedPackagesView.getText();
        final CharSequence receivedPackages = receivedPackagesView.getText();

        try {
            final ImageView icon = (ImageView) detailsView.findViewById(R.id.app_icon);
            icon.setImageDrawable(getPackageManager().getApplicationIcon(packageName.toString()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final TextView detailsPackageName =
                (TextView) detailsView.findViewById(R.id.package_name_value);
        final TextView detailsUid = (TextView) detailsView.findViewById(R.id.uid_value);
        final TextView detailsTxMBytes = (TextView) detailsView.findViewById(R.id.txmbytes_value);
        final TextView detailsRxMBytes = (TextView) detailsView.findViewById(R.id.rxmbytes_value);
        final TextView detailsTxPkgs = (TextView) detailsView.findViewById(R.id.txpkg_value);
        final TextView detailsRxPkgs = (TextView) detailsView.findViewById(R.id.rxpkg_value);

        detailsPackageName.setText(packageName);
        detailsUid.setText(uid);
        detailsTxMBytes.setText(String.format("%s MB", transmittedBytes));
        detailsRxMBytes.setText(String.format("%s MB", receivedBytes));
        detailsTxPkgs.setText(transmittedPackages);
        detailsRxPkgs.setText(receivedPackages);

        dialog.setTitle(appNameView.getText().toString());
        dialog.setContentView(detailsView);
        dialog.show();
    }

    @SuppressWarnings("unused")
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
