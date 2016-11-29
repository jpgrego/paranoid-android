package com.jpgrego.thesisapp.thesisapp.fragments;

import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jpgrego.thesisapp.thesisapp.R;
import com.jpgrego.thesisapp.thesisapp.activities.MainActivity;
import com.jpgrego.thesisapp.thesisapp.data.Cell;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jpgrego on 28/11/16.
 */

public final class SensorsFragment extends Fragment {

    private static final Handler SENSOR_UPDATE_HANDLER = new Handler();

    private TableLayout sensorsTable;
    private MainActivity mainActivity;

    private final Runnable sensorUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateSensorsTable(mainActivity.getSensorInfoListener().getSensorMap());
            SENSOR_UPDATE_HANDLER.postDelayed(this, 3000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View thisView = inflater.inflate(R.layout.fragment_sensors, container, false);
        sensorsTable = (TableLayout) thisView.findViewById(R.id.cells_table);
        mainActivity = (MainActivity) this.getContext();
        return thisView;
    }

    @Override
    public void onResume() {
        super.onResume();
        SENSOR_UPDATE_HANDLER.post(sensorUpdateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        SENSOR_UPDATE_HANDLER.removeCallbacks(sensorUpdateRunnable);
    }

    private void updateSensorsTable(Map<Sensor, float[]> sensorMap) {
        final List<Sensor> sensorList;

        /*
         * This is done to avoid a NullPointerException being thrown by View.inflate, due to the
         * fact that getActivity() returns null in case the fragment isn't added to the activity,
         * which seems to happen occasionally
         */
        if (!isAdded()) {
            return;
        }

        //final TableRow sensorTableDataRow =
        //        (TableRow) View.inflate(getActivity(), R.layout.sensors_table_title_row, null);

        sensorsTable.removeAllViews();
        //sensorsTable.addView(sensorTableDataRow);


        for (Cell cell : cellList) {
            final TextView generation, mcc, mnc, cid, lac, psc, dbm;
            final TableRow cellTableDataRow;

            cellTableDataRow = (TableRow) View.inflate(getActivity(), R.layout.cell_table_data_row,
                    null);
            generation = (TextView) cellTableDataRow.findViewById(R.id.generation);
            mcc = (TextView) cellTableDataRow.findViewById(R.id.mcc);
            mnc = (TextView) cellTableDataRow.findViewById(R.id.mnc);
            cid = (TextView) cellTableDataRow.findViewById(R.id.cid);
            lac = (TextView) cellTableDataRow.findViewById(R.id.lac);
            psc = (TextView) cellTableDataRow.findViewById(R.id.psc);
            dbm = (TextView) cellTableDataRow.findViewById(R.id.cell_dbm);
            generation.setText(cell.generation);
            mcc.setText(String.format(Locale.US, "%d", cell.mcc));
            mnc.setText(String.format(Locale.US, "%d", cell.mnc));
            cid.setText(String.format(Locale.US, "%d", cell.cid));
            lac.setText(String.format(Locale.US, "%d", cell.lac));
            psc.setText(String.format(Locale.US, "%d", cell.psc));
            dbm.setText(String.format(Locale.US, "%d", cell.dbm));

            if (cell.isRegisteredCell()) {
                cellTableDataRow.setBackgroundColor(HIGHLIGHTED_BACKGROUND);
            }

            cellsTable.addView(cellTableDataRow, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

}
