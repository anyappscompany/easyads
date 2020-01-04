package ua.com.anyapps.easyads.easyads;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import junit.framework.Test;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

public class Utilities {
    private static final String TAG = "debapp";
    public static String getHtml(String purl, Context context){
        String html = "";
        // 5 попыток на получение данных
        for(int i=0; i<5;i++) {
            Log.d(TAG, "Utilities.java - Запрос страницы: " + purl + " попытка " + (i+1));
            try {
                URL obj = new URL(purl);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setInstanceFollowRedirects(false); // случайный браузер
                con.setRequestProperty("User-Agent", context.getResources().getString(R.string.default_user_agent));
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        html += inputLine;
                    }
                    in.close();
                } else {
                    Log.e(TAG, "Utilities.java - При запросе страницы " + purl + " ошибка. Ответ сервера: " + String.valueOf(responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Utilities.java - Исключение во время обработки страницы " + purl + ": " + e.getMessage());
            }
            if(html.length()>0) break;

            Log.d(TAG, "Utilities.java - getHtml(...) - Задержка " + (i+1) + " на 1с");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return html;
    }



    public static String GETQuery(String requestURL, HashMap<String, String> getDataParams, HashMap<String, String> getHeaders, Context context){
        String html = "";
        // 5 попыток на получение данных
        String purl = "";
        try {
            purl = requestURL + "?" + getUrlDataString(getDataParams);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for(int i=0; i<5;i++) {
            Log.d(TAG, "Utilities.java - Запрос страницы: " + purl + " попытка " + (i+1));
            try {
                URL obj = new URL(purl);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setInstanceFollowRedirects(false); // случайный браузер
                //con.setRequestProperty("User-Agent", context.getResources().getString(R.string.default_user_agent));
                for(Map.Entry<String, String> entry : getHeaders.entrySet()){
                    //Log.d(TAG, "HEADER: " + entry.getKey() + " " + entry.getValue());
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }

                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = null;
                    if(con.getContentEncoding().indexOf("gzip")>=0){
                        in = new BufferedReader(new InputStreamReader(new GZIPInputStream(con.getInputStream())));
                    }else {
                        in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    }
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        html += inputLine;
                    }
                    in.close();
                } else {
                    Log.e(TAG, "Utilities.java - При запросе страницы " + purl + " ошибка. Ответ сервера: " + String.valueOf(responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Utilities.java - Исключение во время обработки страницы " + purl + ": " + e.getMessage());
            }
            if(html.length()>0) break;

            Log.d(TAG, "Utilities.java - getHtml(...) - Задержка " + (i+1) + " на 1с");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return html;
    }

public static String POSTQuery(String requestURL, HashMap<String, String> postDataParams, HashMap<String, String> postHeaders, Context context){
        String html = "";





    // 5 попыток на получение данных
    for(int i=0; i<5;i++) {
        Log.d(TAG, "Utilities.java - POST запрос: " + requestURL + " попытка " + (i+1));
        try {
            URL url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(true);

            for(Map.Entry<String, String> entry : postDataParams.entrySet()){
                //Log.d(TAG, "PARAM: " + entry.getKey() + " " + entry.getValue());
            }
            for(Map.Entry<String, String> entry : postHeaders.entrySet()){
                //Log.d(TAG, "HEADER: " + entry.getKey() + " " + entry.getValue());
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getUrlDataString(postDataParams));
            Log.d(TAG, "UrlDataString " + getUrlDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    html+=line;
                }
                br.close();
            }
            else {
                html="";
                Log.e(TAG, "Utilities.java - При запросе страницы " + url + " ошибка. Ответ сервера: " + String.valueOf(responseCode));
            }
            /*HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false); // случайный браузер
            con.setRequestProperty("User-Agent", context.getResources().getString(R.string.default_user_agent));
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    html += inputLine;
                }
                in.close();
            } else {
                Log.e(TAG, "Utilities.java - При запросе страницы " + url + " ошибка. Ответ сервера: " + String.valueOf(responseCode));
            }*/
        } catch (Exception e) {
            Log.e(TAG, "Utilities.java - Исключение во время обработки страницы " + requestURL + ": " + e.getMessage());
        }
        if(html.length()>0) break;

        Log.d(TAG, "Utilities.java - POSTQuery(...) - Задержка " + (i+1) + " на 1с");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Log.e(TAG, e.toString());
        }
    }
        return html;
}

    public static String getUrlDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        Log.d(TAG, "result.toString() " + result.toString());
        return result.toString();
    }

    public static String getValue(JSONArray adParams, String needed){
        String result = "";
        try{
        for (int k = 0; k < adParams.length(); k++) {
            JSONObject jsonObj = adParams.getJSONObject(k);
            String key = (String)jsonObj.get("key");
                if(key.equals(needed)){
                    return (String)jsonObj.get("value");
                }
            }
            return result;
        }
        catch (Exception ex){
            //
        }

        return result;
    }

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    private static BufferedWriter httpRequestBodyWriter;
    public static String uploadPhoto(String photoUri, String _riak_key, Context context){
        String result = "";
        String riak_key = "";
        Context ctxt = context;
        if(_riak_key.length()>0) riak_key = _riak_key;

        for(int i=0; i<5;i++) {
            Log.d(TAG, "Utilities.java(uploadPhoto) - " + (i+1) + " Попытка заргрузить фото " + photoUri);
            String html = "";
            String data = "";
            String image;
            try {
                String fileUrl = Utilities.getRealPathFromURI(ctxt, Uri.parse(photoUri));
                URL serverUrl = new URL("https://www.olx.ua/ajax/upload/upload/?riak_key="+riak_key+"&ad_id=&preview=&category=0");

                Log.d(TAG, "Запрос на загрузку фото" + "https://www.olx.ua/ajax/upload/upload/?riak_key="+riak_key+"&ad_id=&preview=&category=0");

                //Log.d(TAG, "fileUrl " + fileUrl);
            /*Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("riak_key", "")
                    .appendQueryParameter("ad_id", "")
                    .appendQueryParameter("preview", "")
                    .appendQueryParameter("category", "0");
            String query = builder.build().getEncodedQuery();*/

                HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();
                String sRand = UUID.randomUUID().toString();
                String boundaryString = "----WebKitFormBoundary" + sRand;
                File fileToUpload = new File(fileUrl);

                String imgType = null;
                String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
                imgType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                //Log.d(TAG, " img type " + imgType);

                //TimeUnit.SECONDS.sleep(25);

// Indicate that we want to write to the HTTP request body
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                urlConnection.setRequestProperty("Host", "www.olx.ua");
                urlConnection.setRequestProperty("Connection", "keep-alive");
                urlConnection.setRequestProperty("Origin", "https://www.olx.ua");
                //urlConnection.setRequestProperty("Content-Type", "multipart/form-data");
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
                urlConnection.setRequestProperty("Accept", "*/*");
                urlConnection.setRequestProperty("Referer", "https://www.olx.ua/post-new-ad/");
                urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
                urlConnection.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                //urlConnection.setRequestProperty("Cookie", "mobile_default=desktop; optimizelyEndUserId=oeu1458859784325r0.9095683588241277; favouritesBar=1; ldTd=true; optimizelySegments=%7B%221033243617%22%3A%22none%22%2C%221039805884%22%3A%22false%22%2C%221044912896%22%3A%22search%22%2C%221099920464%22%3A%22gc%22%7D; optimizelyBuckets=%7B%228225978916%22%3A%228239180794%22%7D; smcx_328387_last_shown_at=1490971076623; highlight_safedeal_search_filter=true; pa=1499622585228.61550.8539757546512878www.olx.ua0.5476592408536907+1; smcx_378357_last_shown_at=1500577472395; highlight_promoteme=true; last_paidads_code_topupaccount=topupaccount_49; last_paidads_provider_topupaccount=payment_chk_2; __zlcmid=j5gpm7w8BxfdFv; _ga=GA1.2.1701745862.1466705667; dfp_segment_test=48; dfp_user_id=8dbd78c4-1905-93d8-b1e7-4ffcea47a9b5; __utmc=250720985; optimizelyBuckets=%7B%228577982582%22%3A%228576051517%22%2C%228455803748%22%3A%228462611249%22%2C%229858968603%22%3A%229940516722%22%7D; surveyPopupInited=rollingNPSOLXUAwave1; dfp_segment_test_v3=73; lister_lifecycle=1519650720; used_adblock=adblock_disabled; sawSaveLayer=1; __gads=ID=5b16810934c53b2e:T=1522953964:S=ALNI_MZ354bYjj9HP8mvI7c_BXAiN6EoAQ; smcx_0_last_shown_at=1523987082134; searchFavTooltip=1; lang=ru; dfp_segment_test_v4=21; fingerprint=fbdc4f53959cdb4af47c1cffae5cc2e300d2d70001a8f455d11dae88fc3236ac3fef60c9cf99daee3fef60c9cf99daeefaed307981c3a6ca4e1f7a2acddfea3321f2c817b2cc220c3fef60c9cf99daee3fef60c9cf99daee4e1f7a2acddfea33b497a357830277b800d2d70001a8f455308e012c59cf7bdd93ba89d2bc096e2993ba89d2bc096e295a1778be62509b00e8380ebb100f22a6ea59d4056f003ddf2c6a8939be9dd0d3f6e9813aa2a88b6b5bca2f51cd5bb96f628eb7a8a649586881c63c85d4a9e431841ff26a6ec22c3e6255da10575393646255da105753936400ab77cc9433c49756b16d11aecc8186428983c35d9b74cafa3266dd6c454ee009470efce3c1f17e279d401915fc1588fc9233e1090cf76937273db529111a0de0a8d9bd2152e9e06df5e1f064573a1144ade9b70b076f747cf587ca35748f297cf587ca35748f297cf587ca35748f297cf587ca35748f297cf587ca35748f297cf587ca35748f297cf587ca35748f297cf587ca35748f297cf587ca35748f297cf587ca35748f29e4788261c6c83237; last_locations=338-0-0-%D0%98%D0%B7%D1%8E%D0%BC-%D0%A5%D0%B0%D1%80%D1%8C%D0%BA%D0%BE%D0%B2%D1%81%D0%BA%D0%B0%D1%8F+%D0%BE%D0%B1%D0%BB%D0%B0%D1%81%D1%82%D1%8C-izyum_69413-0-0-%D0%91%D1%83%D0%B4%D0%B5%D0%BD%D0%B5%D1%86-%D0%A7%D0%B5%D1%80%D0%BD%D0%BE%D0%B2%D0%B8%D1%86%D0%BA%D0%B0%D1%8F+%D0%BE%D0%B1%D0%BB%D0%B0%D1%81%D1%82%D1%8C-budenets_11963-0-0-%D0%94%D0%BD%D0%B5%D0%BF%D1%80%D0%BE%D0%B2%D0%BE%D0%B5-%D0%94%D0%BD%D0%B5%D0%BF%D1%80%D0%BE%D0%BF%D0%B5%D1%82%D1%80%D0%BE%D0%B2%D1%81%D0%BA%D0%B0%D1%8F+%D0%BE%D0%B1%D0%BB%D0%B0%D1%81%D1%82%D1%8C-dneprovoe; NREUM=s=1537173533885&r=775051&p=697938; _gid=GA1.2.165558398.1539454681; ab_test_device_id=e350c935-e470-4ea4-a527-ed902688cd86; dfp_seg_test_mweb_v7=50; is_tablet=0; a_refresh_token=c706fa5fdd9cebdbdedcec9d0c9524715531fb9a; a_grant_type=device; mweb_observed_id=257408467; dfp_segment_test_v2=50; cookieBarSeenV2=true; grant_type=password; optimizelySegments=%7B%221033243617%22%3A%22none%22%2C%221039805884%22%3A%22false%22%2C%221043958548%22%3A%22referral%22%2C%221044912896%22%3A%22search%22%2C%221066831625%22%3A%22none%22%2C%221087063111%22%3A%22gc%22%2C%221099920464%22%3A%22gc%22%2C%221107551002%22%3A%22true%22%7D; widthHeader=true; a_access_token=da592fce865fab20c9ba21ed0cb4bac946d50db3; user_business_status=private; mp_1799dc4067be971353b85127f766a0a4_mixpanel=%7B%22distinct_id%22%3A%20%22166743a24d1a99-0f8a5d32f2bd72-20722047-f9bb6-166743a24d2276%22%2C%22%24initial_referrer%22%3A%20%22https%3A%2F%2Fwww.olx.ua%2Faccount%2F%3Fref%255B0%255D%255Baction%255D%3Dmyaccount%26ref%255B0%255D%255Bmethod%255D%3Dindex%22%2C%22%24initial_referring_domain%22%3A%20%22www.olx.ua%22%7D; __utmz=250720985.1540156930.1188.105.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); cookieBarSeen=true; pt=7b483852c69a72f37dd385a1ade8d7860dabafe698a8ab73f02f7c317afc1e75bf2e5d1ba33950b91dfd43ad07ad8dcf7b9d5d4dae4b9cc7738792cedd4a2407; from_detail=0; _abck=BD97A609DE67C2168A56685792D2E70650EFDEB436250000EA15205B09F39034~0~508iWysQseu1AUqx26exX9qJ9d7KLhj33ByWGFZKjyc=~-1~-1; user_id=32714070; access_token=6728f1fecd8df9e3013fb221621339d57c98048e; bm_sz=2C7F9669F3F01F48509B88BCD28ED9DF~QAAQ5pQRYD4KKq9mAQAAV/nfygmJj3BdvSyocrjqul5tPUQ7R2hYmWZfYKH41amAO+G/EvNlwNhgVv+8Q1IzXyZLkIeOekF8jI+gcdLQKXx1VqXSyS0I6rqnkAzvCFTHQ7Tw65a5jbzIGdjNAX6Ilgj600YZbgGIKM8qlohhcUJJl9EdpnN8hBnZn7+7/1k=; ak_bmsc=6A70BC2FE1A3A60438C6D13DB5C23FDC601194E6401E0000EFD2D95B9B429711~plB+oU+uAwfFHzrzUsHC7UK5b4n0ptqLlVxjZ4HkKy8MFXGsHdvhhysq1f8o5uP4JSrWLX/r3EmzxAR0A25Rl+FdRsAyoMwbMXt3U8/+ORuWfgM2ZmpppwcRcQ1hiZ+S59J+NVeYQV4JROHznolomTjRjkVywpD5NJfykS3EYmlLfpCO1nJpcv8uFjhPweiIi9NAQSggesbMWFqNiffjODDnMSgoHrSe1Uvk7qQG3/5od/GqBnZeGTMyUWTMidlZHz; PHPSESSID=c6056d64c887a15d5cde62d0cd79be36ffb05212; new_dfp_segment_user_id_32714070=%5B%5D; dfp_segment=%5B%5D; bm_sv=7E56D88BF02BBB08FDC582049F67EBF2~WuFpZKXbydi9hlWguM0F5a1PUgpg/x9rup70HZXCZO4k2ZFcVc80lfIyfmVj0oPcUFpTp8uHNCjDsnLLcjT2pK5T/FxOrHD1LEPubXk0S3Z10dWKUQd1WdEOhoI/bEE1QCrC9z2rYhvJ/Do8maQIz6u1uMwSvgRgGgn4uX9u6sQ=; __utma=250720985.1701745862.1466705667.1541001970.1541007426.1238; __utmt=1; __utmb=250720985.1.10.1541007426; mp_1799dc4067be971353b85127f766a0a4_mixpanel=%7B%22distinct_id%22%3A%20%22166743a24d1a99-0f8a5d32f2bd72-20722047-f9bb6-166743a24d2276%22%2C%22%24initial_referrer%22%3A%20%22https%3A%2F%2Fwww.olx.ua%2Faccount%2F%3Fref%255B0%255D%255Baction%255D%3Dmyaccount%26ref%255B0%255D%255Bmethod%255D%3Dindex%22%2C%22%24initial_referring_domain%22%3A%20%22www.olx.ua%22%7D; pvps=1; _gat_UA-23987051-2=1; onap=160dfcd245fxceeda63-1241-166cb334204x5b9cffe3-2-1541009227; _gat_clientNinja=1");

                urlConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

                OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
                httpRequestBodyWriter =
                        new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));

// Include value from the myFileDescription text area in the post data

                httpRequestBodyWriter.write("--" + boundaryString + "\n");
                httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"name\"");

// Include the section to describe the file
                httpRequestBodyWriter.write("\n\n" + sRand + "." + extension);
                httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
                httpRequestBodyWriter.write("Content-Disposition: form-data;"
                        + " name=\"file\";"
                        + " filename=\"" + fileToUpload.getName() + "\""
                        + "\nContent-Type: " + imgType + "\n\n");
                //httpRequestBodyWriter.write(query);
                httpRequestBodyWriter.flush();

// Write the actual file contents
                FileInputStream inputStreamToLogFile = new FileInputStream(fileToUpload);

                int bytesRead;
                byte[] dataBuffer = new byte[1024];
                while ((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
                    outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
                }

                outputStreamToRequestBody.flush();

// Mark the end of the multipart http request
                httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
                httpRequestBodyWriter.flush();

// Close the streams
                outputStreamToRequestBody.close();
                httpRequestBodyWriter.close();

                int respCode = urlConnection.getResponseCode();
                //Log.d(TAG, "Resp code: " + respCode);


                if (respCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        html += inputLine;
                    }
                    in.close();
                    //result = html;
                    JSONObject dataJsonObjUp = null;
                    dataJsonObjUp = new JSONObject(html);
                    result = "{\"apollo_id\":"+dataJsonObjUp.getString("apollo_id")+",\"status\":\""+dataJsonObjUp.getString("status")+"\",\"riak_key\":\""+dataJsonObjUp.getString("riak_key")+"\",\"slot\":"+dataJsonObjUp.getString("slot")+",\"url_thumb\":\""+dataJsonObjUp.getString("url_thumb")+"\",\"baseUriPath\":\""+photoUri+"\"}";

                    Log.d(TAG, "Ответ от OLX Images " + result);
                    JSONObject uploadDataJsonObj = null;
                    uploadDataJsonObj = new JSONObject(result);
                    //boolean errorExist = uploadDataJsonObj.getString("error").equals("0");

                    if(uploadDataJsonObj.getString("status").equals("success")){
                        Log.d(TAG, "Utilities.java(uploadPhoto).java - фото успешно загружено  и доступно по адресу: " + uploadDataJsonObj.getString("url_thumb"));
                        return result;
                    }
                } else {
                    Log.e(TAG, "Utilities.java(uploadPhoto).java - При загрузке страницы ошибка. Ответ сервера: " + String.valueOf(respCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Utilities.java(uploadPhoto).java - Исключение во время загрузки фото " + "https://www.olx.ua/ajax/upload/upload/?riak_key="+riak_key+"&ad_id=&preview=&category=0");
            }
        }

        return result;
    }

    public static String uploadFromEditPagePhoto(String photoUri, String _riak_key, Context context, String _originalAdId, String _adCategory, String _referer, String _cookies){
        String result = "";
        String riak_key = "";
        Context ctxt = context;
        if(_riak_key.length()>0) riak_key = _riak_key;

        for(int i=0; i<5;i++) {
            Log.d(TAG, "Utilities.java(uploadPhoto) - " + (i+1) + " Попытка заргрузить фото " + photoUri);
            String html = "";
            String data = "";
            String image;
            try {
                String fileUrl = Utilities.getRealPathFromURI(ctxt, Uri.parse(photoUri));
                URL serverUrl = new URL("https://www.olx.ua/ajax/upload/upload/?riak_key="+riak_key+"&ad_id="+_originalAdId+"&preview=&category=" + _adCategory);

                Log.d(TAG, "Запрос на загрузку фото: " + "https://www.olx.ua/ajax/upload/upload/?riak_key="+riak_key+"&ad_id="+_originalAdId+"&preview=&category=" + _adCategory);
                Log.d(TAG, "Запрос на загрузку фото _referer: " + _referer);
                Log.d(TAG, "Запрос на загрузку фото cookies: " + _cookies);

                //Log.d(TAG, "fileUrl " + fileUrl);
            /*Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("riak_key", "")
                    .appendQueryParameter("ad_id", "")
                    .appendQueryParameter("preview", "")
                    .appendQueryParameter("category", "0");
            String query = builder.build().getEncodedQuery();*/

                HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();
                String sRand = UUID.randomUUID().toString();
                String boundaryString = "----WebKitFormBoundary" + sRand;
                File fileToUpload = new File(fileUrl);

                String imgType = null;
                String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
                imgType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                //Log.d(TAG, " img type " + imgType);

                //TimeUnit.SECONDS.sleep(25);

// Indicate that we want to write to the HTTP request body
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                urlConnection.setRequestProperty("Host", "www.olx.ua");
                urlConnection.setRequestProperty("Connection", "keep-alive");
                urlConnection.setRequestProperty("Origin", "https://www.olx.ua");
                //urlConnection.setRequestProperty("Content-Type", "multipart/form-data");
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
                urlConnection.setRequestProperty("Accept", "*/*");
                urlConnection.setRequestProperty("Referer", _referer);
                urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
                urlConnection.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                urlConnection.setRequestProperty("Cookie", _cookies);

                urlConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

                OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
                httpRequestBodyWriter =
                        new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));

// Include value from the myFileDescription text area in the post data

                httpRequestBodyWriter.write("--" + boundaryString + "\n");
                httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"name\"");

// Include the section to describe the file
                httpRequestBodyWriter.write("\n\n" + sRand + "." + extension);
                httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
                httpRequestBodyWriter.write("Content-Disposition: form-data;"
                        + " name=\"file\";"
                        + " filename=\"" + fileToUpload.getName() + "\""
                        + "\nContent-Type: " + imgType + "\n\n");
                //httpRequestBodyWriter.write(query);
                httpRequestBodyWriter.flush();

// Write the actual file contents
                FileInputStream inputStreamToLogFile = new FileInputStream(fileToUpload);

                int bytesRead;
                byte[] dataBuffer = new byte[1024];
                while ((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
                    outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
                }

                outputStreamToRequestBody.flush();

// Mark the end of the multipart http request
                httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
                httpRequestBodyWriter.flush();

// Close the streams
                outputStreamToRequestBody.close();
                httpRequestBodyWriter.close();

                int respCode = urlConnection.getResponseCode();
                //Log.d(TAG, "Resp code: " + respCode);


                if (respCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        html += inputLine;
                    }
                    in.close();
                    //result = html;
                    JSONObject dataJsonObjUp = null;
                    Log.d(TAG, "JSON LOADED PHOTO " + html);
                    dataJsonObjUp = new JSONObject(html);
                    result = "{\"apollo_id\":\""+dataJsonObjUp.optString("apollo_id", "")+"\",\"status\":\""+dataJsonObjUp.getString("status")+"\",\"riak_key\":\""+dataJsonObjUp.getString("riak_key")+"\",\"slot\":\""+dataJsonObjUp.getString("slot")+"\",\"url_thumb\":\""+dataJsonObjUp.getString("url_thumb")+"\",\"baseUriPath\":\""+photoUri+"\"}";

                    Log.d(TAG, "Ответ от OLX Images " + result);
                    JSONObject uploadDataJsonObj = null;
                    uploadDataJsonObj = new JSONObject(result);
                    //boolean errorExist = uploadDataJsonObj.getString("error").equals("0");

                    if(uploadDataJsonObj.getString("status").equals("success")){
                        Log.d(TAG, "Utilities.java(uploadPhoto).java - фото успешно загружено  и доступно по адресу: " + uploadDataJsonObj.getString("url_thumb"));
                        return result;
                    }
                } else {
                    Log.e(TAG, "Utilities.java(uploadPhoto).java - При загрузке страницы ошибка. Ответ сервера: " + String.valueOf(respCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Utilities.java(uploadPhoto).java - Исключение во время загрузки фото " + "https://www.olx.ua/ajax/upload/upload/?riak_key="+riak_key+"&ad_id="+_originalAdId+"&preview=&category=" + _adCategory + ": " + e.getMessage());
            }
        }

        return result;
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Ошибка при создании MD5 хеша " + e.getMessage());
        }
        return "";
    }
}
