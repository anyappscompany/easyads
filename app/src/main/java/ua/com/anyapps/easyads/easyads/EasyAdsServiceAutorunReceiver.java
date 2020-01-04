package ua.com.anyapps.easyads.easyads;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.app.AlarmManager.INTERVAL_HOUR;
import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.ALARM_SERVICE;

public class EasyAdsServiceAutorunReceiver extends BroadcastReceiver {
    private static final String TAG = "debapp";
    private SharedPreferences spPreferences;

    private Long updateInterval;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Log.d(TAG, "onReceive " + intent.getAction());

        spPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        updateInterval = Long.parseLong(spPreferences.getString(context.getString(R.string.sp_default_check_service_interval), context.getString(R.string.default_delay_service)));

        updateInterval = updateInterval*1000;

        Intent i = new Intent(context, EasyAdsServiceAutorunReceiver.class);
        Boolean alarmup=(PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_NO_CREATE)!=null);
        if(!alarmup) { // если не создан планировщик
            Intent intent2 = new Intent(context, EasyAdsServiceAutorunReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent2, 0);
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);

            // 1s = 1000

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), updateInterval, pendingIntent);
            Log.d(TAG, "Создано задание в планировщике Receiver" );
        }else{
            Log.d(TAG, "Планировщик не создан т.к. уже создан Receiver" );
        }

        boolean serviceStatus = false;
        Class<?> serviceClass = EasyAdsService.class;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                serviceStatus = true;
            }
        }

        Log.d(TAG, "Вызван ресивер");
        if(!serviceStatus) { // запустить, если не запущен сервис
            context.startService(new Intent(context, EasyAdsService.class));
        }


        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
