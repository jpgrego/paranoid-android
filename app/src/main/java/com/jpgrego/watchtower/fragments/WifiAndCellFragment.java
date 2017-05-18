package com.jpgrego.watchtower.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.data.Cell;
import com.jpgrego.watchtower.data.MyBluetoothDevice;
import com.jpgrego.watchtower.data.WifiAP;
import com.jpgrego.watchtower.utils.BluetoothUtils;
import com.jpgrego.watchtower.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by jpgrego on 6/9/15.
 */
public final class WifiAndCellFragment extends Fragment {

    private static final int HIGHLIGHTED_BACKGROUND = Color.parseColor("#93a2a2");
    private static final int FADED_COLOR = Color.parseColor("#808080");

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

    TableLayout cellsTable, wifiTable, bluetoothTable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View thisView;

        // Inflate the layout for this fragment
        thisView = inflater.inflate(R.layout.fragment_wifiandcells, container, false);

        cellsTable = (TableLayout) thisView.findViewById(R.id.cells_table);
        wifiTable = (TableLayout) thisView.findViewById(R.id.wifi_table);
        bluetoothTable = (TableLayout) thisView.findViewById(R.id.bluetooth_table);

        final TableRow cellTableTitleRow = (TableRow) View.inflate(getActivity(),
                R.layout.cell_table_title_row, null);
        cellsTable.addView(cellTableTitleRow);

        final TableRow wifiTableTitleRow = (TableRow) View.inflate(getActivity(),
                R.layout.wifi_table_title_row, null);
        wifiTable.addView(wifiTableTitleRow);

        final TableRow bluetoothTitleRow = (TableRow) View.inflate(getActivity(),
                R.layout.bluetooth_table_title_row, null);
        bluetoothTable.addView(bluetoothTitleRow);

        return thisView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(cellInfoReceiver,
                new IntentFilter(Constants.CELL_INTENT_FILTER_NAME));
        getActivity().registerReceiver(wifiInfoReceiver,
                new IntentFilter(Constants.WIFI_INTENT_FILTER_NAME));
        getActivity().registerReceiver(bluetoothInfoReceiver,
                new IntentFilter(Constants.BLUETOOTH_INTENT_FILTER_NAME));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(cellInfoReceiver);
        getActivity().unregisterReceiver(wifiInfoReceiver);
        getActivity().unregisterReceiver(bluetoothInfoReceiver);
    }

    void updateCellTable(final List<Cell> cellList) {
        /*
         * This is done to avoid a NullPointerException being thrown by View.inflate, due to the
         * fact that getActivity() returns null in case the fragment isn't added to the activity,
         * which seems to happen occasionally
         */
        if (!isAdded()) {
            return;
        }

        final int cellsTableSize = cellsTable.getChildCount();

        if(cellsTableSize > 1) {
            cellsTable.removeViews(1, cellsTableSize - 1);
        }

        if(cellList.size() == 0) {
            final TableRow cellNoDataRow = (TableRow) View.inflate(getActivity(),
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

            cellTableDataRow = (TableRow) View.inflate(getActivity(), R.layout.cell_table_data_row,
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
        if (!isAdded()) {
            return;
        }

        final int wifiTableSize = wifiTable.getChildCount();

        if(wifiTableSize > 1) {
            wifiTable.removeViews(1, wifiTableSize - 1);
        }

        if(wifiAPList.size() == 0) {
            final TableRow wifiNoDataRow = (TableRow) View.inflate(getActivity(),
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

            wifiTableDataRow = (TableRow) View.inflate(getActivity(), R.layout.wifi_table_data_row,
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
        if (!isAdded()) {
            return;
        }

        final int bluetoothTableSize = bluetoothTable.getChildCount();

        if(bluetoothTableSize > 1) {
            bluetoothTable.removeViews(1, bluetoothTableSize - 1);
        }

        if(bluetoothDevices.size() == 0) {
            final TableRow bluetoothNoDataRow = (TableRow) View.inflate(getActivity(),
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

            bluetoothTableDataRow = (TableRow) View.inflate(getActivity(),
                    R.layout.bluetooth_table_data_row, null);
            name = (TextView) bluetoothTableDataRow.findViewById(R.id.name);
            address = (TextView) bluetoothTableDataRow.findViewById(R.id.address);
            type = (TextView) bluetoothTableDataRow.findViewById(R.id.type);
            rssi = (TextView) bluetoothTableDataRow.findViewById(R.id.rssi);

            name.setText(device.getName());
            address.setText(device.getAddress());
            type.setText(BluetoothUtils.getTypeStringFromInt(device.getType()));
            rssi.setText(String.format(Locale.US, "%d", device.getRssi()));

            if(device.getVisibilityCounter().get() < 3) {
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
}
