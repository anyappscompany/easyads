package ua.com.anyapps.easyads.easyads;



import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ua.com.anyapps.easyads.easyads.CreateNewAd.AsyncTaskDeletePhotos;
import ua.com.anyapps.easyads.easyads.CreateNewAd.AsyncTaskLoadCategoriesLv1;
import ua.com.anyapps.easyads.easyads.CreateNewAd.AsyncTaskLoadCategoriesLv2;
import ua.com.anyapps.easyads.easyads.CreateNewAd.AsyncTaskLoadCategoriesLv3;
import ua.com.anyapps.easyads.easyads.CreateNewAd.DeleteAdCompleted;
import ua.com.anyapps.easyads.easyads.CreateNewAd.LoadCategoriesLv1Completed;
import ua.com.anyapps.easyads.easyads.CreateNewAd.LoadCategoriesLv2Completed;
import ua.com.anyapps.easyads.easyads.CreateNewAd.LoadCategoriesLv3Completed;

import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;




public class AddNewAdActivity extends AppCompatActivity implements LoadCategoriesLv1Completed, DeleteAdCompleted, LoadCategoriesLv2Completed, LoadCategoriesLv3Completed {

    private static final String TAG = "debapp";
    EditText etAdTitle;

    static final int PICK_SELECT_PHOTO_REQUEST = 103;
    static final int PICK_SELECT_CITY_REQUEST = 104;

    private SharedPreferences spPreferences;
    private String authToken;


    ProgressDialog uploadPhotoDialog;
    ProgressDialog deletePhotoDialog;


    Spinner spCategoriesLv1;
    Spinner spCategoriesLv2;
    Spinner spCategoriesLv3;

    // для каждого уровня списка категорий свой адаптер
    SelectCategoriesAdapterLv1 scAdapterLv1;
    SelectCategoriesAdapterLv2 scAdapterLv2;
    SelectCategoriesAdapterLv3 scAdapterLv3;

    OlxCategory targetCategory = new OlxCategory();
    private GridView photosList;


    private int GridViewClickedPosition;
    SQLiteDatabase db = null;
    DBHelper dbHelper = null;

    String newAdID = "";
    Context ctxt;
    String targetAccountID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_ad);
        ctxt = this;

        newAdID = UUID.randomUUID().toString();
        targetAccountID = getIntent().getStringExtra("targetAccountID");

        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            //Log.d(TAG, "Ошибка при получение бд: " + ex.getMessage());
        }
        //Log.d(TAG, " --- db v." + db.getVersion() + " --- ");


        etAdTitle = (EditText) findViewById(R.id.etAdTitle);

        spCategoriesLv1 = (Spinner) findViewById(R.id.spCategoriesLv1);
        spCategoriesLv2 = (Spinner) findViewById(R.id.spCategoriesLv2);
        spCategoriesLv3 = (Spinner) findViewById(R.id.spCategoriesLv3);



        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        authToken = spPreferences.getString(getString(R.string.auth_token), null);

        // запрос на получение списка категорий json
        //String targetUrl = getResources().getString(R.string.host) + "/?act=getcategories&authtoken=" + authToken;
        AsyncTaskLoadCategoriesLv1 task = new AsyncTaskLoadCategoriesLv1(AddNewAdActivity.this, this);
        task.execute();

        spCategoriesLv1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if(item!=null){
                    Log.d(TAG, "1POSITION: " + position);


                    String pos = null;
                    // удалить категорию по умолчанию, если уже была выбрана другая
                    //if(spCategoriesLv1List.get(position).id.equals("-1")){spCategoriesLv1List.remove(0);}
                    if(position>0){

                        if(spCategoriesLv1List.get(position).children.size()<=0) {
                            targetCategory = spCategoriesLv1List.get(position);
                            Log.d(TAG, "TARGET CATEGORY: " + spCategoriesLv1List.get(position).name);

                            spCategoriesLv2List.clear();
                            scAdapterLv2 = new SelectCategoriesAdapterLv2(AddNewAdActivity.this, spCategoriesLv2List);
                            //ArrayAdapter<String> spCategoriesLv1dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spCategoriesLv1ListStr);
                            spCategoriesLv2.setAdapter(scAdapterLv2);
                        }else{
                            AsyncTaskLoadCategoriesLv2 task = new AsyncTaskLoadCategoriesLv2(AddNewAdActivity.this, ctxt);
                            task.execute(spCategoriesLv1List.get(position).id);
                        }


                        /**/
                    }

                    // очистка категорий, если выбрана категория по умолчанию
                    if(position==0){

                        targetCategory = null;

                        // при сброшенной категории показать пустой фрагмент
                        showSettingsFragment("-1", "0");

                        spCategoriesLv2List.clear();
                        scAdapterLv2 = new SelectCategoriesAdapterLv2(AddNewAdActivity.this, spCategoriesLv2List);
                        //ArrayAdapter<String> spCategoriesLv1dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spCategoriesLv1ListStr);
                        spCategoriesLv2.setAdapter(scAdapterLv2);


                        spCategoriesLv3List.clear();
                        scAdapterLv3 = new SelectCategoriesAdapterLv3(AddNewAdActivity.this, spCategoriesLv3List);
                        //ArrayAdapter<String> spCategoriesLv1dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spCategoriesLv1ListStr);
                        spCategoriesLv3.setAdapter(scAdapterLv3);
                    }
                }
                //Log.d(TAG, "SIZE: " + bonding.size());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // если выбрано ВО ВТОРОМ списке, то показать категории в третьем
        spCategoriesLv2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if(item!=null){
                    Log.d(TAG, "2POSITION: " + position);

                    String pos = null;
                    // удалить категорию по умолчанию, если уже была выбрана другая
                    //if(spCategoriesLv1List.get(position).id.equals("-1")){spCategoriesLv1List.remove(0);}
                    if(position>=0){

                        if(spCategoriesLv2List.get(position).children.size()<=0) {
                            targetCategory = spCategoriesLv2List.get(position);
                            showSettingsFragment(targetCategory.id, targetCategory.max_photos);
                            Log.d(TAG, "TARGET CATEGORY: " + spCategoriesLv2List.get(position).name);

                            spCategoriesLv3List.clear();
                            scAdapterLv3 = new SelectCategoriesAdapterLv3(AddNewAdActivity.this, spCategoriesLv3List);
                            //ArrayAdapter<String> spCategoriesLv1dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spCategoriesLv1ListStr);
                            spCategoriesLv3.setAdapter(scAdapterLv3);
                        }else{
                            AsyncTaskLoadCategoriesLv3 task = new AsyncTaskLoadCategoriesLv3(AddNewAdActivity.this, ctxt);
                            task.execute(spCategoriesLv2List.get(position).id);
                        }

                        /**/
                    }


                }
                //Log.d(TAG, "SIZE: " + bonding.size());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spCategoriesLv3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if(item!=null){
                    Log.d(TAG, "POSITION3: " + position);

                    String pos = null;
                    // удалить категорию по умолчанию, если уже была выбрана другая
                    //if(spCategoriesLv1List.get(position).id.equals("-1")){spCategoriesLv1List.remove(0);}
                    if(position>=0){

                        if(spCategoriesLv3List.get(position).children.size()<=0) {
                            targetCategory = spCategoriesLv3List.get(position);
                            showSettingsFragment(targetCategory.id, targetCategory.max_photos);
                            Log.d(TAG, "TARGET CATEGORY: " + spCategoriesLv3List.get(position).name);
                        }
                    }


                }
                //Log.d(TAG, "SIZE: " + bonding.size());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/

        photosList = (GridView) findViewById(R.id.gvPhotos);
        registerForContextMenu(photosList);

        photosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                GridViewClickedPosition = position;
                //Log.d(TAG, "POS:" + position);

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_SELECT_PHOTO_REQUEST);
                /*startActivityForResult(
                        Intent.createChooser(
                                new Intent(Intent.ACTION_GET_CONTENT)
                                        .setType("image/*"), "Choose an image"),
                        PICK_SELECT_PHOTO_REQUEST);*/
            }
        });
        photosList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                GridViewClickedPosition = pos;
                //Log.d(TAG, "POS:" + pos);

                return false;
            }
        });

        // город
        etSelectCity = (EditText)findViewById(R.id.etSelectCity);
        //adSellerName = etSellerName.getText().toString();
        etSelectCity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent i = new Intent(v.getContext(), AddNewAddSelectCityActivity.class);
                if(MotionEvent.ACTION_UP == event.getAction())
                    startActivityForResult(i, PICK_SELECT_CITY_REQUEST);

                return false;
            }
        });
    }



    private static final int CM_DELETE_PHOTO_ID = 1;
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_PHOTO_ID, 0, R.string.clear_ad_photo_context_menu_title);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_PHOTO_ID) {
            //Log.d(TAG, "IMGID: " + GridViewClickedPosition);

            // получаем инфу о пункте списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // удаляем Map из коллекции, используя позицию пункта в списке
            //data.remove(acmi.position);
            adPhotoClass p = new adPhotoClass(this);
            p.oldPhoto = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString();
            p.currentPhoto = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString();
            p.slot = "";
            p.adPhotoId = "";
            //AdPhotosGridViewItems.set(GridViewClickedPosition, p);
            AdPhotosGridViewItems.remove(GridViewClickedPosition);
            AdPhotosGridViewItems.add(p);



            photosAdapter.notifyDataSetChanged();
            photosList.setAdapter(photosAdapter);

            return true;
        }
        return super.onContextItemSelected(item);
    }


    //String apollo_id = "";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_SELECT_PHOTO_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
                final Uri imageUri = data.getData();


                //AdPhotosGridViewItems.get(GridViewClickedPosition).photo = imageUri;
                //final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                //final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                //Drawable d = new BitmapDrawable(getResources(),selectedImage);
                //AdPhotosGridViewItems.get(GridViewClickedPosition).photo = imageUri;
                String photoUrl = "";

                /*String targetUrl = null;
                try {
                    targetUrl = getResources().getString(R.string.host) + "/?act=updatead&authtoken=" + authToken + "&adid="+targetAdId+"&title="+URLEncoder.encode(editAdTitle, "utf-8")+"&adjson=" + URLEncoder.encode(adJson, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }*/


                try {

                    adPhotoClass p = new adPhotoClass(this);
                    p.oldPhoto = imageUri.toString();
                    p.currentPhoto = imageUri.toString();
                    p.slot = "";
                    p.adPhotoId = "";
                    //AdPhotosGridViewItems.set(GridViewClickedPosition, p);

                    for(int j=0;j<AdPhotosGridViewItems.size();j++){
                        if(AdPhotosGridViewItems.get(j).currentPhoto.equals(Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString())){
                            AdPhotosGridViewItems.set(j, p);
                            break;
                        }else if(j>=(AdPhotosGridViewItems.size()-1)){
                            AdPhotosGridViewItems.set(GridViewClickedPosition, p);
                        }
                    }

                        photosAdapter.notifyDataSetChanged();
                        photosList.setAdapter(photosAdapter);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "AddNewAdActivity.java " + e.getMessage());
                }




                //task.execute("{\"riak_key\":\"" + riak_key + "\", \"token\":\"" + authToken + "\", \"idob\":\"" + newAdID + "\", \"pos\":\"" + GridViewClickedPosition + "\", \"type\":\"" + type + "\", \"type2\":\"" + type2 + "\", \"imguri\":\"" + imageUri + "\"}");

                //AdPhotosAdapter photosAdapter= new AdPhotosAdapter(AddNewAdActivity.this, AdPhotosGridViewItems);
                //photosList.setAdapter(photosAdapter);
                photosAdapter.notifyDataSetChanged();
                photosList.setAdapter(photosAdapter);

                //Log.d(TAG, ">>>>>>Фото выбрано");
                // Uri всех фото в GridView
            }
        }
        if (requestCode == PICK_SELECT_CITY_REQUEST) {
            if (resultCode == RESULT_OK) {

                //final String categoryJson = data.getData();
                String city = data.getStringExtra("city");
                String _id = data.getStringExtra("_id");
                String districtId = data.getStringExtra("district");

                EditText etSelectCity = findViewById(R.id.etSelectCity);
                etSelectCity.setText(city);

                //adCityId = _id;
                //adCityDistrictId = districtId;



                //Log.d(TAG, "ГОРОД ВЫБРАН:" + _id + " " + city);
            }
        }

        if (resultCode == RESULT_CANCELED) {
            //Log.d(TAG, "Выбор города - отмена");
            return;
        }
    }

    // ArrayList<PhotoGridViewItem> AdPhotosGridViewItems
    String adTitle;
    String adDescription;
    String adCategory;
    String adPhoneNumber;
    String adSellerName;
    String adCityId;
    String adCityDistrictId;


    ArrayList<urlParameter> urlParametres = new ArrayList<>();
    EditText etSelectCity;
    public void btnSaveAd(View v){
        //Log.d(TAG, "Нажата кнопка сохранить объявление");

        //Log.d(TAG, "TARGET CATEGORY: " + targetCategory.name);

        // добавленные фото
        for(int k =0;k<AdPhotosGridViewItems.size();k++){
            Log.d(TAG, "PHOTOS URI: " + AdPhotosGridViewItems.get(k));
        }

        int totalPhotoSlots = 0;
        for(int k =0;k<AdPhotosGridViewItems.size();k++){
            Log.d(TAG, "PHOTOS URI: " + AdPhotosGridViewItems.get(k).slot);
            if(!AdPhotosGridViewItems.get(k).currentPhoto.equals(Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString())){
                totalPhotoSlots++;
            }
        }
        Log.d(TAG, "TOTAL IMAGES FOR AD "+ totalPhotoSlots);

        if(totalPhotoSlots<=0) {
            Toast.makeText(ctxt, R.string.few_photos_for_new_ad, Toast.LENGTH_SHORT).show();
            return;
        }



        urlParametres.add(new urlParameter("adid", newAdID));


        // заголовок объявления
        EditText etAdTitle = (EditText)findViewById(R.id.etAdTitle);
        adTitle = etAdTitle.getText().toString();
        urlParametres.add(new urlParameter("data[title]", adTitle));

        // описание
        EditText etAdDescription = (EditText)findViewById(R.id.etAdDescription);
        adDescription = etAdDescription.getText().toString();
        urlParametres.add(new urlParameter("data[description]", adDescription));

        // номер телефона
        EditText etPhoneNumber = (EditText)findViewById(R.id.etPhoneNumber);
        adPhoneNumber = etPhoneNumber.getText().toString();
        urlParametres.add(new urlParameter("data[phone]", adPhoneNumber));

        // Контактное лицо
        EditText etSellerName = (EditText)findViewById(R.id.etSellerName);
        adSellerName = etSellerName.getText().toString();
        urlParametres.add(new urlParameter("data[person]", adSellerName));

        // текущая категория
        adCategory = targetCategory.id;
        urlParametres.add(new urlParameter("data[category_id]", adCategory));

        // параметры из фрагмента
        urlParametres.addAll(adSettingsFragment.getUrlParametres());

        // город
        EditText etSelectCity = findViewById(R.id.etSelectCity);

        adCityId = dbHelper.getCityId(etSelectCity.getText().toString());
        adCityDistrictId = dbHelper.getCityDistrict(etSelectCity.getText().toString());

        urlParametres.add(new urlParameter("data[city_id]", adCityId));
        urlParametres.add(new urlParameter("data[district_id]", adCityDistrictId));

        // фото
        urlParametres.add(new urlParameter("photos", AdPhotosGridViewItems));

        for(int i=0; i<urlParametres.size();i++){
            //Log.d(TAG, "Parameter " + i + ": " + urlParametres.get(i).key + "=" + urlParametres.get(i).value);
        }

        //JSONArray jsArray = new JSONArray(urlParametres);


        /*ArrayList<urlParameter> urlParametres2 = new ArrayList<>();
        urlParametres2.add(new urlParameter("data[title]", ""));
        urlParametres2.add(new urlParameter("data[description]", ""));
        urlParametres2.add(new urlParameter("data[phone]", ""));
        urlParametres2.add(new urlParameter("data[person]", ""));
        urlParametres2.add(new urlParameter("data[category_id]", "541"));
        urlParametres2.add(new urlParameter("data[param_price][0]", "price"));
        urlParametres2.add(new urlParameter("data[param_price][1]", ""));
        urlParametres2.add(new urlParameter("data[param_price][currency]", "UAH"));
        urlParametres2.add(new urlParameter("data[param_state]", ""));
        urlParametres2.add(new urlParameter("data[param_size]", ""));
        urlParametres2.add(new urlParameter("data[private_business]", "private"));
        urlParametres2.add(new urlParameter("data[city_id]", "5454"));
        urlParametres2.add(new urlParameter("data[district_id]", "3"));*/
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            //Log.d(TAG, "JSON:: " + gson.toJson(urlParametres));
        }catch (Exception ex){
            Log.d(TAG, ex.getMessage());
            //ex.printStackTrace();
        }

        //GsonBuilder builder2 = new GsonBuilder();
        //Gson gson2 = builder2.create();
        //Log.d(TAG, "JSON:: " + gson2.toJson(urlParametres));


        //Log.d(TAG, adSettingsFragment.testGetData());



        Intent intent = new Intent();
        intent.putExtra("adjson", gson.toJson(urlParametres));
        intent.putExtra("adtitle", adTitle);
        intent.putExtra("adid", newAdID);
        intent.putExtra("targetAccountID", targetAccountID);
        setResult(RESULT_OK, intent);
        finish();

        urlParametres.clear();
        /*loginProgressDialog = new ProgressDialog(LoginRegistrationActivity.this);
        loginProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loginProgressDialog.setCancelable(false);
        loginProgressDialog.setTitle(R.string.login_progress_dialog_title);
        loginProgressDialog.setMessage(getResources().getString(R.string.login_progress_dialog_message));
        loginProgressDialog.show();

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);

        AsyncTaskCheckExistUser task = new AsyncTaskCheckExistUser(LoginRegistrationActivity.this);

        String targetUrl = getResources().getString(R.string.host) + "/?act=checkexistuser&login=" + etLogin.getText().toString() + "&password=" + etPassword.getText().toString();
        Log.d(TAG, "Пользователь проверяется по адресу: " + targetUrl);
        task.execute(targetUrl);*/
    }



    private ArrayList<OlxCategory> spCategoriesLv1List = new ArrayList<OlxCategory>();
    private ArrayList<OlxCategory> spCategoriesLv2List = new ArrayList<OlxCategory>();
    private ArrayList<OlxCategory> spCategoriesLv3List = new ArrayList<OlxCategory>();
    JSONArray categories = null;


    private ArrayList<adPhotoClass> AdPhotosGridViewItems = new ArrayList<adPhotoClass>();

    AdPhotosAdapter photosAdapter;
    AdSettingsFragment adSettingsFragment;
    private void showSettingsFragment(String categoryId, String maxPhotos){
        Bundle bundle = new Bundle();
        bundle.putString("categoryid", categoryId);
        bundle.putString("maxphotos", maxPhotos);

        FragmentManager fragmentManager = getSupportFragmentManager();
        adSettingsFragment = new AdSettingsFragment();
        adSettingsFragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.containerForSettingFromFragments, adSettingsFragment).commit();

        AdPhotosGridViewItems.clear();
        for(int i = 0; i<Integer.parseInt(maxPhotos); i++){ Log.d(TAG, "count:" + i);
            //PhotoGridViewItem photo = new PhotoGridViewItem();
            //photo.photo = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo);
            adPhotoClass p = new adPhotoClass(this);
            p.oldPhoto = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString();
            p.currentPhoto = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString();

            p.slot = "";
            p.adPhotoId = "";
            AdPhotosGridViewItems.add(p);
        }
        Log.d(TAG, "Всего фоток:" + AdPhotosGridViewItems.size());
        photosAdapter= new AdPhotosAdapter(AddNewAdActivity.this, AdPhotosGridViewItems);

        photosList.setAdapter(photosAdapter);
        //AdPhotosGridViewItems.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //idob\":\""+newAdID
        // Удаление фото при нажатии назад
        String targetUrl = getResources().getString(R.string.host) + "/?act=deletephotos&authtoken=" + authToken + "&adid=" + newAdID;
        //Log.d(TAG, getResources().getString(R.string.host) + "/?act=deletephotos&authtoken=" + authToken + "&adid=" + newAdID);
        AsyncTaskDeletePhotos task = new AsyncTaskDeletePhotos(AddNewAdActivity.this, this);
        task.execute(targetUrl);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void DeleteAdCompleted(String response) {
        //
    }

    @Override
    public void LoadCategoriesLv1Completed(String response) {
        Log.d(TAG, "Категории Lv1 загружены: " + response);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        spCategoriesLv1List = gson.fromJson(response, new TypeToken<List<OlxCategory>>(){}.getType());
        // если выбрано в первом списке, то показать категории во втором


    }


    @Override
    public void LoadCategoriesLv2Completed(String response) {
        Log.d(TAG, "Категории Lv2 загружены: " + response);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        spCategoriesLv2List = gson.fromJson(response, new TypeToken<List<OlxCategory>>(){}.getType());



    }

    @Override
    public void LoadCategoriesLv3Completed(String response) {
        spCategoriesLv3.setSelection(0);
        Log.d(TAG, "Категории Lv3 загружены: " + response);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        spCategoriesLv3List = gson.fromJson(response, new TypeToken<List<OlxCategory>>(){}.getType());


    }
}
