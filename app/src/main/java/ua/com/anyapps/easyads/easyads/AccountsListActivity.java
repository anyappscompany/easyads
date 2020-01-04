package ua.com.anyapps.easyads.easyads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import ua.com.anyapps.easyads.easyads.AccountsList.AsyncTaskBonding;
import ua.com.anyapps.easyads.easyads.AccountsList.AsyncTaskGetUserAccounts;
import ua.com.anyapps.easyads.easyads.AccountsList.AsyncTaskGetFreeAccount;
import ua.com.anyapps.easyads.easyads.AccountsList.AsyncTaskReleaseAccounts;
import ua.com.anyapps.easyads.easyads.AccountsList.AsyncTaskUpdateCookies;
import ua.com.anyapps.easyads.easyads.AccountsList.BondingCompleted;
import ua.com.anyapps.easyads.easyads.AccountsList.GetUserAccountsCompleted;
import ua.com.anyapps.easyads.easyads.AccountsList.GetFreeAccountCompleted;
import ua.com.anyapps.easyads.easyads.AccountsList.ReleaseAccountsCompleted;
import ua.com.anyapps.easyads.easyads.AccountsList.UpdateCookiesCompleted;
import ua.com.anyapps.easyads.easyads.EditAd.EditAdActivity;
import ua.com.anyapps.easyads.easyads.EditAd.AsyncTaskUpdateAd;
import ua.com.anyapps.easyads.easyads.EditAd.UpdateAdCompleted;

public class AccountsListActivity extends AppCompatActivity implements GetFreeAccountCompleted, BondingCompleted, ReleaseAccountsCompleted, GetUserAccountsCompleted, UpdateAdCompleted, UpdateCookiesCompleted {

    private Menu menu;
    private static final String TAG = "debapp";
    static final int ADD_NEW_ACCOUNT_REQUEST = 102;
    static final int ADD_NEW_AD_REQUEST = 101;
    static final int EDIT_AD_REQUEST = 103;

    private SharedPreferences spPreferences;
    private String authToken;
    private String ownerId;
    ArrayList < OlxAccount > objOlxAccounts = new ArrayList < OlxAccount > ();
    private ArrayList <OlxAccount> olxAccount;

    AccountsListAdapter acListAdapter;

    ProgressDialog getAccountsDialog;
    ProgressDialog getFreeAccountDialog;
    ProgressDialog bondingDialog;
    ProgressDialog releaseAccountsDialog;
    ProgressDialog updateAdDialog;

    WebView wvBrowser;

    SQLiteDatabase db = null;
    DBHelper dbHelper = null;
    ListView lvAccountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts_list);

        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            Log.e(TAG, "Ошибка при получение бд (AccountListActivity.java): " + ex.getMessage());
        }

        getAccountsDialog = new ProgressDialog(AccountsListActivity.this);
        getAccountsDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        getAccountsDialog.setCancelable(false);
        getAccountsDialog.setTitle(R.string.update_slots_progress_dialog_title);
        getAccountsDialog.setMessage(getResources().getString(R.string.update_slots_progress_dialog_message));
        getAccountsDialog.show();

        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        authToken = spPreferences.getString(getString(R.string.auth_token), null);
        ownerId = spPreferences.getString(getString(R.string.current_user), null);

        wvBrowser = (WebView)findViewById(R.id.wvBrowser);

        // заполнение списка аккаунтами пользователя, если они есть
        String targetUrl = getResources().getString(R.string.host) + "/?act=getdatatime&authtoken=" + authToken;
        AsyncTaskGetUserAccounts task = new AsyncTaskGetUserAccounts(AccountsListActivity.this, this);
        task.execute(targetUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.accounts_list_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String targetUrl = "";
        switch (item.getItemId()) {
            case R.id.accounts_list_menu_add_new_account:
                Log.d(TAG, "AccountListActivity.java - Нажата кнопка меню \"Создать аккаунт\"");
                getFreeAccountDialog = new ProgressDialog(AccountsListActivity.this);
                getFreeAccountDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                getFreeAccountDialog.setCancelable(false);
                getFreeAccountDialog.setTitle(R.string.update_slots_progress_dialog_title);
                getFreeAccountDialog.setMessage(getResources().getString(R.string.update_slots_progress_dialog_message));
                getFreeAccountDialog.show();
                targetUrl = getResources().getString(R.string.host) + "/?act=getfreeaccount&authtoken=" + authToken;
                AsyncTaskGetFreeAccount taskGetFreeAccounts = new AsyncTaskGetFreeAccount(AccountsListActivity.this, this);
                taskGetFreeAccounts.execute(targetUrl);
                break;
            /*case R.id.accounts_list_delete_account:
                Log.d(TAG, "AccountListActivity.java - Нажата кнопка меню \"Удалить аккаунт\". " + acListAdapter.getSelectedItems());
                releaseAccountsDialog = new ProgressDialog(AccountsListActivity.this);
                releaseAccountsDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                releaseAccountsDialog.setCancelable(false);
                releaseAccountsDialog.setTitle(R.string.release_accounts_progress_dialog_title);
                releaseAccountsDialog.setMessage(getResources().getString(R.string.release_accounts_progress_dialog_message));
                releaseAccountsDialog.show();
                try {
                    targetUrl = getResources().getString(R.string.host) + "/?act=releaseaccounts&authtoken=" + authToken + "&data=" + URLEncoder.encode(acListAdapter.getSelectedItems(), "utf-8");
                } catch (Exception e) {
                    Log.e(TAG, "AccountListActivity.java - Исключение. Ошибка удаления аккаунта. " + e.getMessage());
                }
                AsyncTaskReleaseAccounts taskRelease = new AsyncTaskReleaseAccounts(AccountsListActivity.this, this);
                taskRelease.execute(targetUrl);
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void GetUserAccountsCompleted(String response) {
        getAccountsDialog.dismiss();
        Log.d(TAG, "AccountListActivity.java - Получены аккаунты пользователя: " + response);

        JSONObject dataJsonObj = null;
        objOlxAccounts = new ArrayList < OlxAccount > ();
        ArrayList < OneAdForAccountList > objAds = new ArrayList < OneAdForAccountList > ();
        try {
            dataJsonObj = new JSONObject(response);
            String error = dataJsonObj.getString("error");
            // если в ответе отсутствуют ошибки
            if (error.equals("0")) {
                // аккаунты принадлежащие текущему пользователю
                //Log.d(TAG, dataJsonObj.getString("datatime"));
                Log.d(TAG, "OWNERID" + ownerId);

                objOlxAccounts = dbHelper.getUserAccounts(dataJsonObj.getString("datatime"), ownerId);


                acListAdapter = new AccountsListAdapter(AccountsListActivity.this, objOlxAccounts);
                lvAccountList = (ListView) findViewById(R.id.lvAccountList);
                lvAccountList.setAdapter(acListAdapter);

                /*JSONArray accounts = dataJsonObj.getJSONArray("accounts");

                for (int i = 0; i < accounts.length(); i++) {
                    OlxAccount oneOlxAc = new OlxAccount();
                    JSONObject objAccount = accounts.getJSONObject(i);

                    oneOlxAc.accountid = objAccount.getString("accountid");
                    oneOlxAc.email = objAccount.getString("email");
                    oneOlxAc.currentuser = objAccount.getString("currentuser");
                    oneOlxAc.currentadid = objAccount.getString("currentadid");
                    oneOlxAc.ending = objAccount.getString("ending");

                    objOlxAccounts.add(oneOlxAc);
                    //String account = objAccount.getString("ending");
                    //Log.d(TAG, account);
                }



                // объявления текущего пользователя
                JSONArray ads = dataJsonObj.getJSONArray("ads");

                for (int i = 0; i < ads.length(); i++) {
                    OneAdForAccountList oneAd = new OneAdForAccountList();
                    JSONObject objAd = ads.getJSONObject(i);

                    oneAd.id = objAd.getString("id");
                    oneAd.title = objAd.getString("title");
                    oneAd.createdate = objAd.getString("createdate");
                    objAds.add(oneAd);
                    //String ad = objAd.getString("title");
                    //Log.d(TAG, ad);
                }

                acListAdapter = new AccountsListAdapter(AccountsListActivity.this, objOlxAccounts);

                ListView lvAccountList = (ListView) findViewById(R.id.lvAccountList);
                lvAccountList.setAdapter(acListAdapter);*/
            } else {
                Log.e(TAG, "Список аккаунтов не получен. Ошибка на сервере. ");
            }

        } catch (JSONException e) {
            Log.e(TAG, "Исключение во время парсинга списка аккаунтов и объявлений: " + e.getMessage());
            //e.printStackTrace();

        }
    }

    private String accountToAddNewAd = "";
    public void newAd(View v){
        Button btnNewAd = (Button) v;
        Log.d(TAG, "Создание нового объявления для аккаунта " + btnNewAd.getTag().toString());
        Intent addNewAdIntent = new Intent(this, AddNewAdActivity.class);
        accountToAddNewAd = btnNewAd.getTag().toString();
        addNewAdIntent.putExtra("targetAccountID", btnNewAd.getTag().toString());
        startActivityForResult(addNewAdIntent, ADD_NEW_AD_REQUEST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*Log.d(TAG, "Аккаунтов " + objOlxAccounts.size());
        if(objOlxAccounts.size()>0){
            ArrayList < OlxAccount > tmp = new ArrayList < OlxAccount > ();
            tmp.addAll(objOlxAccounts);
            objOlxAccounts.clear();
            objOlxAccounts.addAll(tmp);

            acListAdapter.notifyDataSetChanged();
            lvAccountList.setAdapter(acListAdapter);
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_NEW_AD_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Get result from the result intent.
                Log.d(TAG, data.getStringExtra("adtitle"));
                Log.d(TAG, data.getStringExtra("adid"));
                Log.d(TAG, data.getStringExtra("targetAccountID"));
                Log.d(TAG, data.getStringExtra("adjson"));

                bondingDialog = new ProgressDialog(AccountsListActivity.this);
                bondingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                bondingDialog.setCancelable(false);
                bondingDialog.setTitle(R.string.bonding_progress_dialog_title);
                bondingDialog.setMessage(getResources().getString(R.string.bonding_progress_dialog_message));
                bondingDialog.show();

                AsyncTaskBonding task = new AsyncTaskBonding(AccountsListActivity.this, this, data.getStringExtra("adjson"), data.getStringExtra("targetAccountID"), data.getStringExtra("adid"), data.getStringExtra("adtitle"));
                task.execute();
            }
        }

        if (requestCode == EDIT_AD_REQUEST) {
            Log.d(TAG, "RESULT CODE " + resultCode);
            if(resultCode == RESULT_CANCELED){ // нажата кнопка назад
                Log.d(TAG, "Нажата кнопка назад");

                String targetUrl = getResources().getString(R.string.host) + "/?act=getdatatime&authtoken=" + authToken;
                AsyncTaskGetUserAccounts task = new AsyncTaskGetUserAccounts(AccountsListActivity.this, this);
                task.execute(targetUrl);
            }

            if (resultCode == RESULT_OK) {
                // Get result from the result intent.
                Log.d(TAG, "Данные для изменения объявления");
                Log.d(TAG, data.getStringExtra("adtitle"));
                Log.d(TAG, data.getStringExtra("adid"));
                Log.d(TAG, data.getStringExtra("adjson"));
                Log.d(TAG, data.getStringExtra("adcategory"));
                updateAdDialog = new ProgressDialog(AccountsListActivity.this);
                updateAdDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                updateAdDialog.setCancelable(false);
                updateAdDialog.setTitle(R.string.edit_ad_progress_dialog_title);
                updateAdDialog.setMessage(getResources().getString(R.string.edit_ad_progress_dialog_message));
                updateAdDialog.show();

                AsyncTaskUpdateAd task = new AsyncTaskUpdateAd(AccountsListActivity.this, this);
                task.execute(data.getStringExtra("adid"), data.getStringExtra("adtitle"), data.getStringExtra("adjson"), data.getStringExtra("adcategory"));


            }

        }
    }

    public void deleteAccountAd(View v){
        Button btnDeleteAccountAd = (Button) v;
        Log.d(TAG, btnDeleteAccountAd.getTag().toString());

        releaseAccountsDialog = new ProgressDialog(AccountsListActivity.this);
        releaseAccountsDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        releaseAccountsDialog.setCancelable(false);
        releaseAccountsDialog.setTitle(R.string.release_accounts_progress_dialog_title);
        releaseAccountsDialog.setMessage(getResources().getString(R.string.release_accounts_progress_dialog_message));
        releaseAccountsDialog.show();

        AsyncTaskReleaseAccounts task = new AsyncTaskReleaseAccounts(AccountsListActivity.this, this);
        task.execute(btnDeleteAccountAd.getTag().toString(), authToken);
    }

    public void editAd(View v){
        Button btnEditAd = (Button) v;
        Log.d(TAG, btnEditAd.getTag().toString());
        Intent editAdIntent = new Intent(this, EditAdActivity.class);
        editAdIntent.putExtra("targetAdID", btnEditAd.getTag().toString());
        startActivityForResult(editAdIntent, EDIT_AD_REQUEST);
    }

    public void updateCookies(View v){
        Button btnUpdateCookies = (Button) v;
        Log.d(TAG, btnUpdateCookies.getTag().toString());

        AsyncTaskUpdateCookies task = new AsyncTaskUpdateCookies(AccountsListActivity.this, this);
        task.execute(btnUpdateCookies.getTag().toString(), authToken);
        //Intent editAdIntent = new Intent(this, EditAdActivity.class);
        //editAdIntent.putExtra("targetAdID", btnEditAd.getTag().toString());
        //startActivityForResult(editAdIntent, EDIT_AD_REQUEST);
    }

    public void btnBindClick(View v) {
        //Log.d(TAG, "Нажата кнопка применить");
        String bonding = acListAdapter.getDataForBonding();

        Log.d(TAG, "Bonding --- " + bonding);

        //Log.d(TAG, "Выделенные аккаунты" + acListAdapter.getSelectedItems());
        if (bonding != null) { // если есть данные json для связки аккаунтов и объявлений
            //Log.d(TAG, "FFFFFFFFF" + acListAdapter.toBondingTotalItems());
            //if(acListAdapter.toBondingTotalItems()<=1) return;
            // отправка на сервер

            // если на сервере все ок, то

            bondingDialog = new ProgressDialog(AccountsListActivity.this);
            bondingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            bondingDialog.setCancelable(false);
            bondingDialog.setTitle(R.string.bonding_progress_dialog_title);
            bondingDialog.setMessage(getResources().getString(R.string.bonding_progress_dialog_message));
            bondingDialog.show();

            String targetUrl = null;
            String targetUrl2 = null;
            try {
                targetUrl = getResources().getString(R.string.host) + "/?act=bonding&authtoken=" + authToken + "&data=" + URLEncoder.encode(bonding, "utf-8");
                targetUrl2 = getResources().getString(R.string.host) + "/?act=getaccountsinfoforbonding&authtoken=" + authToken + "&data=" + URLEncoder.encode(bonding, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //AsyncTaskBonding task = new AsyncTaskBonding(AccountsListActivity.this, this);
            //task.execute(targetUrl, targetUrl2);

            //acListAdapter.notifyDataSetChanged();

        }


    }

    @Override
    public void GetFreeAccountCompleted(String response) {
        getFreeAccountDialog.dismiss();
        //Log.d(TAG, "Получение нового аккаунта закончено");

        JSONObject dataJsonObj = null;
        objOlxAccounts = new ArrayList < OlxAccount > ();
        try {
            dataJsonObj = new JSONObject(response);
            String error = dataJsonObj.getString("error");
            // если в ответе отсутствуют ошибки
            if (error.equals("0")) {
                Log.d(TAG, "Аккаунт получен без ошибок");

                JSONObject objAccount = new JSONObject(dataJsonObj.getString("account"));


                    /*OlxAccount oneOlxAc = new OlxAccount();

                    oneOlxAc.accountid = objAccount.getString("accountid");
                    oneOlxAc.email = objAccount.getString("email");
                    oneOlxAc.password = objAccount.getString("password");
                    //oneOlxAc.currentuser = objAccount.getString("currentuser");
                    oneOlxAc.currentadid = objAccount.getString("currentadid");
                    oneOlxAc.getdate = objAccount.getString("getdate");
                    oneOlxAc.ending = objAccount.getString("ending");
                    objOlxAccounts.add(oneOlxAc);*/
                    //String account = objAccount.getString("ending");
                    Log.d(TAG, objAccount.getString("email"));
                    Log.d(TAG, "OWNERID"+objAccount.getString("currentuser"));

                    dbHelper.addAccount(objAccount.getString("accountid"), objAccount.getString("email"), objAccount.getString("password"), objAccount.getString("currentuser") ,objAccount.getString("getdate"), objAccount.getString("ending"));

                    objOlxAccounts = dbHelper.getUserAccounts(objAccount.getString("getdate"), ownerId);


                acListAdapter = new AccountsListAdapter(AccountsListActivity.this, objOlxAccounts);
                lvAccountList = (ListView) findViewById(R.id.lvAccountList);
                lvAccountList.setAdapter(acListAdapter);
            } else {
                // получить свободный аккаунт не удалось
                //Log.d(TAG, "Получить свободный аккаунт не удалось.");
                Toast.makeText(AccountsListActivity.this, getString(R.string.get_free_account_failed), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Исключение во время получения свободного аккаунта: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    @Override
    public void BondingCompleted(String response) {
        bondingDialog.dismiss();
        try {
            Log.d(TAG, "response " + response);
            JSONObject dataJsonObj = new JSONObject(response);
            String result = dataJsonObj.getString("result");
            if(result.equals("1")){

                String updateRes = dbHelper.updateAdStatus(dataJsonObj.getString("accountid"), "pending");
                Log.d(TAG, "Аккаунт для добавления нового объявления " + accountToAddNewAd);

                Log.d(TAG, "updateRes " + updateRes);
                JSONObject joUpRes = new JSONObject(updateRes);

                //все равно нужно получать список аккаунтов из базы
                for(int u=0; u<objOlxAccounts.size(); u++) {
                    if(objOlxAccounts.get(u).accountid.equals(accountToAddNewAd)) {
                        objOlxAccounts.get(u).adstatus = "pending";

                        Log.d(TAG, "adtitle " + joUpRes.getString("adtitle"));
                        objOlxAccounts.get(u).currentadtitle = joUpRes.getString("adtitle");

                        //JSONObject adPhotoClassObj = new JSONObject(updateRes);
                        Log.d(TAG, "photo " + joUpRes.getString("photo"));
                        objOlxAccounts.get(u).currentadphoto = joUpRes.getString("photo");

                        Log.d(TAG, "currentadid " + joUpRes.getString("currentadid"));
                        objOlxAccounts.get(u).currentadid = joUpRes.getString("currentadid");

                    }
                }

                //acListAdapter.notifyDataSetChanged();
                String targetUrl = getResources().getString(R.string.host) + "/?act=getdatatime&authtoken=" + authToken;
                AsyncTaskGetUserAccounts task = new AsyncTaskGetUserAccounts(AccountsListActivity.this, this);
                task.execute(targetUrl);
            }
        } catch (JSONException e) {
            Log.e(TAG, "AccountListActivity.java(public void BondingCompleted(String response))  Объявление добавлено с ошибкой" + e.getMessage());
        }

        //bondingDialog.dismiss();
        //Log.d(TAG, "BONDING COMPLETED");

        /*JSONObject dataJsonObj = null;
        ArrayList < OlxAccount > objOlxAccounts = new ArrayList < OlxAccount > ();
        ArrayList < OneAdForAccountList > objAds = new ArrayList < OneAdForAccountList > ();
        try {
            dataJsonObj = new JSONObject(response);
            String error = dataJsonObj.getString("error");
            // если в ответе отсутствуют ошибки
            if (error.equals("0")) {
                //Log.d(TAG, "Привязка выполнена без ошибок");

                JSONArray accounts = dataJsonObj.getJSONArray("accounts");

                for (int i = 0; i < accounts.length(); i++) {
                    OlxAccount oneOlxAc = new OlxAccount();
                    JSONObject objAccount = accounts.getJSONObject(i);

                    oneOlxAc.accountid = objAccount.getString("accountid");
                    oneOlxAc.email = objAccount.getString("email");
                    oneOlxAc.currentuser = objAccount.getString("currentuser");
                    oneOlxAc.currentadid = objAccount.getString("currentadid");
                    oneOlxAc.ending = objAccount.getString("ending");
                    objOlxAccounts.add(oneOlxAc);
                    //String account = objAccount.getString("ending");
                    //Log.d(TAG, account);
                }

                // объявления текущего пользователя
                JSONArray ads = dataJsonObj.getJSONArray("ads");

                for (int i = 0; i < ads.length(); i++) {
                    OneAdForAccountList oneAd = new OneAdForAccountList();
                    JSONObject objAd = ads.getJSONObject(i);

                    oneAd.id = objAd.getString("id");
                    oneAd.title = objAd.getString("title");
                    oneAd.createdate = objAd.getString("createdate");
                    objAds.add(oneAd);
                    //String ad = objAd.getString("title");
                    //Log.d(TAG, ad);
                }

                acListAdapter = new AccountsListAdapter(AccountsListActivity.this, objOlxAccounts);
                ListView lvAccountList = (ListView) findViewById(R.id.lvAccountList);
                lvAccountList.setAdapter(acListAdapter);
            } else {
                // получить свободный аккаунт не удалось
                //Log.d(TAG, "Связать аккаунты не удалось. Ошибка на сервере.");
                //Toast.makeText(AccountsListActivity.this, getString(R.string.get_free_account_failed), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            //Log.d(TAG, "Исключение во время получения связки аккаунтов: " + e.getMessage());
            e.printStackTrace();
        }*/
    }

    @Override
    public void ReleaseAccountsCompleted(String response) {
        releaseAccountsDialog.dismiss();

        Log.d(TAG, "Ответ на удаление получен");
        Log.d(TAG, "Ответ: "+response);

        if(Integer.parseInt(response)>0){
            Log.d(TAG, "Удаление успешно");
            for(int u=0; u<objOlxAccounts.size(); u++) {
                if(objOlxAccounts.get(u).accountid.equals(response)) {
                    objOlxAccounts.remove(u);
                    Log.d(TAG, "Удаление из списка c _id " + response);
                }
            }

            //acListAdapter.notifyDataSetChanged();
            String targetUrl = getResources().getString(R.string.host) + "/?act=getdatatime&authtoken=" + authToken;
            AsyncTaskGetUserAccounts task = new AsyncTaskGetUserAccounts(AccountsListActivity.this, this);
            task.execute(targetUrl);
        }else{
            // ошибка
        }
        //Log.d(TAG, "Удаление завершено");
        //Log.d(TAG, "Слоты текущего пользователя: " + response);

        /*JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(response);
            String error = dataJsonObj.getString("error");
            // если в ответе отсутствуют ошибки
            if (error.equals("0")) {
                //Log.d(TAG, "Аккаунты деактивировались без ошибок");

                objOlxAccounts = new ArrayList < OlxAccount > ();
                ArrayList < OneAdForAccountList > objAds = new ArrayList < OneAdForAccountList > ();

                //dataJsonObj = new JSONObject(response);
                // аккаунты принадлежащие текущему пользователю
                JSONArray accounts = dataJsonObj.getJSONArray("accounts");

                for (int i = 0; i < accounts.length(); i++) {
                    OlxAccount oneOlxAc = new OlxAccount();
                    JSONObject objAccount = accounts.getJSONObject(i);

                    oneOlxAc.accountid = objAccount.getString("accountid");
                    oneOlxAc.email = objAccount.getString("email");
                    oneOlxAc.adstatus = objAccount.getString("adstatus");
                    oneOlxAc.currentuser = objAccount.getString("currentuser");
                    oneOlxAc.currentadid = objAccount.getString("currentadid");
                    oneOlxAc.ending = objAccount.getString("ending");
                    objOlxAccounts.add(oneOlxAc);
                    //String account = objAccount.getString("ending");
                    //Log.d(TAG, account);
                }

                // объявления текущего пользователя
                JSONArray ads = dataJsonObj.getJSONArray("ads");

                for (int i = 0; i < ads.length(); i++) {
                    OneAdForAccountList oneAd = new OneAdForAccountList();
                    JSONObject objAd = ads.getJSONObject(i);

                    oneAd.id = objAd.getString("id");
                    oneAd.title = objAd.getString("title");
                    oneAd.createdate = objAd.getString("createdate");
                    objAds.add(oneAd);
                    //String ad = objAd.getString("title");
                    //Log.d(TAG, ad);
                }

                acListAdapter = new AccountsListAdapter(AccountsListActivity.this, objOlxAccounts);

                lvAccountList = (ListView) findViewById(R.id.lvAccountList);
                lvAccountList.setAdapter(acListAdapter);
            } else {
                // получить свободный аккаунт не удалось
                //Log.d(TAG, "Удалить аккаунты не удалось. Ошибка на сервере.");
                //Toast.makeText(AccountsListActivity.this, getString(R.string.get_free_account_failed), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            //Log.d(TAG, "Исключение во время удаления аккаунтов: " + e.getMessage());
            e.printStackTrace();
        }*/
    }

    @Override
    public void UpdateAdCompleted(String response) {
        updateAdDialog.dismiss();


        String targetUrl = getResources().getString(R.string.host) + "/?act=getdatatime&authtoken=" + authToken;
        AsyncTaskGetUserAccounts task = new AsyncTaskGetUserAccounts(AccountsListActivity.this, this);
        task.execute(targetUrl);
        Log.d(TAG, "Объявление изменено");
    }

    @Override
    public void UpdateCookiesCompleted(String response) {
        Log.d(TAG, "Обновление куков завершено " + response);
    }
}