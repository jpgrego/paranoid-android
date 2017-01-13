package com.jpgrego.thesisapp.thesisapp.fragments;

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
import com.jpgrego.thesisapp.thesisapp.R;
import com.jpgrego.thesisapp.thesisapp.data.Cell;
import com.jpgrego.thesisapp.thesisapp.data.WifiAP;
import com.jpgrego.thesisapp.thesisapp.utils.Constants;

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
            System.out.println();
        }
    };

    TableLayout cellsTable, wifiTable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View thisView;

        // Inflate the layout for this fragment
        thisView = inflater.inflate(R.layout.fragment_wifiandcells, container, false);

        cellsTable = (TableLayout) thisView.findViewById(R.id.cells_table);
        wifiTable = (TableLayout) thisView.findViewById(R.id.wifi_table);

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
        final TableRow cellTableTitleRow;

        /*
         * This is done to avoid a NullPointerException being thrown by View.inflate, due to the
         * fact that getActivity() returns null in case the fragment isn't added to the activity,
         * which seems to happen occasionally
         */
        if (!isAdded()) {
            return;
        }

        cellTableTitleRow = (TableRow) View.inflate(getActivity(), R.layout.cell_table_title_row,
                null);

        cellsTable.removeAllViews();
        cellsTable.addView(cellTableTitleRow);
        addCellsToTable(cellList);
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
        final TableRow wifiTableTitleRow;

        /*
         * This is done to avoid a NullPointerException being thrown by View.inflate, due to the
         * fact that getActivity() returns null in case the fragment isn't added to the activity,
         * which seems to happen occasionally
         */
        if (!isAdded()) {
            return;
        }

        wifiTableTitleRow = (TableRow) View.inflate(getActivity(), R.layout.wifi_table_title_row,
                null);

        wifiTable.removeAllViews();
        wifiTable.addView(wifiTableTitleRow);

        addWifiAPToTable(wifiAPList, currentWifiConnectionBSSID);
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
}
