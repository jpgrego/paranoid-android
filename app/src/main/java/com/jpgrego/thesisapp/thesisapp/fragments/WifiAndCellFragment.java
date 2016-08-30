package com.jpgrego.thesisapp.thesisapp.fragments;

import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.jpgrego.thesisapp.thesisapp.R;
import com.jpgrego.thesisapp.thesisapp.activities.MainActivity;
import com.jpgrego.thesisapp.thesisapp.data.Cell;
import com.jpgrego.thesisapp.thesisapp.data.WifiAP;
import java.util.List;
import java.util.Locale;

/**
 * Created by jpgrego on 6/9/15.
 */
public class WifiAndCellFragment extends Fragment {

    private static final int CELL_UPDATE_TABLE_PERIOD = 1000;
    private static final int WIFI_UPDATE_TABLE_PERIOD = 1000;
    private static final int HIGHLIGHTED_BACKGROUND = Color.parseColor("#93a2a2");
    private static final int FADED_COLOR = Color.parseColor("#808080");

    private final Handler cellUpdateHandler = new Handler(), wifiUpdateHandler = new Handler();
    private final Runnable cellUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateCellTable();
            cellUpdateHandler.postDelayed(this, CELL_UPDATE_TABLE_PERIOD);
        }
    };
    private final Runnable wifiUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateWifiTable();
            wifiUpdateHandler.postDelayed(this, WIFI_UPDATE_TABLE_PERIOD);
        }
    };

    private TableLayout cellsTable, wifiTable;
    private MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View thisView;

        mainActivity = (MainActivity) this.getContext();

        // Inflate the layout for this fragment
        thisView = inflater.inflate(R.layout.fragment_main, container, false);

        cellsTable = (TableLayout) thisView.findViewById(R.id.cells_table);
        wifiTable = (TableLayout) thisView.findViewById(R.id.wifi_table);

        return thisView;
    }

    @Override
    public void onResume() {
        super.onResume();
        cellUpdateHandler.post(cellUpdateRunnable);
        wifiUpdateHandler.post(wifiUpdateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        cellUpdateHandler.removeCallbacks(cellUpdateRunnable);
        wifiUpdateHandler.removeCallbacks(wifiUpdateRunnable);
    }

    private void updateCellTable() {
        final TableRow cellTableTitleRow;
        final List<Cell> cellList;

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

        cellList = this.mainActivity.getCellInfoListener().getCellList();

        addCellsToTable(cellList);
    }

    private void addCellsToTable(List<Cell> cellList) {
        for(Cell cell : cellList) {
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
            generation.setText(cell.generation);
            mcc.setText(String.format(Locale.US, "%d", cell.mcc));
            mnc.setText(String.format(Locale.US, "%d", cell.mnc));
            cid.setText(String.format(Locale.US, "%d", cell.cid));
            lac.setText(String.format(Locale.US, "%d", cell.lac));
            psc.setText(String.format(Locale.US, "%d", cell.psc));
            dbm.setText(String.format(Locale.US, "%d", cell.dbm));

            if(cell.isRegisteredCell()) {
                cellTableDataRow.setBackgroundColor(HIGHLIGHTED_BACKGROUND);
            }

            cellsTable.addView(cellTableDataRow, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private void updateWifiTable() {
        final TableRow wifiTableTitleRow;
        final List<WifiAP> wifiAPList;
        final WifiInfo currentWifiConnectionInfo;

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

        wifiAPList = this.mainActivity.getWifiInfoReceiver().getOrderedWifiAPList();
        currentWifiConnectionInfo = this.mainActivity.getWifiManager().getConnectionInfo();

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

            securityImage.setImageResource(wifiAP.wifiSecurityImageResource);
            securityLabel.setText(wifiAP.securityLabel);
            ssid.setText(wifiAP.ssid);
            bssid.setText(wifiAP.bssid);
            channel.setText(String.format(Locale.US, "%d", wifiAP.channel));
            dbm.setText(String.format(Locale.US, "%d", wifiAP.dbm));

            if (wifiAP.getVisibilityCounter() < 3) {
                securityLabel.setTextColor(FADED_COLOR);
                ssid.setTextColor(FADED_COLOR);
                bssid.setTextColor(FADED_COLOR);
                channel.setTextColor(FADED_COLOR);
                dbm.setTextColor(FADED_COLOR);
            } else if(wifiAP.bssid.equals(currentWifiConnectionInfo.getBSSID())) {
                wifiTableDataRow.setBackgroundColor(HIGHLIGHTED_BACKGROUND);
            }

            wifiTable.addView(wifiTableDataRow, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }
}