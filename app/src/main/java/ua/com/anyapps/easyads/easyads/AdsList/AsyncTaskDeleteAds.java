package ua.com.anyapps.easyads.easyads.AdsList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskDeleteAds extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private DeleteAdsCompleted taskCompleted;
    private String result;
    private Context ctxt;

    public AsyncTaskDeleteAds(DeleteAdsCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.DeleteAdsCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "AsyncTaskDeleteAds.java - Запрос на удаление объявлений: " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "AsyncTaskDeleteAds.java - При запросе на удаление объявления сервер вернул пустой результат или не удалось получить данные");
        }
        result = responseJson;
        return null;
    }
}
