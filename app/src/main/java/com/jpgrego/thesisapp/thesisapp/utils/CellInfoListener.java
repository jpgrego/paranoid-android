package com.jpgrego.thesisapp.thesisapp.utils;

import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.jpgrego.thesisapp.thesisapp.data.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jpgrego on 25-08-2016.
 */
public class CellInfoListener extends PhoneStateListener {

    private final TelephonyManager telephonyManager;
    private final List<Cell> cellList = new ArrayList<>();
    private int mcc, mnc;
    private int networkType;

    public CellInfoListener(final TelephonyManager telephonyManager) {
        final String networkOperatorString;

        networkOperatorString = telephonyManager.getNetworkOperator();
        this.telephonyManager = telephonyManager;

        try {
            this.mcc = Integer.parseInt(networkOperatorString.substring(0, 3));
            this.mnc = Integer.parseInt(networkOperatorString.substring(3));
        } catch (IndexOutOfBoundsException ex) {
            Log.w(this.getClass().getName(), "Obtaining the MCC and MNC values failed " +
                    "(" + ex.getClass().getName() + ")");
            this.mcc = -1;
            this.mnc = -1;
        } catch (NumberFormatException ex) {
            Log.w(this.getClass().getName(), "Obtaining the MCC and MNC values failed " +
                    "( + " + ex.getClass().getName() + ")");
            this.mcc = -1;
            this.mnc = -1;
        }

        this.networkType = telephonyManager.getNetworkType();
        getCellInfo();
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        final String networkOperatorString;

        networkOperatorString = telephonyManager.getNetworkOperator();

        try {
            this.mcc = Integer.parseInt(networkOperatorString.substring(0, 3));
            this.mnc = Integer.parseInt(networkOperatorString.substring(3));
        } catch (IndexOutOfBoundsException ex) {
            Log.w(this.getClass().getName(), "Obtaining the MCC and MNC values failed " +
                    "(" + ex.getClass().getName() + ")");
            this.mcc = -1;
            this.mnc = -1;
        } catch (NumberFormatException ex) {
            Log.w(this.getClass().getName(), "Obtaining the MCC and MNC values failed " +
                    "( + " + ex.getClass().getName() + ")");
            this.mcc = -1;
            this.mnc = -1;
        }

        this.networkType = telephonyManager.getNetworkType();
        getCellInfo();

        /*
        if (signalStrength.isGsm()) {
            updateCellInfoTable(signalStrength.getGsmSignalStrength() * 2 - 113);
        }
        */
    }

    public List<Cell> getCellList() {
        synchronized (cellList) {
            Collections.sort(cellList);
            return Collections.unmodifiableList(cellList);
        }
    }

    private void getCellInfo() {
        cellList.clear();
        if (!getAllCellInfo()) {
            getRegisteredCellInfo();
            getNeighboringCellsInfo();
        }
    }

    private boolean getAllCellInfo() {
        final List<CellInfo> cellInfoList;

        cellInfoList = telephonyManager.getAllCellInfo();

        if (cellInfoList != null) {
            Log.i(this.getClass().getName(), "Obtaining cell info through getAllCellInfo() has " +
                    "been successful");
            for (final CellInfo cellInfo : cellInfoList) {
                final CellInfoGsm cellInfoGsm;
                if (cellInfo instanceof CellInfoGsm) {
                    cellInfoGsm = (CellInfoGsm) cellInfo;
                    cellList.add(new Cell(this.networkType, cellInfoGsm));
                }
            }
            return true;
        } else {
            Log.i(this.getClass().getName(), "Obtaining cell info through getAllCellInfo() has " +
                    "failed");
            return false;
        }
    }

    private void getRegisteredCellInfo() {
        final CellLocation cellLocation;
        final GsmCellLocation gsmCellLocation;
        final Cell registeredCell;

        try {
            cellLocation = telephonyManager.getCellLocation();

            if (cellLocation != null && cellLocation instanceof GsmCellLocation) {
                gsmCellLocation = (GsmCellLocation) cellLocation;
                registeredCell = new Cell(this.networkType, mcc, mnc, gsmCellLocation);
                registeredCell.setRegisteredCell();
                cellList.add(registeredCell);
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

        synchronized (cellList) {
            try {
                neighboringCellInfoList = telephonyManager.getNeighboringCellInfo();

                if (neighboringCellInfoList != null) {
                    for (NeighboringCellInfo neighboringCellInfo : neighboringCellInfoList) {
                        this.cellList.add(new Cell(this.networkType, mcc, mnc,
                                neighboringCellInfo));
                        //addToCellTable(neighboringCellInfo);
                    }
                }

            } catch (SecurityException ex) {
                Log.w(this.getClass().getName(), "Couldn't get neighboring cells information due" +
                        " to lack of permission.");
            }
        }
    }
}
