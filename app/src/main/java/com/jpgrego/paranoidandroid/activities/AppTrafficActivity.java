package com.jpgrego.paranoidandroid.activities;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jpgrego.paranoidandroid.R;
import com.jpgrego.paranoidandroid.data.AppTrafficData;
import com.jpgrego.paranoidandroid.services.DataService;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jpgrego on 1/31/17.
 */

//TODO: loading icon
public final class AppTrafficActivity extends BaseActivity {

    private static final int SUCCESSFUL_UPDATE_PERIOD_SECONDS = 60;
    private static final int UNSUCCESSFUL_UPDATE_PERIOD_SECONDS = 1;

    private final ReentrantLock lock = new ReentrantLock();

    private volatile ScheduledFuture<?> scheduledUpdates = null;
    private ProgressBar loadingCircle;
    private TableLayout appTrafficTable;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            final DataService dataService =
                    ((DataService.LocalBinder) iBinder).getDataServiceInstance();

/*            scheduledUpdates = DataService.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                    new UpdateAppTrafficRunnable(dataService),
                    0 ,
                    UPDATE_PERIOD_SECONDS,
                    TimeUnit.SECONDS);*/

            DataService.SCHEDULED_EXECUTOR.execute(new UpdateAppTrafficRunnable(dataService));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_traffic_activity_title);
        setContentView(R.layout.activity_app_traffic);
        appTrafficTable =  findViewById(R.id.app_traffic_table);
        loadingCircle = findViewById(R.id.app_traffic_loading_icon);
    }


    @Override
    public void onResume() {
        super.onResume();
        final Intent serviceIntent = new Intent(this, DataService.class);
        bindService(serviceIntent, serviceConnection, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(scheduledUpdates != null) scheduledUpdates.cancel(true);
        unbindService(serviceConnection);
    }

    //TODO: implement this
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    public void appTrafficRowClick(View view) {
        final Dialog dialog = new Dialog(this);
        final TextView appUidView =  view.findViewById(R.id.app_uid);
        final TextView appNameView =  view.findViewById(R.id.app_name);
        final TextView appPackageNameView =  view.findViewById(R.id.app_package_name);
        final TextView transmittedBytesView =  view.findViewById(R.id.transmitted_mbytes);
        final TextView receivedBytesView =  view.findViewById(R.id.received_mbytes);
        final TextView transmittedPackagesView =
               view.findViewById(R.id.transmitted_packages);
        final TextView receivedPackagesView =  view.findViewById(R.id.received_packages);

        final View detailsView = View.inflate(this, R.layout.app_traffic_details, null);

        final CharSequence packageName = appPackageNameView.getText();
        final CharSequence uid = appUidView.getText();
        final CharSequence transmittedBytes = transmittedBytesView.getText();
        final CharSequence receivedBytes = receivedBytesView.getText();
        final CharSequence transmittedPackages = transmittedPackagesView.getText();
        final CharSequence receivedPackages = receivedPackagesView.getText();

        try {
            final ImageView icon =  detailsView.findViewById(R.id.app_icon);
            icon.setImageDrawable(getPackageManager().getApplicationIcon(packageName.toString()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final TextView detailsPackageName =
               detailsView.findViewById(R.id.package_name_value);
        final TextView detailsUid =  detailsView.findViewById(R.id.uid_value);
        final TextView detailsTxMBytes =  detailsView.findViewById(R.id.txmbytes_value);
        final TextView detailsRxMBytes =  detailsView.findViewById(R.id.rxmbytes_value);
        final TextView detailsTxPkgs =  detailsView.findViewById(R.id.txpkg_value);
        final TextView detailsRxPkgs =  detailsView.findViewById(R.id.rxpkg_value);

        detailsPackageName.setText(packageName);
        detailsUid.setText(uid);
        detailsTxMBytes.setText(String.format("%s MB", transmittedBytes));
        detailsRxMBytes.setText(String.format("%s MB", receivedBytes));
        detailsTxPkgs.setText(transmittedPackages);
        detailsRxPkgs.setText(receivedPackages);

        dialog.setTitle(appNameView.getText().toString());
        dialog.setContentView(detailsView);
        dialog.show();
    }

    private void updateAppTrafficTable(List<AppTrafficData> appTrafficList) {
        /*
         * This is done to avoid a NullPointerException being thrown by View.inflate, due to the
         * fact that getActivity() returns null in case the fragment isn't added to the activity,
         * which seems to happen occasionally
         */
        /*
        if (!isAdded()) {
            return;
        }
        */

        //final TableRow sensorTableTitleRow =
        //        (TableRow) View.inflate(getActivity(), R.layout.sensors_table_title_row, null);


        for (final AppTrafficData appTrafficData : appTrafficList) {

            final String appUidVal = String.format(Locale.US, "%d", appTrafficData.getUid());
            final TableRow appTrafficTableDataRow;
            final TableRow existingRow = getAppTableRow(appUidVal);

            if(existingRow != null) {
                appTrafficTableDataRow = existingRow;
            } else {
                appTrafficTableDataRow = (TableRow) View.inflate(this,
                        R.layout.app_traffic_table_data_row, null);
            }

            final ImageView appIcon =
                   appTrafficTableDataRow.findViewById(R.id.app_icon);
            final TextView appUid =  appTrafficTableDataRow.findViewById(R.id.app_uid);
            final TextView appPackageName =
                   appTrafficTableDataRow.findViewById(R.id.app_package_name);
            final TextView appName =  appTrafficTableDataRow.findViewById(R.id.app_name);
            final TextView appCount =
                   appTrafficTableDataRow.findViewById(R.id.app_count);
            final TextView transmittedMBytes =
                   appTrafficTableDataRow.findViewById(R.id.transmitted_mbytes);
            final TextView receivedMBytes =
                   appTrafficTableDataRow.findViewById(R.id.received_mbytes);
            final TextView transmittedPackages =
                   appTrafficTableDataRow.findViewById(R.id.transmitted_packages);
            final TextView receivedPackages =
                   appTrafficTableDataRow.findViewById(R.id.received_packages);

            try {
                appIcon.setImageDrawable(getPackageManager()
                        .getApplicationIcon(appTrafficData.getAppPackageName()));
            } catch (PackageManager.NameNotFoundException ex) {
                // do nothing
            }

            final double transmittedMB = appTrafficData.getTransmittedBytes() / 1048576.0;
            final double receivedMB = appTrafficData.getReceivedBytes() / 1048576.0;

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appUid.setText(appUidVal);
                    appPackageName.setText(appTrafficData.getAppPackageName());
                    appName.setText(appTrafficData.getAppName());
                    appCount.setText(String.format(Locale.US, "%.1f MB", transmittedMB + receivedMB));
                    transmittedMBytes.setText(
                            String.format(Locale.US, "%.3f", transmittedMB));
                    receivedMBytes.setText(String.format(Locale.US, "%.3f", receivedMB));
                    transmittedPackages.setText(
                            String.format(Locale.US, "%d", appTrafficData.getTransmittedPackages()));
                    receivedPackages.setText(String.format(Locale.US, "%d",
                            appTrafficData.getReceivedPackages()));

                    if(existingRow == null) {
                        lock.lock();
                        appTrafficTable.addView(appTrafficTableDataRow,
                                new TableLayout.LayoutParams(
                                        TableLayout.LayoutParams.WRAP_CONTENT,
                                        TableLayout.LayoutParams.WRAP_CONTENT)
                        );
                        lock.unlock();
                    }
                }
            });

        }
    }

    private TableRow getAppTableRow(final String appUidVal) {
        for(int i = 0; i < appTrafficTable.getChildCount(); ++i) {
            final View view = appTrafficTable.getChildAt(i);

            if(!(view instanceof TableRow)) continue;

            final TableRow tableRow = (TableRow) view;
            final TextView appUid =  tableRow.findViewById(R.id.app_uid);
            if(appUidVal.equalsIgnoreCase(appUid.getText().toString())) return tableRow;
        }

        return null;
    }

    private final class UpdateAppTrafficRunnable implements Runnable {

        private final DataService service;

        private UpdateAppTrafficRunnable(final DataService service) {
            this.service = service;
        }

        @Override
        public void run() {
            final List<AppTrafficData> appTrafficList = service.getAppTrafficDataList();
            if(appTrafficList != null && appTrafficList.size() > 0) {

                AppTrafficActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingCircle.setVisibility(View.GONE);
                    }
                });

                updateAppTrafficTable(appTrafficList);
                scheduledUpdates = DataService.SCHEDULED_EXECUTOR.schedule(this,
                        SUCCESSFUL_UPDATE_PERIOD_SECONDS,
                        TimeUnit.SECONDS);
            } else {
                scheduledUpdates = DataService.SCHEDULED_EXECUTOR.schedule(this,
                        UNSUCCESSFUL_UPDATE_PERIOD_SECONDS,
                        TimeUnit.SECONDS);
            }
        }
    }
}
