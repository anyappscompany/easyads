package ua.com.anyapps.easyads.easyads.EditAd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.anyapps.easyads.easyads.DBHelper;
import ua.com.anyapps.easyads.easyads.R;
import ua.com.anyapps.easyads.easyads.Utilities;
import ua.com.anyapps.easyads.easyads.adPhotoClass;
import ua.com.anyapps.easyads.easyads.urlParameter;


public class AsyncTaskUpdateAd extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private UpdateAdCompleted taskCompleted;
    private String result;
    private Context ctxt;
    SQLiteDatabase db = null;
    DBHelper dbHelper = null;

    public AsyncTaskUpdateAd(UpdateAdCompleted context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;

        dbHelper = new DBHelper(ctxt);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            //Log.d(TAG, "Ошибка при получение бд: " + ex.getMessage());
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.UpdateAdCompleted(result);
        super.onPostExecute(aVoid);
    }

    String adid = "";
    String adtitle = "";
    String adjson = "";
    String accountId = "";
    String cookies = "";
    String adCategory = "";
    String originalOlxAdId = "";
    //HashMap<String, String> adPostParams = new HashMap<String, String>();
    ArrayList<urlParameter> adPostParams = new ArrayList<>();
    String postUrl = "";
    String referer = "";
    String cacheLink = "https://www.olx.ua/myaccount/waiting/";
    BufferedWriter httpRequestBodyWriter;
    String htmlResult = "";
    String photos = "";
    String riakkey = "";
    ArrayList<adPhotoClass> AdPhotosGridViewItems = new ArrayList<adPhotoClass>();
    ArrayList<urlParameter> urlParametres = new ArrayList<>();
    String newAdJson = "";

    @Override
    protected Void doInBackground(String... strings) {
        adid = strings[0];
        adtitle = strings[1];
        adjson = strings[2];
        adCategory = strings[3];




        /*изменить объявление на олх
        Log.d(TAG, "AsyncTaskUpdateAd.java - Запрос на обновление объявления: " + strings[0]);
        String responseJson = Utilities.getHtml(strings[0], ctxt);
        if(responseJson.length()<=0){
            Log.e(TAG, "AsyncTaskUpdateAd.java - Обновление объявления завершилось неудачей. Сервер вернул пустой результат или не удалось получить данные");
        }
        result = responseJson;*/
        accountId = dbHelper.getAccountIdByAdId(adid);
        cookies = dbHelper.getAccountCookies(accountId);
        originalOlxAdId = dbHelper.getOriginalOlxAdId(adid);
        riakkey = dbHelper.getRiakKey(adid);

        //Log.d(TAG, "originalOlxAdId" + originalOlxAdId);
        //if(true) return null;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {

            postUrl = "https://www.olx.ua/post-new-ad/edit/"+originalOlxAdId+"/";

            String pageHtml = "";
            HashMap<String, String> paramsRef = new HashMap<String, String>();
            HashMap<String, String> headersRef = new HashMap<String, String>();
            headersRef.put("Host", "www.olx.ua");
            headersRef.put("Connection", "keep-alive");
            headersRef.put("Upgrade-Insecure-Requests", "1");
            headersRef.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
            headersRef.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            headersRef.put("Referer", "https://www.olx.ua/myaccount/");
            headersRef.put("Accept-Encoding", "gzip, deflate, br");
            headersRef.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
            headersRef.put("Cookie", cookies);
            pageHtml = Utilities.GETQuery("https://www.olx.ua/myaccount/", paramsRef, headersRef, ctxt);

            Matcher mAct = Pattern.compile("<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"fbold\">\t\t\t\t\t\t\t\t\tАктивные<\\/span> <span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t \\([0-9]+").matcher(pageHtml);
            if (mAct.find( )) {
                Log.d(TAG, "Найден: Активные");
                //adPostParams.put("data[cancel-link]", "https://www.olx.ua/myaccount/");
                adPostParams.add(new urlParameter("data[cancel-link]", "https://www.olx.ua/myaccount/"));
                referer = "https://www.olx.ua/post-new-ad/edit/"+originalOlxAdId+"/?bs=myaccount_edit&ref%5B0%5D%5Baction%5D=myaccount&ref%5B0%5D%5Bmethod%5D=index";
                cacheLink = "https://www.olx.ua/myaccount/";
            }

            Matcher mOj = Pattern.compile("<a class=\"fbold\" href=\"https:\\/\\/www.olx.ua\\/myaccount\\/waiting\\/\">Ожидающие\t\t\t\t\t\t\t<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\\([0-9]+").matcher(pageHtml);
            if (mOj.find( )) {
                Log.d(TAG, "Найден: Ожидающие");
                //adPostParams.put("data[cancel-link]", "https://www.olx.ua/myaccount/waiting/");
                adPostParams.add(new urlParameter("data[cancel-link]", "https://www.olx.ua/myaccount/waiting/"));
                referer = "https://www.olx.ua/post-new-ad/edit/"+originalOlxAdId+"/?bs=myaccount_edit&ref%5B0%5D%5Bpath%5D%5B0%5D=waiting&ref%5B0%5D%5Baction%5D=myaccount&ref%5B0%5D%5Bmethod%5D=index";
                cacheLink = "https://www.olx.ua/myaccount/waiting/";
            }

            Matcher mNeak = Pattern.compile("<a class=\"fbold\" href=\"https:\\/\\/www.olx.ua\\/myaccount\\/archive\\/\">Неактивные\t\t\t\t\t\t\t<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\\([0-9]+").matcher(pageHtml);
            if (mNeak.find( )) {
                Log.d(TAG, "Найден: Неактивные");
                //adPostParams.put("data[cancel-link]", "https://www.olx.ua/myaccount/archive/");
                adPostParams.add(new urlParameter("data[cancel-link]", "https://www.olx.ua/myaccount/archive/"));
                referer = "https://www.olx.ua/post-new-ad/edit/"+originalOlxAdId+"/?bs=myaccount_edit&ref%5B0%5D%5Bpath%5D%5B0%5D=archive&ref%5B0%5D%5Baction%5D=myaccount&ref%5B0%5D%5Bmethod%5D=index";
                cacheLink = "https://www.olx.ua/myaccount/archive/";
            }

            Matcher mDel = Pattern.compile("<a class=\"fbold\" href=\"https:\\/\\/www.olx.ua\\/myaccount\\/moderated\\/\">Удаленные\t\t\t\t\t\t\t<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\\([0-9]+").matcher(pageHtml);
            if (mDel.find( )) {
                Log.d(TAG, "Найден: Удаленные");
                // Удаленные нельзя редактировать
                //return null;
            }

            /******************************************/

            JSONArray ad = new JSONArray(adjson);
            for(int k = 0; k<ad.length(); k++){ // параметры объявления
                JSONObject ob;
                ob = new JSONObject(ad.get(k)+"");
                //Log.d(TAG, ">>>>>kex33y: " + ob.getString("key") + " value: " + ob.getString("value"));

                if(ob.getString("key").equals("photos")) {
                    JSONArray adPhotoClassArrs = new JSONArray(ob.getString("value"));
                    //Log.d(TAG, "tot photos " + adPhotoClassArrs.length());

                    for(int n=0; n<adPhotoClassArrs.length(); n++){
                        JSONObject photoOB =  new JSONObject(adPhotoClassArrs.getString(n));
                        //Log.d(TAG, n+ " Full Photo Object"+photoOB.toString());
                        //Log.d(TAG, n+ " Slot " + photoOB.getString("slot") + " adPhotoId " + photoOB.getString("adPhotoId"));
                    }
                    //if(true) return null;

                    for(int n=0; n<adPhotoClassArrs.length(); n++){
                        JSONObject photoOB =  new JSONObject(adPhotoClassArrs.getString(n));
                        //Log.d(TAG, "FFFFFFFFFFFFF"+photoOB.toString());
                        //Log.d(TAG, "old " + photoOB.getString("oldPhoto") + " cur " + photoOB.getString("currentPhoto"));
                        // если фото изменилось
                        adPhotoClass photoCl = new adPhotoClass(ctxt);
                        photoCl.adPhotoId = photoOB.getString("adPhotoId");
                        photoCl.slot = photoOB.getString("slot");
                        photoCl.currentPhoto = photoOB.getString("currentPhoto");
                        photoCl.oldPhoto = photoOB.getString("oldPhoto");
                        AdPhotosGridViewItems.add(photoCl);
                    }


                    //String newPhotos = "";
                    for(int h =0;h<AdPhotosGridViewItems.size();h++){
                        Log.d(TAG, "currentPhoto: " + AdPhotosGridViewItems.get(h).currentPhoto + " oldPhoto: " + AdPhotosGridViewItems.get(h).oldPhoto + " slot: " + AdPhotosGridViewItems.get(h).slot + " adPhotoId: " + AdPhotosGridViewItems.get(h).adPhotoId);
                    }


                    //Log.d(TAG, "all "+gson.toJson(AdPhotosGridViewItems));
                    String imageOrders = "";
                    for (int h=0; h<AdPhotosGridViewItems.size();h++){
                        if(AdPhotosGridViewItems.get(h).adPhotoId.length()>0){
                            //adPostParams.put("image[]", AdPhotosGridViewItems.get(h).slot);
                            adPostParams.add(new urlParameter("image[]", AdPhotosGridViewItems.get(h).slot));
                            if(h == 0){
                                imageOrders += AdPhotosGridViewItems.get(h).adPhotoId;
                            }else{
                                imageOrders += " " + AdPhotosGridViewItems.get(h).adPhotoId;
                            }
                        }
                    }
                    //adPostParams.put("data[apollo_image_order]", imageOrders);
                    adPostParams.add(new urlParameter("data[apollo_image_order]", imageOrders));


                    urlParametres.add(new urlParameter("photos", gson.toJson(AdPhotosGridViewItems)));





                }else {

                    if(!ob.getString("key").equals("adid")){
                        if(ob.getString("key").equals("data[cancel-link]")){
                            //adPostParams.put("data[cancel-link]", cacheLink);
                            adPostParams.add(new urlParameter("data[cancel-link]", cacheLink));
                        }else {
                            //adPostParams.put(ob.getString("key"), ob.getString("value"));
                            adPostParams.add(new urlParameter(ob.getString("key"), ob.getString("value")));
                        }
                    }




                    urlParametres.add(new urlParameter(ob.getString("key"), ob.getString("value")));
                }

            }
        } catch (JSONException e) {
            Log.e(TAG, "AsyncTaskUpdateAd.java (doInBackground()) " + e.getMessage());
        }

        //Log.d(TAG, "NEW ADJSON " + gson.toJson(urlParametres));
        newAdJson = gson.toJson(urlParametres);
        for(int b=0; b<urlParametres.size();  b++){
            Log.d(TAG, "xxxx KEY: " + urlParametres.get(b).key + " VALUE: " + urlParametres.get(b).value);
        }
        //if(true) return null;
        /*try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ctxt.openFileOutput("2adjson.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(newAdJson);
            outputStreamWriter.close();
        }
        catch (Exception ex) {
            Log.e("Exception", "File write failed: " + ex.toString());
        }*/



        //if(true) return null;









        //adPostParams.put("data[offer_seek]", "offer");
        //adPostParams.add(new urlParameter("data[offer_seek]", "offer"));
        adPostParams.add(new urlParameter("data[riak_key]", riakkey));




/*Найти эти значения
        adPostParams.add(new urlParameter("data[param_currency]", ""));
        adPostParams.add(new urlParameter("data[partner_offer_url]", ""));
        adPostParams.add(new urlParameter("data[gallery_html]", ""));
        adPostParams.add(new urlParameter("data[safedeal_external_uuid]", ""));
        adPostParams.add(new urlParameter("data[city]", "Александровка, Киевская область, Мироновский район"));
        adPostParams.add(new urlParameter("loc-option", "loc-opt-2"));
        adPostParams.add(new urlParameter("data[map_zoom]", "12"));
        adPostParams.add(new urlParameter("data[map_lat]", "49.68990"));
        adPostParams.add(new urlParameter("data[map_lon]", "30.77726"));
        adPostParams.add(new urlParameter("data[payment_code]", ""));
        adPostParams.add(new urlParameter("data[sms_number]", ""));
        adPostParams.add(new urlParameter("data[adding_key]", ""));
        adPostParams.add(new urlParameter("paidadFirstPrice", ""));
        adPostParams.add(new urlParameter("paidadChangesLog", ""));
        adPostParams.add(new urlParameter("data[map_radius]", "0"));

        if(true) return null;*/



        Log.d(TAG, "PARAMS SIZE " + adPostParams.size());

        /*for(Map.Entry<String, String> entry : adPostParams.entrySet()){
            Log.d(TAG, entry.getKey() + " ___ " + entry.getValue());
        }*/
        for(int b=0;b<adPostParams.size();b++){
            Log.d(TAG, adPostParams.get(b).key + " ___ " + adPostParams.get(b).value);
        }


        String sRand = UUID.randomUUID().toString();
        String boundaryString = "----WebKitFormBoundary" + sRand;

        HashMap<String, String> postEditAdheaders = new HashMap<String, String>();
        postEditAdheaders.put("Host", "www.olx.ua");
        postEditAdheaders.put("Connection", "keep-alive");
        postEditAdheaders.put("Cache-Control", "max-age=0");
        postEditAdheaders.put("Origin", "https://www.olx.ua");
        postEditAdheaders.put("Upgrade-Insecure-Requests", "1");
        postEditAdheaders.put("Content-Type", "multipart/form-data; boundary=" + boundaryString);
        postEditAdheaders.put("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
        postEditAdheaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        postEditAdheaders.put("Referer", referer);
        postEditAdheaders.put("Accept-Encoding", "gzip, deflate, br");
        postEditAdheaders.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        postEditAdheaders.put("Cookie", cookies);

        /*String res = Utilities.POSTQuery(postUrl, adPostParams, postEditAdheaders, ctxt);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ctxt.openFileOutput("edit.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(res);
            outputStreamWriter.close();
        }
        catch (Exception ex) {
            Log.e("Exception", "File write failed: " + ex.toString());
        }*/

        /***********************************/
        try {
            URL serverUrl = new URL(postUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Host", "www.olx.ua");
            urlConnection.setRequestProperty("Connection", "keep-alive");
            urlConnection.setRequestProperty("Cache-Control", "max-age=0");
            urlConnection.setRequestProperty("Origin", "https://www.olx.ua");
            urlConnection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);
            urlConnection.setRequestProperty("User-Agent", ctxt.getResources().getString(R.string.default_user_agent));
            urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            urlConnection.setRequestProperty("Referer", referer);
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            urlConnection.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
            urlConnection.setRequestProperty("Cookie", cookies);

            Log.d(TAG, "REFERER " + referer);
            Log.d(TAG, "COOKIES " + cookies);

            OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
            httpRequestBodyWriter =
                    new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));
            for(int b=0;b<adPostParams.size();b++){
                //Log.d(TAG, entry.getKey() + " ___ " + entry.getValue());
                httpRequestBodyWriter.write("--" + boundaryString + "\n");
                httpRequestBodyWriter.write("Content-Disposition: form-data; name=\""+adPostParams.get(b).key+"\"");
                httpRequestBodyWriter.write("\r\n");
                httpRequestBodyWriter.write("\r\n");
                httpRequestBodyWriter.write(adPostParams.get(b).value.toString());
                httpRequestBodyWriter.write("\r\n");
            }
            httpRequestBodyWriter.flush();

            httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
            httpRequestBodyWriter.flush();

            outputStreamToRequestBody.close();
            httpRequestBodyWriter.close();

            int respCode = urlConnection.getResponseCode();

            if (respCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    htmlResult += inputLine;
                }
                in.close();
                dbHelper.updateAdJson(newAdJson, adid);

            } else {
                Log.e(TAG, "Во время редактирования ответ от сервера " + String.valueOf(respCode));
            }
        } catch (Exception e) {
            Log.e(TAG, "Исключение во время редактирования " + ctxt.getResources().getString(R.string.host) + "/upload.php" + ": " + e.getMessage());
        }
        /***********************************/


        Log.d(TAG, "cookies " + cookies);
        return null;
    }
}
