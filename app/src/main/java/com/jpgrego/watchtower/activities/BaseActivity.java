package com.jpgrego.watchtower.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.services.DataService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by jpgrego on 3/22/18.
 */

public class BaseActivity extends AppCompatActivity {

    private static final String LOG_FILE_NAME = "CRASH_LOG.TXT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, DataService.class));
        //Thread.setDefaultUncaughtExceptionHandler(new ThesisAppExceptionHandler());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabbed, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(this instanceof WifiCellsBTActivity) {
            greyOutAndDisableItem(menu, R.id.action_radio, R.drawable.network);
        } else if(this instanceof AppTrafficActivity) {
            greyOutAndDisableItem(menu, R.id.action_traffic, R.drawable.traffic);
        } else if(this instanceof SensorsActivity) {
            greyOutAndDisableItem(menu, R.id.action_sensors, R.drawable.sensors);
        } else if(this instanceof MapActivity) {
            greyOutAndDisableItem(menu, R.id.action_map, R.drawable.location);
        }

        return true;
    }

    public void actionBarButtonClicked(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_radio:
                startActivity(new Intent(this, WifiCellsBTActivity.class));
                break;
            case R.id.action_traffic:
                startActivity(new Intent(this, AppTrafficActivity.class));
                break;
            case R.id.action_sensors:
                startActivity(new Intent(this, SensorsActivity.class));
                break;
            case R.id.action_map:
                startActivity(new Intent(this, MapActivity.class));
                break;
            default:
        }

        finish();
    }

    private void greyOutAndDisableItem(final Menu menu, final int itemId, final int iconId) {
        final MenuItem item = menu.findItem(itemId);
        final Drawable icon = getResources().getDrawable(iconId);

        icon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        item.setEnabled(false);
        item.setIcon(icon);
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
                        + "/" + getResources().getString(R.string.app_name) + "/");
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
