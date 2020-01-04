package ua.com.anyapps.easyads.easyads.Messages;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import ua.com.anyapps.easyads.easyads.AccountsList.AsyncTaskGetUserAccounts;
import ua.com.anyapps.easyads.easyads.AccountsListActivity;
import ua.com.anyapps.easyads.easyads.AddNewAdActivity;
import ua.com.anyapps.easyads.easyads.DBHelper;
import ua.com.anyapps.easyads.easyads.R;

public class MessagesFromOneSenderActivity extends AppCompatActivity implements CreateNewChatMessageCompleted {

    private static final String TAG = "debapp";
    private String currentadid = ""; // _id
    private Long messageid; // ID сообщения на OLX
    private String uniquechat;
    private String user;
    private String account;

    MessagesFromOneSenderAdapter messagesListAdapter;
    ArrayList< ChatMessage > senderMessages = new ArrayList < ChatMessage>();
    ListView lvMessagesList;
    SQLiteDatabase db = null;
    DBHelper dbHelper = null;
    ProgressDialog createNewChatMessageDialog;
    EditText etNewChatMessage;
    private String newMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            Log.e(TAG, "Ошибка при получение бд (MessagesFromOneSenderActivity.java): " + ex.getMessage());
        }

        etNewChatMessage = (EditText) findViewById(R.id.etNewChatMessage);


        Intent messageListIntent = getIntent(); // gets the previously created intent
        //currentadid = messageListIntent.getStringExtra("currentadid");
        //messageid = messageListIntent.getLongExtra("messageid", 0);
        uniquechat = messageListIntent.getStringExtra("uniquechat");
        //user = messageListIntent.getStringExtra("user");
        //account = messageListIntent.getStringExtra("account");

        //Log.d(TAG, "CURADID: "+ currentadid + " messageid: " + messageid + " uniquechat " +uniquechat );

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Long uchat = Long.parseLong(uniquechat);
        notificationManager.cancel(uchat.intValue());

        //senderMessages = dbHelper.getSenderMessages(currentadid, messageid.toString(), uniquechat, user, account);
        senderMessages = dbHelper.getSenderMessages(uniquechat);

        messagesListAdapter = new MessagesFromOneSenderAdapter(MessagesFromOneSenderActivity.this, senderMessages);
        lvMessagesList = (ListView) findViewById(R.id.lvMessagesList);
        lvMessagesList.setAdapter(messagesListAdapter);

        dbHelper.setViewedMessage(uniquechat);
        //бновить список когда назад
    }

    public void sendNewChatMessage(View v){
        Button btnSendNewChatMessage = (Button) v;
        Log.d(TAG, "Создание нового сообщения");
        createNewChatMessageDialog = new ProgressDialog(MessagesFromOneSenderActivity.this);
        createNewChatMessageDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        createNewChatMessageDialog.setCancelable(false);
        createNewChatMessageDialog.setTitle(R.string.create_new_chat_message_dialog_title);
        createNewChatMessageDialog.setMessage(getResources().getString(R.string.create_new_chat_message_dialog_message));
        createNewChatMessageDialog.show();
        /*Intent addNewAdIntent = new Intent(this, AddNewAdActivity.class);
        accountToAddNewAd = btnNewAd.getTag().toString();
        addNewAdIntent.putExtra("targetAccountID", btnNewAd.getTag().toString());
        startActivityForResult(addNewAdIntent, ADD_NEW_AD_REQUEST);*/
        String dataJson = "";
        newMessage = etNewChatMessage.getText().toString();
        CreateNewChatMessageAsyncTask task = new CreateNewChatMessageAsyncTask(MessagesFromOneSenderActivity.this, this);
        task.execute(uniquechat, newMessage);
    }


    @Override
    public void CreateNewChatMessageCompleted(String response) {
        Log.d(TAG, "Сообщение создано ");

        etNewChatMessage.setText("");
        senderMessages = dbHelper.getSenderMessages(uniquechat);
        messagesListAdapter = new MessagesFromOneSenderAdapter(MessagesFromOneSenderActivity.this, senderMessages);
        lvMessagesList = (ListView) findViewById(R.id.lvMessagesList);
        lvMessagesList.setAdapter(messagesListAdapter);
        dbHelper.setViewedMessage(uniquechat);

        createNewChatMessageDialog.dismiss();
    }
}
