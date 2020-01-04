package ua.com.anyapps.easyads.easyads.Bonding;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginAsyncTask extends AsyncTask<String, Void, Void> {
    private static final String TAG = "111232312312";

    private LoginCompleted taskCompleted;
    private String result;


    public LoginAsyncTask(LoginCompleted context) {
        this.taskCompleted = context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.LoginCompleted(result);
        super.onPostExecute(aVoid);
    }

    String responseJson = "";
    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "LoginAsyncTask.java: " + strings[0]);


        try {
            URL obj = new URL("https://www.olx.ua/myaccount/answers/");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(true); // случайный браузер
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
            con.setRequestProperty("Cache-Control", "max-age=0");
            con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            //con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            con.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Referer", "https://www.olx.ua/account/?ref%5B0%5D%5Baction%5D=myaccount&ref%5B0%5D%5Bmethod%5D=index");
            con.setRequestProperty("Origin", "https://www.olx.ua");
            con.setRequestProperty("Upgrade-Insecure-Requests", "1");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Host", "www.olx.ua");

            //con.setRequestProperty("Cookie", "PHPSESSID=12114e9c64dadf9ee4e4e6ff8731ebc4710122dc; access_token=941c12ccd888ca8468fd3630b4eafc50201c8443;remember_login=3197517%3A1541160662%3B5af5e10cf0e6052395a185960870825f;refresh_token=aff30a88f4dd574275723df0c806d0e5fc348a5c;_abck=BD97A609DE67C2168A56685792D2E70650EFDEB436250000EA15205B09F39034~-1~CWLA3n6UAirlKs6eHEYPDiT2YTKfYSALCQAmpxNwb8M=~0~-1;bm_sv=72D1D992FCEDEADF4AC2B450B6A298D8~c/vW4bOFuM2li02+PWRQIJ08eHuMP+IQQO1+UnFfcLM8ECWr2a4VUTkILQSsKZMm28JBK26sWcOVhYrXzpHGF4xjnEr+qj5FAzekL3QKo98HQKNhD8O0j7LTHNKueDrk6asdrr8RQb0V0Vi3MEAxnIj43styH5uEjtP6weELQ9Q=;mobile_default=desktop;dfp_segment_test_v3=73;dfp_segment_test=48;dfp_segment_test_v4=21;lister_lifecycle=1519650720");
            con.setRequestProperty("Cookie", "mobile_default=desktop; dfp_segment_test=48; dfp_segment_test_v3=73; lister_lifecycle=1519650720; dfp_segment_test_v4=21; a_refresh_token=c706fa5fdd9cebdbdedcec9d0c9524715531fb9a; a_access_token=da592fce865fab20c9ba21ed0cb4bac946d50db3; PHPSESSID=a91b8a0d9a5278ecc754771ec1d781f930a6fa2a; remember_login=23865891%3A1541622429%3B090b02f61c4ed77dab6479570e328df9; _abck=BD97A609DE67C2168A56685792D2E70650EFDEB436250000EA15205B09F39034~0~508iWysQseu1AUqx26exX9qJ9d7KLhj33ByWGFZKjyc=~-1~-1; bm_sv=903CCC11BED36E820348F672E9C3F9EC~XwPAnHAjcadhSekcY7IgSVu9U8zjV6/VUZnWrrJmEUttAOHouZmjgD6yZpsosU6//BD/wTvmzBIzaEanZGEoXiasm+YU5mM5sqjI3OcKunCbWB/jvlI2LDjMrIL/ppfUpYOFCv+vUa8/Jhm1QsyrPG/xlKCVitEPo75eclejYOk=;");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    responseJson += inputLine;
                }

                /*CookieManager msCookieManager = new java.net.CookieManager();

                Map<String, List<String>> headerFields = con.getHeaderFields();
                List<String> cookiesHeader = headerFields.get("Set-Cookie");

                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        msCookieManager.getCookieStore().add(null,HttpCookie.parse(cookie).get(0));
                        Log.d(TAG, cookie);
                    }
                }*/


                in.close();
            } else {
                Log.e(TAG, "LoginAsyncTask.java - При запросе страницы ошибка. Ответ сервера: " + String.valueOf(responseCode));
            }
        } catch (Exception e) {
            Log.e(TAG, "LoginAsyncTask.java - Исключение во время обработки страницы " + e.getMessage());
        }


        result = responseJson;
        return null;
    }
}