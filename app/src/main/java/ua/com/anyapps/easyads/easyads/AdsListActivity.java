package ua.com.anyapps.easyads.easyads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import ua.com.anyapps.easyads.easyads.AdsList.AdSelectedCallback;
import ua.com.anyapps.easyads.easyads.AdsList.AsyncTaskDeleteAds;
import ua.com.anyapps.easyads.easyads.AdsList.AsyncTaskGetAds;
import ua.com.anyapps.easyads.easyads.AdsList.DeleteAdsCompleted;
import ua.com.anyapps.easyads.easyads.AdsList.GetAdsCompleted;
import ua.com.anyapps.easyads.easyads.CreateNewAd.AsyncTaskCreateNewAd;
import ua.com.anyapps.easyads.easyads.CreateNewAd.CreateNewAdCompleted;
import ua.com.anyapps.easyads.easyads.EditAd.EditAdActivity;

public class AdsListActivity extends AppCompatActivity implements CreateNewAdCompleted, GetAdsCompleted, DeleteAdsCompleted, AdSelectedCallback {

    private Menu menu;
    private static final String TAG = "debapp";
    static final int ADD_NEW_AD_REQUEST = 101;
    static final int UPDATE_AD_REQUEST = 102;

    private SharedPreferences spPreferences;
    private String authToken;

    ProgressDialog createNewAdDialog;
    ProgressDialog loadAdListDialog;
    ProgressDialog deleteAdListDialog;


    AdsListAdapter adsListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_list);

        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        authToken = spPreferences.getString(getString(R.string.auth_token), null);

        ListView lvAdsList = findViewById(R.id.lvAdsList);

        loadAdListDialog = new ProgressDialog(AdsListActivity.this);
        loadAdListDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadAdListDialog.setCancelable(false);
        loadAdListDialog.setTitle(R.string.load_ad_list_progress_dialog_title);
        loadAdListDialog.setMessage(getResources().getString(R.string.load_ad_list_progress_dialog_message));
        loadAdListDialog.show();

        String targetUrl = getResources().getString(R.string.host) + "/?act=getallads&authtoken=" + authToken;
        AsyncTaskGetAds task = new AsyncTaskGetAds(AdsListActivity.this, this);
        task.execute(targetUrl);

        //MenuItem it = menu.findItem(R.id.ads_list_menu_edit_ad);
        //it.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ads_list_menu, menu);
        this.menu = menu;

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String jsonCheckedAds = null;
        String targetUrl = null;
        switch (item.getItemId()) {
            case R.id.ads_list_menu_add_new_ad:
                //Log.d(TAG, "Меню - создать объявление");
                Intent addNewAdIntent = new Intent(this, AddNewAdActivity.class);
                startActivityForResult(addNewAdIntent, ADD_NEW_AD_REQUEST);
                break;
            case R.id.ads_list_delete_ad:
                //Log.d(TAG, "Меню - удалить объявления");
                jsonCheckedAds = adsListAdapter.getSelectedItems();
                //Log.d(TAG, jsonCheckedAds);
                // ОТПРАВИТЬ СПИСОК НА СЕРВЕР ДЛЯ УДАЛЕНИЯ
                deleteAdListDialog = new ProgressDialog(AdsListActivity.this);
                deleteAdListDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                deleteAdListDialog.setCancelable(false);
                deleteAdListDialog.setTitle(R.string.delete_ad_list_progress_dialog_title);
                deleteAdListDialog.setMessage(getResources().getString(R.string.delete_ad_list_progress_dialog_message));
                deleteAdListDialog.show();


                try {
                    targetUrl = getResources().getString(R.string.host) + "/?act=deleteads&authtoken=" + authToken + "&data="+URLEncoder.encode(jsonCheckedAds, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                AsyncTaskDeleteAds task1 = new AsyncTaskDeleteAds(AdsListActivity.this, this);
                task1.execute(targetUrl);

                break;
            case R.id.ads_list_menu_edit_ad:
                //Log.d(TAG, "Меню - изменить объявления");
                jsonCheckedAds = adsListAdapter.getSelectedItems();

                Intent updateAdIntent = new Intent(this, EditAdActivity.class);
                Bundle b = new Bundle();
                b.putString("jsoncheckedads", jsonCheckedAds); //Your id
                b.putString("selectedadid", adsListAdapter.getSelectedId());
                //b.putString("adid", adId);

                updateAdIntent.putExtras(b); //Put your id to your next Intent


                //Log.d(TAG, "SELECTED AD ID: " + adsListAdapter.getSelectedId());

                startActivityForResult(updateAdIntent, UPDATE_AD_REQUEST);
                //finish();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    String adId = "";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        if (requestCode == ADD_NEW_AD_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String adJson = data.getStringExtra("adjson");
                String adTitle = data.getStringExtra("adtitle");
                adId = data.getStringExtra("adid");
                //Log.d(TAG, "Для пользователя: " + authToken + " создается объявление: " + adJson);

                createNewAdDialog = new ProgressDialog(AdsListActivity.this);
                createNewAdDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                createNewAdDialog.setCancelable(false);
                createNewAdDialog.setTitle(R.string.add_new_ad_progress_dialog_title);
                createNewAdDialog.setMessage(getResources().getString(R.string.add_new_ad_progress_dialog_message));
                createNewAdDialog.show();


                String targetUrl = null;
                try {
                    targetUrl = getResources().getString(R.string.host) + "/?act=createnewad&authtoken=" + authToken + "&adid=" + adId + "&title="+URLEncoder.encode(adTitle, "utf-8")+"&adjson=" + URLEncoder.encode(adJson, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                AsyncTaskCreateNewAd task = new AsyncTaskCreateNewAd(AdsListActivity.this, this);
                task.execute(targetUrl);
            }
        }

        if (requestCode == UPDATE_AD_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // ЗАПРОС СПИСКА ОБЪЯВ
                //ДИАЛОГ
                String targetUrl = getResources().getString(R.string.host) + "/?act=getallads&authtoken=" + authToken;
                AsyncTaskGetAds task = new AsyncTaskGetAds(AdsListActivity.this, this);
                task.execute(targetUrl);
            }
        }



    }

    @Override
    public void CreateNewAdCompleted(String response) {
        createNewAdDialog.dismiss();
        //Log.d(TAG, "Создание объявления закончено. Ответ сервера:" + response);
        // Если error=0 и id>0 то все Ок
        JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(response);
            String error = dataJsonObj.getString("error");
            // если в ответе отсутствуют ошибки
            if(error.equals("0")){
                // ОБНОВИТЬ СПИСОК
                loadAdListDialog = new ProgressDialog(AdsListActivity.this);
                loadAdListDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loadAdListDialog.setCancelable(false);
                loadAdListDialog.setTitle(R.string.load_ad_list_progress_dialog_title);
                loadAdListDialog.setMessage(getResources().getString(R.string.load_ad_list_progress_dialog_message));
                loadAdListDialog.show();

                String targetUrl = getResources().getString(R.string.host) + "/?act=getallads&authtoken=" + authToken;
                AsyncTaskGetAds task = new AsyncTaskGetAds(AdsListActivity.this, this);
                task.execute(targetUrl);
            }else{
                // регистрация не удалась
                //Log.d(TAG, "Ошибка на сервере при добавлении пользователя");
                //Toast.makeText(LoginRegistrationActivity.this, getString(R.string.login_failed), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            //Log.d(TAG, "Исключение приложения во время создания объявления: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    @Override
    public void GetAdsCompleted(String response) {
        loadAdListDialog.dismiss();
        //Log.d(TAG, "Получение всех объявлений закончено. Ответ сервера:" + response);

        JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(response);
            String error = dataJsonObj.getString("error");
            // если в ответе отсутствуют ошибки
            if(error.equals("0")){
                JSONArray ads = dataJsonObj.getJSONArray("ads");

                ArrayList<OlxAd> objOlxAds = new ArrayList<OlxAd>();

                for(int i=0; i<ads.length(); i++){
                    OlxAd oneOlxAd = new OlxAd();
                    //OneAdForAccountList oneAd = new OneAdForAccountList();
                    JSONObject objAd = ads.getJSONObject(i);

                    oneOlxAd.id = objAd.getString("id");
                    oneOlxAd.title = objAd.getString("title");
                    oneOlxAd.createdate = objAd.getString("createdate");
                    oneOlxAd.activeinaccount = objAd.getString("activeinaccount");

                    //Log.d(TAG, objAd.getString("title"));

                    objOlxAds.add(oneOlxAd);
                }

                adsListAdapter = new AdsListAdapter(AdsListActivity.this, objOlxAds);
                ListView lvAdsList = (ListView) findViewById(R.id.lvAdsList);

                lvAdsList.setAdapter(adsListAdapter);
            }else{
                // регистрация не удалась
                //Log.d(TAG, "Получение списка объявлений завершилось с ошибкой на сервере");
                //Toast.makeText(LoginRegistrationActivity.this, getString(R.string.login_failed), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            //Log.d(TAG, "Исключение в приложении во время получения списка объявлений: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    @Override
    public void DeleteAdsCompleted(String response) {
        deleteAdListDialog.dismiss();

        //Log.d(TAG, "Удаление объявлений закончено. Ответ сервера:" + response);

        JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(response);
            String error = dataJsonObj.getString("error");
            // если в ответе отсутствуют ошибки
            if(error.equals("0")){
                JSONArray ads = dataJsonObj.getJSONArray("ads");

                ArrayList<OlxAd> objOlxAds = new ArrayList<OlxAd>();

                for(int i=0; i<ads.length(); i++){
                    OlxAd oneOlxAd = new OlxAd();
                    //OneAdForAccountList oneAd = new OneAdForAccountList();
                    JSONObject objAd = ads.getJSONObject(i);

                    oneOlxAd.id = objAd.getString("id");
                    oneOlxAd.title = objAd.getString("title");
                    oneOlxAd.createdate = objAd.getString("createdate");
                    oneOlxAd.activeinaccount = objAd.getString("activeinaccount");

                    //Log.d(TAG, objAd.getString("title"));

                    objOlxAds.add(oneOlxAd);
                }

                adsListAdapter = new AdsListAdapter(AdsListActivity.this, objOlxAds);
                ListView lvAdsList = (ListView) findViewById(R.id.lvAdsList);

                lvAdsList.setAdapter(adsListAdapter);
            }else{
                // регистрация не удалась
                //Log.d(TAG, "Удаление объявлений завершилось с ошибкой на сервере");
                //Toast.makeText(LoginRegistrationActivity.this, getString(R.string.login_failed), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            //Log.d(TAG, "Исключение в приложении во время удаления объявлений: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    // если выбрано больше чем 1 объявление, то деактивирвать кнопку редактировать
    @Override
    public void AdSelectedCallback(int total) {
        //Log.d(TAG, "Объявление выбрано: " + total);
        MenuItem item = menu.findItem(R.id.ads_list_menu_edit_ad);
        if(total>1){
            item.setEnabled(false);
            item.getIcon().setAlpha(60);
        }else{
            item.setEnabled(true);
            item.getIcon().setAlpha(255);
        }
    }
}
