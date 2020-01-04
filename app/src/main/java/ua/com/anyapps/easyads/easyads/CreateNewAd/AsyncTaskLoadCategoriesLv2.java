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
import java.util.Iterator;

import ua.com.anyapps.easyads.easyads.OlxCategory;
import ua.com.anyapps.easyads.easyads.R;
import ua.com.anyapps.easyads.easyads.SelectCategoriesAdapterLv2;
import ua.com.anyapps.easyads.easyads.Utilities;

public class AsyncTaskLoadCategoriesLv2 extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private LoadCategoriesLv2Completed taskCompleted;
    private String result="";
    private Context ctxt;
    Handler mHandler = new Handler();

    public AsyncTaskLoadCategoriesLv2(LoadCategoriesLv2Completed context, Context _context) {
        this.taskCompleted = context;
        this.ctxt = _context;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.LoadCategoriesLv2Completed(result);
        super.onPostExecute(aVoid);
    }

    private ArrayList<OlxCategory> spCategoriesLv2List = new ArrayList<OlxCategory>();
    JSONArray categories = null;
    SelectCategoriesAdapterLv2 scAdapterLv2;
    Spinner spCategoriesLv2;
    String pos = "";
    @Override
    protected Void doInBackground(String... strings) {
        String jsonCategories = "";
        spCategoriesLv2 = (Spinner)((Activity)ctxt).findViewById(R.id.spCategoriesLv2);
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

            spCategoriesLv2List = new ArrayList<OlxCategory>();
            pos = strings[0];
            categories = catsArray;
            //Log.d(TAG, "pos"+strings[0]);
            for (int i = 0; i < categories.length(); i++) {
                JSONObject obj = null;

                    obj = categories.getJSONObject(i);
                    if(obj.getString("parent_id").equals(pos)) {

                        OlxCategory olxCat = new OlxCategory();

                        olxCat.id = obj.getString("id");
                        olxCat.parent_id = obj.getString("parent_id");

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
                        olxCat.offer_seek = obj.getString("offer_seek");
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
                    }






                //Log.d(TAG, pos.toString());
            }//Log.d(TAG, "В КАТЕГОРИИ:" + spCategoriesLv2List.size() + " POS:" + pos);

            //ArrayAdapter<String> spCategoriesLv2dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spCategoriesLv2List);
            //spCategoriesLv2.setAdapter(spCategoriesLv2dataAdapter);
            scAdapterLv2 = new SelectCategoriesAdapterLv2(ctxt, spCategoriesLv2List);

            //ArrayAdapter<String> spCategoriesLv1dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spCategoriesLv1ListStr);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    spCategoriesLv2.setAdapter(scAdapterLv2);

                }
            });

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            result = gson.toJson(spCategoriesLv2List);

        }
        catch(Exception ex){
            Log.e(TAG, "(AsyncTaskLoadCategoriesLv2.java) Ошибка при загрузке категорий лвл 2 " + ex.getMessage());
        }

        //result = responseJson;
        return null;
    }
}
