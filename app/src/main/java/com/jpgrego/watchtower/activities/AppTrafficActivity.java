package com.jpgrego.watchtower.activities;

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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.data.AppTrafficData;
import com.jpgrego.watchtower.services.DataService;
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
        appTrafficTable = (TableLayout) findViewById(R.id.app_traffic_table);
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
        final TextView appUidView = (TextView) view.findViewById(R.id.app_uid);
        final TextView appNameView = (TextView) view.findViewById(R.id.app_name);
        final TextView appPackageNameView = (TextView) view.findViewById(R.id.app_package_name);
        final TextView transmittedBytesView = (TextView) view.findViewById(R.id.transmitted_mbytes);
        final TextView receivedBytesView = (TextView) view.findViewById(R.id.received_mbytes);
        final TextView transmittedPackagesView =
                (TextView) view.findViewById(R.id.transmitted_packages);
        final TextView receivedPackagesView = (TextView) view.findViewById(R.id.received_packages);

        final View detailsView = View.inflate(this, R.layout.app_traffic_details, null);

        final CharSequence packageName = appPackageNameView.getText();
        final CharSequence uid = appUidView.getText();
        final CharSequence transmittedBytes = transmittedBytesView.getText();
        final CharSequence receivedBytes = receivedBytesView.getText();
        final CharSequence transmittedPackages = transmittedPackagesView.getText();
        final CharSequence receivedPackages = receivedPackagesView.getText();

        try {
            final ImageView icon = (ImageView) detailsView.findViewById(R.id.app_icon);
            icon.setImageDrawable(getPackageManager().getApplicationIcon(packageName.toString()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final TextView detailsPackageName =
                (TextView) detailsView.findViewById(R.id.package_name_value);
        final TextView detailsUid = (TextView) detailsView.findViewById(R.id.uid_value);
        final TextView detailsTxMBytes = (TextView) detailsView.findViewById(R.id.txmbytes_value);
        final TextView detailsRxMBytes = (TextView) detailsView.findViewById(R.id.rxmbytes_value);
        final TextView detailsTxPkgs = (TextView) detailsView.findViewById(R.id.txpkg_value);
        final TextView detailsRxPkgs = (TextView) detailsView.findViewById(R.id.rxpkg_value);

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

        //sensorsTable.addView(sensorTableTitleRow);

        for (AppTrafficData appTrafficData : appTrafficList) {

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
                    (ImageView) appTrafficTableDataRow.findViewById(R.id.app_icon);
            final TextView appUid = (TextView) appTrafficTableDataRow.findViewById(R.id.app_uid);
            final TextView appPackageName =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.app_package_name);
            final TextView appName = (TextView) appTrafficTableDataRow.findViewById(R.id.app_name);
            final TextView appCount =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.app_count);
            final TextView transmittedMBytes =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.transmitted_mbytes);
            final TextView receivedMBytes =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.received_mbytes);
            final TextView transmittedPackages =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.transmitted_packages);
            final TextView receivedPackages =
                    (TextView) appTrafficTableDataRow.findViewById(R.id.received_packages);

            try {
                appIcon.setImageDrawable(getPackageManager()
                        .getApplicationIcon(appTrafficData.getAppPackageName()));
            } catch (PackageManager.NameNotFoundException ex) {
                // do nothing
            }

            final double transmittedMB = appTrafficData.getTransmittedBytes() / 1048576.0;
            final double receivedMB = appTrafficData.getReceivedBytes() / 1048576.0;

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
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lock.lock();
                        appTrafficTable.addView(appTrafficTableDataRow,
                                new TableLayout.LayoutParams(
                                        TableLayout.LayoutParams.WRAP_CONTENT,
                                        TableLayout.LayoutParams.WRAP_CONTENT)
                        );
                        lock.unlock();
                    }
                });
            }
        }
    }

    private TableRow getAppTableRow(final String appUidVal) {
        for(int i = 0; i < appTrafficTable.getChildCount(); ++i) {
            final View view = appTrafficTable.getChildAt(i);

            if(!(view instanceof TableRow)) continue;

            final TableRow tableRow = (TableRow) view;
            final TextView appUid = (TextView) tableRow.findViewById(R.id.app_uid);
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
