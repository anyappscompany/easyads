package ua.com.anyapps.easyads.easyads.Messages;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.WebView;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.anyapps.easyads.easyads.Bonding.GetRucaptchaResultCompleted;
import ua.com.anyapps.easyads.easyads.DBHelper;
import ua.com.anyapps.easyads.easyads.R;
import ua.com.anyapps.easyads.easyads.Utilities;

import static android.app.NotificationChannel.DEFAULT_CHANNEL_ID;

public class CreateNewChatMessageAsyncTask extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";

    private CreateNewChatMessageCompleted taskCompleted;
    private String result;
    private Context context;
    //WebView wvBrowser;
    SQLiteDatabase db = null;
    DBHelper dbHelper = null;
    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.CreateNewChatMessageCompleted(result);
        super.onPostExecute(aVoid);
    }

    public CreateNewChatMessageAsyncTask(CreateNewChatMessageCompleted context, Context _context) {
        this.taskCompleted = context;
        this.context = _context;

        dbHelper = new DBHelper(_context);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            Log.e(TAG, "Ошибка при получение бд (EasyAnsService.java): " + ex.getMessage());
        }
    }

    String responseJson = "";
    String accountCookies;
    String uniqueChat = "";
    String accountId = "";
    String token = "";
    String message;
    String currentadid = "";
    String ownerId = "";
    @Override
    protected Void doInBackground(String... strings) {
        uniqueChat = strings[0];
        message = strings[1];
        //wvBrowser = (WebView) ((Activity)context).findViewById(R.id.wvBrowser);

        Log.d(TAG, ">>>>ttt"+uniqueChat);


        accountId = dbHelper.getAccountIdFromMessage(uniqueChat);

        currentadid = dbHelper.getCurrentAdIdFromMessage(uniqueChat);
        Log.d(TAG, "CURADID " + currentadid);

        ownerId = dbHelper.getOwnerIdFromMessage(uniqueChat);
        Log.d(TAG, "OWNER " + ownerId);

        accountCookies  = dbHelper.getAccountCookies(accountId);
        Log.d(TAG, accountCookies);

        String pageHtml = "";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ref[0][action]", "myaccount");
        params.put("ref[0][method]", "answers");
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Host", "www.olx.ua");
        headers.put("Connection", "keep-alive");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        headers.put("Referer", "https://www.olx.ua/myaccount/answers/");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.put("Cookie", accountCookies);
        pageHtml = Utilities.GETQuery("https://www.olx.ua/myaccount/answer/"+uniqueChat+"/", params, headers, context);//555



        Pattern tokenPat = Pattern.compile("data-token=\"(.*?)\">");
        Matcher mToken = tokenPat.matcher(pageHtml);
        while(mToken.find()) {
            if (mToken.group(1).length() > 0) {
                token = mToken.group(1);
                Log.d(TAG, "TOKEN " + token + " MESSAGE " + message);
                // Отправка сообщения
                String addNewMessageHtml = "";
                HashMap<String, String> params2 = new HashMap<String, String>();
                params2.put("msg", message);
                params2.put("id", uniqueChat);
                params2.put("token", token);
                params2.put("attach_cv", "");

                Log.d(TAG, "MSG " + message + " ID " + uniqueChat + " TOKEN " + token);

                HashMap<String, String> headers2 = new HashMap<String, String>();
                headers2.put("Host", "www.olx.ua");
                headers2.put("Connection", "keep-alive");
                headers2.put("Accept", "*/*");
                headers2.put("Origin", "https://www.olx.ua");
                headers2.put("X-Requested-With", "XMLHttpRequest");
                headers2.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                headers2.put("Content-Type", "application/x-www-form-urlencoded");
                headers2.put("Referer", "https://www.olx.ua/myaccount/answer/"+uniqueChat+"/?ref%5B0%5D%5Baction%5D=myaccount&ref%5B0%5D%5Bmethod%5D=answers");
                headers2.put("Accept-Encoding", "gzip, deflate, br");
                headers2.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                headers2.put("Cookie", accountCookies);
                addNewMessageHtml = Utilities.POSTQuery("https://www.olx.ua/ajax/myaccount/addanswer/", params2, headers2, context);

                // парсинг входящих сообщений
                // *****************************************************************************************




                    Boolean newMessage = false;

                        String answersHtml = "";
                        HashMap<String, String> headers3 = new HashMap<String, String>();
                        headers3.put("Host", "www.olx.ua");
                        headers3.put("Connection", "keep-alive");
                        headers3.put("Cache-Control", "max-age=0");
                        headers3.put("Upgrade-Insecure-Requests", "1");
                        headers3.put("User-Agent", context.getResources().getString(R.string.default_user_agent));
                        headers3.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                        headers3.put("Referer", "https://www.olx.ua/myaccount/answers/");
                        headers3.put("Accept-Encoding", "gzip, deflate, br");
                        headers3.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                        headers3.put("Cookie", accountCookies);
                        HashMap<String, String> params3 = new HashMap<String, String>();

                        answersHtml = Utilities.GETQuery("https://www.olx.ua/myaccount/answer/"+uniqueChat+"/?ref%5B0%5D%5Baction%5D=myaccount&ref%5B0%5D%5Bmethod%5D=answers#last", params3, headers3, context);

                        Pattern pAnswersNames = Pattern.compile("(class=\"link\">\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<span>|<\\/span>\t\t\t\t\t\t\t\t\t\t\t    \t\t\t\t\t\t\t        \t\t\t\t\t\t\t        \t\t\t\t\t\t\t\t        \t\t\t\t<span>|\t\t\t\t\t\t\t<\\/span>\t\t\t\t\t\t\t\t\t\t\t\t\t\t)(.*?)<");
                        Matcher mAnswersNames = pAnswersNames.matcher(answersHtml);

                        Pattern pAnswersMessageTexts = Pattern.compile("<\\/p>\\s+<\\/div>\\s+<div class=\"cloud clr br5\">\\s+<p>(.*?)<\\/p>");
                        Matcher mAnswersMessagesTexts = pAnswersMessageTexts.matcher(answersHtml);


                        Pattern pMessages = Pattern.compile("<div class=\"titlebar\">						<a id=\"(.*?)\"><\\/a>						<p class=\"clr\">							<span class=\"fright time\">");
                        Matcher mMessages = pMessages.matcher(answersHtml);

                        // номер объявления в списке сообщений к текущему обьявлению
                        Pattern pAdNumberOnPage = Pattern.compile("Номер объявления: (.*?)<");
                        Matcher mAdNumberOnPage = pAdNumberOnPage.matcher(answersHtml);
                        String onPageNumberAd = "";
                        while(mAdNumberOnPage.find()) {
                            onPageNumberAd = mAdNumberOnPage.group(1);
                        }
                        Log.d(TAG, "onPageNumberAd " + onPageNumberAd);


                        // Уникальный номер чата
                        Pattern pUniqueChat = Pattern.compile("<input type=\"hidden\" value=\"(.*?)\" id=\"header_id\"");
                        Matcher mUniqueChat = pUniqueChat.matcher(answersHtml);
                        String uniqueChat = "";
                        while(mUniqueChat.find()) {
                            uniqueChat = mUniqueChat.group(1);
                        }
                        Log.d(TAG, "uniqueChat " + uniqueChat);


                        ArrayList<String> originalMessageIDs = new ArrayList <String> ();
                        while(mMessages.find()) {
                            //Log.d(TAG, "AAAA" + mMessages.group(1));
                            originalMessageIDs.add(mMessages.group(1));
                        }

                        int counter = 0;
                        while(mAnswersNames.find() && mAnswersMessagesTexts.find()) {
                            Log.d(TAG, "ANSWERS FIND!!!!!!!");
                            Long messageId;
                            String sender = "";
                            String message = "";

                            sender = mAnswersNames.group(2).trim();
                            message = mAnswersMessagesTexts.group(1).trim();


                            Log.d(TAG, "COMPARE " + onPageNumberAd + " VS " + currentadid);

                            if(onPageNumberAd.equals(currentadid)) {
                                messageId = new Long(originalMessageIDs.get(counter));

                                Log.d(TAG, sender + ": " + message + " MessageID: " + originalMessageIDs.get(counter) + " AdID: " + currentadid + " uniqueChat " + uniqueChat + " accountId " + accountId + " ownerId " +ownerId);
                                Boolean insertResult = dbHelper.adNewMessage(sender, message, originalMessageIDs.get(counter), currentadid, uniqueChat, accountId, ownerId);
                                // если вставлено новое сообщение, то показать уведомление


                            }
                            //Log.d(TAG, sender + ": " + message);

                            //i++;
                            counter++;
                        }

    /*        CREATE TABLE `messages` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
	`originaladid`	varchar ( 255 ),
	`sender`	varchar ( 255 ),
	`message`	TEXT,
	`writedate`	datetime,
	`viewed`	INTEGER
);*/
            /*

             switch(olxAc.adstatus){
            case "default":
                tvAccountTitle.setTextColor(context.getResources().getColor(R.color.defaultAdStatus));
                break;
            case "active":
                tvAccountTitle.setTextColor(context.getResources().getColor(R.color.activeAdStatus));
                break;
            case "pending":
                tvAccountTitle.setTextColor(context.getResources().getColor(R.color.pendingAdStatus));
                break;
            case "inactive":
                tvAccountTitle.setTextColor(context.getResources().getColor(R.color.inactiveAdStatus));
                break;
            case "deleted":
                tvAccountTitle.setTextColor(context.getResources().getColor(R.color.deletedAdStatus));
                break;
            default:
                tvAccountTitle.setTextColor(context.getResources().getColor(R.color.defaultAdStatus));
        }
             */

                //Log.d(TAG, objOlxAccounts.get(i).email);


                // ****************************************************************************************
                // ****************************************************************************************

            }
            break;
        }

        Log.d(TAG, "WORK " + strings[0]);
        result = responseJson;
        return null;
    }
}
