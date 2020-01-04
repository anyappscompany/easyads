package ua.com.anyapps.easyads.easyads;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import ua.com.anyapps.easyads.easyads.Messages.MessagesSenderListActivity;

import static android.app.AlarmManager.INTERVAL_HOUR;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "debapp";
    private SharedPreferences spPreferences;
    private String authToken;
    private Long updateInterval;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        updateInterval = Long.parseLong(spPreferences.getString(getString(R.string.sp_default_check_service_interval), getString(R.string.default_delay_service)));
        /*
        //https://stackoverflow.com/questions/4556670/how-to-check-if-alarmmanager-already-has-an-alarm-set

        Intent intent = new Intent("com.my.package.MY_UNIQUE_ACTION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 1);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60, pendingIntent);









        boolean alarmUp = (PendingIntent.getBroadcast(this, 0,
                new Intent("com.my.package.MY_UNIQUE_ACTION"),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp)
        {
            Log.d("myTag", "Alarm is already active");
        }*/
        /*Cursor c = db.rawQuery("select * from cities limit 10", null);
        logCursor(c, "Table people");
        c.close();*/
        /*Intent alarmIntent = new Intent(MainActivity.this, EasyAdsServiceAutorunReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 900;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);*/

        /*Intent intent = new Intent(MainActivity.this, EasyAdsService.class);
        PendingIntent pintent = PendingIntent.getService(MainActivity.this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 900, pintent);*/

        //проверить существует ли задание
        //определить время запуска: при установке или загрузке
        Intent i = new Intent(this, EasyAdsServiceAutorunReceiver.class);
        Boolean alarmup=(PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_NO_CREATE)!=null);

if(!alarmup) { // если не создан планировщик
    Intent intent = new Intent(this, EasyAdsServiceAutorunReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this.getApplicationContext(), 0, intent, 0);
    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), updateInterval, pendingIntent);
    Log.d(TAG, "Создано задание в планировщике Main" );
}else{
    Log.d(TAG, "Планировщик не создан т.к. уже создан Main" );
}

    }

    // вывод в лог данных из курсора
    void logCursor(Cursor c, String title) {
        if (c != null) {
            if (c.moveToFirst()) {
                //Log.d(TAG, title + ". " + c.getCount() + " rows");
                StringBuilder sb = new StringBuilder();
                do {
                    sb.setLength(0);
                    for (String cn : c.getColumnNames()) {
                        sb.append(cn + " = "
                                + c.getString(c.getColumnIndex(cn)) + "; ");
                    }
                    //Log.d(TAG, sb.toString());
                } while (c.moveToNext());
            }
        } else{
            //Log.d(TAG, title + ". Cursor is null");
        }
    }

    public void btnLogoutClick(View v){
        // очистить хранилище с данными о текущем пользователе


        if(spPreferences.contains(getString(R.string.auth_token))) {
            //Log.d(TAG, "Пользователь удален из хранилища");
            authToken = spPreferences.getString(getString(R.string.auth_token), null);

            SharedPreferences.Editor editor = spPreferences.edit();
            editor.remove(getString(R.string.auth_token));
            editor.commit();

            // и перейти на экран входа
            //Log.d(TAG, "Переход к экрану регистрации");
            Intent intent = new Intent(this, LoginRegistrationActivity.class);
            startActivity(intent);
            finish();
        }else{
            //Log.d(TAG, "Невозможно выйти т.к. в хранилище не найдет текущий пользователь");
        }
    }

    // открыть активити со списком аккаунтов olx
    public void btnGoToAccountsList(View v){
        Intent intent = new Intent(MainActivity.this, AccountsListActivity.class);
        startActivity(intent);
        //finish();
    }

    public void btnSettings(View v){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        //finish();
    }

    // открыть активити со списком сообщений
    public void btnGoToMessagesList(View v){
        Intent intent = new Intent(MainActivity.this, MessagesSenderListActivity.class);
        startActivity(intent);
        //finish();
    }

    public void btnStartService(View v){
        boolean serviceStatus = false;
        Class<?> serviceClass = EasyAdsService.class;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                serviceStatus = true;
            }
        }
        startService(new Intent(this, EasyAdsService.class));

        Log.d(TAG, "Сервис запущен");
    }

    public void btnStopService(View v){

        boolean serviceStatus = false;
        Class<?> serviceClass = EasyAdsService.class;

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            //Log.d(TAG, service.service.getClassName());
            if (serviceClass.getName().equals(service.service.getClassName())) {
                serviceStatus = true;
                EasyAdsService.shouldContinue = false;
            }
        }

        Log.d(TAG, "SERVICE STATUS " + serviceStatus);

        Log.d(TAG, "Сервис остановлен");
    }

}
