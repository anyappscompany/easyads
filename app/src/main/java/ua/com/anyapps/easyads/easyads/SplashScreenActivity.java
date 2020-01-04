package ua.com.anyapps.easyads.easyads;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isDigitsOnly;

public class SplashScreenActivity extends AppCompatActivity {

    private SharedPreferences spPreferences;
    private String authToken;
    private static final String TAG = "debapp";

    SQLiteDatabase db = null;
    DBHelper dbHelper = null;

    public static String CATEGORIES_FILE_DIRECTORY_PATH = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);





        // При первом запуске копируется уже готовая база из файла в assets
        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getDatabase();
            Log.d(TAG, "SplashScreenActivity.java - Версия db v." + db.getVersion());
        }catch (Exception ex){
            Log.e(TAG, "SplashScreenActivity.java - Ошибка доступа к базе данных: " + ex.getMessage());
        }

        //dbHelper.dsfsdfsdf();

        CATEGORIES_FILE_DIRECTORY_PATH = "/data/data/" + this.getPackageName() + "/files/"; // cw.getFilesDir().getAbsolutePath()+ "/databases/";

        File f = new File(CATEGORIES_FILE_DIRECTORY_PATH + "categories.json");

        File directory = new File(CATEGORIES_FILE_DIRECTORY_PATH);
        if (! directory.exists()){
            directory.mkdir();
        }


        if(!f.exists()){
            byte[] buffer = new byte[1024];
            OutputStream myOutput = null;
            int length;
            // Open your local db as the input stream
            InputStream myInput = null;
            try {
                myInput = this.getAssets().open("categories.json");
                // transfer bytes from the inputfile to the
                // outputfile
                myOutput = new FileOutputStream(CATEGORIES_FILE_DIRECTORY_PATH + "categories.json");
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.close();
                myOutput.flush();
                myInput.close();
                //Log.i(TAG,                        "New database has been copied to device!");


            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Ошибка при копировании файла категорий: " + e.getMessage());
            }
        }
        //dbHelper.saveCookies("sveta@mail.ru", "my cook");


        /*File f2 = new File(CATEGORIES_FILE_DIRECTORY_PATH + "page.txt");
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(f2));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        Pattern pActive = Pattern.compile("Активные<\\/span> <span class=\"counter fnormal\">\\s+\\((.*?)\\)\\s+<\\/span>");
        Matcher mActive = pActive.matcher(text.toString());
        while(mActive.find()) {
            if (mActive.group(1).length() > 0 && isDigitsOnly(mActive.group(1))) {
                Log.d(TAG, "+++++++++++++");
            }
        }*/


        /*HashMap<String, String> params = new HashMap<String, String>();
        params.put("par1", "val1");
        params.put("par2", "val2");
        params.put("par3", "val3");
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("head1", "val1");
        headers.put("head2", "val2");
        headers.put("head3", "val3");
        Utilities.POSTQuery("https://stackoverflow.com", params, headers, getApplicationContext());*/



        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        authToken = spPreferences.getString(getString(R.string.auth_token), null);
        if(authToken != null){
            // Переход в кабинет пользователя
            Log.d(TAG, "SplashScreenActivity.java - В хранилище есть токен. Переход в кабинет пользователя.");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            // Пользователь не вошел. Показать ему форму регистрации/входа
             Log.d(TAG, "SplashScreenActivity.java - Токен в хранилище отсутствует. Переход на страницу регистрации.");
            Intent intent = new Intent(this, LoginRegistrationActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
