package ua.com.anyapps.easyads.easyads.AdsList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskGetAds extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private GetAdsCompleted taskCompleted;
    private String result;
    private Context ctxt;

    public AsyncTaskGetAds(GetAdsCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.GetAdsCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "AsyncTaskGetAds.java - Запрос на получение списка обявлений для текущего пользователя: " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "AsyncTaskGetAds.java - При получении списка объявлений для текущего пользователя сервер вернул пустой результат или не удалось получить данные");
        }
        result = responseJson;
        return null;
    }
}
