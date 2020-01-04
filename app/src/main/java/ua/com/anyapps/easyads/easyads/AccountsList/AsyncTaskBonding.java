package ua.com.anyapps.easyads.easyads.AccountsList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MultipartBody;
import okhttp3.internal.Util;
import ua.com.anyapps.easyads.easyads.DBHelper;
import ua.com.anyapps.easyads.easyads.OlxAccount;
import ua.com.anyapps.easyads.easyads.R;
import ua.com.anyapps.easyads.easyads.Utilities;

import static android.text.TextUtils.isDigitsOnly;

public class AsyncTaskBonding extends AsyncTask<String, Void, Void>{
    private static final String TAG = "debapp";

    private BondingCompleted taskCompleted;
    private String result;
    private Context context;

    private SharedPreferences spPreferences;
    private String ownerId;

    SQLiteDatabase db = null;
    DBHelper dbHelper = null;

    String mHtmlCode;
    String g_recaptcha_data_sitekey = "";
    Handler mHandler = new Handler();
    boolean entered = false; // уже входил
    boolean login = false; // вошел?

    MyJavaScriptInterface MJInterface;
    private String adjson = "";
    private String targetAccountID = "";
    private String adid;
    private String adtitle;
    String riak_key = "";

    public AsyncTaskBonding(BondingCompleted _context, Context mContext, String _adjson, String _targetAccountID, String _adid, String _adtitle) {
        this.taskCompleted = _context;
        this.context = mContext;
        this.adjson = _adjson;
        this.targetAccountID = _targetAccountID;
        this.adid = _adid;
        this.adtitle = _adtitle;

        dbHelper = new DBHelper(context);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            //Log.d(TAG, "Ошибка при получение бд: " + ex.getMessage());
        }

        spPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        ownerId = spPreferences.getString(context.getString(R.string.current_user), null);

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.BondingCompleted(result);
        super.onPostExecute(aVoid);
    }

    WebView wvBrowser;
    JSONArray accountAdInfo;
    String currentAccount = "";
    String currentPassword = "";
    ArrayList<String> currentSuggestedCategories = new ArrayList<>();
    JSONArray catsArray = new JSONArray();
    JSONArray currentAd;

    String []startParams;
    int enterAttempt = 0;
    @Override
    protected Void doInBackground(String... strings) {
        startParams = strings;

Log.d(TAG, "Попытка войти " + enterAttempt);
        //Log.d(TAG, this.context.getResources().getString(R.string.host));
        // запросить данные для аккауунтов
        //Log.d(TAG, strings[1]);

        String accountsInfoJson = "";


        MJInterface  = new MyJavaScriptInterface();
        wvBrowser = (WebView) ((Activity)context).findViewById(R.id.wvBrowser);
        wvBrowser.post(new Runnable() {
            @Override
            public void run() {
                // настройка браузера для авторизации
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Log.d(TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                } else
                {
                    Log.d(TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
                    CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
                    cookieSyncMngr.startSync();
                    CookieManager cookieManager=CookieManager.getInstance();
                    cookieManager.removeAllCookie();
                    cookieManager.removeSessionCookie();
                    cookieSyncMngr.stopSync();
                    cookieSyncMngr.sync();
                }

                wvBrowser.getSettings().setJavaScriptEnabled(true);
                wvBrowser.getSettings().setUserAgentString(context.getResources().getString(R.string.default_user_agent));

                wvBrowser.setWebViewClient(new WebViewClient() {

                    public void onPageFinished(WebView view, String url) {
                        String cookies = CookieManager.getInstance().getCookie(url);
                        //Log.d(TAG, url+ " All the cookies in a string:" + cookies);
                        wvBrowser.loadUrl("javascript:window.HTMLOUT.showHTML('"+url+"', '<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                    }
                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        Log.d(TAG, "ERROR");
                        super.onReceivedError(view, request, error);
                    }
                });

                wvBrowser.setWebChromeClient(new WebChromeClient(){
                    public void onProgressChanged(WebView view, int newProgress){
                        Log.d(TAG, "Page loading : " + newProgress + "%");
                        if(newProgress == 100){
                            // Page loading finish
                            Log.d(TAG,"Page Loaded.");
                        }
                    }
                });
                wvBrowser.addJavascriptInterface(MJInterface, "HTMLOUT");
                //wvBrowser.loadUrl("https://www.olx.ua/myaccount/");
            }
        });





        JSONObject dataJsonObj = null;

            String jsonCategories = "";
            try {
                jsonCategories = Utilities.getStringFromFile("/data/data/" + context.getPackageName() + "/files/" + "categories.json");


                JSONObject resobj = new JSONObject(jsonCategories);
                Iterator<?> keys = resobj.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (resobj.get(key) instanceof JSONObject) {
                        JSONObject xx = new JSONObject(resobj.get(key).toString());

                        if (xx.optString("id", "").length() <= 0) continue;
                        if(xx.getString("id").equals("1701") && xx.getString("name").equals("Автомобили из Польши")) continue;
                        catsArray.put(xx);
                    }
                }

                OlxAccount oneOlxAc = dbHelper.getAccount(targetAccountID);
                currentAccount = oneOlxAc.email;
                currentPassword = oneOlxAc.password;

                int sugtotal = (1 + (int) (Math.random() * 4));
                for(int s=0; s<sugtotal; s++){
                    JSONObject kk = catsArray.getJSONObject((0 + (int) (Math.random() * catsArray.length())));
                    currentSuggestedCategories.add(kk.getString("id"));
                }

                    /*currentAccount = obj.getString("account");
                    currentPassword = obj.getString("password");
                    currentSuggestedCategories = obj.getJSONArray("suggestedcategories");
                    Log.d(TAG, "Acc: " + obj.getString("account"));
                    Log.d(TAG, "Pass: " + obj.getString("password"));
                    JSONArray ad = currentAd = obj.getJSONArray("ad");
                    for(int k = 0; k<ad.length(); k++){ // post параметры
                        JSONObject ob;
                        ob = new JSONObject(ad.get(k)+"");
                        Log.d(TAG, ">>>>>key: " + ob.getString("key") + " value: " + ob.getString("value"));
                    }*/

                JSONArray ad = currentAd = new JSONArray(adjson);
                for(int k = 0; k<ad.length(); k++){ // post параметры
                    JSONObject ob;
                    ob = new JSONObject(ad.get(k)+"");
                    Log.d(TAG, ">>>>>key: " + ob.getString("key") + " value: " + ob.getString("value"));
                }

                //if(true) return null;

                    wvBrowser.post(new Runnable() {
                        @Override
                        public void run() {
//если возраст кук в базе подходящий, то установить их
                            wvBrowser.loadUrl("https://www.olx.ua/myaccount/");
                        }
                    });

int count = 0;
while(true){
    if(login){

        login = false;
        entered = false;

        break;
    }
    Log.d(TAG, " count = " + count + " , login = " + login);
        TimeUnit.SECONDS.sleep(1);
        count++;
}




                    //CookieManager.getInstance().removeAllCookie();


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Исключение во время парсинга списка категорий: " + e.getMessage());
        }







        //Log.d(TAG, "AsyncTaskBonding.java - Запрос на объединение аккаунтов и объявлений: " + strings[0]);

        //responseJson = Utilities.getHtml(strings[0]);

        return null;
    }

    String rucaptchaTaskId = "";
    public void CreateRecaptchaTaskCompleted(String url) {

        String response = Utilities.getHtml(url, context);
        Log.d(TAG, response);
        if(response.matches("^OK\\|[0-9]+$")){
            //Log.d(TAG, "ALL OK - " + response.substring(3, response.length()));
            rucaptchaTaskId = response.substring(3, response.length());
            Log.d(TAG, "rucaptchaTaskId: " + rucaptchaTaskId);
            // проверка выполнения задания

            String targetUrl = "http://rucaptcha.com/res.php?key=28bcc58b56b459a06f8d3c19ad2221b0&action=get&id=" + rucaptchaTaskId;
            //GetRucaptchaResultAsyncTask task = new GetRucaptchaResultAsyncTask(MainActivity.this);
            //task.execute(targetUrl);
            GetRucaptchaResultCompleted(targetUrl);
        }else{
            Log.d(TAG, "Не удалось создать задачу на рекапче");
        }

    }

    String rucaptcha_response = "";
    String postData = "";

    public void GetRucaptchaResultCompleted(String url) {
        String response = "";
        String responseJson = "";

        for(int i=0; i<100; i++) {
            responseJson = Utilities.getHtml(url, context);
            Log.d(TAG, "Response: " + responseJson);
            if(responseJson.trim().matches("^OK\\|[A-Za-z0-9_-]+$")) break;
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(responseJson.length()<=0){
            Log.e(TAG, "AsynctaskBonding.java - Во время запроса на получение результатов с рукаптчи");
        }
        response = responseJson;


        rucaptcha_response = response.substring(3, response.length());
        Log.d(TAG, "g_recaptcha_response: " + rucaptcha_response);
        // ВХОД
        entered = true;
        postData = "login[remember-me]=on&login[password]=" + currentPassword + "&login[email_phone]=" + currentAccount + "&g-recaptcha-response="+rucaptcha_response;

        wvBrowser.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Вход в аккаунт с POST параметрами: " + postData);
                wvBrowser.postUrl("https://www.olx.ua/account/?ref[0][action]=myaccount&ref[0][method]=index", postData.getBytes());
            }
        });


        //String postData = "username=" + URLEncoder.encode(my_username, "UTF-8") + "&password=" + URLEncoder.encode(my_password, "UTF-8");
        //webview.postUrl(url,postData.getBytes());
    }

    /*String resp = "";
    @Override
    public void LoginCompleted(String response) {
        Log.d(TAG, "LOGIN COMPLETED: " + response);
        resp = response;
        //WebView simpleWebView=(WebView) findViewById(R.id.wvBrowser);
        wvBrowser.post(new Runnable() {
            @Override
            public void run() {
                wvBrowser.loadData(resp, "text/html; charset=UTF-8", null);
            }
        });
        //simpleWebView.loadData(response, "text/html; charset=UTF-8", null);
    }

    @Override
    public void UploadPhotoCompleted(String response) {
        Log.d(TAG, "Upload Completed " + response);
    }*/
    class upPhoto{
        public String photo;
        public String slot;
        public String adPhotoId; // apollo
        public String oldPhoto = "";
        public String currentPhoto = "";
    }
    class urlParameter{
        public String key;
        public Object value;
        urlParameter(){
            //
        }
        urlParameter(String key, Object value){
            this.key = key;
            this.value = value;
        }
    }

    List<upPhoto> photosToUpload = new ArrayList<upPhoto>();


    class MyJavaScriptInterface {
        String htmlString = "";
        String adding_key = "";
        List<String> postParams = new ArrayList<String>();
        String originalOlxAdId = "";
        ArrayList<urlParameter> urlParametres = new ArrayList<>();
        String adjson2 = "";
        ArrayList<upPhoto> photosWithNewparams = new ArrayList<>();
        @JavascriptInterface
        public void showHTML(String url, String html) {
            htmlString = mHtmlCode = html;
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("page.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(mHtmlCode);
                outputStreamWriter.close();
            }
            catch (Exception ex) {
                Log.e(TAG, "File write failed: " + ex.toString());
            }
            Log.d(TAG, "Title: " + mHtmlCode.indexOf("<title><"));
            Log.d(TAG, "Multuplay: " + mHtmlCode.indexOf("olx-multipay"));
            Log.d(TAG, "Entered: " + entered);



            String cookies = CookieManager.getInstance().getCookie(url);
            Log.d(TAG, url+ " All the cookies in a string:" + cookies);

            // загрузилась страница успешного создания объявления
            if((mHtmlCode.indexOf("Рекламируйте ваше объявление")>=0 && entered) || (entered && mHtmlCode.indexOf("Варианты размещений")>=0)){
                Log.d(TAG, "Объявление создано 100%");
                //
                login = true;


                Pattern p = Pattern.compile("<input type=\"hidden\" id=\"adId\" value=\"(.*?)\"");
                Matcher m = p.matcher(mHtmlCode);
                while(m.find()) {
                    if(m.group(1).length()>0) {
                        originalOlxAdId = m.group(1);
                    }
                }
                Log.d(TAG, "originalOlxAdId " + originalOlxAdId);
                try {
                    JSONArray jsonArr = new JSONArray(adjson);
                    for (int k = 0; k < jsonArr.length(); k++) {
                        JSONObject jsonObj = jsonArr.getJSONObject(k);
                        String key = (String)jsonObj.get("key");
                        switch(key) {
                            case "photos":
                                JSONArray photosArray = jsonObj.getJSONArray("value");
                                for (int s=0; s < photosArray.length(); s++) {
                                    JSONObject adPhotoClassObj = new JSONObject(photosArray.getString(s));
                                    /*upPhoto photo1 = new upPhoto();
                                    photo1.currentPhoto = adPhotoClassObj.getString("currentPhoto");
                                    //photo1.oldPhoto = adPhotoClassObj.getString("oldPhoto");
                                    photo1.oldPhoto = Uri.parse("android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo).toString();
                                    photo1.adPhotoId = photosToUpload.get(s).adPhotoId.toString();
                                    photo1.slot = photosToUpload.get(s).slot.toString();
                                    //photo1.photo = Uri.parse(photosToUpload.get(s).photo.toString());
                                    Log.d(TAG, "BOND PHOTO " + adPhotoClassObj.getString("currentPhoto") + " SLOT " + photosToUpload.get(s).slot.toString());
                                    photosWithNewparams.add(photo1);*/


                                }
                                urlParametres.add(new urlParameter((String)jsonObj.get("key"), gson.toJson(photosWithNewparams)));
                                break;
                            default:
                                urlParametres.add(new urlParameter((String)jsonObj.get("key"), (String)jsonObj.get("value")));
                                break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                adjson2 = gson.toJson(urlParametres);

                boolean res = dbHelper.insertNewAd(adid, adtitle, adjson2, ownerId, targetAccountID, originalOlxAdId, riak_key);
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("1adjson.txt", Context.MODE_PRIVATE));
                    outputStreamWriter.write(adjson2);
                    outputStreamWriter.close();
                }
                catch (Exception ex) {
                    Log.e("Exception", "File write failed: " + ex.toString());
                }
                for(int n = 0; n<photosToUpload.size(); n++){
                    Log.d(TAG, "_photo " + Uri.parse(photosToUpload.get(n).photo).toString());
                    Log.d(TAG, "_slot " + photosToUpload.get(n).slot.toString());
                    Log.d(TAG, "_photoid " + photosToUpload.get(n).adPhotoId.toString());
                }

                //распарсить adjson и добавить в него инфу


                if(res){
                    result = "{\"result\":\"1\", \"accountid\":\""+targetAccountID+"\"}";
                }else{
                    result = "{\"result\":\"0\", \"accountid\":\""+targetAccountID+"\"}";
                }
                //добавить объявление в базу и прописать для аккаунта
            }

            Pattern pTitle = Pattern.compile("<title>(.*?)</title>");
            Matcher mTitle = pTitle.matcher(mHtmlCode);
            while(mTitle.find()) {
                if(mTitle.group(1).length()>0) {

                    Log.d(TAG, "URL>>" + url);
                    Log.d(TAG, "Length: " + mHtmlCode.length());
                    Log.d(TAG, "Page title: " + mTitle.group(1));
                    Log.d(TAG, "Entered: " + entered);
                    // страница ввода логина пароля
                    if(mTitle.group(1).equals("Сервис объявлений OLX: сайт частных объявлений в Украине - купля/продажа б/у товаров на OLX.ua") && url.equals("https://www.olx.ua/account/?ref[0][action]=myaccount&ref[0][method]=index") && !entered){

                        Pattern p = Pattern.compile("data-sitekey=\"(.*?)\"><");
                        Matcher m = p.matcher(mHtmlCode);
                        while(m.find()) {
                            if(m.group(1).length()>0) {
                                g_recaptcha_data_sitekey = m.group(1);
                                Log.d(TAG, "g_recaptcha_data_sitekey: " + g_recaptcha_data_sitekey);
                                // запрос к рекапче
                                String targetUrl = "http://rucaptcha.com/in.php?key=28bcc58b56b459a06f8d3c19ad2221b0&method=userrecaptcha&googlekey="+g_recaptcha_data_sitekey+"&pageurl=https://www.olx.ua/myaccount/";
                                //CreateRecaptchaTaskAsyncTask task = new CreateRecaptchaTaskAsyncTask(MainActivity.this);
                                //task.execute(targetUrl);
                                CreateRecaptchaTaskCompleted(targetUrl);
                                break;
                            }
                        }
// вход выполнен, главная страница профиля
                    }else if(mTitle.group(1).equals("Мой профиль • OLX.ua") && url.equals("https://www.olx.ua/myaccount/") && entered){
                        Log.d(TAG, "Вход в профиль выполнен");

                        Log.d(TAG, "КУКИ " + cookies);
                        // запись куков в базу
                        dbHelper.saveCookies(cookies, currentAccount, targetAccountID);//vfffffffff



                        /*try {
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("page.txt", Context.MODE_PRIVATE));
                            outputStreamWriter.write(mHtmlCode);
                            outputStreamWriter.close();
                        }
                        catch (Exception ex) {
                            Log.e("Exception", "File write failed: " + ex.toString());
                        }*/
//переписать все без браузера. браузер только для авторизации
                        // порядок важен
                        boolean previous = false;
                        //Matcher mAct = Pattern.compile("<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"fbold\">\t\t\t\t\t\t\t\t\tАктивные<\\/span> <span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t \\([0-9]+").matcher(mHtmlCode);
                        //if (mAct.find( )) {
                        if(mHtmlCode.indexOf("<li id=\"typeactive\" class=\"fleft rel\">")>=0 || mHtmlCode.indexOf("<li id=\"typeactive\" class=\"fleft rel selected\">")>=0){

                            Log.d(TAG, "Найден: Активные");
                            Pattern tokenPat = Pattern.compile("data-token=\"(.*?)\" title=\"Деактивировать\" class=\"tdnone marginright5 nowrap remove-link globalAction deactivateme\\n                deactivateme(.*?)                \\{");
                            Matcher mToken = tokenPat.matcher(mHtmlCode);
                            while(mToken.find()) {
                                if (mToken.group(1).length() > 0) {
                                    previous=true;
                                    Log.d(TAG, "token : " + mToken.group(1));
                                    Log.d(TAG, "ad-id : " + mToken.group(2));
                                    //Log.d(TAG, "adid : " + mToken.group(2));
                                    String deactivateResult = "";
                                    HashMap<String, String> headers = new HashMap<String, String>();
                                    headers.put("Host", "www.olx.ua");
                                    headers.put("Connection", "keep-alive");
                                    headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
                                    headers.put("Origin", "https://www.olx.ua");
                                    headers.put("X-Requested-With", "XMLHttpRequest");
                                    headers.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                                    headers.put("Content-Type", "application/x-www-form-urlencoded");
                                    headers.put("Referer", "https://www.olx.ua/myaccount/");
                                    headers.put("Accept-Encoding", "gzip, deflate, br");
                                    headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                                    headers.put("Cookie", cookies);

                                    HashMap<String, String> params = new HashMap<String, String>();
                                    params.put("adID", mToken.group(2));
                                    params.put("reasonID", "6");
                                    params.put("text", "");
                                    params.put("token", mToken.group(1));

                                    deactivateResult = Utilities.POSTQuery("https://www.olx.ua/ajax/myaccount/deactivateme/", params, headers, context);
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

                        //Matcher mOj = Pattern.compile("<a class=\"fbold\" href=\"https:\\/\\/www.olx.ua\\/myaccount\\/waiting\\/\">Ожидающие\t\t\t\t\t\t\t<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\\([0-9]+").matcher(mHtmlCode);
                        //if (mOj.find( )|| previous) {
                        if(mHtmlCode.indexOf("<li id=\"typewaiting\" class=\"fleft rel\">")>=0 || mHtmlCode.indexOf("<li id=\"typewaiting\" class=\"fleft rel selected\">")>=0 || previous){
                            Log.d(TAG, "Найден: Ожидающие");


                            String pageHtml = "";
                            HashMap<String, String> params = new HashMap<String, String>();
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Host", "www.olx.ua");
                            headers.put("Connection", "keep-alive");
                            headers.put("Upgrade-Insecure-Requests", "1");
                            headers.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                            headers.put("Referer", "https://www.olx.ua/myaccount/");
                            headers.put("Accept-Encoding", "gzip, deflate, br");
                            headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                            headers.put("Cookie", cookies);
                            pageHtml = Utilities.GETQuery("https://www.olx.ua/myaccount/waiting/", params, headers, context);

                            if(pageHtml.length()>0){
                                Pattern tokenPat = Pattern.compile("marginright5 globalAction resignme\"                           data-code=\"resignme\" data-token=\"(.*?)\"");
                                Matcher mToken = tokenPat.matcher(pageHtml);

                                Pattern adidPat = Pattern.compile("data-ad=\"(.*?)\"");
                                Matcher mAdId = adidPat.matcher(pageHtml);
                                int i=0;

                                String adId = "";
                                String token = "";
                                while(mToken.find() && mAdId.find()) {
                                    previous = true;
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

                                        String waitingDeactivateResult = "";
                                        HashMap<String, String> headers2 = new HashMap<String, String>();
                                        headers2.put("Origin", "https://www.olx.ua");
                                        headers2.put("Referer", "https://www.olx.ua/myaccount/waiting/");
                                        headers2.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                                        headers2.put("Content-Type", "application/x-www-form-urlencoded");
                                        headers2.put("Accept", "application/json, text/javascript, */*; q=0.01");
                                        headers2.put("X-Requested-With", "XMLHttpRequest");
                                        headers2.put("Accept-Language", "en-US,en;q=0.7,ru;q=0.3");
                                        headers2.put("Accept-Encoding", "gzip, deflate, br");
                                        headers2.put("Host", "www.olx.ua");
                                        headers2.put("Connection", "Keep-Alive");
                                        headers2.put("Cache-Control", "no-cache");
                                        headers2.put("Cookie", cookies);

                                        HashMap<String, String> params2 = new HashMap<String, String>();
                                        params2.put("adID", adId);
                                        params2.put("token", token);

                                        waitingDeactivateResult = Utilities.POSTQuery("https://www.olx.ua/ajax/myaccount/resignme/", params2, headers2, context);
                                        if(waitingDeactivateResult.length()>0){
                                            //
                                            try {
                                                JSONObject deactObjRes = null;
                                                deactObjRes = new JSONObject(waitingDeactivateResult);
                                                if(deactObjRes.getString("success").equals("0")){
                                                    Log.d(TAG, "Объявление " +adId+ " удалено из ожидающих +");
                                                }
                                            }catch (Exception ex){
                                                Log.e(TAG, "Объявление не удалено из ожидающих " + ex.getMessage());
                                            }
                                        }else{
                                            Log.e(TAG, "Объявление " +adId+ " не удалено");
                                        }

                                        Log.d(TAG, "Результат удаления объявления из ожидающих " + waitingDeactivateResult);//{"is_for_sale":1,"id":"458125140","success":"0"}
                                    }
                                    i++;
                                }
                            }

                            /*try {
                                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("pagewaiting.txt", Context.MODE_PRIVATE));
                                outputStreamWriter.write(pageHtml);
                                outputStreamWriter.close();
                            }
                            catch (Exception ex) {
                                Log.e("Exception", "File write failed: " + ex.toString());
                            }*/
                        }

                        //Matcher mNeak = Pattern.compile("<a class=\"fbold\" href=\"https:\\/\\/www.olx.ua\\/myaccount\\/archive\\/\">Неактивные\t\t\t\t\t\t\t<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\\([0-9]+").matcher(mHtmlCode);
                        //if (mNeak.find( )|| previous) {
                        if(mHtmlCode.indexOf("<li id=\"typearchive\" class=\"fleft rel\">")>=0 || mHtmlCode.indexOf("<li id=\"typearchive\" class=\"fleft rel selected\">")>=0 || previous){
                            Log.d(TAG, "Найден: Неактивные");

                            String pageHtml = "";
                            HashMap<String, String> params = new HashMap<String, String>();
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Host", "www.olx.ua");
                            headers.put("Connection", "keep-alive");
                            headers.put("Upgrade-Insecure-Requests", "1");
                            headers.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                            headers.put("Referer", "https://www.olx.ua/myaccount/");
                            headers.put("Accept-Encoding", "gzip, deflate, br");
                            headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                            headers.put("Cookie", cookies);
                            pageHtml = Utilities.GETQuery("https://www.olx.ua/myaccount/archive/", params, headers, context);

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
                                    previous = true;
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
                                        headers2.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                                        headers2.put("Content-Type", "application/x-www-form-urlencoded");
                                        headers2.put("Referer", "https://www.olx.ua/myaccount/archive/");
                                        headers2.put("Accept-Encoding", "gzip, deflate, br");
                                        headers2.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                                        headers2.put("Cookie", cookies);

                                        HashMap<String, String> params2 = new HashMap<String, String>();
                                        params2.put("adID", adId);
                                        params2.put("token", token);

                                        archiveDeleteResult = Utilities.POSTQuery("https://www.olx.ua/ajax/myaccount/removeme/", params2, headers2, context);
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

                        //Matcher mDel = Pattern.compile("<a class=\"fbold\" href=\"https:\\/\\/www.olx.ua\\/myaccount\\/moderated\\/\">Удаленные\t\t\t\t\t\t\t<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\\([0-9]+").matcher(mHtmlCode);
                        //if (mDel.find( )|| previous) {
                        if(mHtmlCode.indexOf("<li id=\"typemoderated\" class=\"fleft rel\">")>=0 || mHtmlCode.indexOf("<li id=\"typemoderated\" class=\"fleft rel selected\">")>=0 || previous){
                            Log.d(TAG, "Найден: Удаленные");

                            String pageHtml = "";
                            HashMap<String, String> params = new HashMap<String, String>();
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Host", "www.olx.ua");
                            headers.put("Connection", "keep-alive");
                            headers.put("Upgrade-Insecure-Requests", "1");
                            headers.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                            headers.put("Referer", "https://www.olx.ua/myaccount/");
                            headers.put("Accept-Encoding", "gzip, deflate, br");
                            headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                            headers.put("Cookie", cookies);
                            pageHtml = Utilities.GETQuery("https://www.olx.ua/myaccount/moderated/", params, headers, context);

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
                                    previous = true;
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
                                        headers2.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                                        headers2.put("Content-Type", "application/x-www-form-urlencoded");
                                        headers2.put("Referer", "https://www.olx.ua/myaccount/archive/");
                                        headers2.put("Accept-Encoding", "gzip, deflate, br");
                                        headers2.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                                        headers2.put("Cookie", cookies);

                                        HashMap<String, String> params2 = new HashMap<String, String>();
                                        params2.put("adID", adId);
                                        params2.put("token", token);

                                        moderatedResult = Utilities.POSTQuery("https://www.olx.ua/ajax/myaccount/removeme/", params2, headers2, context);
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





                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("bs", "myaccount_adding");
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Host", "www.olx.ua");
                        headers.put("Connection", "keep-alive");
                        headers.put("Upgrade-Insecure-Requests", "1");
                        headers.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                        headers.put("Referer", "https://www.olx.ua/myaccount/");
                        headers.put("Accept-Encoding", "gzip, deflate, br");
                        headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                        headers.put("Cookie", cookies);

                        // страница с заполнением данных объявления
                        String myaccountAddingPageHtml = Utilities.GETQuery("https://www.olx.ua/post-new-ad/", params, headers, context);

                        Pattern pAddingKey = Pattern.compile("<input type=\"hidden\" name=\"data\\[adding_key\\]\" value=\"(.*?)\"");
                        Matcher mAddingKey = pAddingKey.matcher(myaccountAddingPageHtml);

                        while(mAddingKey.find()) {
                            if(mAddingKey.group(1).length()>0) {
                                Log.d(TAG, "adding_key: " + mAddingKey.group(1));
                                adding_key = mAddingKey.group(1);
                                break;
                            }
                        }

                        // постинг
                        String sRand = UUID.randomUUID().toString();
                        String boundaryString = "----WebKitFormBoundary" + sRand;

                        HashMap<String, String> postNewAdheaders = new HashMap<String, String>();
                        headers.put("Host", "www.olx.ua");
                        headers.put("Connection", "keep-alive");
                        headers.put("Cache-Control", "max-age=0");
                        headers.put("Origin", "https://www.olx.ua");
                        headers.put("Upgrade-Insecure-Requests", "1");
                        headers.put("Content-Type", "multipart/form-data; boundary=" + boundaryString);
                        headers.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                        headers.put("Referer", "https://www.olx.ua/post-new-ad/?bs=myaccount_adding");
                        headers.put("Accept-Encoding", "gzip, deflate, br");
                        headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                        headers.put("Cookie", cookies);

                        HashMap<String, String> postNewAdparams = new HashMap<String, String>();

                        try {

                            for(int k = 0; k<currentAd.length(); k++){ // post параметры
                                JSONObject ob;
                                ob = new JSONObject(currentAd.get(k)+"");
                                // мои параметры
                                if(ob.getString("key").equals("adid") || ob.getString("key").equals("riak_key")) continue;
                                //Log.d(TAG, "==========key: " + ob.getString("key") + " value: " + ob.getString("value"));

                                switch (ob.getString("key")){
                                    case "photos":
                                        // установка фото
                                        //JSONObject obPhotos;
                                        //obPhotos = new JSONObject(ob.getString("value"));
                                        JSONArray photosInf = new JSONArray(ob.getString("value"));
                                        int totalPhotos = 0;
                                        int totalPhotosForUpload = 0;
                                        totalPhotos = photosInf.length();
                                        String image_order = "";
                                        int photoCur = 1;
                                        for(int j=0;j<photosInf.length(); j++){
                                            JSONObject photoClassObj = new JSONObject(photosInf.getString(j));
                                            String photo = photoClassObj.getString("currentPhoto");
                                            Log.d(TAG, "PPPPPP " + photo);

                                            upPhoto upImg = new upPhoto();
                                            Log.d(TAG, "====" + photo + " VS " + "android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo);
                                            upPhoto photo1 = new upPhoto();
                                            if(!photo.equals("android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo) && photo!=null){
                                                String realPhotoPath = Utilities.getRealPathFromURI(context, Uri.parse(photo));
                                                File file = new File(realPhotoPath);
                                                if (file.exists()) {

                                                    //Log.d(TAG, "Фото для загрузки " + n + " === " + photosToUpload.get(n));

                                                    String upRes = "";
                                                    JSONObject dataJsonObj = null;
                                                    if(photoCur==1){
                                                        upRes =  Utilities.uploadPhoto(Uri.parse(photo).toString(), "", context);
                                                        Log.d(TAG, "Фото загружено. Ответ: " + upRes);

                                                        //JSONObject dataJsonObj = null;
                                                        dataJsonObj = new JSONObject(upRes);

                                                        //Log.d(TAG, "Получен riak_key " + dataJsonObj.getString("riak_key"));
                                                        if(dataJsonObj.getString("riak_key").length()>0){
                                                            riak_key = dataJsonObj.getString("riak_key");
                                                        }
                                                        image_order += dataJsonObj.getString("apollo_id");
                                                    }else{
                                                        upRes =  Utilities.uploadPhoto(Uri.parse(photo).toString(), riak_key, context);
                                                        //JSONObject dataJsonObj = null;
                                                        dataJsonObj = new JSONObject(upRes);

                                                        Log.d(TAG, "Фото загружено. Ответ: " + upRes);
                                                        image_order += " " + dataJsonObj.getString("apollo_id");
                                                    }

                                                    //Utilities.uploadPhoto(cookies, Utilities.getRealPathFromURI(context, photosToUpload.get(n)));
                                                    int image  = photoCur+1;
                                                    postNewAdparams.put("image[]", image+"");
                                                    postParams.add("image[]=" + image);
                                                    Log.d(TAG, "++++++++++" + "image[]=" + image);

//url_thumb
                                                    upImg.photo = photo;
                                                    upImg.adPhotoId = dataJsonObj.getString("apollo_id");
                                                    upImg.slot = dataJsonObj.getString("slot");

                                                    photo1.currentPhoto = dataJsonObj.getString("url_thumb");
                                                    photo1.oldPhoto = Uri.parse("android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo).toString();
                                                    photo1.adPhotoId = dataJsonObj.getString("apollo_id");
                                                    photo1.slot = dataJsonObj.getString("slot");

                                                    photoCur++;
                                                }else{
                                                    Log.d(TAG, "Фото не существует " + photo + " real " + realPhotoPath);
                                                }

                                            }else{
                                                upImg.photo = photo;
                                                upImg.adPhotoId = "";
                                                upImg.slot = "";

                                                photo1.currentPhoto = Uri.parse("android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo).toString();
                                                //photo1.oldPhoto = adPhotoClassObj.getString("oldPhoto");
                                                photo1.oldPhoto = Uri.parse("android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo).toString();
                                                photo1.adPhotoId = "";
                                                photo1.slot = "";
                                            }
                                            photosWithNewparams.add(photo1);
                                            photosToUpload.add(upImg);
                                            //photosToUpload
                                        }
                                        totalPhotosForUpload = photosToUpload.size();

                                        Log.d(TAG, "Есть фото для загрузки " + totalPhotosForUpload);

                                        if(totalPhotosForUpload>0){
                                            // есть фото для загрузки

                                            int photoNum = 1;



                                            //if(true) return;


                                            postNewAdparams.put("data[riak_key]", riak_key);
                                            postParams.add("data[riak_key]=" + riak_key);
                                            Log.d(TAG, "++++++++++" + "data[riak_key]=" + riak_key);
                                            postNewAdparams.put("data[apollo_image_order]", image_order);
                                            postParams.add("data[apollo_image_order]=" + image_order); // пустще значение - фото
                                            Log.d(TAG, "++++++++++" + "data[apollo_image_order]=" + image_order);

                                        }else{
                                            // нет фото для загрузки
                                            for(int u = 0; u<totalPhotos; u++){
                                                postNewAdparams.put("image["+(u+1)+"];filename", "");
                                                postParams.add("image["+(u+1)+"];filename=");
                                            }
                                            postNewAdparams.put("data[riak_key]", "");
                                            postParams.add("data[riak_key]="); // пустще значение - фото
                                            postNewAdparams.put("data[apollo_image_order]", "");
                                            postParams.add("data[apollo_image_order]="); // пустще значение - фото
                                        }


                                        break;
                                    default:
                                        //postParams.add(ob.getString("key") + "=" + Utilities.getValue(currentAd, ob.getString("key")));
                                        //Log.d(TAG, "DEF: " + ob.getString("key") + "=" + Utilities.getValue(currentAd, ob.getString("key")));
                                        postNewAdparams.put(ob.getString("key"), ob.getString("value"));
                                        postParams.add(ob.getString("key") + "=" + ob.getString("value"));
                                        Log.d(TAG, "DEF: " + ob.getString("key") + "=" + ob.getString("value"));
                                        break;
                                }
                            }

                            switch(Utilities.getValue(currentAd, "data[category_id]")) {
                                case "6":
                                case "144":
                                case "140":
                                case "150":
                                case "1477":
                                case "133":
                                case "148":
                                case "1473":
                                case "152":
                                case "183":
                                case "137":
                                case "132":
                                case "141":
                                case "154":
                                case "134":
                                case "147":
                                case "1475":
                                case "1159":
                                case "145":
                                case "143":
                                case "1479":
                                case "149":
                                case "1165":
                                case "1163":
                                case "151":
                                case "1481":
                                    // Параметр data[offer_seek]= имеет значение в этих категориях, категориях с работой
                                    // Предлагаю/ ищу работу
                                    break;
                                default:
                                    postNewAdparams.put("data[offer_seek]", "");
                                    postNewAdparams.put("data[param_currency]", "");
                                    postNewAdparams.put("data[partner_offer_url]", "");
                                    postNewAdparams.put("data[gallery_html]", "");
                                    postNewAdparams.put("data[safedeal_external_uuid]", "");
                                    postParams.add("data[offer_seek]=");
                                    postParams.add("data[param_currency]="); // Не понял, что за параметр. Всегда пустой. Параметр отсутсвует в работе
                                    postParams.add("data[partner_offer_url]="); // Доступно только в пакете Премиум. Заполните адрес вашей веб-страницы с формой подачи резюме для того, чтобы кандидаты смогли подавать заявки на вакансию на вашем сайте (опционально)
                                    postParams.add("data[gallery_html]="); // Не понял, что за параметр. Всегда пустой. Параметр отсутсвует в работе
                                    postParams.add("data[safedeal_external_uuid]="); // Что-то связанное с безопасной доставкой

                                    break;
                            }
                            postNewAdparams.put("data[city]", dbHelper.getCityByIdAndDistrict(Utilities.getValue(currentAd, "data[city_id]"), Utilities.getValue(currentAd, "data[district_id]")));
                            postParams.add("data[city]=" + dbHelper.getCityByIdAndDistrict(Utilities.getValue(currentAd, "data[city_id]"), Utilities.getValue(currentAd, "data[district_id]")));
                            postNewAdparams.put("loc-option", "loc-opt-2");
                            postParams.add("loc-option=loc-opt-2"); // loc-opt-2 - примерное местоположение, loc-opt-1 - точное местоположение

                            String mapZoom = "";
                            String mapLon = "";
                            String mapLat = "";

                            Pattern pMapZoom = Pattern.compile("data-zoom=\"(.*?)\"");
                            Matcher mMapZoom = pMapZoom.matcher(mHtmlCode);
                            while(mMapZoom.find()) {
                                if(mMapZoom.group(1).length()>0) {
                                    //Log.d(TAG, "data[map_zoom]=: " + mMapZoom.group(1));
                                    mapZoom = mMapZoom.group(1);
                                    break;
                                }
                            }
                            String z = dbHelper.getCityMapZoom(Utilities.getValue(currentAd, "data[city_id]"), Utilities.getValue(currentAd, "data[district_id]"));
                            if(z.length()>0){ // если получены данные из базы, то взять их, а иначе данные со страницы
                                postNewAdparams.put("data[map_zoom]", z);
                                postParams.add("data[map_zoom]=" + z);
                            }else {
                                postNewAdparams.put("data[map_zoom]", mapZoom);
                                postParams.add("data[map_zoom]=" + mapZoom);
                            }


                            Pattern pMapLon = Pattern.compile("data-lon=\"(.*?)\"");
                            Matcher mMapLon = pMapLon.matcher(mHtmlCode);
                            while(mMapLon.find()) {
                                if(mMapLon.group(1).length()>0) {
                                    //Log.d(TAG, "data[map_lon]=: " + mMapLon.group(1));
                                    mapLon = mMapLon.group(1);
                                    break;
                                }
                            }
                            String lon = dbHelper.getCityLon(Utilities.getValue(currentAd, "data[city_id]"), Utilities.getValue(currentAd, "data[district_id]"));
                            if(lon.length()>0){
                                postNewAdparams.put("data[map_lon]", lon);
                                postParams.add("data[map_lon]=" + lon);
                            }else{
                                postNewAdparams.put("data[map_lon]", mapLon);
                                postParams.add("data[map_lon]=" + mapLon);
                            }
                            //postParams.add("data[map_lon]=" + mapLon);

                            Pattern pMapLat = Pattern.compile("data-lat=\"(.*?)\"");
                            Matcher mMapLat = pMapLat.matcher(mHtmlCode);
                            while(mMapLat.find()) {
                                if(mMapLat.group(1).length()>0) {
                                    //Log.d(TAG, "data[map_lat]=: " + mMapLat.group(1));
                                    mapLat = mMapLat.group(1);
                                    break;
                                }
                            }
                            String lat = dbHelper.getCityLat(Utilities.getValue(currentAd, "data[city_id]"), Utilities.getValue(currentAd, "data[district_id]"));
                            if(lat.length()>0){
                                postNewAdparams.put("data[map_lat]]", lat);
                                postParams.add("data[map_lat]=" + lat);
                            }else{
                                postNewAdparams.put("data[map_lat]", mapLat);
                                postParams.add("data[map_lat]=" + mapLat);
                            }

                            postNewAdparams.put("data[payment_code]", "");
                            postNewAdparams.put("data[sms_number]", "");
                            postNewAdparams.put("data[adding_key]", "");
                            postNewAdparams.put("paidadFirstPrice", "");
                            postNewAdparams.put("paidadChangesLog", "");
                            postParams.add("data[payment_code]="); // VIP объявление
                            postParams.add("data[sms_number]="); // На этот номер будут приходить SMS-уведомления о новых сообщениях
                            postParams.add("data[adding_key]=" + adding_key);
                            postParams.add("paidadFirstPrice="); // не понял
                            postParams.add("paidadChangesLog="); // не понял

                            // Предложенные категории
                            for (int f = 0; f < currentSuggestedCategories.size(); f++) {
                                //Log.d(TAG, "QQQQQQ" + currentSuggestedCategories.get(f));
                                postNewAdparams.put("data[suggested_categories][]", currentSuggestedCategories.get(f));
                                postParams.add("data[suggested_categories][]=" + currentSuggestedCategories.get(f));
                            }



                            postNewAdparams.put("data[map_radius]", "0");
                            postParams.add("data[map_radius]=0");
                            //Log.d(TAG, "MAP_ZOOM: " + mapZoom + " - " + dbHelper.getCityMapZoom(Utilities.getValue(currentAd, "data[city_id]"), Utilities.getValue(currentAd, "data[district_id]")));
                            //Log.d(TAG, "MAP_LON: " + mapLon + " - " + dbHelper.getCityLon(Utilities.getValue(currentAd, "data[city_id]"), Utilities.getValue(currentAd, "data[district_id]")));
                            //Log.d(TAG, "MAP_LAT: " + mapLat + " - " + dbHelper.getCityLat(Utilities.getValue(currentAd, "data[city_id]"), Utilities.getValue(currentAd, "data[district_id]")));

                            //Log.d(TAG, dbHelper.getCityByIdAndDistrict(Utilities.getValue(currentAd, "data[city_id]"), Utilities.getValue(currentAd, "data[district_id]")) );




                            // Публикация поста
                            /*for (int k = 0; k < currentAd.length(); k++) { // post параметры
                                JSONObject jsonObj = currentAd.getJSONObject(k);
                                //Log.d(TAG, "OBject: " + jsonObj.get("key") + jsonObj.get("value"));
                                String key = (String)jsonObj.get("key");
                                switch(key) {
                                    case "data[title]":
                                        postParams.add("data[title]=" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[category_id]":
                                        postParams.add("data[category_id]=" + (String)jsonObj.get("value"));
                                        switch ((String)jsonObj.get("value")){
                                            case "6":
                                            case "144":
                                            case "140":
                                            case "150":
                                            case "1477":
                                            case "133":
                                            case "148":
                                            case "1473":
                                            case "152":
                                            case "183":
                                            case "137":
                                            case "132":
                                            case "141":
                                            case "154":
                                            case "134":
                                            case "147":
                                            case "1475":
                                            case "1159":
                                            case "145":
                                            case "143":
                                            case "1479":
                                            case "149":
                                            case "1165":
                                            case "1163":
                                            case "151":
                                            case "1481":
                                                // Установить data[offer_seek]=
                                                break;
                                            default:
                                                postParams.add("data[offer_seek]=");
                                                break;
                                        }
                                        break;
                                    case "data[param_price][0]":
                                        postParams.add("data[param_price][0]=" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[param_price][1]":
                                        postParams.add("data[param_price][1]=" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[param_price][currency]":
                                        postParams.add("data[param_price][currency]=" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[param_currency]": // ????????????????????????????????????????????
                                        postParams.add("data[param_currency]=" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[param_state]":
                                        postParams.add("data[param_state]=" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[param_size]":
                                        postParams.add("data[param_size]=" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[private_business]":
                                        postParams.add("data[private_business]=" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[description]":
                                        postParams.add("data[description]=" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[partner_offer_url]": //????????????????????????????????????????????
                                        postParams.add("data[partner_offer_url]" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[riak_key]": //????????????????????????????????????????????
                                        postParams.add("data[riak_key]" + (String)jsonObj.get("value"));
                                        break;
                                    case "data[apollo_image_order]": //????????????????????????????????????????????
                                        postParams.add("data[apollo_image_order]" + (String)jsonObj.get("value"));
                                        break;
                                    //image[1];filename=&image[2];filename=&image[3];filename=&image[4];filename=&image[5];filename=&image[6];filename=&image[7];filename=&image[8];filename=&data[gallery_html]=&data[safedeal_external_uuid]=&data[city]=Киев, Киевская область, Дарницкий&loc-option=loc-opt-2&data[phone]=&data[person]=Юрий&data[payment_code]=&data[sms_number]=&data[adding_key]="+adding_key+"&paidadFirstPrice=&paidadChangesLog=&data[suggested_categories][]=49&data[suggested_categories][]=54&data[suggested_categories][]=485&data[suggested_categories][]=1498&data[map_radius]=0&data[city_id]=268&data[district_id]=3&data[map_zoom]=11&data[map_lat]=50.40931&data[map_lon]=30.69263"
                                    default:
                                        break;
                                }
                                // если не работа, то data[offer_seek]=


                                //Log.d(TAG, "=========key: " + ob.getString("key") + " value: " + ob.getString("value"));
                            }*/
                            //final String postData = "data[title]=zaggg test zagggtest zagggtest zagggtest zaggg&data[category_id]=541&data[offer_seek]=&data[param_price][0]=price&data[param_price][1]=100&data[param_price][currency]=UAH&data[param_currency]=&data[param_state]=used&data[param_size]=&data[private_business]=private&data[description]=test zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zaggg&data[partner_offer_url]=&data[riak_key]=&data[apollo_image_order]=&image[1];filename=&image[2];filename=&image[3];filename=&image[4];filename=&image[5];filename=&image[6];filename=&image[7];filename=&image[8];filename=&data[gallery_html]=&data[safedeal_external_uuid]=&data[city]=Киев, Киевская область, Дарницкий&loc-option=loc-opt-2&data[phone]=&data[person]=Юрий&data[payment_code]=&data[sms_number]=&data[adding_key]="+adding_key+"&paidadFirstPrice=&paidadChangesLog=&data[suggested_categories][]=49&data[suggested_categories][]=54&data[suggested_categories][]=485&data[suggested_categories][]=1498&data[map_radius]=0&data[city_id]=268&data[district_id]=3&data[map_zoom]=11&data[map_lat]=50.40931&data[map_lon]=30.69263";
                            String cookies2 = CookieManager.getInstance().getCookie(url);
                            String[] temp=cookies2.split(";");
                            for (String ar1 : temp ){
                                Log.d(TAG, ">>>>>Cookie: " + ar1);
                            }




                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // загрузка фоток

                                    //String postData = "data[title]=test zaggg test zagggtest zagggtest zagggtest zaggg&data[category_id]=541&data[offer_seek]=&data[param_price][0]=price&data[param_price][1]=100&data[param_price][currency]=UAH&data[param_currency]=&data[param_state]=used&data[param_size]=&data[private_business]=private&data[description]=test zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zagggtest zaggg test zagggtest zagggtest zagggtest zaggg&data[partner_offer_url]=&data[riak_key]=&data[apollo_image_order]=&image[1];filename=&image[2];filename=&image[3];filename=&image[4];filename=&image[5];filename=&image[6];filename=&image[7];filename=&image[8];filename=&data[gallery_html]=&data[safedeal_external_uuid]=&data[city]=Киев, Киевская область, Дарницкий&loc-option=loc-opt-2&data[phone]=&data[person]=Юрий&data[payment_code]=&data[sms_number]=&data[adding_key]="+adding_key+"&paidadFirstPrice=&paidadChangesLog=&data[suggested_categories][]=49&data[suggested_categories][]=54&data[suggested_categories][]=485&data[suggested_categories][]=1498&data[map_radius]=0&data[city_id]=268&data[district_id]=3&data[map_zoom]=11&data[map_lat]=50.40931&data[map_lon]=30.69263";
                                    //публикация объявы
                                    Log.d(TAG, "POST: " + TextUtils.join("&", postParams));
                                    wvBrowser.postUrl("https://www.olx.ua/post-new-ad/", TextUtils.join("&", postParams).getBytes());
                                    postParams.clear();
                                }
                            });
                        }catch(Exception ex){
                            Log.e(TAG, "ошибка " + ex.getMessage());
                        }


                        //login = true;

                        // КОНЕЦ ПОСТИНГА!!!!!!!!!!!!!!!!!!!
                        // КОНЕЦ ПОСТИНГА!!!!!!!!!!!!!!!!!!!
                        // КОНЕЦ ПОСТИНГА!!!!!!!!!!!!!!!!!!!



                        // открыта страница ожидающих объявлений
                    }else if(mTitle.group(1).equals("Мой профиль • OLX.ua") && url.equals("https://www.olx.ua/myaccount/waiting/") && entered){

                        /*try {
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("page.txt", Context.MODE_PRIVATE));
                            outputStreamWriter.write(mHtmlCode);
                            outputStreamWriter.close();
                        }
                        catch (Exception ex) {
                            Log.e("Exception", "File write failed: " + ex.toString());
                        }*/

                        // редирект, когда нет активных объявлений
                        Log.d(TAG, "Вход в профиль выполнен. Редирект на ожидающие объявления");

                        Log.d(TAG, "КУКИ " + cookies);
                        // запись куков в базу
                        //dbHelper.saveCookies(cookies, currentAccount, targetAccountID);//vfffffffff




                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //wvBrowser.loadUrl("https://www.olx.ua/post-new-ad/?bs=myaccount_adding");
                                wvBrowser.loadUrl("https://www.olx.ua/myaccount/");
                            }
                        });
                    }else if(mHtmlCode.contains("Форма содержит следующие ошибки")){

                        Log.e(TAG, "Форма входа содержит ошибки");
                        if(mHtmlCode.contains("Не удалось подтвердить пользователя") && enterAttempt<3){
                            Log.e(TAG, "Не удалось подтвердить пользователя");
                        }
                        enterAttempt ++;

                        login = true;
                        result = "{\"result\":\"0\", \"accountid\":\""+targetAccountID+"\"}";


                    }else if(mHtmlCode.contains("виникла помилка, тому рекомендуємо")){

                        Log.e(TAG, "Бан IP 10 минут");
                        login = true;
                        result = "{\"result\":\"0\", \"accountid\":\""+targetAccountID+"\"}";
                    }else{
                        // ошибка
                        Log.d(TAG, "THE END");
                        login = true;
                        result = "{\"result\":\"0\", \"accountid\":\""+targetAccountID+"\"}";
                    }


                    break;
                }
            }


            /*Log.d(TAG, "Length: " + mHtmlCode.length());

            Pattern p = Pattern.compile("data-sitekey=\"(.*?)\"><");
            Matcher m = p.matcher(mHtmlCode);
            while(m.find()) {
                if(m.group(1).length()>0) {
                    g_recaptcha_data_sitekey = m.group(1);
                    Log.d(TAG, "g_recaptcha_data_sitekey: " + g_recaptcha_data_sitekey);
                    // запрос к рекапче
                    String targetUrl = "http://rucaptcha.com/in.php?key=28bcc58b56b459a06f8d3c19ad2221b0&method=userrecaptcha&googlekey="+g_recaptcha_data_sitekey+"&pageurl=https://www.olx.ua/myaccount/";
                    CreateRecaptchaTaskAsyncTask task = new CreateRecaptchaTaskAsyncTask(MainActivity.this);
                    task.execute(targetUrl);

                    break;
                }
            }*/

        }
    }
}
