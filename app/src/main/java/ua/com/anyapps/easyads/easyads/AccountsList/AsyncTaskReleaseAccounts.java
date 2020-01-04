package ua.com.anyapps.easyads.easyads.AccountsList;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.anyapps.easyads.easyads.DBHelper;
import ua.com.anyapps.easyads.easyads.R;
import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskReleaseAccounts extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private ReleaseAccountsCompleted taskCompleted;
    private String result;
    private Context ctxt;
    private String authToken="";

    SQLiteDatabase db = null;
    DBHelper dbHelper = null;

    WebView wvBrowser;
    public AsyncTaskReleaseAccounts(ReleaseAccountsCompleted context, Context _context) {
        this.taskCompleted = context;
        ctxt = _context;

        dbHelper = new DBHelper(ctxt);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            //Log.d(TAG, "Ошибка при получение бд: " + ex.getMessage());
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //Log.d(TAG, "Удаление выполнено. Метод onPostExecute");
        taskCompleted.ReleaseAccountsCompleted(result);
        super.onPostExecute(aVoid);
    }

    String accountCookies = "";
    String mHtmlCode = "";
    boolean deleteResult = false; // езультат удаления. если истина, то было удаление
    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "Начало освобождения аккаунта");
        /*Log.d(TAG, "AsyncTaskReleaseAccounts.java - Запрос на деактивацию аккаунта olx: " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "AsyncTaskReleaseAccounts.java - При запросе на деактивацию olx аккаунта сервер вернул пустой результат или не удалось получить данные");
        }
        result = responseJson;*/

        authToken = strings[1];

        accountCookies  = dbHelper.getAccountCookies(strings[0]);

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Host", "www.olx.ua");
        headers.put("Connection", "keep-alive");
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Origin", "https://www.olx.ua");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Referer", "https://www.olx.ua/myaccount/");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.put("Cookie", accountCookies);
        HashMap<String, String> params = new HashMap<String, String>();
        mHtmlCode = Utilities.GETQuery("https://www.olx.ua/myaccount/", params, headers, ctxt);



        // Активные
        if(true){
            //Pattern tokenPat = Pattern.compile("data-token=\"(.*?)\" title=\"Деактивировать\" class=\"tdnone marginright5 nowrap remove-link globalAction deactivateme\\n                deactivateme(.*?)                \\{");
            Pattern tokenPat = Pattern.compile("data-token=\"(.*?)\"                   title=\"Деактивировать\"                 class=\"tdnone marginright5 nowrap remove-link globalAction deactivateme                deactivateme(.*?)                \\{");
            Matcher mToken = tokenPat.matcher(mHtmlCode);
            while(mToken.find()) {
                if (mToken.group(1).length() > 0) {


                    //Log.d(TAG, "adid : " + mToken.group(2));
                    String deactivateResult = "";
                    HashMap<String, String> headersAct = new HashMap<String, String>();
                    headersAct.put("Host", "www.olx.ua");
                    headersAct.put("Connection", "keep-alive");
                    headersAct.put("Accept", "application/json, text/javascript, */*; q=0.01");
                    headersAct.put("Origin", "https://www.olx.ua");
                    headersAct.put("X-Requested-With", "XMLHttpRequest");
                    headersAct.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
                    headersAct.put("Content-Type", "application/x-www-form-urlencoded");
                    headersAct.put("Referer", "https://www.olx.ua/myaccount/");
                    headersAct.put("Accept-Encoding", "gzip, deflate, br");
                    headersAct.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                    headersAct.put("Cookie", accountCookies);

                    HashMap<String, String> paramsAct = new HashMap<String, String>();
                    paramsAct.put("adID", mToken.group(2));
                    paramsAct.put("reasonID", "6");
                    paramsAct.put("text", "");
                    paramsAct.put("token", mToken.group(1));

                    deactivateResult = Utilities.POSTQuery("https://www.olx.ua/ajax/myaccount/deactivateme/", paramsAct, headersAct, ctxt);
                    deleteResult = true;
                    if(deactivateResult.length()>0){
                        //
                        try {
                            JSONObject deactObjRes = null;
                            deactObjRes = new JSONObject(deactivateResult);
                            if(deactObjRes.getString("success").equals("0")){
                                Log.d(TAG, "Объявление " +mToken.group(2)+ " деактивировано +");
                            }
                        }catch (Exception ex){
                            Log.e(TAG, "Объявление не деактивировано" + ex.getMessage());
                        }
                    }else{
                        Log.e(TAG, "Объявление " +mToken.group(2)+ " не деактивировано");
                    }

                    Log.d(TAG, "Результат деактивации " + deactivateResult);//{"is_for_sale":1,"id":"458125140","success":"0"}
                }else{
                    Log.d(TAG, "Не найден токен объявления");
                }
            }
        }

        // Ожидающие
        if(true) {
            String pageHtml = "";
            HashMap<String, String> paramsResig = new HashMap<String, String>();
            HashMap<String, String> headersResig = new HashMap<String, String>();
            headersResig.put("Host", "www.olx.ua");
            headersResig.put("Connection", "keep-alive");
            headersResig.put("Upgrade-Insecure-Requests", "1");
            headersResig.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
            headersResig.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            headersResig.put("Referer", "https://www.olx.ua/myaccount/");
            headersResig.put("Accept-Encoding", "gzip, deflate, br");
            headersResig.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
            headersResig.put("Cookie", accountCookies);
            pageHtml = Utilities.GETQuery("https://www.olx.ua/myaccount/waiting/", paramsResig, headersResig, ctxt);



            if (pageHtml.length() > 0) {


                Pattern tokenPat = Pattern.compile("marginright5 globalAction resignme\"                           data-code=\"resignme\" data-token=\"(.*?)\"");
                Matcher mToken = tokenPat.matcher(pageHtml);

                Pattern adidPat = Pattern.compile("data-ad=\"(.*?)\"");
                Matcher mAdId = adidPat.matcher(pageHtml);
                int i = 0;

                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ctxt.openFileOutput("pageoj22.txt", Context.MODE_PRIVATE));
                    outputStreamWriter.write(pageHtml);
                    outputStreamWriter.close();
                }
                catch (Exception ex) {
                    Log.e("Exception", "File write failed: " + ex.toString());
                }

                String adId = "";
                String token = "";



                while (mToken.find() && mAdId.find()) {

                    if (mToken.group(1).length() > 0 && mAdId.group(1).length() > 0) {
                        token = mToken.group(1);
                        adId = mAdId.group(1);
                                        /*if(i%2<=0){
                                            //Log.d(TAG,"waitad-id=: " + mToken.group(2));
                                            adId = mToken.group(2);
                                        }else{
                                            //Log.d(TAG,"waittoken=: " + mToken.group(2));
                                            token = mToken.group(2);
                                        }*/
                        Log.d(TAG, "waitadId: " + adId + " waittoken: " + token);

                        String waitingDeactivateResult = "";
                        HashMap<String, String> headers2 = new HashMap<String, String>();
                        headers2.put("Origin", "https://www.olx.ua");
                        headers2.put("Referer", "https://www.olx.ua/myaccount/waiting/");
                        headers2.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
                        headers2.put("Content-Type", "application/x-www-form-urlencoded");
                        headers2.put("Accept", "application/json, text/javascript, */*; q=0.01");
                        headers2.put("X-Requested-With", "XMLHttpRequest");
                        headers2.put("Accept-Language", "en-US,en;q=0.7,ru;q=0.3");
                        headers2.put("Accept-Encoding", "gzip, deflate, br");
                        headers2.put("Host", "www.olx.ua");
                        headers2.put("Connection", "Keep-Alive");
                        headers2.put("Cache-Control", "no-cache");
                        headers2.put("Cookie", accountCookies);

                        HashMap<String, String> params2 = new HashMap<String, String>();
                        params2.put("adID", adId);
                        params2.put("token", token);

                        waitingDeactivateResult = Utilities.POSTQuery("https://www.olx.ua/ajax/myaccount/resignme/", params2, headers2, ctxt);
                        deleteResult = true;
                        if (waitingDeactivateResult.length() > 0) {
                            Log.d(TAG, "ddddd" + waitingDeactivateResult);

                            //
                            try {
                                JSONObject deactObjRes = null;
                                deactObjRes = new JSONObject(waitingDeactivateResult);
                                if (deactObjRes.getString("success").equals("0")) {
                                    Log.d(TAG, "Объявление " + adId + " удалено из ожидающих +");
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Объявление не удалено из ожидающих " + ex.getMessage());
                            }
                        } else {
                            Log.e(TAG, "Объявление " + adId + " не удалено");
                        }

                        Log.d(TAG, "Результат удаления объявления из ожидающих " + waitingDeactivateResult);//{"is_for_sale":1,"id":"458125140","success":"0"}
                    }
                    i++;
                }
            }
        }

            // Неактивные
            if(true){

                String pageHtml = "";
                HashMap<String, String> paramsArch = new HashMap<String, String>();
                HashMap<String, String> headersArch = new HashMap<String, String>();
                headersArch.put("Host", "www.olx.ua");
                headersArch.put("Connection", "keep-alive");
                headersArch.put("Upgrade-Insecure-Requests", "1");
                headersArch.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
                headersArch.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                headersArch.put("Referer", "https://www.olx.ua/myaccount/");
                headersArch.put("Accept-Encoding", "gzip, deflate, br");
                headersArch.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                headersArch.put("Cookie", accountCookies);
                pageHtml = Utilities.GETQuery("https://www.olx.ua/myaccount/archive/", paramsArch, headersArch, ctxt);

                            /*try {
                                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("pagearchive.txt", Context.MODE_PRIVATE));
                                outputStreamWriter.write(pageHtml);
                                outputStreamWriter.close();
                            }
                            catch (Exception ex) {
                                Log.e("Exception", "File write failed: " + ex.toString());
                            }*/

                if(pageHtml.length()>0){
                    Pattern tokenPat = Pattern.compile("marginright5 nowrap remove-link globalAction removeme\" data-code=\"removeme\"                   data-token=\"(.*?)\"");
                    Matcher mToken = tokenPat.matcher(pageHtml);

                    Pattern adidPat = Pattern.compile("data-ad=\"(.*?)\"");
                    Matcher mAdId = adidPat.matcher(pageHtml);
                    int i=0;

                    String adId = "";
                    String token = "";
                    while(mToken.find() && mAdId.find()) {
                        if (mToken.group(1).length() > 0 && mAdId.group(1).length()>0) {
                            token = mToken.group(1);
                            adId = mAdId.group(1);
                                        /*if(i%2<=0){
                                            //Log.d(TAG,"waitad-id=: " + mToken.group(2));
                                            adId = mToken.group(2);
                                        }else{
                                            //Log.d(TAG,"waittoken=: " + mToken.group(2));
                                            token = mToken.group(2);
                                        }*/
                            Log.d(TAG, "waitadId: " + adId + " waittoken: " + token);


                            String archiveDeleteResult = "";
                            HashMap<String, String> headers2 = new HashMap<String, String>();
                            headers2.put("Host", "www.olx.ua");
                            headers2.put("Connection", "keep-alive");
                            headers2.put("Accept", "application/json, text/javascript, */*; q=0.01");
                            headers2.put("Origin", "https://www.olx.ua");
                            headers2.put("X-Requested-With", "XMLHttpRequest");
                            headers2.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
                            headers2.put("Content-Type", "application/x-www-form-urlencoded");
                            headers2.put("Referer", "https://www.olx.ua/myaccount/archive/");
                            headers2.put("Accept-Encoding", "gzip, deflate, br");
                            headers2.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                            headers2.put("Cookie", accountCookies);

                            HashMap<String, String> params2 = new HashMap<String, String>();
                            params2.put("adID", adId);
                            params2.put("token", token);

                            archiveDeleteResult = Utilities.POSTQuery("https://www.olx.ua/ajax/myaccount/removeme/", params2, headers2, ctxt);
                            deleteResult = true;
                            if(archiveDeleteResult.length()>0){
                                //
                                try {
                                    JSONObject deactObjRes = null;
                                    deactObjRes = new JSONObject(archiveDeleteResult);
                                    if(deactObjRes.getString("success").equals("0")){
                                        Log.d(TAG, "Объявление " +adId+ " удалено из неактивных +");
                                    }
                                }catch (Exception ex){
                                    Log.e(TAG, "Объявление не удалено из неактивных " + ex.getMessage());
                                }
                            }else{
                                Log.e(TAG, "Объявление " +adId+ " не удалено");
                            }

                            Log.d(TAG, "Результат удаления объявления из неактивных " + archiveDeleteResult);//{"is_for_sale":1,"id":"458125140","success":"0"}
                        }
                        i++;
                    }
                }
            }

            // Удаленные
            if(true){

                String pageHtml = "";
                HashMap<String, String> paramsDeleted = new HashMap<String, String>();
                HashMap<String, String> headersDeleted = new HashMap<String, String>();
                headersDeleted.put("Host", "www.olx.ua");
                headersDeleted.put("Connection", "keep-alive");
                headersDeleted.put("Upgrade-Insecure-Requests", "1");
                headersDeleted.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
                headersDeleted.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                headersDeleted.put("Referer", "https://www.olx.ua/myaccount/");
                headersDeleted.put("Accept-Encoding", "gzip, deflate, br");
                headersDeleted.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                headersDeleted.put("Cookie", accountCookies);
                pageHtml = Utilities.GETQuery("https://www.olx.ua/myaccount/moderated/", paramsDeleted, headersDeleted, ctxt);

                            /*try {
                                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("pagemoderated.txt", Context.MODE_PRIVATE));
                                outputStreamWriter.write(pageHtml);
                                outputStreamWriter.close();
                            }
                            catch (Exception ex) {
                                Log.e("Exception", "File write failed: " + ex.toString());
                            }*/

                if(pageHtml.length()>0){
                    Pattern tokenPat = Pattern.compile("marginright5 nowrap remove-link globalAction removeme\" data-code=\"removeme\"                   data-token=\"(.*?)\"");
                    Matcher mToken = tokenPat.matcher(pageHtml);

                    Pattern adidPat = Pattern.compile("data-ad-id=\"(.*?)\"");
                    Matcher mAdId = adidPat.matcher(pageHtml);
                    int i=0;

                    String adId = "";
                    String token = "";
                    while(mToken.find() && mAdId.find()) {
                        if (mToken.group(1).length() > 0 && mAdId.group(1).length()>0) {
                            token = mToken.group(1);
                            adId = mAdId.group(1);
                                        /*if(i%2<=0){
                                            //Log.d(TAG,"waitad-id=: " + mToken.group(2));
                                            adId = mToken.group(2);
                                        }else{
                                            //Log.d(TAG,"waittoken=: " + mToken.group(2));
                                            token = mToken.group(2);
                                        }*/
                            Log.d(TAG, "waitadId: " + adId + " waittoken: " + token);

                            String moderatedResult = "";
                            HashMap<String, String> headers2 = new HashMap<String, String>();
                            headers2.put("Host", "www.olx.ua");
                            headers2.put("Connection", "keep-alive");
                            headers2.put("Accept", "application/json, text/javascript, */*; q=0.01");
                            headers2.put("Origin", "https://www.olx.ua");
                            headers2.put("X-Requested-With", "XMLHttpRequest");
                            headers2.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
                            headers2.put("Content-Type", "application/x-www-form-urlencoded");
                            headers2.put("Referer", "https://www.olx.ua/myaccount/archive/");
                            headers2.put("Accept-Encoding", "gzip, deflate, br");
                            headers2.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                            headers2.put("Cookie", accountCookies);

                            HashMap<String, String> params2 = new HashMap<String, String>();
                            params2.put("adID", adId);
                            params2.put("token", token);

                            moderatedResult = Utilities.POSTQuery("https://www.olx.ua/ajax/myaccount/removeme/", params2, headers2, ctxt);
                            deleteResult = true;
                            if(moderatedResult.length()>0){
                                //
                                try {
                                    JSONObject deactObjRes = null;
                                    deactObjRes = new JSONObject(moderatedResult);
                                    if(deactObjRes.getString("success").equals("0")){
                                        Log.d(TAG, "Объявление " +adId+ " удалено из удаленных +");
                                    }
                                }catch (Exception ex){
                                    Log.e(TAG, "Объявление не удалено из удаленных " + ex.getMessage());
                                }
                            }else{
                                Log.e(TAG, "Объявление " +adId+ " не удалено");
                            }

                            Log.d(TAG, "Результат удаления объявления из удаленных " + moderatedResult);//{"is_for_sale":1,"id":"458125140","success":"0"}
                        }
                        i++;
                    }
                }
            }

            //if(deleteResult){ // если из олх удалилось, то деактивировать аккаунт на сервере и в приложении
                String serverAccountId = dbHelper.getServerAccountId(strings[0]);
                Log.d(TAG, "Id аккаунта на сервере " + serverAccountId);

                //String targetUrl = ctxt.getResources().getString(R.string.host) + "/?act=releaseaccount&authtoken=" + authToken + "&account="+serverAccountId;
                HashMap<String, String> paramsRel = new HashMap<String, String>();
                HashMap<String, String> headersRel = new HashMap<String, String>();
                paramsRel.put("act", "releaseaccount");
                paramsRel.put("authtoken", authToken);
                paramsRel.put("account", serverAccountId);
                //String res = Utilities.GETQuery(ctxt.getResources().getString(R.string.host) + "/", paramsRel, headersRel, ctxt);
                String targetUrl = ctxt.getResources().getString(R.string.host) + "/?act=releaseaccount&authtoken=" + authToken + "&account="+serverAccountId;
                String res = Utilities.getHtml(targetUrl, ctxt);
                Log.d(TAG, "Результат удаления на сервере " + res);
                JSONObject dataJsonObj = null;
                try {
                    dataJsonObj = new JSONObject(res);
                    String error = dataJsonObj.getString("error");
                    if (error.equals("0")) {
                        dbHelper.deactivateAccount(strings[0]);
                        result = strings[0];
                    }else{
                        result = "0";
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Освобождение аккаунта ошибка "+ e.getMessage());
                }
            //}

        return null;
    }
}
