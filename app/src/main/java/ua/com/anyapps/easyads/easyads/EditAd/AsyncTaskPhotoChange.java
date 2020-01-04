package ua.com.anyapps.easyads.easyads.EditAd;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskPhotoChange extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private PhotoChangeCompleted taskCompleted;
    private String result;
    private Context ctxt;

    public AsyncTaskPhotoChange(PhotoChangeCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.PhotoChangeCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "AsyncTaskUpdateAd.java - Запрос на обновление фото объявления: " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "AsyncTaskUpdateAd.java - Обновление фото объявления завершилось неудачей. Сервер вернул пустой результат или не удалось получить данные");
        }
        result = responseJson;
        return null;
    }
}