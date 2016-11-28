package com.jpgrego.thesisapp.thesisapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jpgrego.thesisapp.thesisapp.R;

/**
 * Created by jpgrego on 28/11/16.
 */

public final class SensorsFragment extends Fragment {

    private final Handler sensorUpdateHandler = new Handler();

    private final Runnable sensorUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            //map = mainActivity.getSensorInfoListener().getSensorMap();
            sensorUpdateHandler.postDelayed(this, 3000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensors, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorUpdateHandler.post(sensorUpdateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorUpdateHandler.removeCallbacks(sensorUpdateRunnable);
    }
}
