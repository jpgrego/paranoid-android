package com.jpgrego.thesisapp.thesisapp;

/**
 * Created by jpgrego on 6/10/15.
 */
public class MyCellInfo {

    private final int cellID;
    private final int localAreaCode;
    private final int primaryScramblingCode;
    private final int mobileCountryCode;
    private final int mobileNationalCode;

    public MyCellInfo(int cellID, int localAreaCode, int primaryScramblingCode,
                      int mobileCountryCode, int mobileNationalCode) {
        this.cellID = cellID;
        this.localAreaCode = localAreaCode;
        this.primaryScramblingCode = primaryScramblingCode;
        this.mobileCountryCode = mobileCountryCode;
        this.mobileNationalCode = mobileNationalCode;
    }

    public int getCellID() {
        return cellID;
    }

    public int getLocalAreaCode() {
        return localAreaCode;
    }

    public int getPrimaryScramblingCode() {
        return primaryScramblingCode;
    }

    public int getMobileCountryCode() {
        return mobileCountryCode;
    }

    public int getMobileNationalCode() {
        return mobileNationalCode;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (this.getClass() != object.getClass()) {
            return false;
        }

        final MyCellInfo otherCellInfo = (MyCellInfo) object;

        if (this.cellID == otherCellInfo.cellID
                && this.localAreaCode == otherCellInfo.localAreaCode
                && this.primaryScramblingCode == otherCellInfo.primaryScramblingCode) {
            return true;
        } else {
            return false;
        }
    }


}
