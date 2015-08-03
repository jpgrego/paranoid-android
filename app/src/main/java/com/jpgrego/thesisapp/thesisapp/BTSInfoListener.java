package com.jpgrego.thesisapp.thesisapp;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

/**
 * Created by jpgrego on 6/9/15.
 */
public class BTSInfoListener extends PhoneStateListener {

    private final TelephonyManager telephonyManager;


    public BTSInfoListener(Context context) {
        this.telephonyManager = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE
        );
    }

    @Override
    public void onCellLocationChanged(CellLocation cellLocation) {
        GsmCellLocation gsmCellLocation;
        int cellID, localAreaCode, primaryScramblingCode, rssi, mcc = -1, mnc = -1;
        String networkOperatorString;

        gsmCellLocation = (GsmCellLocation) cellLocation;
        cellID = gsmCellLocation.getCid();
        localAreaCode = gsmCellLocation.getLac();
        primaryScramblingCode = gsmCellLocation.getPsc();
        networkOperatorString = telephonyManager.getNetworkOperator();

        // getNetworkOperator() returns an empty string if the SIM card isn't present
        if(networkOperatorString != null && networkOperatorString.equals("") == false) {
            mcc = Integer.parseInt(networkOperatorString.substring(0, 3));
            mnc = Integer.parseInt(networkOperatorString.substring(3));
        }

        MainActivity.setOperator(telephonyManager.getNetworkOperatorName());

        MainActivity.setRegisteredCell(
                new MyCellInfo(cellID, localAreaCode, primaryScramblingCode, mcc, mnc)
        );

        // there seems to be no listener to obtain NeighboringCells so we use this one
        MainActivity.setNeighboringCells(telephonyManager.getNeighboringCellInfo());
    }

    // this doesn't work in all devices
    /*
    public void onCellInfoChanged(List<CellInfo> cellInfoList) {
        for(CellInfo cellInfo : cellInfoList) {
            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;

            CellIdentityGsm cellIdentity = cellInfoGsm.getCellIdentity();
            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();

            Log.println(Log.INFO, "cell", "registered: " + cellInfoGsm.isRegistered());
            Log.println(Log.INFO, "cell", cellIdentity.toString());
            Log.println(Log.INFO, "cell", cellSignalStrengthGsm.toString());
        }
    }
    */
}
