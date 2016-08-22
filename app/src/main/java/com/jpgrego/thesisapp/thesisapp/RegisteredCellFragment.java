package com.jpgrego.thesisapp.thesisapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

/**
 * Created by jpgrego on 6/9/15.
 */
public class RegisteredCellFragment extends Fragment {

    private String mccString, mncString, networkOperatorString, generation;
    private TelephonyManager telephonyManager;
    private TableLayout cellsTable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View thisView;
        this.telephonyManager = ((MainActivity) this.getContext()).getTelephonyManager();

        // Inflate the layout for this fragment
        thisView = inflater.inflate(R.layout.fragment_main, container, false);
        cellsTable = (TableLayout) thisView.findViewById(R.id.cells_table);
        telephonyManager.listen(new CellInfoListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        return thisView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        telephonyManager.listen(null, PhoneStateListener.LISTEN_NONE);
    }

    /*
    Thanks SatStat!
     */
    private static String getGenerationFromNetworkType(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "?G";
        }
    }

    private void updateCellInfoTable(int dbm) {
        final int rowsExceptTitle;

        // obtain number of views contained in the cells table, not counting with the title row
        rowsExceptTitle = cellsTable.getChildCount() - 1;

        // remove all except title
        if(rowsExceptTitle > 0) {
            cellsTable.removeViewsInLayout(1, rowsExceptTitle);
        }

        // obtain fresh data
        getRegisteredCellInfo(dbm);
        getNeighboringCellsInfo();
    }

    private void getRegisteredCellInfo(int dbm) {
        final CellLocation cellLocation;
        final GsmCellLocation gsmCellLocation;

        try {
            cellLocation = telephonyManager.getCellLocation();

            if (cellLocation != null && cellLocation instanceof GsmCellLocation) {
                gsmCellLocation = (GsmCellLocation) cellLocation;
                addToCellTable(gsmCellLocation, dbm);
            }
        } catch (SecurityException ex) {
            Log.w(this.getClass().getName(), "Couldn't get registered cell information due to " +
                    "lack of permission.");
        }
    }

    // only way to get this info on my phone, getAllCellInfo() doesn't seem to be implemented
    @SuppressWarnings("deprecation")
    private void getNeighboringCellsInfo() {
        final List<NeighboringCellInfo> neighboringCellInfoList;

        try {
            neighboringCellInfoList = telephonyManager.getNeighboringCellInfo();

            if (neighboringCellInfoList != null) {
                for (NeighboringCellInfo neighboringCellInfo : neighboringCellInfoList) {
                    addToCellTable(neighboringCellInfo);
                }
            }

        } catch (SecurityException ex) {
            Log.w(this.getClass().getName(), "Couldn't get neighboring cells information due to " +
                    "lack of permission.");
        }

    }

    private void addToCellTable(GsmCellLocation cellInfo, int dbmInt) {
        final TableRow tableRow;
        final TextView generation, mcc, mnc, cid, lac, psc, dbm;

        tableRow = (TableRow) View.inflate(getActivity(), R.layout.cell_table_row, null);
        generation = (TextView) tableRow.findViewById(R.id.generation);
        mcc = (TextView) tableRow.findViewById(R.id.mcc);
        mnc = (TextView) tableRow.findViewById(R.id.mnc);
        cid = (TextView) tableRow.findViewById(R.id.cid);
        lac = (TextView) tableRow.findViewById(R.id.lac);
        psc = (TextView) tableRow.findViewById(R.id.psc);
        dbm = (TextView) tableRow.findViewById(R.id.cell_dbm);

        generation.setText(this.generation);
        mcc.setText(this.mccString);
        mnc.setText(this.mncString);
        cid.setText(String.format(Locale.US, "%d", cellInfo.getCid()));
        lac.setText(String.format(Locale.US, "%d", cellInfo.getLac()));
        psc.setText(String.format(Locale.US, "%d", cellInfo.getPsc()));
        dbm.setText(String.format(Locale.US, "%d", dbmInt));

        tableRow.setBackgroundColor(Color.parseColor("#93a2a2"));
        cellsTable.addView(tableRow, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    private void addToCellTable(NeighboringCellInfo cellInfo) {
        final TableRow tableRow;
        final TextView generation, mcc, mnc, cid, lac, psc, dbm;

        tableRow = (TableRow) View.inflate(getActivity(), R.layout.cell_table_row, null);
        generation = (TextView) tableRow.findViewById(R.id.generation);
        mcc = (TextView) tableRow.findViewById(R.id.mcc);
        mnc = (TextView) tableRow.findViewById(R.id.mnc);
        cid = (TextView) tableRow.findViewById(R.id.cid);
        lac = (TextView) tableRow.findViewById(R.id.lac);
        psc = (TextView) tableRow.findViewById(R.id.psc);
        dbm = (TextView) tableRow.findViewById(R.id.cell_dbm);

        generation.setText(this.generation);
        mcc.setText(this.mccString);
        mnc.setText(this.mncString);
        cid.setText(String.format(Locale.US, "%d", cellInfo.getCid()));
        lac.setText(String.format(Locale.US, "%d", cellInfo.getLac()));
        psc.setText(String.format(Locale.US, "%d", cellInfo.getPsc()));
        dbm.setText(String.format(Locale.US, "%d", rssiToDbm(cellInfo.getRssi())));

        cellsTable.addView(tableRow, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    /*
    thanks SatStat!
     */
    private int rssiToDbm(int rssi) {
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
				/*
				 * for details see TS 25.133 section 9.1.1.3
				 * http://www.3gpp.org/DynaReport/25133.htm
				 */
                if ((rssi >= -5) && (rssi <= 91)) {
                    return rssi - 116;
                } else if ((rssi >= -121) && (rssi <=-25)) {
                    return rssi;
                }
                return -1111;
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS:
                if ((rssi >= 0) || (rssi <= 31)) {
                    return rssi * 2 - 113;
                }
                return -1111;
            default:
                return -1111;
        }
    }

    private class CellInfoListener extends PhoneStateListener {

        /*
        Only this method seems to work. onCellInfoChanged always gives a null list as argument, on my
        BQ Aquarius E5 FHD
        */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            RegisteredCellFragment.this.networkOperatorString = telephonyManager.getNetworkOperator();
            RegisteredCellFragment.this.mccString = networkOperatorString.substring(0, 3);
            RegisteredCellFragment.this.mncString = networkOperatorString.substring(3);
            RegisteredCellFragment.this.generation = getGenerationFromNetworkType(telephonyManager.getNetworkType());

            if(signalStrength.isGsm()) {
                updateCellInfoTable(signalStrength.getGsmSignalStrength() * 2 - 113);
            }
        }

    }

}
