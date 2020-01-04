package ua.com.anyapps.easyads.easyads.Messages;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;

import ua.com.anyapps.easyads.easyads.AccountsList.AsyncTaskGetUserAccounts;
import ua.com.anyapps.easyads.easyads.AccountsList.GetUserAccountsCompleted;
import ua.com.anyapps.easyads.easyads.AccountsListActivity;
import ua.com.anyapps.easyads.easyads.DBHelper;
import ua.com.anyapps.easyads.easyads.OlxAccount;
import ua.com.anyapps.easyads.easyads.R;

public class MessagesSenderListActivity extends AppCompatActivity implements GetUserAccountsCompleted {

    private static final String TAG = "debapp";

    MessagesSenderListAdapter messagesSenderListAdapter;
    ArrayList< SenderInfo > senders = new ArrayList < SenderInfo>();
    ListView lvMessagesSenderList;
    SQLiteDatabase db = null;
    DBHelper dbHelper = null;
    private String authToken;
    private SharedPreferences spPreferences;
    ProgressDialog getDateOnServerDialog;
    ArrayList <OlxAccount> objOlxAccounts = new ArrayList < OlxAccount > ();
    private String ownerId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_sender_list);

        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        authToken = spPreferences.getString(getString(R.string.auth_token), null);
        ownerId = spPreferences.getString(getString(R.string.current_user), null);

        lvMessagesSenderList = (ListView) findViewById(R.id.lvMessagesSenderList);

        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            Log.e(TAG, "Ошибка при получение бд (MessagesSenderListActivity.java): " + ex.getMessage());
        }

        getDateOnServerDialog = new ProgressDialog(MessagesSenderListActivity.this);
        getDateOnServerDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        getDateOnServerDialog.setCancelable(false);
        getDateOnServerDialog.setTitle(R.string.get_date_on_server_dialog_title);
        getDateOnServerDialog.setMessage(getResources().getString(R.string.get_date_on_server_dialog_message));
        getDateOnServerDialog.show();

        String targetUrl = getResources().getString(R.string.host) + "/?act=getdatatime&authtoken=" + authToken;
        AsyncTaskGetUserAccounts task = new AsyncTaskGetUserAccounts(MessagesSenderListActivity.this, this);
        task.execute(targetUrl);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MessagesSenderListActivity: onResume()");

        if(!getDateOnServerDialog.isShowing()) {
            getDateOnServerDialog = new ProgressDialog(MessagesSenderListActivity.this);
            getDateOnServerDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            getDateOnServerDialog.setCancelable(false);
            getDateOnServerDialog.setTitle(R.string.get_date_on_server_dialog_title);
            getDateOnServerDialog.setMessage(getResources().getString(R.string.get_date_on_server_dialog_message));
            getDateOnServerDialog.show();
        }

        String targetUrl = getResources().getString(R.string.host) + "/?act=getdatatime&authtoken=" + authToken;
        AsyncTaskGetUserAccounts task = new AsyncTaskGetUserAccounts(MessagesSenderListActivity.this, this);
        task.execute(targetUrl);
    }

    @Override
    public void GetUserAccountsCompleted(String response) {
        getDateOnServerDialog.dismiss();

        Log.d(TAG, ">>>"+response);
        JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(response);
            String error = dataJsonObj.getString("error");
            if (error.equals("0")) {

                //objOlxAccounts = dbHelper.getUserAccounts(dataJsonObj.getString("datatime"), ownerId);

                senders = dbHelper.getSenders(dataJsonObj.getString("datatime"), ownerId);



                messagesSenderListAdapter = new MessagesSenderListAdapter(MessagesSenderListActivity.this, senders);

                lvMessagesSenderList.setAdapter(messagesSenderListAdapter);

                /*senders = dbHelper.getSenders(currentadid, messageid.toString(), uniquechat, user, account);

                messagesSenderListAdapter = new MessagesSenderListAdapter(MessagesSenderListActivity.this, senders);
                lvMessagesSenderList = (ListView) findViewById(R.id.lvMessagesList);
                lvMessagesSenderList.setAdapter(messagesSenderListAdapter);*/
                lvMessagesSenderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Log.d(TAG, "itemClick: position = " + position + ", id = "+ id + " " + view.getTag());

                        Intent intent = new Intent(getBaseContext(), MessagesFromOneSenderActivity.class);
                        //intent.putExtra("EXTRA_SESSION_ID", sessionId);

                        /*intent.putExtra("currentadid");
                        intent.putExtra("messageid");

                        intent.putExtra("user");
                        intent.putExtra("account");*/

                        intent.putExtra("uniquechat", view.getTag().toString());
                        startActivity(intent);
                    }
                });

            }
        }catch (Exception ex){
            Log.e(TAG, "MessagesSenderListActivity.java Ошибка при загрузке времени с сервера " + ex.getMessage());
        }

    }


}
