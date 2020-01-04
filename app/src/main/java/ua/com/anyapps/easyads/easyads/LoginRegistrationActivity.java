package ua.com.anyapps.easyads.easyads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import ua.com.anyapps.easyads.easyads.Login.AsyncTaskCheckExistUser;
import ua.com.anyapps.easyads.easyads.Login.CheckExistUserCompleted;
import ua.com.anyapps.easyads.easyads.RegistrationNewUser.AsyncTaskRegistrationNewUser;
import ua.com.anyapps.easyads.easyads.RegistrationNewUser.RegistrationNewUserCompleted;

public class LoginRegistrationActivity extends AppCompatActivity implements RegistrationNewUserCompleted, CheckExistUserCompleted {

    EditText etLogin;
    EditText etPassword;
    private static final String TAG = "debapp";

    ProgressDialog registrationProgressDialog;
    ProgressDialog loginProgressDialog;

    private SharedPreferences spPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_registration);

        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public void btnLoginClick(View v){
        //Log.d(TAG, "Нажата кнопка войти");

        loginProgressDialog = new ProgressDialog(LoginRegistrationActivity.this);
        loginProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loginProgressDialog.setCancelable(false);
        loginProgressDialog.setTitle(R.string.login_progress_dialog_title);
        loginProgressDialog.setMessage(getResources().getString(R.string.login_progress_dialog_message));
        loginProgressDialog.show();

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);

        AsyncTaskCheckExistUser task = new AsyncTaskCheckExistUser(LoginRegistrationActivity.this, this);

        String targetUrl = getResources().getString(R.string.host) + "/?act=checkexistuser&login=" + etLogin.getText().toString() + "&password=" + etPassword.getText().toString();
        //Log.d(TAG, "Пользователь проверяется по адресу: " + targetUrl);
        task.execute(targetUrl);
    }

    public void btnRegistrationClick(View v){
        //Log.d(TAG, "Нажата кнопка регистрации");
        registrationProgressDialog = new ProgressDialog(LoginRegistrationActivity.this);
        registrationProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        registrationProgressDialog.setCancelable(false);
        registrationProgressDialog.setTitle(R.string.user_registration_progress_dialog_title);
        registrationProgressDialog.setMessage(getResources().getString(R.string.user_registration_progress_dialog_message));
        registrationProgressDialog.show();

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);

        // регистрация нового пользователя в отдельномпотоке
        AsyncTaskRegistrationNewUser task = new AsyncTaskRegistrationNewUser(LoginRegistrationActivity.this, this);

        String targetUrl = getResources().getString(R.string.host) + "/?act=registrationnewuser&login=" + etLogin.getText().toString() + "&password=" + etPassword.getText().toString();
        //Log.d(TAG, "Новый пользователь регистрируется по адресу: " + targetUrl);
        task.execute(targetUrl);
    }

    @Override
    public void RegistrationNewUserCompleted(String response) {
        registrationProgressDialog.dismiss();

        JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(response);
            String error = dataJsonObj.getString("error");
            // если в ответе отсутствуют ошибки
            if(error.equals("0")){
                //Log.d(TAG, "Регистрация пользователя завершена удачно. Токен записан в хранилище.");
                SharedPreferences.Editor editor = spPreferences.edit();
                // сохранить токен в хранилище
                editor.putString(getString(R.string.auth_token), dataJsonObj.getString("authtoken"));
                editor.commit();
                // перейти на начальный экран после успешной регистрации
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                // регистрация не удалась
                //Log.d(TAG, "Регистрация пользователя не удалась.");
                Toast.makeText(LoginRegistrationActivity.this, getString(R.string.user_registration_failed), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            //Log.d(TAG, "Исключение во время регистрации нового пользователя: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void CheckExistUserCompleted(String response) {
        loginProgressDialog.dismiss();
        // ПРОВЕРИТЬ НА СЕРВЕРЕ СУЩЕСТВОВАНИЕ ПОЛЬЗОВАТЕЛЯ И ЕСЛИ ОН ЕСТЬ, ТО ПЕРЕЗАПИСАТЬ В PREFERENCES
        //Log.d(TAG, "Проверка существования пользователя завершена.");
        JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(response);
            String error = dataJsonObj.getString("error");
            // если в ответе отсутствуют ошибки
            if(error.equals("0")){
                String result = dataJsonObj.getString("result");
                if(result.equals("1")) {
                    // существует, вход
                    //Log.d(TAG, "Пользователь существует->вход." + dataJsonObj.getString("authtoken"));
                    SharedPreferences.Editor editor = spPreferences.edit();
                    // сохранить токен в хранилище
                    editor.putString(getString(R.string.auth_token), dataJsonObj.getString("authtoken"));
                    editor.putString(getString(R.string.current_user), dataJsonObj.getString("id"));
                    editor.commit();
                    // перейти на начальный экран после входа
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    // пользователь не существует
                    Toast.makeText(LoginRegistrationActivity.this, getString(R.string.login_user_not_exist), Toast.LENGTH_LONG).show();
                    //Log.d(TAG, "Пользователь с таким логином не зарегистрирован.");
                }
            }else{
                // регистрация не удалась
                //Log.d(TAG, "Проверка существования пользователя завершилась с ошибкой.");
                Toast.makeText(LoginRegistrationActivity.this, getString(R.string.login_failed), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            //Log.d(TAG, "Исключение во время входа в аккаунт: " + e.getMessage());
            //e.printStackTrace();
        }
    }
}