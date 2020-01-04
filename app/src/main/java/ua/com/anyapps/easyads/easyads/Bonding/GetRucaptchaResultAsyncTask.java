package ua.com.anyapps.easyads.easyads.Bonding;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import ua.com.anyapps.easyads.easyads.Utilities;

public class GetRucaptchaResultAsyncTask extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private GetRucaptchaResultCompleted taskCompleted;
    private String result;
    private Context ctxt;

    public GetRucaptchaResultAsyncTask(GetRucaptchaResultCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.GetRucaptchaResultCompleted(result);
        super.onPostExecute(aVoid);
    }

    String responseJson = "";
    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "CreateRecaptchaTaskAsyncTask.java - Получение результатов с рукаптчи: " + strings[0]);


        for(int i=0; i<100; i++) {
            responseJson = Utilities.getHtml(strings[0], ctxt);
            Log.d(TAG, "Response: " + responseJson);
            if(responseJson.trim().matches("^OK\\|[A-Za-z0-9_-]+$")) break;
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(responseJson.length()<=0){
            Log.e(TAG, "CreateRecaptchaTaskAsyncTask.java - Во время запроса на получение результатов");
        }
        result = responseJson;
        return null;
    }
}