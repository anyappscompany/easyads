package ua.com.anyapps.easyads.easyads.Login;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskCheckExistUser  extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private CheckExistUserCompleted taskCompleted;
    private String result;
    private Context ctxt;

    public AsyncTaskCheckExistUser(CheckExistUserCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.CheckExistUserCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "AsyncTaskCheckExistUser.java - Запрос на проверку существования пользователя приложения: " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "AsyncTaskCheckExistUser.java - Проверка существования пользователя завершилась неудачно. Сервер вернул пустой результат или не удалось получить данные");
        }
        result = responseJson;
        return null;
    }
}
