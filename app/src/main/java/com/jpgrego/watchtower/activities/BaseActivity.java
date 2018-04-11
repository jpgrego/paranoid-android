package com.jpgrego.watchtower.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.jpgrego.watchtower.R;

/**
 * Created by jpgrego on 3/22/18.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabbed, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(this instanceof WifiAndCells) {
            greyOutAndDisableItem(menu, R.id.action_radio, R.drawable.network);
        } else if(this instanceof AppTrafficActivity) {
            greyOutAndDisableItem(menu, R.id.action_traffic, R.drawable.traffic);
        } else if(this instanceof SensorsActivity) {
            greyOutAndDisableItem(menu, R.id.action_sensors, R.drawable.sensors);
        } else if(this instanceof MapActivity) {
            greyOutAndDisableItem(menu, R.id.action_map, R.drawable.location);
        }

        return true;
    }

    public void actionBarButtonClicked(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_radio:
                startActivity(new Intent(this, WifiAndCells.class));
                break;
            case R.id.action_traffic:
                startActivity(new Intent(this, AppTrafficActivity.class));
                break;
            case R.id.action_sensors:
                startActivity(new Intent(this, SensorsActivity.class));
                break;
            case R.id.action_map:
                startActivity(new Intent(this, MapActivity.class));
                break;
            default:
        }
    }

    private void greyOutAndDisableItem(final Menu menu, final int itemId, final int iconId) {
        final MenuItem item = menu.findItem(itemId);
        final Drawable icon = getResources().getDrawable(iconId);

        icon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        item.setEnabled(false);
        item.setIcon(icon);
    }
}
