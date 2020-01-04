package ua.com.anyapps.easyads.easyads;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import ua.com.anyapps.easyads.easyads.Messages.ChatMessage;
import ua.com.anyapps.easyads.easyads.Messages.SenderInfo;

class City{
    String _id;
    String city;
    String id;
    String district;
    public City(String _id, String city, String id, String district){
        this._id = _id;
        this.city = city;
        this.id = id;
        this.district = district;
    }
}
public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "easyads.db"; // имя БД
    public static final int DB_VERSION = 1; // версия БД
    Context context;
    private static final String TAG = "debapp";


    public static String DB_PATH = "";


    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;

        //ContextWrapper cw = new ContextWrapper(context);
        DB_PATH = "/data/data/" + context.getPackageName() + "/databases/"; // cw.getFilesDir().getAbsolutePath()+ "/databases/";

        if(!dbExist()) {
            copyDBFromAssets();
        }

        database = getDatabase();

        //Log.d(TAG, "DB PATH: " + DB_PATH);
    }

    SQLiteDatabase database = null;

    public SQLiteDatabase getDatabase(){

            return SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
                    SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);

    }

    public String getCityByIdAndDistrict(String cityId, String cityDistrict){
        String result = "";

        Cursor cursor = database.rawQuery("select * from cities where id = \"" + cityId + "\" and district = \""+cityDistrict+"\"",null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex("city"));
        }
        cursor.close();

        return result;
    }

    public String getCityMapZoom(String cityId, String cityDistrict){
        String result = "";

        Cursor cursor = database.rawQuery("select * from cities where id = \"" + cityId + "\" and district = \""+cityDistrict+"\"",null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex("zoom"));
        }
        cursor.close();

        return result;
    }

    public String getCityLon(String cityId, String cityDistrict){
        String result = "";

        Cursor cursor = database.rawQuery("select * from cities where id = \"" + cityId + "\" and district = \""+cityDistrict+"\"",null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex("lon"));
        }
        cursor.close();

        return result;
    }

    public String getCityLat(String cityId, String cityDistrict){
        String result = "";

        Cursor cursor = database.rawQuery("select * from cities where id = \"" + cityId + "\" and district = \""+cityDistrict+"\"",null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex("lat"));
        }
        cursor.close();

        return result;
    }


    public String getCityId(String cityName){
        String result = "";

        Cursor cursor = database.rawQuery("select * from cities where citylower = \"" + cityName.toLowerCase() + "\"",null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex("id"));
        }
        cursor.close();

        return result;
    }

    public String getCityDistrict(String cityName){
        String result = "";

        Cursor cursor = database.rawQuery("select * from cities where citylower = \"" + cityName.toLowerCase() + "\"",null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex("district"));
        }
        cursor.close();

        return result;
    }

    public ArrayList<City> autosuggest(String partOfCity){
        ArrayList<City> geoAutosuggest = new ArrayList<>();
        Cursor cursor = database.rawQuery("select * from cities where citylower like \"%"+partOfCity+"%\"",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());
        while(cursor.moveToNext()) {
            String column1 = cursor.getString(cursor.getColumnIndex("_id"));
            String column2 = cursor.getString(cursor.getColumnIndex("city"));
            String column3 = cursor.getString(cursor.getColumnIndex("id"));
            String column4 = cursor.getString(cursor.getColumnIndex("district"));
            City city = new City(column1, column2, column3, column4);
            geoAutosuggest.add(city);
        }
        //Log.d(TAG, "Размер arraylist: " + geoAutosuggest.size());

        return geoAutosuggest;
    }

    public boolean saveCookies(String _cookies, String _email, String _targetAccountID){ //if(true)return false;//!!!!!!!!!!!!!!!!!!!!!!!!!
        boolean result = false;

        //Cursor cursor = database.rawQuery("select * from cities where citylower like \"%"+partOfCity+"%\"",null);

        Log.d(TAG, "UPDATE accounts SET cookies="+_cookies+", cookieupdate=strftime('%Y-%m-%d %H:%M:%S','now') WHERE _id="+_targetAccountID);

            String updateQuery = "UPDATE accounts SET cookies='"+_cookies+"', cookieupdate=strftime('%Y-%m-%d %H:%M:%S','now') WHERE _id="+_targetAccountID;
            Cursor c = database.rawQuery(updateQuery, null);

            c.moveToFirst();
            c.close();


        return result;
    }

    public void deactivateAccount(String _accId){
        String updateQuery = "UPDATE accounts SET active='0' WHERE _id="+_accId;
        Cursor c = database.rawQuery(updateQuery, null);

        c.moveToFirst();
        c.close();
    }

    public void updateAdJson(String _adjson, String _adid){
        Log.d(TAG, "Обновление adid " + _adid + "  adjson " + _adjson);
        String updateQuery = "UPDATE ads SET adjson='" + _adjson + "' WHERE adid='" + _adid + "'";
        Cursor c = database.rawQuery(updateQuery, null);

        c.moveToFirst();
        c.close();
    }

    public String getAccountCookies(String _accountId){
        String result = "";

        Cursor cursor = database.rawQuery("select * from accounts where _id = \"" + _accountId + "\"",null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex("cookies"));
        }
        cursor.close();

        return result;
    }

    // получает ID аккаунта на сервере
    public String getServerAccountId(String _appAccountId){
        String id = "";
        //
        Cursor cursor = database.rawQuery("select * from accounts where _id = \"" + _appAccountId + "\"",null);
        if (cursor.moveToFirst()) {
            id = cursor.getString(cursor.getColumnIndex("id"));
        }
        cursor.close();
        return id;
    }

    /*public String dsfsdfsdf(){
        String result = "";
        String photo="";
        Cursor cursor = database.rawQuery("select * from ads where _id='1'",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());
        while(cursor.moveToNext()) {
            try {
                JSONArray jsonArr = new JSONArray(cursor.getString(cursor.getColumnIndex("adjson")));
                for (int k = 0; k < jsonArr.length(); k++) {
                    JSONObject jsonObj = jsonArr.getJSONObject(k);
                    String key = (String)jsonObj.get("key");
                    switch(key){
                        case "photos":{
                            JSONArray photosArray = jsonObj.getJSONArray("value");
                            for (int s=0; s < photosArray.length(); s++) {
                                //JSONObject jo = photosArray.getJSONObject(s);

                                //JSONArray  photoInfoArray = jo.getJSONArray("photo");
                                //Log.d(TAG, "Всего инфы: " + photoInfoArray.length());
                                //Log.d(TAG, "Всего инфы: " + jo.get("photo"));

                                //JSONObject photoParams = new JSONObject(jo.get("photo").toString());
                                //Log.d(TAG, "PPPP: " + photoParams.get("uriString"));
                                //PhotoGridViewItem photo = new PhotoGridViewItem();
                                //photo.photo = Uri.parse(photoParams.get("uriString").toString());

                                //Log.d(TAG, "zzzzzzz"+photosArray.getString(s));
                                if(!photosArray.getString(s).equals("android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo)){
                                    photo = "android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo;
                                    break;
                                }
                                Log.d(TAG, photosArray.getString(s));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            //Log.d(TAG, cursor.getString(cursor.getColumnIndex("title")));
            result = "{\"adtitle\":\""+cursor.getString(cursor.getColumnIndex("title"))+"\", \"photo\":\""+photo+"\"}";
        }
        Log.d(TAG, result);
        return result;
    }*/

    public String getReferer(String _idob, String _originalOlxAdId){
        String accountId = "";
        String referer = "";
        String result = "";
        accountId = this.getAccountIdByAdId(_idob);

        Cursor cursor = database.rawQuery("select * from accounts where _id = \"" + accountId + "\"",null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex("adstatus"));
        }
        cursor.close();
        switch (result){
            case "pending":
                //result = "waiting";
                referer = "https://www.olx.ua/post-new-ad/edit/"+_originalOlxAdId+"/?bs=myaccount_edit&ref%5B0%5D%5Bpath%5D%5B0%5D=waiting&ref%5B0%5D%5Baction%5D=myaccount&ref%5B0%5D%5Bmethod%5D=index";
                break;
            case "active":
                referer = "https://www.olx.ua/post-new-ad/edit/"+_originalOlxAdId+"/?bs=myaccount_edit&ref%5B0%5D%5Baction%5D=myaccount&ref%5B0%5D%5Bmethod%5D=index";
                break;
            case "inactive":
                //result = "archive";
                referer = "https://www.olx.ua/post-new-ad/edit/"+_originalOlxAdId+"/?bs=myaccount_edit&ref%5B0%5D%5Bpath%5D%5B0%5D=archive&ref%5B0%5D%5Baction%5D=myaccount&ref%5B0%5D%5Bmethod%5D=index";
                break;
            case "default":
                referer = "https://www.olx.ua/post-new-ad/edit/"+_originalOlxAdId+"/?bs=myaccount_edit&ref%5B0%5D%5Bpath%5D%5B0%5D=archive&ref%5B0%5D%5Baction%5D=myaccount&ref%5B0%5D%5Bmethod%5D=index";
                break;
            default:
        }
        return referer;
        //
    }
    public void updateAdStatusFromService(String _accountId, String _status){
        String result = "";
        String photo = "";
        Log.d(TAG, "UPDATE ID " + _accountId + " STATUS " + _status );
        String updateQuery = "UPDATE accounts SET adstatus='"+_status+"' WHERE _id="+_accountId;
        Cursor c = database.rawQuery(updateQuery, null);
        c.moveToFirst();
        c.close();
    }

    public String updateAdStatus(String _accountId, String _status){
        String result = "";
        String photo = "";
        String updateQuery = "UPDATE accounts SET adstatus='"+_status+"' WHERE _id="+_accountId;
        Cursor c = database.rawQuery(updateQuery, null);
        c.moveToFirst();
        c.close();

        //Cursor cursor = database.rawQuery("select * from ads where _id='"+_accountId+"'",null);
        Cursor cursor = database.rawQuery("select * from ads where activeinaccount='"+_accountId+"'",null);
        while(cursor.moveToNext()) {
            try {
                JSONArray jsonArr = new JSONArray(cursor.getString(cursor.getColumnIndex("adjson")));
                for (int k = 0; k < jsonArr.length(); k++) {
                    JSONObject jsonObj = jsonArr.getJSONObject(k);
                    String key = (String)jsonObj.get("key");
                    switch(key){
                        case "photos":{
                            JSONArray photosArray = new JSONArray((String)jsonObj.get("value")); //jsonObj.getJSONArray("value");
                            for (int s=0; s < photosArray.length(); s++) {
                                JSONObject adPhotoClassObj = new JSONObject(photosArray.getString(s));

                                Log.d(TAG, "currentPhoto " + adPhotoClassObj.getString("currentPhoto"));

                                if(!adPhotoClassObj.getString("currentPhoto").equals("android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo)){
                                //if(!photosArray.getString(s).equals("android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo)){
                                    //photo = "android.resource://"+context.getPackageName()+"/" + R.drawable.default_add_photo;
                                    photo = adPhotoClassObj.getString("currentPhoto");
                                    break;
                                }
                                //Log.d(TAG, photosArray.getString(s));
                            }
                            if(photo.length()<=0){
                                photo = "";
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "DBHelper.java (updateAdStatus())"+ e.getMessage());
            }
            Log.d(TAG, "adtitle " + cursor.getString(cursor.getColumnIndex("title")));
            Log.d(TAG, "photo " + photo);
            Log.d(TAG, "_id " + cursor.getString(cursor.getColumnIndex("_id")));

            result = "{\"adtitle\":\""+cursor.getString(cursor.getColumnIndex("title"))+"\", \"photo\":\""+photo+"\", \"currentadid\":\""+cursor.getString(cursor.getColumnIndex("_id"))+"\"}";
        }

        return result;
    }

    public Boolean insertNewAd(String _adid, String _adtitle, String _adjson, String _ownerId, String _targetAccountID, String _originalOlxAdId, String _riak_key){
        int updCount = -1;
        database.beginTransaction();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = new Date();

            ContentValues insertValues = new ContentValues();
            insertValues.put("adid", _adid);
            insertValues.put("title", _adtitle);
            insertValues.put("adjson", _adjson);
            insertValues.put("ownerid", _ownerId);
            insertValues.put("activeinaccount", _targetAccountID);
            insertValues.put("originalolxid", _originalOlxAdId);
            insertValues.put("createdate", dateFormat.format(date));
            insertValues.put("riakkey", _riak_key);
            long newRowId = database.insert("ads", null, insertValues);

            if(newRowId>=0){
                ContentValues cv = new ContentValues();
                cv.put("currentadid", newRowId);
                updCount = database.update("accounts", cv, "_id = ?", new String[] { _targetAccountID });
                /*String updateQuery = "UPDATE accounts SET currentadid='"+newRowId+"' WHERE _id="+_targetAccountID;
                Cursor c = database.rawQuery(updateQuery, null);

                c.moveToFirst();
                c.close();*/
                if(updCount<=0)throw new AdOrAccountNotUpdated("Ошибка аккаунт не обновился.");
                database.setTransactionSuccessful();
            }else{
                throw new AdOrAccountNotUpdated("Ошибка объявление не добавилось.");
            }
        }catch (Exception ex){
            Log.d(TAG, "DBHelper.java " + ex.getMessage());
        }finally {
            database.endTransaction();
        }
        if(updCount<=0){return false;}else{return true;}
    }

    public boolean addAccount(String _accountid, String _email, String _password, String _ownerid, String _getdate, String _ending){
        boolean result = false;

        String insertQuery = "INSERT INTO accounts (id, email, password, ownerid, adstatus, active, getdate, ending) VALUES (\""+_accountid+"\", \""+_email+"\", \""+_password+"\", \""+_ownerid+"\" ,\"default\", 1, \""+_getdate+"\",\""+_ending+"\");";
        Cursor c = database.rawQuery(insertQuery, null);

        c.moveToFirst();
        c.close();

        return result;
    }

    public ArrayList < OlxAccount > getUserAccounts(String _getdate, String _currentUserId){
        ArrayList < OlxAccount > objOlxAccounts = new ArrayList < OlxAccount > ();
//прочитать узер айди из найстроек и вывести аки этого пользователя
        //Cursor cursor = database.rawQuery("select * from accounts where strftime('%Y-%m-%d %H:%M:%S', ending) BETWEEN strftime('%Y-%m-%d %H:%M:%S', '"+_getdate+"') AND strftime('%Y-%m-%d %H:%M:%S', '"+_ending+"')",null);
        Cursor cursor = database.rawQuery("select * from accounts where strftime('%Y-%m-%d %H:%M:%S', ending) > strftime('%Y-%m-%d %H:%M:%S', '"+_getdate+"') AND ownerid='"+_currentUserId+"' AND active=1",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());
        while(cursor.moveToNext()) {
            String photo = "";
            /*String column1 = cursor.getString(cursor.getColumnIndex("_id"));
            String column2 = cursor.getString(cursor.getColumnIndex("city"));
            String column3 = cursor.getString(cursor.getColumnIndex("id"));
            String column4 = cursor.getString(cursor.getColumnIndex("district"));
            City city = new City(column1, column2, column3, column4);
            geoAutosuggest.add(city);*/

            OlxAccount oneOlxAc = new OlxAccount();
            oneOlxAc.accountid = cursor.getString(cursor.getColumnIndex("_id"));
            oneOlxAc.email = cursor.getString(cursor.getColumnIndex("email"));
            oneOlxAc.password = cursor.getString(cursor.getColumnIndex("password"));
            oneOlxAc.adstatus = cursor.getString(cursor.getColumnIndex("adstatus"));
            oneOlxAc.currentadid = cursor.getString(cursor.getColumnIndex("currentadid"));
            oneOlxAc.getdate = cursor.getString(cursor.getColumnIndex("getdate"));
            oneOlxAc.ending = cursor.getString(cursor.getColumnIndex("ending"));
            oneOlxAc.active = cursor.getString(cursor.getColumnIndex("active"));

            if(cursor.getString(cursor.getColumnIndex("currentadid"))!=null && cursor.getString(cursor.getColumnIndex("currentadid")).length()>0) {
                Log.d(TAG, "select * from ads where _id=" + cursor.getString(cursor.getColumnIndex("currentadid")) + "");
                Cursor cursor2 = database.rawQuery("select * from ads where _id=" + cursor.getString(cursor.getColumnIndex("currentadid")) + "", null);
                while (cursor2.moveToNext()) {
                    try {
                        //oneOlxAc.currentadtitle = cursor2.getString(cursor2.getColumnIndex("title"));

                        JSONArray jsonArr = new JSONArray(cursor2.getString(cursor2.getColumnIndex("adjson")));
                        for (int k = 0; k < jsonArr.length(); k++) {
                            JSONObject jsonObj = jsonArr.getJSONObject(k);
                            String key = (String) jsonObj.get("key");
                            switch (key) {
                                case "photos": {
                                    JSONArray photosArray = new JSONArray((String) jsonObj.get("value")); // jsonObj.getJSONArray("value");
                                    for (int s = 0; s < photosArray.length(); s++) {
                                        JSONObject adPhotoClassObj = new JSONObject(photosArray.getString(s));

                                        if (!adPhotoClassObj.getString("currentPhoto").equals("android.resource://" + context.getPackageName() + "/" + R.drawable.default_add_photo)) {
                                            //oneOlxAc.currentadphoto = "android.resource://" + context.getPackageName() + "/" + R.drawable.default_add_photo;
                                            oneOlxAc.currentadphoto = adPhotoClassObj.getString("currentPhoto");
                                            break;
                                        }
                                        //Log.d(TAG, photosArray.getString(s));
                                    }
                                    break;
                                }
                                case "data[title]":{
                                    oneOlxAc.currentadtitle = (String)jsonObj.get("value");
                                    //Log.d(TAG, "fffffffffffffff"+(String)jsonObj.get("value"));
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "DBHelper.java getUserAccounts()" + e.getMessage());
                    }
                    //result = "{\"adtitle\":\""+cursor.getString(cursor.getColumnIndex("title"))+"\", \"photo\":\""+photo+"\"}";
                }
            }

            objOlxAccounts.add(oneOlxAc);
        }

        return objOlxAccounts;
    }

    public OlxAccount getAccount(String _id){
        OlxAccount oneOlxAc = new OlxAccount();

        Cursor cursor = database.rawQuery("select * from accounts where _id='"+_id+"' AND active=1",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());
        while(cursor.moveToNext()) {
            /*String column1 = cursor.getString(cursor.getColumnIndex("_id"));
            String column2 = cursor.getString(cursor.getColumnIndex("city"));
            String column3 = cursor.getString(cursor.getColumnIndex("id"));
            String column4 = cursor.getString(cursor.getColumnIndex("district"));
            City city = new City(column1, column2, column3, column4);
            geoAutosuggest.add(city);*/
            oneOlxAc.accountid = cursor.getString(cursor.getColumnIndex("_id"));
            oneOlxAc.email = cursor.getString(cursor.getColumnIndex("email"));
            oneOlxAc.password = cursor.getString(cursor.getColumnIndex("password"));
            //oneOlxAc.currentuser = objAccount.getString("currentuser");
            oneOlxAc.currentadid = cursor.getString(cursor.getColumnIndex("currentadid"));;
            oneOlxAc.getdate = cursor.getString(cursor.getColumnIndex("getdate"));
            oneOlxAc.ending = cursor.getString(cursor.getColumnIndex("ending"));
        }

        return oneOlxAc;
    }

    public String getAccountIdByAdId(String _adId){
        String result = "";

        Cursor cursor = database.rawQuery("select * from ads where adid = \"" + _adId + "\"",null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex("activeinaccount"));
        }
        cursor.close();

        return result;
    }

    public String getAd(String _adId){
        String result = "";

        Cursor cursor = database.rawQuery("select * from ads where _id='"+_adId+"'",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());
        while(cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("adjson"));
        }

        return result;
    }

    public String getOriginalOlxAdId(String _adId){
        String result = "";

        Cursor cursor = database.rawQuery("select * from ads where _id='"+_adId+"' OR adid='"+_adId+"'",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());
        while(cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("originalolxid"));
        }

        return result;
    }

    public String getRiakKey(String _adId){
        String result = "";

        Cursor cursor = database.rawQuery("select * from ads where _id='"+_adId+"' OR adid='"+_adId+"'",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());

        while(cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("riakkey"));
        }

        return result;
    }

    public Boolean adNewMessage(String _sender, String _message, String _originalmessageid, String _originaladid, String _uniqueChat, String _account, String _user){

        Boolean result = false;
        String sender = "";
        String message = "";
        String originaladid = "";
        String originalmessageid = ""; // зашифрованное сообщение, айди аккаунта, айди номера аккаунта, имя отправителя
        String viewed = "";
        String createdate= "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();

        try { //вставка, если сообщение относится к объявлению
            String insertQuery = "INSERT INTO messages (originaladid, sender, message, viewed, originalmessageid, createdate, uniquechat, account, user) VALUES (\"" + _originaladid + "\", \"" + _sender + "\", \"" + _message + "\", \"0\" ,\"" + _originalmessageid + "\", \"" + dateFormat.format(date) + "\", \""+_uniqueChat+"\", \""+_account+"\", \""+_user+"\");";
            Cursor c = database.rawQuery(insertQuery, null);
            c.moveToFirst();
            c.close();
            Log.d(TAG, "NEW MESSAGE");
            result = true;
        }catch (Exception ex){
            Log.d(TAG, "Ошибка при вставке нового сообщения в базу: " +  ex.getMessage());
            result = false;
        }

        return result;
        //
    }

    //public ArrayList <ChatMessage> getSenderMessages(String _currentadid, String _messageid, String uniquechat, String _user, String _account){
    public ArrayList <ChatMessage> getSenderMessages(String uniquechat){
        ArrayList < ChatMessage > chatMessages = new ArrayList < ChatMessage > ();
//прочитать узер айди из найстроек и вывести аки этого пользователя
        //Cursor cursor = database.rawQuery("select * from accounts where strftime('%Y-%m-%d %H:%M:%S', ending) BETWEEN strftime('%Y-%m-%d %H:%M:%S', '"+_getdate+"') AND strftime('%Y-%m-%d %H:%M:%S', '"+_ending+"')",null);
        Cursor cursor = database.rawQuery("select * from messages where uniquechat='"+uniquechat+"' order by originalmessageid asc",null);
        //фигня какая-то с сортировкой
                //попробовать с сортировкой originalmessageid
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());
        while(cursor.moveToNext()) {
            ChatMessage oneMessage = new ChatMessage();
            oneMessage.sender = cursor.getString(cursor.getColumnIndex("sender"));
            oneMessage.message = cursor.getString(cursor.getColumnIndex("message"));
            oneMessage.createdate = cursor.getString(cursor.getColumnIndex("createdate"));
            oneMessage.originalAdId = cursor.getString(cursor.getColumnIndex("originaladid"));
            oneMessage.viewed = cursor.getString(cursor.getColumnIndex("viewed"));
            oneMessage.originalmessageid = cursor.getString(cursor.getColumnIndex("originalmessageid"));
            oneMessage.account = cursor.getString(cursor.getColumnIndex("account"));
            oneMessage.user= cursor.getString(cursor.getColumnIndex("user"));

            chatMessages.add(oneMessage);
        }

        return chatMessages;
    }

    public ArrayList <SenderInfo> getSenders(String _getdate, String _currentUserId){
        ArrayList < SenderInfo > senders = new ArrayList < SenderInfo > ();

        Cursor cursor = database.rawQuery("select * from accounts where strftime('%Y-%m-%d %H:%M:%S', ending) > strftime('%Y-%m-%d %H:%M:%S', '"+_getdate+"') AND ownerid='"+_currentUserId+"' AND active=1",null);

        //cursor.getString(cursor.getColumnIndex("currentadid"))
        ArrayList<String> uniqueChats = new ArrayList<>();


        while(cursor.moveToNext()) {
            Log.d(TAG, "ACC " + cursor.getString(cursor.getColumnIndex("_id")));
            Cursor cursor2 = database.rawQuery("select * from messages where account='"+cursor.getString(cursor.getColumnIndex("_id"))+"' order by originalmessageid asc",null);

            while(cursor2.moveToNext()) {
                if(cursor2.getString(cursor2.getColumnIndex("sender")).equals("Ваше сообщение")) continue;
                uniqueChats.add(cursor2.getString(cursor2.getColumnIndex("uniquechat")));
            }
            cursor2.close();
        }
        // senderNames - уникальные имена отправителей
        Set<String> set = new HashSet<>(uniqueChats);
        uniqueChats.clear();
        uniqueChats.addAll(set);
        Log.d(TAG, "Chats " + uniqueChats.size());


        for(int b=0; b<uniqueChats.size(); b++){
            String sender="";
            String adTitle="";
            String lastMessage="";
            String date="";
            String viewed="";
            String uniqueChat = "";
            Cursor cursor2 = database.rawQuery("select * from messages where uniquechat='"+uniqueChats.get(b)+"' order by originalmessageid asc",null);
            while(cursor2.moveToNext()) {
                if(cursor2.getString(cursor2.getColumnIndex("sender")).equals("Ваше сообщение")) continue;
                Log.d(TAG, cursor2.getString(cursor2.getColumnIndex("message")));

                sender = cursor2.getString(cursor2.getColumnIndex("sender"));
                adTitle = cursor2.getString(cursor2.getColumnIndex("originaladid"));
                lastMessage = cursor2.getString(cursor2.getColumnIndex("message"));
                date = cursor2.getString(cursor2.getColumnIndex("createdate"));
                viewed = cursor2.getString(cursor2.getColumnIndex("viewed"));
                uniqueChat = cursor2.getString(cursor2.getColumnIndex("uniquechat"));
            }

            SenderInfo sInfo = new SenderInfo();
            sInfo.adTitle = adTitle;
            sInfo.sender = sender;
            sInfo.lastMessage = lastMessage;
            sInfo.date = date;
            sInfo.viewed = viewed;
            sInfo.uniqueChat = uniqueChat;
            senders.add(sInfo);


            Log.d(TAG, "+++++++++++++++++++++++++++++++++++++");
            //
            cursor2.close();
        }

        /*String sender="";
        String adTitle="";
        String lastMessage="";
        String date="";*/

        /*sender = cursor2.getString(cursor2.getColumnIndex("sender"));
                adTitle = cursor2.getString(cursor2.getColumnIndex("originaladid"));
                lastMessage = cursor2.getString(cursor2.getColumnIndex("message"));
                date = cursor2.getString(cursor2.getColumnIndex("createdate"));
                Log.d(TAG, "MESSAGE " +cursor2.getString(cursor2.getColumnIndex("message")));*/

        //

        return senders;
    }

    public String getAdTitleFromOriginalId(String _originalId){
        String result = "";

        Cursor cursor = database.rawQuery("select * from ads where originalolxid='"+_originalId+"'",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());

        while(cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("title"));
        }

        return result;
    }

    public void setViewedMessage(String _uniqueChat){
        String updateQuery = "UPDATE messages SET viewed='1' WHERE uniquechat='"+_uniqueChat+"'";
        Cursor c = database.rawQuery(updateQuery, null);

        c.moveToFirst();
        c.close();
    }

    public String getAccountIdFromMessage(String _uniqueChat){
        String result = "";

        Cursor cursor = database.rawQuery("select * from messages where uniquechat='"+_uniqueChat+"'",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());

        while(cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("account"));
            break;
        }

        return result;
    }

    public String getCurrentAdIdFromMessage(String _uniqueChat){
        String result = "";

        Cursor cursor = database.rawQuery("select * from messages where uniquechat='"+_uniqueChat+"'",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());

        while(cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("originaladid"));
            break;
        }

        return result;
    }

    public String getOwnerIdFromMessage(String _uniqueChat){
        String result = "";

        Cursor cursor = database.rawQuery("select * from messages where uniquechat='"+_uniqueChat+"'",null);
        //Log.d(TAG, "Всего полходящих населенных пунктов: " + res.getCount());

        //Cursor cursor = database.query("cities", new String[] {"_id","city"},"city LIKE '?'", new String[]{"%" + partOfCity + "%"}, null, null, null);
        //Log.d(TAG, "Всего подходящих населенных пунктов: " + cursor.getCount());

        while(cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("user"));
            break;
        }

        return result;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        /* // Строка для создания таблицы
        String SQL_CITIES_TABLE = "CREATE TABLE `cities` (\n" +
                "  `_id` int(11) NOT NULL,\n" +
                "  `city` varchar(255) DEFAULT NULL,\n" +
                "  `id` text,\n" +
                "  `district` tinytext,\n" +
                "  PRIMARY KEY (`_id`)\n" +
                ")";

        // Запускаем создание таблицы
        db.execSQL(SQL_CITIES_TABLE);*/



        /*    int result = 0;
            // Iterate through lines (assuming each insert has its own line and theres no other stuff)
            while (insertReader.ready()) {
                String insertStmt = insertReader.readLine();
                db.execSQL(insertStmt);
                result++;
                Log.d(TAG, "LINE: " + result);
            }
            is.close();
            */






    }

    private void copyDBFromAssets(){
        if(!dbExist()) {

            File f = new File(DB_PATH);

            File directory = new File(DB_PATH);
            if (! directory.exists()){
                directory.mkdir();
            }

            //Log.i(TAG, "New database is being copied to device!");
            byte[] buffer = new byte[1024];
            OutputStream myOutput = null;
            int length;
            // Open your local db as the input stream
            InputStream myInput = null;
            try {
                myInput = context.getAssets().open(DB_NAME);
                // transfer bytes from the inputfile to the
                // outputfile
                myOutput = new FileOutputStream(DB_PATH + DB_NAME);
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.close();
                myOutput.flush();
                myInput.close();
                //Log.i(TAG,                        "New database has been copied to device!");


            } catch (IOException e) {
                e.printStackTrace();
                //Log.d(TAG, "Ошибка при копировании БД: " + e.getMessage());
            }
        }else{
            //Log.d(TAG, "Базу данных уже существует");
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private boolean dbExist(){
        SQLiteDatabase dbExist = null;
        try {
            dbExist = SQLiteDatabase.openDatabase(DB_PATH+ DB_NAME, null,
                    SQLiteDatabase.OPEN_READONLY);
            dbExist.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet.
        }
        return dbExist != null;
    }
}
class AdOrAccountNotUpdated extends Exception {
    AdOrAccountNotUpdated(String msg) {
        super(msg);
    }
}