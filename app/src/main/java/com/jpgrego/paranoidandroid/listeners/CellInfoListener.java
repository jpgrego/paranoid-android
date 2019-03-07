package com.jpgrego.paranoidandroid.listeners;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.jpgrego.paranoidandroid.data.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jpgrego on 25-08-2016.
 */
public final class CellInfoListener extends PhoneStateListener {

    private final TelephonyManager telephonyManager;
    private final List<Cell> cellList = new ArrayList<>();
    private int mcc, mnc;
    private int networkType;
    private int registedCellSignalStrength = -1;

    public CellInfoListener(final Context context) {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(this, LISTEN_SIGNAL_STRENGTHS);
        onSignalStrengthsChanged(null);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        final String networkOperatorString = telephonyManager.getNetworkOperator();
        this.networkType = telephonyManager.getNetworkType();

        if(signalStrength != null) {
            registedCellSignalStrength = signalStrength.getGsmSignalStrength() * 2 - 113;
        }

        try {
            this.mcc = Integer.parseInt(networkOperatorString.substring(0, 3));
            this.mnc = Integer.parseInt(networkOperatorString.substring(3));
        } catch (IndexOutOfBoundsException | NumberFormatException | NullPointerException ex) {
            Log.w(this.getClass().getName(), "Obtaining the MCC and MNC values failed " +
                    "(" + ex.getClass().getName() + ")");
            this.mcc = -1;
            this.mnc = -1;
        }

        getCellInfo();
    }

    public List<Cell> getSortedCellList() {
        synchronized (cellList) {
            Collections.sort(cellList);
            return Collections.unmodifiableList(cellList);
        }
    }

    public int getMcc() {
        return mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public String getNetworkOperatorName() {
        return telephonyManager.getNetworkOperatorName();
    }

    private void getCellInfo() {
        synchronized (cellList) {
            cellList.clear();
            if (!getAllCellInfo()) {
                getRegisteredCellInfo();
                getNeighboringCellsInfo();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private boolean getAllCellInfo() {
        final List<CellInfo> cellInfoList;

        // the permission for this info is requested at the start of the app
        cellInfoList = telephonyManager.getAllCellInfo();

        if (cellInfoList != null) {
            Log.i(this.getClass().getName(), "Obtaining cell info through getAllCellInfo() has " +
                    "been successful");
            for (final CellInfo cellInfo : cellInfoList) {
                cellList.add(Cell.fromCellInfo(cellInfo));
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
                registeredCell = Cell.fromGsmCellLocation(this.networkType, mcc, mnc,
                        registedCellSignalStrength, gsmCellLocation);
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
                        this.cellList.add(Cell.fromNeighboringCellInfo(mcc, mnc,
                                neighboringCellInfo));
                    }
                }

            } catch (SecurityException ex) {
                Log.w(this.getClass().getName(), "Couldn't get neighboring cells information due" +
                        " to lack of permission.");
            }
        }
    }
}
