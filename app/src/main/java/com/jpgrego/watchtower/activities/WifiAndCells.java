package com.jpgrego.watchtower.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.data.Cell;
import com.jpgrego.watchtower.data.MyBluetoothDevice;
import com.jpgrego.watchtower.data.WifiAP;
import com.jpgrego.watchtower.services.DataService;
import com.jpgrego.watchtower.utils.BluetoothUtils;
import com.jpgrego.watchtower.utils.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Process;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class WifiAndCells extends BaseActivity {

    private static final String LOG_FILE_NAME = "CRASH_LOG.TXT";
    private static final int HIGHLIGHTED_BACKGROUND = Color.parseColor("#93a2a2");
    private static final int FADED_COLOR = Color.parseColor("#808080");

    private TableLayout cellsTable, wifiTable, bluetoothTable;

    private final BroadcastReceiver cellInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<Cell> cellList = intent.getParcelableArrayListExtra(
                    Constants.CELL_INFO_LIST_INTENT_EXTRA_NAME);
            updateCellTable(cellList);
        }
    };

    private final BroadcastReceiver wifiInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<WifiAP> wifiAPList = intent.getParcelableArrayListExtra(
                    Constants.WIFI_INFO_LIST_INTENT_EXTRA_NAME);
            final String currentBSSID = intent.getStringExtra(
                    Constants.WIFI_CURRENT_BSSID_INTENT_EXTRA_NAME);
            updateWifiTable(wifiAPList, currentBSSID);
        }
    };

    private final BroadcastReceiver bluetoothInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<MyBluetoothDevice> bluetoothDevices =
                    intent.getParcelableArrayListExtra(
                            Constants.BLUETOOTH_INFO_LIST_INTENT_EXTRA_NAME);
            final String currentUUID =
                    intent.getStringExtra(Constants.BLUETOOTH_CURRENT_UUID_INTENT_EXTRA_NAME);
            updateBluetoothTable(bluetoothDevices, currentUUID);
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.wifi_cells_activity_title);
        startService(new Intent(this, DataService.class));
        //Thread.setDefaultUncaughtExceptionHandler(new ThesisAppExceptionHandler());
        setContentView(R.layout.activity_wifiandcells);

        cellsTable = (TableLayout) findViewById(R.id.cells_table);
        wifiTable = (TableLayout) findViewById(R.id.wifi_table);
        bluetoothTable = (TableLayout) findViewById(R.id.bluetooth_table);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(cellInfoReceiver, new IntentFilter(Constants.CELL_INTENT_FILTER_NAME));
        registerReceiver(wifiInfoReceiver, new IntentFilter(Constants.WIFI_INTENT_FILTER_NAME));
        registerReceiver(bluetoothInfoReceiver,
                new IntentFilter(Constants.BLUETOOTH_INTENT_FILTER_NAME));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(cellInfoReceiver);
        unregisterReceiver(wifiInfoReceiver);
        unregisterReceiver(bluetoothInfoReceiver);
    }

    //TODO: implement this
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
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

    void updateCellTable(final List<Cell> cellList) {
        /*
         * This is done to avoid a NullPointerException being thrown by View.inflate, due to the
         * fact that getActivity() returns null in case the fragment isn't added to the activity,
         * which seems to happen occasionally
         */
        /*
        if (!isAdded()) {
            return;
        }
        */

        final int cellsTableSize = cellsTable.getChildCount();

        if(cellsTableSize > 1) {
            cellsTable.removeViews(1, cellsTableSize - 1);
        }

        if(cellList.size() == 0) {
            final TableRow cellNoDataRow = (TableRow) View.inflate(this,
                    R.layout.cell_table_no_data_row, null);
            final TextView text = (TextView) cellNoDataRow.findViewById(R.id.text);
            text.setTextColor(FADED_COLOR);
            cellsTable.addView(cellNoDataRow);
        } else {
            addCellsToTable(cellList);
        }
    }

    void addCellsToTable(List<Cell> cellList) {
        for (Cell cell : cellList) {
            final TextView generation, mcc, mnc, cid, lac, psc, dbm;
            final TableRow cellTableDataRow;

            cellTableDataRow = (TableRow) View.inflate(this, R.layout.cell_table_data_row,
                    null);
            generation = (TextView) cellTableDataRow.findViewById(R.id.generation);
            mcc = (TextView) cellTableDataRow.findViewById(R.id.mcc);
            mnc = (TextView) cellTableDataRow.findViewById(R.id.mnc);
            cid = (TextView) cellTableDataRow.findViewById(R.id.cid);
            lac = (TextView) cellTableDataRow.findViewById(R.id.lac);
            psc = (TextView) cellTableDataRow.findViewById(R.id.psc);
            dbm = (TextView) cellTableDataRow.findViewById(R.id.cell_dbm);
            generation.setText(cell.getGeneration());
            mcc.setText(String.format(Locale.US, "%d", cell.getMcc()));
            mnc.setText(String.format(Locale.US, "%d", cell.getMnc()));
            cid.setText(String.format(Locale.US, "%d", cell.getCid()));
            lac.setText(String.format(Locale.US, "%d", cell.getLac()));
            psc.setText(String.format(Locale.US, "%d", cell.getPsc()));
            dbm.setText(String.format(Locale.US, "%d", cell.getDbm()));

            if (cell.isRegisteredCell()) {
                cellTableDataRow.setBackgroundColor(HIGHLIGHTED_BACKGROUND);
            }

            cellsTable.addView(cellTableDataRow, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    void updateWifiTable(final List<WifiAP> wifiAPList,
                         final String currentWifiConnectionBSSID) {
        /*
         * This is done to avoid a NullPointerException being thrown by View.inflate, due to the
         * fact that getActivity() returns null in case the fragment isn't added to the activity,
         * which seems to happen occasionally
         */
        /*
        if (!isAdded()) {
            return;
        }
        */

        final int wifiTableSize = wifiTable.getChildCount();

        if(wifiTableSize > 1) {
            wifiTable.removeViews(1, wifiTableSize - 1);
        }

        if(wifiAPList.size() == 0) {
            final TableRow wifiNoDataRow = (TableRow) View.inflate(this,
                    R.layout.wifi_table_no_data_row, null);
            final TextView text = (TextView) wifiNoDataRow.findViewById(R.id.text);
            text.setTextColor(FADED_COLOR);
            wifiTable.addView(wifiNoDataRow);
        } else {
            addWifiAPToTable(wifiAPList, currentWifiConnectionBSSID);
        }
    }

    void addWifiAPToTable(final List<WifiAP> wifiAPList, final String currentWifiConnectionBSSID) {
        for (WifiAP wifiAP : wifiAPList) {
            final TableRow wifiTableDataRow;
            final ImageView securityImage;
            final TableLayout ssidTable;
            final TextView securityLabel, ssid, bssid, channel, dbm;

            wifiTableDataRow = (TableRow) View.inflate(this, R.layout.wifi_table_data_row,
                    null);
            securityImage = (ImageView) wifiTableDataRow.findViewById(R.id.security_image);
            securityLabel = (TextView) wifiTableDataRow.findViewById(R.id.security_label);
            ssidTable = (TableLayout) wifiTableDataRow.findViewById(R.id.ssid_table);
            ssid = (TextView) ssidTable.findViewById(R.id.ssid);
            bssid = (TextView) ssidTable.findViewById(R.id.bssid);
            channel = (TextView) wifiTableDataRow.findViewById(R.id.channel);
            dbm = (TextView) wifiTableDataRow.findViewById(R.id.wifi_dbm);

            securityImage.setImageResource(wifiAP.getWifiSecurityImageResource());
            securityLabel.setText(wifiAP.getSecurityLabel());
            ssid.setText(wifiAP.getSsid());
            bssid.setText(wifiAP.getBssid());
            channel.setText(String.format(Locale.US, "%d", wifiAP.getChannel()));
            dbm.setText(String.format(Locale.US, "%d", wifiAP.getDbm()));

            if (wifiAP.getVisibilityCounter().get() < 3) {
                securityLabel.setTextColor(FADED_COLOR);
                ssid.setTextColor(FADED_COLOR);
                bssid.setTextColor(FADED_COLOR);
                channel.setTextColor(FADED_COLOR);
                dbm.setTextColor(FADED_COLOR);
            } else if (wifiAP.getBssid().equals(currentWifiConnectionBSSID)) {
                wifiTableDataRow.setBackgroundColor(HIGHLIGHTED_BACKGROUND);
            }

            wifiTable.addView(wifiTableDataRow, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    void updateBluetoothTable(final List<MyBluetoothDevice> bluetoothDevices,
                              final String currentUUID) {
        /*
         * This is done to avoid a NullPointerException being thrown by View.inflate, due to the
         * fact that getActivity() returns null in case the fragment isn't added to the activity,
         * which seems to happen occasionally
         */
        /*
        if (!isAdded()) {
            return;
        }
        */

        final int bluetoothTableSize = bluetoothTable.getChildCount();

        if(bluetoothTableSize > 1) {
            bluetoothTable.removeViews(1, bluetoothTableSize - 1);
        }

        if(bluetoothDevices.size() == 0) {
            final TableRow bluetoothNoDataRow = (TableRow) View.inflate(this,
                    R.layout.bluetooth_table_no_data_row, null);
            final TextView text = (TextView) bluetoothNoDataRow.findViewById(R.id.text);
            text.setTextColor(FADED_COLOR);
            bluetoothTable.addView(bluetoothNoDataRow);
        } else {
            addBluetoothDevicesToTable(bluetoothDevices, currentUUID);
        }

    }

    void addBluetoothDevicesToTable(final List<MyBluetoothDevice> bluetoothDevices,
                                    final String currentUUID) {
        for(MyBluetoothDevice device : bluetoothDevices) {
            final TableRow bluetoothTableDataRow;
            final TextView name, address, rssi, type;

            bluetoothTableDataRow = (TableRow) View.inflate(this,
                    R.layout.bluetooth_table_data_row, null);
            name = (TextView) bluetoothTableDataRow.findViewById(R.id.name);
            address = (TextView) bluetoothTableDataRow.findViewById(R.id.address);
            type = (TextView) bluetoothTableDataRow.findViewById(R.id.type);
            rssi = (TextView) bluetoothTableDataRow.findViewById(R.id.rssi);

            name.setText(device.getName());
            address.setText(device.getAddress());
            type.setText(BluetoothUtils.getTypeStringFromInt(device.getType()));
            rssi.setText(String.format(Locale.US, "%d", device.getRssi()));

            if(device.getVisibilityCounter().get() <= 0) {
                name.setTextColor(FADED_COLOR);
                address.setTextColor(FADED_COLOR);
                type.setTextColor(FADED_COLOR);
                rssi.setTextColor(FADED_COLOR);
            } else if(device.getAddress().equals(currentUUID))  {
                bluetoothTableDataRow.setBackgroundColor(HIGHLIGHTED_BACKGROUND);
            }

            bluetoothTable.addView(bluetoothTableDataRow, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }

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
