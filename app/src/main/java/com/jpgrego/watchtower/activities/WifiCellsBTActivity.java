package com.jpgrego.watchtower.activities;

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

import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.data.Cell;
import com.jpgrego.watchtower.data.MyBluetoothDevice;
import com.jpgrego.watchtower.data.WifiAP;
import com.jpgrego.watchtower.services.DataService;
import com.jpgrego.watchtower.utils.BluetoothUtils;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class WifiCellsBTActivity extends BaseActivity {

    private static final int UPDATE_PERIOD_SECONDS = 1;
    private static final int HIGHLIGHTED_BACKGROUND = Color.parseColor("#93a2a2");
    private static final int FADED_COLOR = Color.parseColor("#808080");

    private volatile ScheduledFuture<?> scheduledUpdates = null;

    private TableLayout cellsTable, wifiTable, bluetoothTable;

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

        cellsTable = (TableLayout) findViewById(R.id.cells_table);
        wifiTable = (TableLayout) findViewById(R.id.wifi_table);
        bluetoothTable = (TableLayout) findViewById(R.id.bluetooth_table);
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
