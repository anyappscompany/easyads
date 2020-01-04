package ua.com.anyapps.easyads.easyads.EditAd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

import ua.com.anyapps.easyads.easyads.DBHelper;
import ua.com.anyapps.easyads.easyads.R;
import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncDeleteAdPhoto extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private DeleteAdPhotoCompleted taskCompleted;
    private String result;
    private Context ctxt;

    String accountId = "";
    String cookies = "";
    SQLiteDatabase db = null;
    DBHelper dbHelper = null;
    String referer = "";

    public AsyncDeleteAdPhoto(DeleteAdPhotoCompleted _context, Context _mContext) {
        this.taskCompleted = _context;
        this.ctxt = _mContext;

        dbHelper = new DBHelper(_mContext);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            //Log.d(TAG, "Ошибка при получение бд: " + ex.getMessage());
        }


    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.DeleteAdPhotoCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "AsyncDeleteAdPhoto.java - Запрос на удаление фото из объявления: " + strings[0]);
        //String responseJson = Utilities.getHtml("" + strings[0]);
        String responseJson = "";
        //String urlParameters  = "";
        try {
            JSONObject dataJsonObj = null;
            dataJsonObj = new JSONObject(strings[0]);//dataJsonObj.getString("riak_key")
            //urlParameters = "riak_key="+dataJsonObj.getString("riak_key")+"&ad_id=&slot="+dataJsonObj.getString("pos")+"&ad_photo_id="+dataJsonObj.getString("apollo_id");

            //byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
            accountId = dbHelper.getAccountIdByAdId(dataJsonObj.getString("idob"));
            cookies = dbHelper.getAccountCookies(accountId);
            referer = dbHelper.getReferer(dataJsonObj.getString("idob"), dataJsonObj.getString("originalOlxAdId"));

        // 5 попыток на получение данных
        for(int i=0; i<5;i++) {
            Log.d(TAG, "AsyncDeleteAdPhoto.java - Запрос страницы: https://www.olx.ua/ajax/upload/remove/?preview= попытка " + (i+1));

            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Host", "www.olx.ua");
            headers.put("Connection", "keep-alive");
            headers.put("Accept", "*/*");
            headers.put("Origin", "https://www.olx.ua");
            headers.put("X-Requested-With", "XMLHttpRequest");
            headers.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            headers.put("Referer", referer);
            headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
            headers.put("Cookie", cookies);

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("riak_key", dataJsonObj.getString("riak_key"));
            params.put("ad_id", dataJsonObj.getString("originalOlxAdId"));
            params.put("slot", dataJsonObj.getString("slot"));
            params.put("ad_photo_id", dataJsonObj.getString("apollo_id"));

            //Log.d(TAG, "riakkey - " + riakkey + ", " + "ad_id - " + originalOlxAdId + ", " + "slot - " + photoOB.optString("slot", "") + ", " + " ad_photo_id - " + photoOB.optString("adPhotoId", ""));

            responseJson = Utilities.POSTQuery("https://www.olx.ua/ajax/upload/remove/?preview=", params, headers, ctxt);

            JSONObject dataJsonObj2 = null;
            dataJsonObj2 = new JSONObject(responseJson);
            if (dataJsonObj2.getString("status").equals("ok")) {
                break;
            }

            }
            } catch (Exception e) {
                Log.e(TAG, "AsyncDeleteAdPhoto.java - Исключение во время обработки страницы https://www.olx.ua/ajax/upload/remove/?preview=: " + e.getMessage());
            }

        result = responseJson;
        return null;
    }
}