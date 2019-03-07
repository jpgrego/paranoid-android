package com.jpgrego.paranoidandroid.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.gsm.GsmCellLocation;
import com.jpgrego.paranoidandroid.utils.CellUtils;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpgrego on 25-08-2016.
 */

public final class Cell implements Comparable<Cell>, Parcelable {

    public enum RadioType {
        gsm,
        wcdma,
        lte
    }

    private final RadioType radioType;
    private final int mcc, mnc, cid, lac, psc, dbm;
    private final String generation;
    private final boolean isRegisteredCell;
    private final long timestamp;
    private volatile int hashCode;

    private Cell(Parcel in) {
        this.radioType = RadioType.valueOf(in.readString());
        this.mcc = in.readInt();
        this.mnc = in.readInt();
        this.cid = in.readInt();
        this.lac = in.readInt();
        this.psc = in.readInt();
        this.dbm = in.readInt();
        this.generation = in.readString();
        this.isRegisteredCell = in.readByte() != 0;
        this.timestamp = in.readLong();
    }

    private Cell(final CellInfoGsm cellInfoGsm) {
        final CellIdentityGsm cellIdentityGsm;
        final CellSignalStrengthGsm cellSignalStrengthGsm;

        cellIdentityGsm = cellInfoGsm.getCellIdentity();
        cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();

        final int mcc = cellIdentityGsm.getMcc();
        final int mnc = cellIdentityGsm.getMnc();
        final int cid = cellIdentityGsm.getCid();
        final int lac = cellIdentityGsm.getLac();

        this.radioType = RadioType.gsm;
        this.mcc = mcc != Integer.MAX_VALUE ? mcc : -1;
        this.mnc = mnc != Integer.MAX_VALUE ? mnc : -1;
        this.cid = cid != Integer.MAX_VALUE ? cid : -1;
        this.lac = lac != Integer.MAX_VALUE ? lac : -1;
        //noinspection deprecation
        this.psc = -1;  // undefined for GSM
        this.dbm = cellSignalStrengthGsm.getDbm();
        //this.generation = CellUtils.getGenerationFromNetworkType(networkType);
        this.generation = "2G";
        this.isRegisteredCell = cellInfoGsm.isRegistered();
        this.timestamp = TimeUnit.NANOSECONDS.toMillis(cellInfoGsm.getTimeStamp());
    }

    private Cell(final CellInfoLte cellInfoLte) {
        final CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
        final CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();

        this.radioType = RadioType.lte;
        final int mcc = cellIdentityLte.getMcc();
        final int mnc = cellIdentityLte.getMnc();
        final int cid = cellIdentityLte.getCi();

        this.mcc = mcc != Integer.MAX_VALUE ? mcc : -1;
        this.mnc = mnc != Integer.MAX_VALUE ? mnc : -1;
        this.cid = cid != Integer.MAX_VALUE ? cid : -1;
        //this.lac = cellIdentityLte.getTac();
        this.lac = -1;
        //this.psc = cellIdentityLte.getPci();
        this.psc = -1;
        this.dbm = cellSignalStrengthLte.getDbm();
        this.generation = "4G";
        this.isRegisteredCell = cellInfoLte.isRegistered();
        this.timestamp = TimeUnit.NANOSECONDS.toMillis(cellInfoLte.getTimeStamp());
    }

    private Cell(final CellInfoWcdma cellInfoWcdma) {
        final CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
        final CellSignalStrengthWcdma cellSignalStrengthWcdma =
                cellInfoWcdma.getCellSignalStrength();

        final int mcc = cellIdentityWcdma.getMcc();
        final int mnc = cellIdentityWcdma.getMnc();
        final int cid = cellIdentityWcdma.getCid();
        final int lac = cellIdentityWcdma.getLac();
        final int psc = cellIdentityWcdma.getPsc();

        this.radioType = RadioType.wcdma;
        this.mcc = mcc != Integer.MAX_VALUE ? mcc : -1;
        this.mnc = mnc != Integer.MAX_VALUE ? mnc : -1;
        this.cid = cid != Integer.MAX_VALUE ? cid : -1;
        this.lac = lac != Integer.MAX_VALUE ? lac : -1;
        this.psc = psc != Integer.MAX_VALUE ? psc : -1;
        this.dbm = cellSignalStrengthWcdma.getDbm();
        this.generation = "3G";
        this.isRegisteredCell = cellInfoWcdma.isRegistered();
        this.timestamp = TimeUnit.NANOSECONDS.toMillis(cellInfoWcdma.getTimeStamp());
    }

    private Cell(final boolean isRegisteredCell, final int networkType, final int mcc,
                 final int mnc, final int dbm, final GsmCellLocation cellLocation) {
        this.radioType = RadioType.gsm;
        this.mcc = mcc;
        this.mnc = mnc;
        this.cid = cellLocation.getCid();
        this.lac = cellLocation.getLac();
        this.psc = cellLocation.getPsc();
        this.dbm = dbm;
        this.isRegisteredCell = isRegisteredCell;
        this.generation = CellUtils.getGenerationFromNetworkType(networkType);
        this.timestamp = SystemClock.elapsedRealtime();
    }

    private Cell(final boolean isRegisteredCell, final int mcc, final int mnc,
                 final NeighboringCellInfo neighboringCellInfo) {
        this.radioType = RadioType.gsm;
        this.mcc = mcc;
        this.mnc = mnc;
        this.cid = neighboringCellInfo.getCid();
        this.lac = neighboringCellInfo.getLac();
        this.psc = neighboringCellInfo.getPsc();
        this.dbm = CellUtils.rssiToDbm(neighboringCellInfo.getNetworkType(),
                neighboringCellInfo.getRssi());
        this.isRegisteredCell = isRegisteredCell;
        this.generation = CellUtils.getGenerationFromNetworkType(
                neighboringCellInfo.getNetworkType());
        this.timestamp = SystemClock.elapsedRealtime();
    }

    public static Cell fromGsmCellLocation(final int networkType, final int mcc, final int mnc,
                                           final int dbm, final GsmCellLocation cellLocation) {
        return new Cell(true, networkType, mcc, mnc, dbm, cellLocation);
    }

    public static Cell fromNeighboringCellInfo(final int mcc, final int mnc,
                                                         final NeighboringCellInfo
                                                                 neighboringCellInfo) {
        return new Cell(false, mcc, mnc, neighboringCellInfo);
    }

    public static Cell fromCellInfo(final CellInfo cellInfo) {
        if(cellInfo instanceof CellInfoGsm) {
            return new Cell((CellInfoGsm) cellInfo);
        } else if(cellInfo instanceof CellInfoLte) {
            return new Cell((CellInfoLte) cellInfo);
        } else if(cellInfo instanceof CellInfoWcdma) {
            return new Cell((CellInfoWcdma) cellInfo);
        } else {
            return null;
        }
    }

    public static final Creator<Cell> CREATOR = new Creator<Cell>() {
        @Override
        public Cell createFromParcel(Parcel in) {
            return new Cell(in);
        }

        @Override
        public Cell[] newArray(int size) {
            return new Cell[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        // CellID is not guaranteed to be correct, so there's no foolproof way to ensure cell
        // equality
        return false;
    }

    @Override
    public int hashCode() {
        int result = hashCode;

        if(result == 0) {
            result = 17;
            result = 37 * result + mcc;
            result = 37 * result + mnc;
            result = 37 * result + cid;
            result = 37 * result + lac;
            result = 37 * result + psc;
            hashCode = result;
        }

        return result;
    }


    @Override
    public int compareTo(@NonNull Cell another) {
        final int dbmDiff = another.dbm - this.dbm;
        return dbmDiff != 0 ? dbmDiff : 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(radioType.name());
        parcel.writeInt(mcc);
        parcel.writeInt(mnc);
        parcel.writeInt(cid);
        parcel.writeInt(lac);
        parcel.writeInt(psc);
        parcel.writeInt(dbm);
        parcel.writeString(generation);
        parcel.writeByte((byte) (isRegisteredCell ? 1 : 0));
        parcel.writeLong(timestamp);
    }

    public boolean isRegisteredCell() {
        return isRegisteredCell;
    }

    public RadioType getRadioType() {
        return radioType;
    }

    public int getMcc() {
        return mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public int getCid() {
        return cid;
    }

    public int getLac() {
        return lac;
    }

    public int getPsc() {
        return psc;
    }

    public int getDbm() {
        return dbm;
    }

    public String getGeneration() {
        return generation;
    }

    public long getTimeSinceLastSeen() {
        return SystemClock.elapsedRealtime() - this.timestamp;
    }

}
