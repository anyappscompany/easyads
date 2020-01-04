package ua.com.anyapps.easyads.easyads.Bonding;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ua.com.anyapps.easyads.easyads.Utilities;

public class CreateRecaptchaTaskAsyncTask extends AsyncTask<String, Void, Void> {
    private static final String TAG = "111232312312";

    private CreateRecaptchaTaskCompleted taskCompleted;
    private String result;
    private Context ctxt;

    public CreateRecaptchaTaskAsyncTask(CreateRecaptchaTaskCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.CreateRecaptchaTaskCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "CreateRecaptchaTaskAsyncTask.java - Запрос на разгадывание каптчи: " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "CreateRecaptchaTaskAsyncTask.java - Во время запроса на разгадывание каптчи ошибка");
        }
        result = responseJson;
        return null;
    }
}