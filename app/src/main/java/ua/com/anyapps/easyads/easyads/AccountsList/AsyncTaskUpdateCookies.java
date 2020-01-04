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

public class AsyncTaskUpdateCookies extends AsyncTask<String, Void, Void>{
    private static final String TAG = "debapp";

    private UpdateCookiesCompleted taskCompleted;
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

    public AsyncTaskUpdateCookies(UpdateCookiesCompleted _context, Context mContext) {
        this.taskCompleted = _context;
        this.context = mContext;
        /*this.adjson = _adjson;
        this.targetAccountID = _targetAccountID;
        this.adid = _adid;
        this.adtitle = _adtitle;*/

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
        taskCompleted.UpdateCookiesCompleted(result);
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
        targetAccountID = strings[0];

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


            OlxAccount oneOlxAc = dbHelper.getAccount(targetAccountID);
            currentAccount = oneOlxAc.email;
            currentPassword = oneOlxAc.password;

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
            Log.e(TAG, "AsynctaskUpdateCookies.java - Во время запроса на получение результатов с рукаптчи");
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




            String cookies = CookieManager.getInstance().getCookie(url);
            Log.d(TAG, url+ " All the cookies in a string:" + cookies);



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
                        result = "Cookies UPDATED OKKKKKKKKKKKKKKKKK";
                        // запись куков в базу
                        dbHelper.saveCookies(cookies, currentAccount, targetAccountID);

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
