package ua.com.anyapps.easyads.easyads.CreateNewAd;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskDeletePhotos extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private DeleteAdCompleted taskCompleted;
    private String result;
    private Context ctxt;

    public AsyncTaskDeletePhotos(DeleteAdCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.DeleteAdCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "AsyncTaskDeletePhotos.java - Запрос на удаление фотографий с сервера (Отмена создания объяв. или удаление объяв.). : " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "AsyncTaskDeletePhotos.java - Во время удаления фотографий сервер вернул пустой результат или не удалось получить данные");
        }
        result = responseJson;
        return null;
    }
}
