package ua.com.anyapps.easyads.easyads.CreateNewAd;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskCreateNewAd extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private CreateNewAdCompleted taskCompleted;
    private String result;
    private Context ctxt;
    public AsyncTaskCreateNewAd(CreateNewAdCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.CreateNewAdCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "AsyncTaskCreateNewAd.java - Запрос на создание нового объявления: " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "AsyncTaskCreateNewAd.java - Во время запроса на создание нового объявления сервер вернул пустой результат или не удалось получить данные");
        }
        result = responseJson;
        return null;
    }
}
