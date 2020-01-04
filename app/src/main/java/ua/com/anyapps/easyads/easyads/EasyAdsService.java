package ua.com.anyapps.easyads.easyads;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.anyapps.easyads.easyads.Messages.MessagesFromOneSenderActivity;

import static android.app.NotificationChannel.DEFAULT_CHANNEL_ID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class EasyAdsService extends IntentService {
    public static volatile boolean shouldContinue = true;
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "ua.com.anyapps.easyads.easyads.action.FOO";
    private static final String ACTION_BAZ = "ua.com.anyapps.easyads.easyads.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "ua.com.anyapps.easyads.easyads.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "ua.com.anyapps.easyads.easyads.extra.PARAM2";
    private static final String TAG = "debapp";
    public EasyAdsService() {
        super("EasyAdsService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, EasyAdsService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, EasyAdsService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    ArrayList < OlxAccount > objOlxAccounts = new ArrayList < OlxAccount > ();
    SQLiteDatabase db = null;
    DBHelper dbHelper = null;
    private SharedPreferences spPreferences;
    private String authToken;
    private String ownerId;

    private NotificationManager mNotificationManager;
    @Override
    protected void onHandleIntent(Intent intent) {
        int count = 0;

        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            Log.e(TAG, "Ошибка при получение бд (EasyAnsService.java): " + ex.getMessage());
        }

        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        authToken = spPreferences.getString(getString(R.string.auth_token), null);
        ownerId = spPreferences.getString(getString(R.string.current_user), null);

        String dateTimeJson = Utilities.getHtml(getResources().getString(R.string.host) + "/?act=getdatatime&authtoken=" + authToken, this);
        String datetime = "";
        JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(dateTimeJson);
            datetime = dataJsonObj.getString("datatime");
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }



        Log.d(TAG, "serv " + count);
        objOlxAccounts = new ArrayList< OlxAccount >();
        objOlxAccounts = dbHelper.getUserAccounts(datetime, ownerId);

        for(int i=0;i<objOlxAccounts.size();i++){
            if(!shouldContinue) return;
            Log.d(TAG, "cuadid " + objOlxAccounts.get(i).currentadid);
            // если не привязано объявление к аккаунту
            if(objOlxAccounts.get(i).currentadid == null || objOlxAccounts.get(i).currentadid.length()<=0) continue;

            String mHtmlCode = "";
            String mHtmlCode2 = "";

            String accountCookies  = dbHelper.getAccountCookies(objOlxAccounts.get(i).accountid);

            // Получение статуса
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Host", "www.olx.ua");
            headers.put("Connection", "keep-alive");
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("Origin", "https://www.olx.ua");
            headers.put("X-Requested-With", "XMLHttpRequest");
            headers.put("User-Agent", getResources().getString(R.string.default_user_agent));
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            headers.put("Referer", "https://www.olx.ua/myaccount/");
            headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
            headers.put("Cookie", accountCookies);
            HashMap<String, String> params = new HashMap<String, String>();
            mHtmlCode = Utilities.GETQuery("https://www.olx.ua/myaccount/", params, headers, this);

            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(objOlxAccounts.get(i).email+"_page.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(mHtmlCode);
                outputStreamWriter.close();
            }
            catch (Exception ex) {
                Log.e("Exception", "File write failed: " + ex.toString());
            }

            String adStatus = "default";


            /*if(mHtmlCode.indexOf("<li id=\"typeactive\" class=\"fleft rel\">")>=0 || mHtmlCode.indexOf("<li id=\"typeactive\" class=\"fleft rel selected\">")>=0){
                Log.d(TAG, "Найден: Активные");
                adStatus = "active";
            }*/
            Pattern activePat = Pattern.compile("Активные<\\/span> <span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t \\([0-9]+\\)");
            Matcher activeToken = activePat.matcher(mHtmlCode);
            while(activeToken.find()) {
                if (activeToken.group(0).length() > 0) {
                    Log.d(TAG, "Найден: Активные");
                    adStatus = "active";
                }
            }
            /*if(mHtmlCode.indexOf("<li id=\"typewaiting\" class=\"fleft rel\">")>=0 || mHtmlCode.indexOf("<li id=\"typewaiting\" class=\"fleft rel selected\">")>=0){
                Log.d(TAG, "Найден: Ожидающие");
                adStatus = "pending";
            }*/
            Pattern waitingPat = Pattern.compile("Ожидающие\t\t\t\t\t\t\t<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\\([0-9]+\\)");
            Matcher waitingToken = waitingPat.matcher(mHtmlCode);
            while(waitingToken.find()) {
                if (waitingToken.group(0).length() > 0) {
                    Log.d(TAG, "Найден: Ожидающие");
                    adStatus = "pending";
                }
            }

            /*if(mHtmlCode.indexOf("<li id=\"typearchive\" class=\"fleft rel\">")>=0 || mHtmlCode.indexOf("<li id=\"typearchive\" class=\"fleft rel selected\">")>=0){
                Log.d(TAG, "Найден: Неактивные");
                adStatus = "inactive";
            }*/
            Pattern archivePat = Pattern.compile("Неактивные\t\t\t\t\t\t\t<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\\([0-9]+\\)");
            Matcher archiveToken = archivePat.matcher(mHtmlCode);
            while(archiveToken.find()) {
                if (archiveToken.group(0).length() > 0) {
                    Log.d(TAG, "Найден: Неактивные");
                    adStatus = "inactive";
                }
            }

            /*if(mHtmlCode.indexOf("<li id=\"typemoderated\" class=\"fleft rel\">")>=0 || mHtmlCode.indexOf("<li id=\"typemoderated\" class=\"fleft rel selected\">")>=0) {
                Log.d(TAG, "Найден: Удаленные");
                adStatus = "deleted";
            }*/
            Pattern moderatedPat = Pattern.compile("Удаленные\t\t\t\t\t\t\t<span class=\"pointer\">\t\t\t\t\t\t\t\t<span class=\"counter fnormal\">\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\\([0-9]+\\)");
            Matcher moderatedToken = moderatedPat.matcher(mHtmlCode);
            while(moderatedToken.find()) {
                if (moderatedToken.group(0).length() > 0) {
                    Log.d(TAG, "Найден: Удаленные");
                    adStatus = "deleted";
                }
            }

            dbHelper.updateAdStatusFromService(objOlxAccounts.get(i).accountid, adStatus);

            // Получение новых сообщений
            HashMap<String, String> headers2 = new HashMap<String, String>();
            headers2.put("Host", "www.olx.ua");
            headers2.put("Connection", "keep-alive");
            headers2.put("Cache-Control", "max-age=0");
            headers2.put("Upgrade-Insecure-Requests", "1");
            headers2.put("User-Agent", getResources().getString(R.string.default_user_agent));
            headers2.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            headers2.put("Referer", "https://www.olx.ua/myaccount/answers/");
            headers2.put("Accept-Encoding", "gzip, deflate, br");
            headers2.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
            headers2.put("Cookie", accountCookies);
            HashMap<String, String> params2 = new HashMap<String, String>();
            // новые/ все
            params2.put("type", "unreaded");
            mHtmlCode2 = Utilities.GETQuery("https://www.olx.ua/myaccount/answers/", params2, headers2, this);

            Pattern pTotalAd = Pattern.compile("class=\"headerRow normal (unreaded fbold c000|color-5) \\{'url': '(.*?)'\\}\"\\>");
            Matcher mTotalAd = pTotalAd.matcher(mHtmlCode2);
            int newAnswersTotal=0;
            String answer = "";
            while(mTotalAd.find()) {
                Boolean newMessage = false;
                if (mTotalAd.group(1).length() > 0) {
                    answer = mTotalAd.group(2);
                    Log.d(TAG, "Answer URL: " + answer);

                    String answersHtml = "";
                    HashMap<String, String> headers3 = new HashMap<String, String>();
                    headers3.put("Host", "www.olx.ua");
                    headers3.put("Connection", "keep-alive");
                    headers3.put("Cache-Control", "max-age=0");
                    headers3.put("Upgrade-Insecure-Requests", "1");
                    headers3.put("User-Agent", getResources().getString(R.string.default_user_agent));
                    headers3.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                    headers3.put("Referer", "https://www.olx.ua/myaccount/answers/");
                    headers3.put("Accept-Encoding", "gzip, deflate, br");
                    headers3.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                    headers3.put("Cookie", accountCookies);
                    HashMap<String, String> params3 = new HashMap<String, String>();

                    answersHtml = Utilities.GETQuery(answer, params3, headers3, this);

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


                    ArrayList <String> originalMessageIDs = new ArrayList <String> ();
                    while(mMessages.find()) {
                        //Log.d(TAG, "AAAA" + mMessages.group(1));
                        originalMessageIDs.add(mMessages.group(1));
                    }

                    int counter = 0;
                    while(mAnswersNames.find() && mAnswersMessagesTexts.find()) {
                        Long messageId;
                        String sender = "";
                        String message = "";

                        sender = mAnswersNames.group(2).trim();
                        message = mAnswersMessagesTexts.group(1).trim();

                        Log.d(TAG, sender + ": " + message + " MessageID: " + originalMessageIDs.get(counter) + " AdID: " + dbHelper.getOriginalOlxAdId(objOlxAccounts.get(i).currentadid));
                        //if(true)return;

                        if(onPageNumberAd.equals(dbHelper.getOriginalOlxAdId(objOlxAccounts.get(i).currentadid))) {
                            messageId = new Long(originalMessageIDs.get(counter));


                            Boolean insertResult = dbHelper.adNewMessage(sender, message, originalMessageIDs.get(counter), dbHelper.getOriginalOlxAdId(objOlxAccounts.get(i).currentadid), uniqueChat, objOlxAccounts.get(i).accountid, ownerId);
                            // если вставлено новое сообщение, то показать уведомление
                            if(insertResult && !sender.equals("Ваше сообщение")) {
                                newMessage = true;

                                NotificationCompat.Builder builder =
                                        new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                                                .setSmallIcon(R.drawable.default_add_photo)
                                                .setContentTitle("Сообщение от " + sender)   //this is the title of notification
                                                .setColor(101)
                                                //.setAutoCancel(true)
                                                .setContentText(message);   //this is the message showed in notification
                                // активити открываемое при клике на уведомлении
                                Intent messageListIntent = new Intent(this, MessagesFromOneSenderActivity.class);
                                messageListIntent.putExtra("currentadid", objOlxAccounts.get(i).currentadid);
                                Log.d(TAG, "IDIDIIDIDIDIIID" + messageId);
                                messageListIntent.putExtra("messageid", messageId);
                                messageListIntent.putExtra("uniquechat", uniqueChat);

                                messageListIntent.putExtra("account", objOlxAccounts.get(i).accountid);
                                messageListIntent.putExtra("user", ownerId);

                                PendingIntent contentIntent = PendingIntent.getActivity(this, messageId.intValue(), messageListIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(contentIntent);
                                // Add as notification
                                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                // id- уникальный id чата на сайте
                                Long uchat = Long.parseLong(uniqueChat);
                                manager.notify(uchat.intValue(), builder.build());
                            }

                        }
                        //Log.d(TAG, sender + ": " + message);

                        //i++;
                        counter++;
                    }

                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput("answersHtml.txt", Context.MODE_PRIVATE));
                        outputStreamWriter.write(answersHtml);
                        outputStreamWriter.close();
                    }
                    catch (Exception ex) {
                        Log.e(TAG, "File write failed: " + ex.toString());
                    }

                    newAnswersTotal++;
                }

                if(newMessage){
                    Log.d(TAG, "NMNMNMNMNMNMNMNMNMNMNMNM");
                }
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
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        count++;
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        /*if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }*/
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
