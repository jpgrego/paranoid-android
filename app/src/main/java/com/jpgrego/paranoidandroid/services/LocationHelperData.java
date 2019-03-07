package com.jpgrego.paranoidandroid.services;

import com.jpgrego.paranoidandroid.data.Cell;
import com.jpgrego.paranoidandroid.data.MyBluetoothDevice;
import com.jpgrego.paranoidandroid.data.WifiAP;
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
    private final List<BluetoothBeacon> bluetoothBeacons;
    private final List<CellTower> cellTowers;
    private final List<WifiAccessPoint> wifiAccessPoints;

    LocationHelperData(final String carrier, final int homeMobileCountryCode,
                       final int homeMobileNetworkCode, final List<Cell> cellList,
                       final List<WifiAP> wifiAPList,
                       final List<MyBluetoothDevice> bluetoothDevices) {
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


        bluetoothBeacons = new ArrayList<>();
        for(MyBluetoothDevice bluetoothDevice: bluetoothDevices) {
            bluetoothBeacons.add(BluetoothBeacon.buildFromMyBluetoothDevice(bluetoothDevice));
        }
    }

    private static class BluetoothBeacon {
        private final String macAddress, name;
        private final int signalStrength;
        private final long age;

        private BluetoothBeacon(final MyBluetoothDevice bluetoothDevice) {
            this.macAddress = bluetoothDevice.getAddress();
            this.name = bluetoothDevice.getName();
            this.signalStrength = bluetoothDevice.getRssi();
            this.age = bluetoothDevice.getTimeSinceLastSeen();
        }

        static BluetoothBeacon buildFromMyBluetoothDevice(final MyBluetoothDevice bluetoothDevice) {
            return new BluetoothBeacon(bluetoothDevice);
        }
    }

    private static class CellTower {

        private final Cell.RadioType radioType;
        private final int mobileCountryCode, mobileNetworkCode, locationAreaCode, cellId, psc,
                signalStrength;
        private final long age;

        private CellTower(final Cell cell) {
            this.radioType = cell.getRadioType();
            this.mobileCountryCode = cell.getMcc();
            this.mobileNetworkCode = cell.getMnc();
            this.locationAreaCode = cell.getLac();
            this.cellId = cell.getCid();
            this.age = cell.getTimeSinceLastSeen();
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
}
