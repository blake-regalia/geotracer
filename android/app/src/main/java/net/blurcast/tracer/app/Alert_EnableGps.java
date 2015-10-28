package net.blurcast.tracer.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import net.blurcast.tracer.R;

public class Alert_EnableGps extends Activity {

    private static final int RESULT_ENABLE_GPS = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_enable_gps);

        final Activity self = this;

        final Intent enableGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        Button enableGps = (Button) findViewById(R.id.alert_enable_gps);
        enableGps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                self.startActivityForResult(enableGpsIntent, RESULT_ENABLE_GPS);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestId, int resultCode, Intent data) {
        if(requestId == RESULT_ENABLE_GPS) {
            startActivity(new Intent(this, Activity_Main.class));
            finish();
        }
    }
}
