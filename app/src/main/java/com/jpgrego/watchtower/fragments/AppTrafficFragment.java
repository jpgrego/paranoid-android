package com.jpgrego.watchtower.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.data.AppTrafficData;
import com.jpgrego.watchtower.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by jpgrego on 1/31/17.
 */

public final class AppTrafficFragment extends Fragment {

    private final BroadcastReceiver appTrafficInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<AppTrafficData> appTrafficList = intent.getParcelableArrayListExtra(
                    Constants.APP_TRAFFIC_LIST_INTENT_EXTRA_NAME);
            if(appTrafficList != null) {
                updateAppTrafficTable(appTrafficList);
            }
        }
    };

    private TableLayout appTrafficTable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View thisView = inflater.inflate(R.layout.fragment_app_traffic, container, false);
        appTrafficTable = (TableLayout) thisView.findViewById(R.id.app_traffic_table);
        return thisView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(appTrafficInfoReceiver,
                new IntentFilter(Constants.APP_TRAFFIC_RESPONSE_INTENT_FILTER_NAME));

        final Intent requestInfo = new Intent(Constants.APP_TRAFFIC_REQUEST_INTENT_FILTER_NAME);
        getActivity().sendBroadcast(requestInfo);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(appTrafficInfoReceiver);
    }

    private void updateAppTrafficTable(List<AppTrafficData> appTrafficList) {
        /*
         * This is done to avoid a NullPointerException being thrown by View.inflate, due to the
         * fact that getActivity() returns null in case the fragment isn't added to the activity,
         * which seems to happen occasionally
         */
        if (!isAdded()) {
            return;
        }

        //final TableRow sensorTableTitleRow =
        //        (TableRow) View.inflate(getActivity(), R.layout.sensors_table_title_row, null);

        appTrafficTable.removeAllViews();
        //sensorsTable.addView(sensorTableTitleRow);

        for (AppTrafficData appTrafficData : appTrafficList) {
            final TableRow appTrafficTableDataRow =
                    (TableRow) View.inflate(getActivity(),
                            R.layout.app_traffic_table_data_row, null);
            final ImageView appIcon =
                    (ImageView) appTrafficTableDataRow.findViewById(R.id.app_icon);
            final TextView appUid = (TextView) appTrafficTableDataRow.findViewById(R.id.app_uid);
            final TextView appPackageName =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.app_package_name);
            final TextView appName = (TextView) appTrafficTableDataRow.findViewById(R.id.app_name);
            final TextView appCount =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.app_count);
            final TextView transmittedBytes =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.transmitted_bytes);
            final TextView receivedBytes =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.received_bytes);
            final TextView transmittedPackages =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.transmitted_packages);
            final TextView receivedPackages =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.received_packages);

            try {
                appIcon.setImageDrawable(getActivity().getPackageManager()
                        .getApplicationIcon(appTrafficData.getAppPackageName()));
            } catch (PackageManager.NameNotFoundException ex) {
                // do nothing
            }

            final double transmittedMB = appTrafficData.getTransmittedBytes() / 1048576.0;
            final double receivedMB = appTrafficData.getReceivedBytes() / 1048576.0;

            appUid.setText(String.format(Locale.US, "%d", appTrafficData.getUid()));
            appPackageName.setText(appTrafficData.getAppPackageName());
            appName.setText(appTrafficData.getAppName());
            appCount.setText(String.format(Locale.US, "%.1f MB", transmittedMB + receivedMB));
            transmittedBytes.setText(
                    String.format(Locale.US, "%d", appTrafficData.getTransmittedBytes()));
            receivedBytes.setText(String.format(Locale.US, "%d",
                    appTrafficData.getReceivedBytes()));
            transmittedPackages.setText(
                    String.format(Locale.US, "%d", appTrafficData.getTransmittedPackages()));
            receivedPackages.setText(String.format(Locale.US, "%d",
                    appTrafficData.getReceivedPackages()));


            appTrafficTable.addView(appTrafficTableDataRow, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }
}
