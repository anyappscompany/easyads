package ua.com.anyapps.easyads.easyads.CreateNewAd;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ua.com.anyapps.easyads.easyads.OlxCategory;
import ua.com.anyapps.easyads.easyads.R;
import ua.com.anyapps.easyads.easyads.SelectCategoriesAdapterLv1;
import ua.com.anyapps.easyads.easyads.SelectCategoriesAdapterLv2;
import ua.com.anyapps.easyads.easyads.SelectCategoriesAdapterLv3;
import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskLoadCategoriesForEdit extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";
    private String result="";
    private Context ctxt;
    private LoadCategoriesForEditCompleted taskCompleted;
    Handler mHandler = new Handler();

    public AsyncTaskLoadCategoriesForEdit (LoadCategoriesForEditCompleted context, Context _context){
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.LoadCategoriesForEditCompleted(result);
        super.onPostExecute(aVoid);
    }

    List<String> parrentCats = new ArrayList<>();
    private JSONArray categories=null;
    @Override
    protected Void doInBackground(String... strings) {
        String jsonCategories = "";
        try {
            jsonCategories = Utilities.getStringFromFile("/data/data/" + ctxt.getPackageName() + "/files/" + "categories.json");

            JSONArray catsArray = new JSONArray();
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
            categories = catsArray;

            parrentCats.add(strings[0]);
            fillParrentCats(jsonCategories, strings[0]);

            Collections.reverse(parrentCats);
            for(Integer f=0; f<parrentCats.size();f++){
                //Log.d(TAG, "Cat~" + parrentCats.get(f));
                switch(f){
                    case 0:
                        loadCatsLv1(jsonCategories, parrentCats.get(f));
                        break;
                    case 1:
                        loadCatsLv2(jsonCategories, parrentCats.get(0));
                        break;
                    case 2:
                        loadCatsLv3(jsonCategories, parrentCats.get(1));
                        break;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при заполнении категориями " + e.getMessage());
        }

        return null;
    }



    private Integer selectedIndex;

    SelectCategoriesAdapterLv1 scAdapterLv1;
    Spinner spCategoriesLv1;
    private ArrayList<OlxCategory> spCategoriesLv1List = new ArrayList<OlxCategory>();
    private void loadCatsLv1(String _jsonCategories, String _activeCat){
        selectedIndex = 0;
        //categories = null;

        spCategoriesLv1 = (Spinner)((Activity)ctxt).findViewById(R.id.spCategoriesLv1);
        try {



            spCategoriesLv1List = new ArrayList<OlxCategory>();


            Integer count = 1;
            for (int i = 0; i < categories.length(); i++) {
                JSONObject obj = categories.getJSONObject(i);
                if(obj.getString("level").equals("1")) { // заполнение списка для первого Spinner если уровень категории 1

                    OlxCategory olxCat = new OlxCategory();
                    olxCat.id = obj.getString("id");
                    olxCat.parent_id = obj.getString("parent_id");

                    // создание списка подкатегорий текущей категории
                    ArrayList<String> tmpData = new ArrayList<String>();
                    JSONArray children = obj.getJSONArray("children");

                    if (children != null) {
                        for (int v=0;v<children.length();v++){
                            tmpData.add(children.getString(v));
                        }
                    }
                    olxCat.children = tmpData;

                    if(children.length()>0){
                        olxCat.name = obj.getString("name") + " >";
                    }else{
                        olxCat.name = obj.getString("name");
                    }

                    olxCat.code = obj.getString("code");
                    olxCat.is_job_category = obj.getString("is_job_category");
                    olxCat.offer_seek = obj.getString("offer_seek"); // предлагаю.ищу для рубрики работа
                    olxCat.private_business = obj.getString("private_business");
                    olxCat.level = obj.getString("level");
                    olxCat.private_name = obj.getString("private_name");
                    olxCat.bussiness_name = obj.getString("bussiness_name");
                    olxCat.districts = obj.getString("districts");
                    olxCat.max_photos = obj.getString("max_photos");
                    olxCat.hint_description = obj.getString("hint_description");
                    olxCat.notice = obj.getString("notice");
                    spCategoriesLv1List.add(olxCat);
                    //Log.d(TAG, obj.getString("name"));
                    if(obj.getString("id").equals(_activeCat)) {
                        selectedIndex = count;
                    }
                    count++;
                }
            }
            //Log.d(TAG, "Список категорий получен без ошибок");

            scAdapterLv1 = new SelectCategoriesAdapterLv1(ctxt, spCategoriesLv1List);
            //ArrayAdapter<String> spCategoriesLv1dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spCategoriesLv1ListStr);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    spCategoriesLv1.setAdapter(scAdapterLv1);
                    //Log.d(TAG, "selected index " + selectedIndex);
                    spCategoriesLv1.setSelection(selectedIndex);

                    spCategoriesLv1.setEnabled(false);
                    spCategoriesLv1.setClickable(false);
                }
            });

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            result = gson.toJson(spCategoriesLv1List);

        }
        catch(Exception ex){
            Log.e(TAG, "(AsyncTaskLoadCategoriesLv1.java) Ошибка при загрузке категорий лвл 1 " + ex.getMessage());
        }
    }

    SelectCategoriesAdapterLv2 scAdapterLv2;
    Spinner spCategoriesLv2;
    private ArrayList<OlxCategory> spCategoriesLv2List = new ArrayList<OlxCategory>();
    private void loadCatsLv2(String _jsonCategories, String _parentCat){
        selectedIndex = 0;
        //categories = null;

        spCategoriesLv2 = (Spinner)((Activity)ctxt).findViewById(R.id.spCategoriesLv2);
        try {



            spCategoriesLv2List = new ArrayList<OlxCategory>();

            //categories = catsArray;
            Integer count = 1;

            for (int i = 0; i < categories.length(); i++) {
                JSONObject obj = categories.getJSONObject(i);
                if(obj.getString("level").equals("2") && obj.getString("parent_id").equals(_parentCat)) { // заполнение списка для первого Spinner если уровень категории 1

                    OlxCategory olxCat = new OlxCategory();
                    olxCat.id = obj.getString("id");
                    olxCat.parent_id = obj.getString("parent_id");

                    // создание списка подкатегорий текущей категории
                    ArrayList<String> tmpData = new ArrayList<String>();
                    JSONArray children = obj.getJSONArray("children");

                    if (children != null) {
                        for (int v=0;v<children.length();v++){
                            tmpData.add(children.getString(v));
                        }
                    }
                    olxCat.children = tmpData;

                    if(children.length()>0){
                        olxCat.name = obj.getString("name") + " >";
                    }else{
                        olxCat.name = obj.getString("name");
                    }

                    olxCat.code = obj.getString("code");
                    olxCat.is_job_category = obj.getString("is_job_category");
                    olxCat.offer_seek = obj.getString("offer_seek"); // предлагаю.ищу для рубрики работа
                    olxCat.private_business = obj.getString("private_business");
                    olxCat.level = obj.getString("level");
                    olxCat.private_name = obj.getString("private_name");
                    olxCat.bussiness_name = obj.getString("bussiness_name");
                    olxCat.districts = obj.getString("districts");
                    olxCat.max_photos = obj.getString("max_photos");
                    olxCat.hint_description = obj.getString("hint_description");
                    olxCat.notice = obj.getString("notice");

                    spCategoriesLv2List.add(olxCat);
                    //Log.d(TAG, obj.getString("name"));
                    if(obj.getString("id").equals(_parentCat)) {
                        selectedIndex = count;
                    }
                    count++;
                }
            }
            //Log.d(TAG, "Список категорий получен без ошибок");
//Log.d(TAG, "CCCCCCCCCCCC"+spCategoriesLv2List.size());
            scAdapterLv2 = new SelectCategoriesAdapterLv2(ctxt, spCategoriesLv2List);
            //ArrayAdapter<String> spCategoriesLv1dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spCategoriesLv1ListStr);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    spCategoriesLv2.setAdapter(scAdapterLv2);
                    //Log.d(TAG, "selected index " + selectedIndex);
                    spCategoriesLv2.setSelection(selectedIndex);

                    spCategoriesLv2.setEnabled(false);
                    spCategoriesLv2.setClickable(false);
                }
            });

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            result = gson.toJson(spCategoriesLv2List);

        }
        catch(Exception ex){
            Log.e(TAG, "(AsyncTaskLoadCategoriesLv2.java) Ошибка при загрузке категорий лвл 2 " + ex.getMessage());
        }
    }

    SelectCategoriesAdapterLv3 scAdapterLv3;
    Spinner spCategoriesLv3;
    private ArrayList<OlxCategory> spCategoriesLv3List = new ArrayList<OlxCategory>();
    private void loadCatsLv3(String _jsonCategories, String _parentCat){
        selectedIndex = 0;
        //categories = null;

        spCategoriesLv3 = (Spinner)((Activity)ctxt).findViewById(R.id.spCategoriesLv3);
        try {



            spCategoriesLv3List = new ArrayList<OlxCategory>();

            //categories = catsArray;
            Integer count = 1;

            for (int i = 0; i < categories.length(); i++) {
                JSONObject obj = categories.getJSONObject(i);
                if(obj.getString("level").equals("3") && obj.getString("parent_id").equals(_parentCat)) { // заполнение списка для первого Spinner если уровень категории 1

                    OlxCategory olxCat = new OlxCategory();
                    olxCat.id = obj.getString("id");
                    olxCat.parent_id = obj.getString("parent_id");

                    // создание списка подкатегорий текущей категории
                    ArrayList<String> tmpData = new ArrayList<String>();
                    JSONArray children = obj.getJSONArray("children");

                    if (children != null) {
                        for (int v=0;v<children.length();v++){
                            tmpData.add(children.getString(v));
                        }
                    }
                    olxCat.children = tmpData;

                    if(children.length()>0){
                        olxCat.name = obj.getString("name") + " >";
                    }else{
                        olxCat.name = obj.getString("name");
                    }

                    olxCat.code = obj.getString("code");
                    olxCat.is_job_category = obj.getString("is_job_category");
                    olxCat.offer_seek = obj.getString("offer_seek"); // предлагаю.ищу для рубрики работа
                    olxCat.private_business = obj.getString("private_business");
                    olxCat.level = obj.getString("level");
                    olxCat.private_name = obj.getString("private_name");
                    olxCat.bussiness_name = obj.getString("bussiness_name");
                    olxCat.districts = obj.getString("districts");
                    olxCat.max_photos = obj.getString("max_photos");
                    olxCat.hint_description = obj.getString("hint_description");
                    olxCat.notice = obj.getString("notice");

                    spCategoriesLv3List.add(olxCat);
                    //Log.d(TAG, obj.getString("name"));
                    if(obj.getString("id").equals(_parentCat)) {
                        selectedIndex = count;
                    }
                    count++;
                }
            }
            //Log.d(TAG, "Список категорий получен без ошибок");
            //Log.d(TAG, "CCCCCCCCCCCC"+spCategoriesLv3List.size());
            scAdapterLv3 = new SelectCategoriesAdapterLv3(ctxt, spCategoriesLv3List);
            //ArrayAdapter<String> spCategoriesLv1dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spCategoriesLv1ListStr);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    spCategoriesLv3.setAdapter(scAdapterLv3);
                    //Log.d(TAG, "selected index " + selectedIndex);
                    spCategoriesLv3.setSelection(selectedIndex);

                    spCategoriesLv3.setEnabled(false);
                    spCategoriesLv3.setClickable(false);
                }
            });

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            result = gson.toJson(spCategoriesLv3List);

        }
        catch(Exception ex){
            Log.e(TAG, "(AsyncTaskLoadCategoriesLv3.java) Ошибка при загрузке категорий лвл 3 " + ex.getMessage());
        }
    }
    // получение списка подкатегорий
    private void fillParrentCats(String _jsonCategories, String _catId){
        try {
            JSONArray catsArray = new JSONArray();
            JSONObject resobj = new JSONObject(_jsonCategories);
            Iterator<?> keys = resobj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (resobj.get(key) instanceof JSONObject) {
                    JSONObject xx = new JSONObject(resobj.get(key).toString());

                    if (xx.optString("id", "").length() <= 0) continue;
                    if (xx.getString("id").equals("1701") && xx.getString("name").equals("Автомобили из Польши"))
                        continue;
                    if(xx.getString("id").equals(_catId)) {
                        if(!xx.getString("parent_id").equals("0")){
                            parrentCats.add(xx.getString("parent_id"));
                            fillParrentCats(_jsonCategories, xx.getString("parent_id"));
                            break;
                        }
                    }
                    catsArray.put(xx);
                }
            }
        }catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }

}
