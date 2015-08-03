package com.jpgrego.thesisapp.thesisapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jpgrego on 6/9/15.
 */
public class RegisteredCellFragment extends Fragment {

    private TextView operatorView;
    private TextView cellIDView;
    private TextView cellLACView;
    private TextView cellMCCView;
    private TextView cellMNCView;
    //private TextView cellPSCView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisView = inflater.inflate(R.layout.fragment_main, container, false);
        operatorView = (TextView) thisView.findViewById(R.id.operator_id);
        cellIDView = (TextView) thisView.findViewById(R.id.current_cell_id_val);
        cellLACView = (TextView) thisView.findViewById(R.id.current_cell_lac_val);
        cellMCCView = (TextView) thisView.findViewById(R.id.current_cell_mcc_val);
        cellMNCView = (TextView) thisView.findViewById(R.id.current_cell_mnc_val);
        //cellPSCView = (TextView) thisView.findViewById(R.id.current_cell_psc_val);
        return thisView;
    }

    public void setOperatorView(String operatorName) {
        if(operatorView != null) {
            operatorView.setText(operatorName);
        }
    }

    public void setCurrentCellInfo(MyCellInfo registeredCell) {
        if(registeredCell != null) {
            cellIDView.setText(String.valueOf(registeredCell.getCellID()));
            cellLACView.setText(String.valueOf(registeredCell.getLocalAreaCode()));
            cellMCCView.setText(String.valueOf(registeredCell.getMobileCountryCode()));
            cellMNCView.setText(String.valueOf(registeredCell.getMobileNationalCode()));
            //cellPSCView.setText(String.valueOf(registeredCell.getPrimaryScramblingCode()));
        }
    }

}
