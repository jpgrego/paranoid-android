package com.jpgrego.thesisapp.thesisapp.utils;

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
    private String mccString, mncString;
    private int networkType;

    public CellInfoListener(final TelephonyManager telephonyManager) {
        final String networkOperatorString;

        networkOperatorString = telephonyManager.getNetworkOperator();
        this.telephonyManager = telephonyManager;
        this.mccString = networkOperatorString.substring(0, 3);
        this.mncString = networkOperatorString.substring(3);
        this.networkType = telephonyManager.getNetworkType();
        getCellInfo();
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        final String networkOperatorString;

        networkOperatorString = telephonyManager.getNetworkOperator();
        this.mccString = networkOperatorString.substring(0, 3);
        this.mncString = networkOperatorString.substring(3);
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

    public String getMCC() {
        return mccString;
    }

    public String getMNC() {
        return mncString;
    }

    private void getCellInfo() {
        this.cellList.clear();
        getRegisteredCellInfo();
        getNeighboringCellsInfo();
    }

    private void getRegisteredCellInfo() {
        final CellLocation cellLocation;
        final GsmCellLocation gsmCellLocation;
        final Cell registeredCell;

        try {
            cellLocation = telephonyManager.getCellLocation();

            if (cellLocation != null && cellLocation instanceof GsmCellLocation) {
                gsmCellLocation = (GsmCellLocation) cellLocation;
                registeredCell = new Cell(this.networkType, gsmCellLocation);
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
                        this.cellList.add(new Cell(this.networkType, neighboringCellInfo));
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
