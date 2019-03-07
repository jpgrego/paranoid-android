package com.jpgrego.paranoidandroid.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jpgrego.paranoidandroid.R;
import com.jpgrego.paranoidandroid.data.Cell;
import com.jpgrego.paranoidandroid.data.MyBluetoothDevice;
import com.jpgrego.paranoidandroid.data.WifiAP;
import com.jpgrego.paranoidandroid.services.DataService;
import com.jpgrego.paranoidandroid.utils.BluetoothUtils;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class WifiCellsBTActivity extends BaseActivity {

    private static final int UPDATE_PERIOD_SECONDS = 1;
    private static final int HIGHLIGHTED_BACKGROUND = Color.parseColor("#93a2a2");
    private static final int FADED_COLOR = Color.parseColor("#808080");
//    private static final int REQUEST_ENABLE_BT = 100;

    private volatile ScheduledFuture<?> scheduledUpdates = null;

    private TableLayout registeredCellsTable, neighbouringCellsTable, wifiTable, bluetoothTable;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            final DataService dataService =
                    ((DataService.LocalBinder) iBinder).getDataServiceInstance();

            scheduledUpdates = DataService.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                    new UpdateDataRunnable(dataService),
                    0 ,
                    UPDATE_PERIOD_SECONDS,
                    TimeUnit.SECONDS);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // do nothing
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.wifi_cells_activity_title);
        setContentView(R.layout.activity_wifiandcells);

        registeredCellsTable = findViewById(R.id.registered_cells_table);
        neighbouringCellsTable = findViewById(R.id.neighbouring_cells_table);
        wifiTable = findViewById(R.id.wifi_table);
        bluetoothTable = findViewById(R.id.bluetooth_table);

//        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
//            final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Intent serviceIntent = new Intent(this, DataService.class);
        bindService(serviceIntent, serviceConnection, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(scheduledUpdates != null) scheduledUpdates.cancel(true);
        unbindService(serviceConnection);
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

        final int registeredCellsTableSize = registeredCellsTable.getChildCount();
        final int neighbouringCellsTableSize = neighbouringCellsTable.getChildCount();

        if(registeredCellsTableSize > 1) {
            registeredCellsTable.removeViews(1, registeredCellsTableSize - 1);
        }

        if(neighbouringCellsTableSize > 1) {
            neighbouringCellsTable.removeViews(1, neighbouringCellsTableSize - 1);
        }

        addCellsToTable(cellList);
    }

    void addCellsToTable(final List<Cell> cellList) {
        for (final Cell cell : cellList) {
            final TextView generation, mcc, mnc, cid, lac, psc, dbm;
            final TableRow cellTableDataRow;
            final boolean isRegistered = cell.isRegisteredCell();

            if(isRegistered) {
                cellTableDataRow = (TableRow) View.inflate(this,
                        R.layout.registered_cell_table_data_row,
                        null);

                mcc = cellTableDataRow.findViewById(R.id.mcc);
                mnc = cellTableDataRow.findViewById(R.id.mnc);
                cid = cellTableDataRow.findViewById(R.id.cid);
                lac = cellTableDataRow.findViewById(R.id.lac);

                mcc.setText(String.format(Locale.US, "%d", cell.getMcc()));
                mnc.setText(String.format(Locale.US, "%d", cell.getMnc()));
                cid.setText(String.format(Locale.US, "%d", cell.getCid()));
                lac.setText(String.format(Locale.US, "%d", cell.getLac()));

            } else {
                cellTableDataRow = (TableRow) View.inflate(this,
                        R.layout.neighbouring_cell_table_data_row,
                        null);
            }

            generation = cellTableDataRow.findViewById(R.id.generation);
            dbm = cellTableDataRow.findViewById(R.id.cell_dbm);
            psc = cellTableDataRow.findViewById(R.id.psc);

            dbm.setText(String.format(Locale.US, "%d", cell.getDbm()));
            if(cell.getPsc() > -1) {
                psc.setText(String.format(Locale.US, "%d", cell.getPsc()));
            } else psc.setText("");
            generation.setText(cell.getGeneration());

            dbm.setText(String.format(Locale.US, "%d", cell.getDbm()));

//            if (cell.isRegisteredCell()) {
//                cellTableDataRow.setBackgroundColor(HIGHLIGHTED_BACKGROUND);
//            }

            if(isRegistered) {
                registeredCellsTable.addView(cellTableDataRow, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));
            } else {
                neighbouringCellsTable.addView(cellTableDataRow, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));
            }
        }

        if(registeredCellsTable.getChildCount() == 0) {
            final TableRow cellNoDataRow = (TableRow) View.inflate(this,
                    R.layout.cell_table_no_data_row, null);
            final TextView text = cellNoDataRow.findViewById(R.id.text);
            text.setTextColor(FADED_COLOR);
            registeredCellsTable.addView(cellNoDataRow);
        }

        if(neighbouringCellsTable.getChildCount() == 0) {
            final TableRow cellNoDataRow = (TableRow) View.inflate(this,
                    R.layout.cell_table_no_data_row, null);
            final TextView text = cellNoDataRow.findViewById(R.id.text);
            text.setTextColor(FADED_COLOR);
            neighbouringCellsTable.addView(cellNoDataRow);
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
            final TextView text = wifiNoDataRow.findViewById(R.id.text);
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
            securityImage = wifiTableDataRow.findViewById(R.id.security_image);
            securityLabel = wifiTableDataRow.findViewById(R.id.security_label);
            ssidTable = wifiTableDataRow.findViewById(R.id.ssid_table);
            ssid = ssidTable.findViewById(R.id.ssid);
            bssid = ssidTable.findViewById(R.id.bssid);
            channel = wifiTableDataRow.findViewById(R.id.channel);
            dbm = wifiTableDataRow.findViewById(R.id.wifi_dbm);

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
            final TextView text = bluetoothNoDataRow.findViewById(R.id.text);
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
            name = bluetoothTableDataRow.findViewById(R.id.name);
            address = bluetoothTableDataRow.findViewById(R.id.address);
            type = bluetoothTableDataRow.findViewById(R.id.type);
            rssi = bluetoothTableDataRow.findViewById(R.id.rssi);

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

    private final class UpdateDataRunnable implements Runnable {

        private final DataService service;

        private UpdateDataRunnable(final DataService service) {
            this.service = service;
        }

        @Override
        public void run() {
            WifiCellsBTActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        updateCellTable(service.getCellList());
                        updateWifiTable(service.getWifiAPList(),
                                service.getCurrentlyConnectedWifiAP());
                        updateBluetoothTable(service.getBluetoothDeviceList(),
                                service.getCurrentlyConnectedBTDevice());
                }
            });
        }
    }
}
