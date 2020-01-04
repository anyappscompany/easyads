package ua.com.anyapps.easyads.easyads.AccountsList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskGetUserAccounts extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private GetUserAccountsCompleted taskCompleted;
    private String result;
    private Context ctxt;

    public AsyncTaskGetUserAccounts(GetUserAccountsCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.GetUserAccountsCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        // возвращает текущее время на сервере
        Log.d(TAG, "AsyncTaskGetUserAccounts.java - Запрос на получение времени сервера: " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "AsyncTaskGetUserAccounts.java - Запрос на получение времени сервера вернул пустой результат или не удалось получить данные");
        }
        result = responseJson;
        return null;
    }
}
