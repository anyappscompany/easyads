package ua.com.anyapps.easyads.easyads.AccountsList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskGetFreeAccount extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private GetFreeAccountCompleted taskCompleted;
    private String result;
    private Context ctxt;

    public AsyncTaskGetFreeAccount(GetFreeAccountCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.GetFreeAccountCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "AsyncTaskGetFreeAccounts.java - Запрос на на получение свободного аккаунта olx: " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "AsyncTaskGetFreeAccounts.java - При запросе на получение свободного olx аккаунта сервер вернул пустой результат или не удалось получить данные");
        }
        result = responseJson;
        return null;
    }
}
