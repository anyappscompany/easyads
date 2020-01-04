package ua.com.anyapps.easyads.easyads;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences spPreferences;

    private static final String TAG = "debapp";
    private Long updateInterval;
    EditText etCheckServiceInterval;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        updateInterval = Long.parseLong(spPreferences.getString(getString(R.string.sp_default_check_service_interval), getString(R.string.default_delay_service)));

        etCheckServiceInterval = (EditText) findViewById(R.id.etCheckServiceInterval);
        etCheckServiceInterval.setText(updateInterval.toString());
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "На странице настроек нажата кнопка \"Назад\"");

        SharedPreferences.Editor editor = spPreferences.edit();
        editor.putString(getString(R.string.sp_default_check_service_interval), etCheckServiceInterval.getText().toString());
        editor.commit();


        Intent myIntent = new Intent(getApplicationContext(), EasyAdsServiceAutorunReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC, updateInterval, pendingIntent);

        finish();
    }
}
