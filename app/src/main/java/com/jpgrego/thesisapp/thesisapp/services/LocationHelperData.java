package com.jpgrego.thesisapp.thesisapp.services;

import com.jpgrego.thesisapp.thesisapp.data.Cell;
import com.jpgrego.thesisapp.thesisapp.data.WifiAP;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpgrego on 10/01/17.
 */

@SuppressWarnings({"unused", "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection"})
final class LocationHelperData {

    private final boolean considerIp = true;

    private final String carrier;
    private final int homeMobileCountryCode;
    private final int homeMobileNetworkCode;
    //private final List<BluetoothBeacon> bluetoothBeacons;
    private final List<CellTower> cellTowers;
    private final List<WifiAccessPoint> wifiAccessPoints;

    LocationHelperData(final String carrier, final int homeMobileCountryCode,
                       final int homeMobileNetworkCode, final List<Cell> cellList,
                       final List<WifiAP> wifiAPList) {
        this.carrier = carrier;
        this.homeMobileCountryCode = homeMobileCountryCode;
        this.homeMobileNetworkCode = homeMobileNetworkCode;

        cellTowers = new ArrayList<>();
        for(Cell cell : cellList) {
            cellTowers.add(CellTower.buildFromCell(cell));
        }

        wifiAccessPoints = new ArrayList<>();
        for(WifiAP wifiAP : wifiAPList) {
            wifiAccessPoints.add(WifiAccessPoint.buildFromWifiAP(wifiAP));
        }
    }

    private static class BluetoothBeacon {
        private final String macAddress, name;
        private final int age, signalStrength;

        private BluetoothBeacon(final String macAddress, final String name, final int age,
                               final int signalStrength) {
            this.macAddress = macAddress;
            this.name = name;
            this.age = age;
            this.signalStrength = signalStrength;
        }
    }

    private static class CellTower {

        private final RadioType radioType;
        private final int mobileCountryCode, mobileNetworkCode, locationAreaCode, cellId, age, psc,
                signalStrength;

        @SuppressWarnings("unused")
        private CellTower() {
            throw new AssertionError();
        }

        private CellTower(final Cell cell) {
            this.radioType = RadioType.gsm;
            this.mobileCountryCode = cell.getMcc();
            this.mobileNetworkCode = cell.getMnc();
            this.locationAreaCode = cell.getLac();
            this.cellId = cell.getCid();
            this.age = -1;
            this.psc = cell.getPsc();
            this.signalStrength = cell.getDbm();
        }

        static CellTower buildFromCell(final Cell cell) {
            return new CellTower(cell);
        }
    }

    private static class WifiAccessPoint {

        private final String macAddress;
        private final int channel, frequency, signalStrength;
        private final long age;

        private WifiAccessPoint(final WifiAP wifiAP) {
            this.macAddress = wifiAP.getBssid();
            this.age = wifiAP.getTimeSinceLastSeen();
            this.channel = wifiAP.getChannel();
            this.frequency = wifiAP.getFrequency();
            this.signalStrength = wifiAP.getDbm();
        }

        static WifiAccessPoint buildFromWifiAP(final WifiAP wifiAP) {
            return new WifiAccessPoint(wifiAP);
        }

    }

    private enum RadioType {
        gsm,
        wcdma,
        lte

        /*
        RadioType(final String name) {
            try {
                final Field nameField = this.getClass().getSuperclass().getDeclaredField("name");
                nameField.setAccessible(true);
                nameField.set(this, name);
                nameField.setAccessible(false);
            } catch (Exception e) {
                // do nothing
            }
        }
        */

    }
}
