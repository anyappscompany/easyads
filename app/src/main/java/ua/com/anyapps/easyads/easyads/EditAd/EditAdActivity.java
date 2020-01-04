package ua.com.anyapps.easyads.easyads.EditAd;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;

import ua.com.anyapps.easyads.easyads.AdPhotosAdapter;
import ua.com.anyapps.easyads.easyads.AdSettingsFragment;
import ua.com.anyapps.easyads.easyads.AddNewAddSelectCityActivity;
import ua.com.anyapps.easyads.easyads.AdsList.GetAdsForEditCompleted;
import ua.com.anyapps.easyads.easyads.CreateNewAd.AsyncTaskLoadCategoriesForEdit;
import ua.com.anyapps.easyads.easyads.CreateNewAd.LoadCategoriesForEditCompleted;
import ua.com.anyapps.easyads.easyads.DBHelper;
import ua.com.anyapps.easyads.easyads.FullHeightGridView;
import ua.com.anyapps.easyads.easyads.OlxCategory;
import ua.com.anyapps.easyads.easyads.R;
import ua.com.anyapps.easyads.easyads.SelectCategoriesAdapterLv1;
import ua.com.anyapps.easyads.easyads.SelectCategoriesAdapterLv2;
import ua.com.anyapps.easyads.easyads.SelectCategoriesAdapterLv3;
import ua.com.anyapps.easyads.easyads.adPhotoClass;
import ua.com.anyapps.easyads.easyads.urlParameter;

import static java.util.Collections.sort;

public class EditAdActivity extends AppCompatActivity implements GetAdsForEditCompleted, UpdateAdCompleted, PhotoChangeCompleted, LoadCategoriesForEditCompleted, DeleteAdPhotoCompleted, UploadPhotoCompleted {

    ProgressDialog getAdsForEdittDialog;
    private SharedPreferences spPreferences;
    private String authToken;

    private static final String TAG = "debapp";

    ProgressDialog getCategoriesDialog;
    ProgressDialog updateAdDialog;
    ProgressDialog uploadPhotoDialog;
    ProgressDialog deletePhotoDialog;
    ProgressDialog updatePhotoInfDialog;
    String originalOlxAdId = "";

    static final int PICK_SELECT_PHOTO_REQUEST = 103;
    static final int PICK_SELECT_CITY_REQUEST = 104;

    Spinner spCategoriesLv1;
    Spinner spCategoriesLv2;
    Spinner spCategoriesLv3;

    // для каждого уровня списка категорий свой адаптер
    SelectCategoriesAdapterLv1 scAdapterLv1;
    SelectCategoriesAdapterLv2 scAdapterLv2;
    SelectCategoriesAdapterLv3 scAdapterLv3;

    OlxCategory targetCategory = new OlxCategory();
    private FullHeightGridView photosList;

    SQLiteDatabase db = null;
    DBHelper dbHelper = null;
    String targetAdId;

    String uniqAdID = "";
    String riakkey = "";
    AdPhotosAdapter photosAdapter;
    String adId = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_edit_ads);

        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            //Log.d(TAG, "Ошибка при получение бд: " + ex.getMessage());
        }
        //Log.d(TAG, " --- db v." + db.getVersion() + " --- ");


        setContentView(R.layout.activity_edit_ads);

        spCategoriesLv1 = (Spinner) findViewById(R.id.spCategoriesLv1);
        spCategoriesLv2 = (Spinner) findViewById(R.id.spCategoriesLv2);
        spCategoriesLv3 = (Spinner) findViewById(R.id.spCategoriesLv3);

        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        authToken = spPreferences.getString(getString(R.string.auth_token), null);

        /*getAdsForEdittDialog = new ProgressDialog(EditAdActivity.this);
        getAdsForEdittDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        getAdsForEdittDialog.setCancelable(false);
        getAdsForEdittDialog.setTitle(R.string.edit_ads_list_progress_dialog_title);
        getAdsForEdittDialog.setMessage(getResources().getString(R.string.edit_ads_list_progress_dialog_message));
        getAdsForEdittDialog.show();*/


        Bundle b = getIntent().getExtras();
        String jsonCheckedAds = ""; // or other values
        if(b != null) {
            //jsonCheckedAds = b.getString("jsoncheckedads");
            targetAdId = b.getString("targetAdID");
            //adID = b.getString("adid");
            //Log.d(TAG, "cc" + targetAdId);
        }

        riakkey = dbHelper.getRiakKey(b.getString("targetAdID"));
        originalOlxAdId = dbHelper.getOriginalOlxAdId(b.getString("targetAdID"));

        String response = adData =dbHelper.getAd(targetAdId);

        JSONObject dataJsonObj = null;
        try {
            //dataJsonObj = new JSONObject(response);
            //String adid = dataJsonObj.getString("adid");
            //Log.d(TAG, "-------" + response);

                // данные редактируемого объявления
                //Log.d(TAG, "1111111111111" + dataJsonObj.getString("ads"));


                //Log.d(TAG, "Всего объявлений для редактирования: " + ads.length());



                    String jsonAd = "";
                    //JSONObject jo = ads.get;

                    //Log.d(TAG, "-----" + jo.toString());

                    //Log.d(TAG, ">>>" +  jsonObject.get("key").getAsString());


                    //jsonAd = "{\"value\":\"Amouage\",\"key\":\"data[title]\"},{\"value\":\"5000\",\"key\":\"data[description]\"}";



                    JSONArray jsonArr = new JSONArray(response);

                    //JSONObject obj = new JSONObject(jsonAd);// ads.getString(i)


                    // в зависимость от категории, подгрузить настройки объявления


                    for (int k = 0; k < jsonArr.length(); k++)
                    {
                        JSONObject jsonObj = jsonArr.getJSONObject(k);

                        //Log.d(TAG, "OBject: " + jsonObj.get("key") + jsonObj.get("value"));
                        //if(true) return;


                        String key = (String)jsonObj.get("key");

                        switch(key){
                            case "adid":
                                adId = (String)jsonObj.get("value"); //Log.d(TAG, "111111-" + adID);
                                //Log.d(TAG, "DDDDDDDDDDDDDDDDD" + adTitle);
                                break;
                            case "data[title]":
                                adTitle = (String)jsonObj.get("value");
                                //Log.d(TAG, "DDDDDDDDDDDDDDDDD" + adTitle);
                                break;
                            case "data[description]":
                                adDescription = (String)jsonObj.get("value");
                                break;
                            case "data[phone]":
                                adPhoneNumber = (String)jsonObj.get("value");
                                break;
                            case "data[category_id]":
                                adCategory = (String)jsonObj.get("value");
                                break;
                            case "data[person]":
                                adSellerName = (String)jsonObj.get("value");
                                break;
                            case "data[city_id]":
                                adCityId = (String)jsonObj.get("value");
                                break;
                            case "data[district_id]":
                                adCityDistrictId = (String)jsonObj.get("value");
                                break;
                            case "photos":
                                JSONArray photosArray = new JSONArray((String)jsonObj.get("value")); // jsonObj.getJSONArray("value");
                                //Log.d(TAG, "Всего фоток: " + photosArray.length());
                                Log.d(TAG, "Заполнение массива с фото");
                                for (int s=0; s < photosArray.length(); s++) {
                                    //JSONObject jo = photosArray.getJSONObject(s);

                                    //JSONArray  photoInfoArray = jo.getJSONArray("photo");
                                    //Log.d(TAG, "Всего инфы: " + photoInfoArray.length());
                                    //Log.d(TAG, "Всего инфы: " + jo.get("photo"));

                                    //JSONObject photoParams = new JSONObject(jo.get("photo").toString());
                                    //Log.d(TAG, "PPPP: " + photoParams.get("uriString"));
                                    //PhotoGridViewItem photo = new PhotoGridViewItem();
                                    //photo.photo = Uri.parse(photoParams.get("uriString").toString());

                                    //Log.d(TAG, "zzzzzzz"+photosArray.getString(s));
                                    JSONObject adPhotoClassObj = new JSONObject(photosArray.getString(s));

                                    //Log.d(TAG, "SSSSSSSSSSSSSSS" + adPhotoClassObj.toString());

                                    adPhotoClass p = new adPhotoClass(this);
                                    p.oldPhoto = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString();//adPhotoClassObj.getString("currentPhoto");
                                    p.currentPhoto = adPhotoClassObj.getString("currentPhoto");
                                    p.slot = adPhotoClassObj.getString("slot");
                                    p.adPhotoId = adPhotoClassObj.getString("adPhotoId");
                                    //Log.d(TAG, "111111" + adPhotoClassObj.getString("currentPhoto"));
                                    AdPhotosGridViewItems.add(p);
                                    Log.d(TAG, "ID фото на сервере: " + adPhotoClassObj.optString("adPhotoId", "") + " Slot: " + adPhotoClassObj.optString("slot", "") + " Старое фото: " + adPhotoClassObj.getString("oldPhoto") + " Текущее фото " + adPhotoClassObj.getString("currentPhoto"));
                                }


                                // сортировка фото только со слотами
                                int totaPhotos = AdPhotosGridViewItems.size();

                                ArrayList<adPhotoClass> photoListWithSlots = new ArrayList<>();
                                ArrayList<adPhotoClass> photosResult = new ArrayList<>();

                                for(int u=0;u<AdPhotosGridViewItems.size(); u++){
                                    if(AdPhotosGridViewItems.get(u).slot.toString().length()>0){
                                        photoListWithSlots.add(AdPhotosGridViewItems.get(u));
                                    }
                                }
                                int totalPhotosWithSlots = photoListWithSlots.size();

                                photosResult = new ArrayList<adPhotoClass>(photoListWithSlots);
                                /*Collections.sort(photosResult, new Comparator<adPhotoClass>() {
                                    public int compare(adPhotoClass p1, adPhotoClass p2) {
                                        return Integer.valueOf(Integer.parseInt(p1.getSlot())).compareTo(Integer.parseInt(p2.getSlot()));
                                    }
                                });*/
                                AdPhotosGridViewItems = photosResult;
                                // добавление пустых элементов в список фото
                                for(int s = AdPhotosGridViewItems.size(); s<totaPhotos; s++){
                                    adPhotoClass p = new adPhotoClass(this);
                                    p.oldPhoto = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString();
                                    p.currentPhoto = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString();
                                    p.slot = "";
                                    p.adPhotoId = "";
                                    //Log.d(TAG, "111111" + adPhotoClassObj.getString("currentPhoto"));
                                    AdPhotosGridViewItems.add(p);
                                }





                                Log.d(TAG, "Фоток со слотами " +totalPhotosWithSlots);
                                Log.d(TAG, "Всего офток " +totaPhotos);
                                // сортировака - пустые фото в конец списка
                                /*for(int m=0;m<AdPhotosGridViewItems.size(); m++){
                                    //Log.d(TAG, AdPhotosGridViewItems.get(m).currentPhoto + " compare " + Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString());
                                    if(AdPhotosGridViewItems.get(m).currentPhoto.equals(Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString())){

                                        adPhotoClass p = new adPhotoClass(this);
                                        p.oldPhoto = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString();
                                        p.currentPhoto = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString();
                                        p.slot = "";
                                        p.adPhotoId = "";
                                        AdPhotosGridViewItems.add(p);
                                        AdPhotosGridViewItems.remove(m);
                                    }
                                }*/

                                adMaxPhotos = String.valueOf(AdPhotosGridViewItems.size());
                                break;
                            default:
                                break;
                        }
                        //Log.d(TAG, ">>>>>>>>>" + adCategory);
                    }



                    //Log.d(TAG, jsonArr.length() + "+++++++++" + ads.getString(i));
                    // НАПОЛНЕНИЕ ФОРМ







                //lvAdsList.setAdapter(adsListAdapter);

        } catch (JSONException e) {
            Log.e(TAG, "Исключение в приложении во время получения списка объявлений для редактирования: " + e.getMessage());
            //e.printStackTrace();
        }

        photosList = (FullHeightGridView) findViewById(R.id.gvPhotos);
        registerForContextMenu(photosList);

        photosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                GridViewClickedPosition = position;
                //Log.d(TAG, "POS:" + position);

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_SELECT_PHOTO_REQUEST);
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


        //Log.d(TAG, error2);
        // Город
        String cityName = dbHelper.getCityByIdAndDistrict(adCityId, adCityDistrictId);
        EditText etSelectCity = findViewById(R.id.etSelectCity);
        etSelectCity.setText(cityName);


        for(int h=0;h<AdPhotosGridViewItems.size();h++){
            //JSONObject jok = new JSONObject(AdPhotosGridViewItems.);
            //Log.d(TAG, "XX " + AdPhotosGridViewItems.get(h).currentPhoto);
        }

        photosAdapter= new AdPhotosAdapter(EditAdActivity.this, AdPhotosGridViewItems);
        photosList.setAdapter(photosAdapter);

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

        // Заголовок
        EditText etAdTitle = findViewById(R.id.etAdTitle);
        etAdTitle.setText(adTitle);
        //Log.d(TAG, "SSSSSSSSSSSSSSSSS"+adTitle);
        // Описание
        EditText etAdDescription = findViewById(R.id.etAdDescription);
        etAdDescription.setText(adDescription);
        //  Телефон
        EditText etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etPhoneNumber.setText(adPhoneNumber);
        // Контактное лицо
        EditText etSellerName = findViewById(R.id.etSellerName);
        etSellerName.setText(adSellerName);




        spPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        authToken = spPreferences.getString(getString(R.string.auth_token), null);

        // запрос на получение списка категорий json
        //Log.d(TAG, "MAX PHOTOS " + adMaxPhotos);
        showSettingsFragment(adCategory, adMaxPhotos);

        AsyncTaskLoadCategoriesForEdit task = new AsyncTaskLoadCategoriesForEdit(EditAdActivity.this, this);
        task.execute(adCategory);
    }

    String adTitle;
    String adDescription;
    String adCategory;
    String adMaxPhotos;
    String adPhoneNumber;
    String adSellerName;
    String adCityId;
    String adCityDistrictId;
    ArrayList<adPhotoClass> AdPhotosGridViewItems = new ArrayList<adPhotoClass>();

    private int GridViewClickedPosition;
    private String adData = "";
    //String apollo_id = "";

    @Override
    public void GetAdsForEditCompleted(String response) {
        getAdsForEdittDialog.dismiss();

        //Log.d(TAG, "Список объявлений для редактирования получен. Ответ сервера:" + response);




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
            //Log.d(TAG, "AdPhotosGridViewItems total: " + AdPhotosGridViewItems.size() + " POSITION " + GridViewClickedPosition + " APOLLO"+ AdPhotosGridViewItems.get(GridViewClickedPosition).apollo_id);
            ArrayList<adPhotoClass> photoListWithSlots = new ArrayList<>();
            int totaPhotos = AdPhotosGridViewItems.size();
            for(int u=0;u<AdPhotosGridViewItems.size(); u++){
                if(AdPhotosGridViewItems.get(u).slot.toString().length()>0){
                    photoListWithSlots.add(AdPhotosGridViewItems.get(u));
                }
            }
            int totalPhotosWithSlots = photoListWithSlots.size();

            // если фото загружено в слот
            if(AdPhotosGridViewItems.get(GridViewClickedPosition).slot.toString().length()>0 && totalPhotosWithSlots>=2) {
                deletePhotoDialog = new ProgressDialog(EditAdActivity.this);
                deletePhotoDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                deletePhotoDialog.setCancelable(false);
                deletePhotoDialog.setTitle(R.string.delete_photo_progress_dialog_title);
                deletePhotoDialog.setMessage(getResources().getString(R.string.delete_photo_progress_dialog_message));
                deletePhotoDialog.show();

                Log.d(TAG, "{\"riak_key\":\"" + riakkey + "\", \"originalOlxAdId\":\""+originalOlxAdId+"\" , \"authtoken\":\"" + authToken + "\", \"idob\":\"" + adId + "\", \"slot\":\"" + AdPhotosGridViewItems.get(GridViewClickedPosition).slot + "\", \"apollo_id\":\"" + AdPhotosGridViewItems.get(GridViewClickedPosition).adPhotoId + "\"}");
                //if(true) return true;
                AsyncDeleteAdPhoto task = new AsyncDeleteAdPhoto(EditAdActivity.this, this);
                task.execute("{\"riak_key\":\"" + riakkey + "\", \"originalOlxAdId\":\""+originalOlxAdId+"\" , \"authtoken\":\"" + authToken + "\", \"idob\":\"" + adId + "\", \"slot\":\"" + AdPhotosGridViewItems.get(GridViewClickedPosition).slot + "\", \"apollo_id\":\"" + AdPhotosGridViewItems.get(GridViewClickedPosition).adPhotoId + "\"}");

            }

            return true;
        }
        return super.onContextItemSelected(item);
    }


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

                //Log.d(TAG, "URI:" +GridViewClickedPosition + "" + imageUri);

                //AdPhotosGridViewItems.get(GridViewClickedPosition).photo = imageUri;
                //final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                //final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                //Drawable d = new BitmapDrawable(getResources(),selectedImage);


                boolean freeSlot = false;
                for(int y=0; y<AdPhotosGridViewItems.size();y++){
                    if(AdPhotosGridViewItems.get(y).currentPhoto.equals(Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString())){
                        freeSlot = true;
                        break;
                    }
                }

                // если есть свободные слоты
                if(freeSlot) {
                    uploadPhotoDialog = new ProgressDialog(EditAdActivity.this);
                    uploadPhotoDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    uploadPhotoDialog.setCancelable(false);
                    uploadPhotoDialog.setTitle(R.string.upload_photo_progress_dialog_title);
                    uploadPhotoDialog.setMessage(getResources().getString(R.string.upload_photo_progress_dialog_message));
                    uploadPhotoDialog.show();

                    AsyncTaskUploadPhoto task = new AsyncTaskUploadPhoto(EditAdActivity.this, this);
                    //task.execute("{\"riak_key\":\""+riak_key+"\", \"token\":\""+authToken+"\", \"idob\":\""+adId+"\", \"pos\":\""+GridViewClickedPosition+"\", \"type\":\""+type+"\", \"type2\":\""+type2+"\", \"imguri\":\""+imageUri+"\"}");
                    task.execute("{\"riak_key\":\"" + riakkey + "\", \"originalOlxAdId\":\"" + originalOlxAdId + "\" , \"authtoken\":\"" + authToken + "\", \"idob\":\"" + adId + "\", \"pos\":\"" + GridViewClickedPosition + "\", \"adcategory\":\""+adCategory+"\" , \"imguri\":\"" + imageUri + "\"}");
                }



            }
        }

        if (requestCode == PICK_SELECT_CITY_REQUEST) {
            if (resultCode == RESULT_OK) {

                //final String categoryJson = data.getData();
                String city = data.getStringExtra("city");
                String _id = data.getStringExtra("_id");
                String districtId = data.getStringExtra("district");

                adCityId = data.getStringExtra("id");
                adCityDistrictId = data.getStringExtra("district");

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

    private ArrayList<OlxCategory> spCategoriesLv1List = new ArrayList<OlxCategory>();
    private ArrayList<OlxCategory> spCategoriesLv2List = new ArrayList<OlxCategory>();
    private ArrayList<OlxCategory> spCategoriesLv3List = new ArrayList<OlxCategory>();
    JSONArray categories;

    int selectedIndex = 0; // индекс категории в спинере
    int count = 0;
    String categoryId = "0";
    Deque<String> catsIndexes = new LinkedList<>();



    AdSettingsFragment adSettingsFragment;
    private void showSettingsFragment(String categoryId, String maxPhotos){
        Bundle bundle = new Bundle();
        bundle.putString("categoryid", categoryId);
        bundle.putString("maxphotos", maxPhotos);
        bundle.putString("data", adData);

        FragmentManager fragmentManager = getSupportFragmentManager();
        adSettingsFragment = new AdSettingsFragment();
        adSettingsFragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.containerForSettingFromFragments, adSettingsFragment).commit();

        /*AdPhotosGridViewItems.clear();
        for(int i = 0; i<Integer.parseInt(maxPhotos); i++){ Log.d(TAG, "count:" + i);
            PhotoGridViewItem photo = new PhotoGridViewItem();
            photo.photo = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo);
            AdPhotosGridViewItems.add(photo);
        }*/
        //Log.d(TAG, "Всего фоток:" + AdPhotosGridViewItems.size());
        //photosAdapter= new AdPhotosAdapter(EditAdActivity.this, AdPhotosGridViewItems);

        //photosList.setAdapter(photosAdapter);
        //AdPhotosGridViewItems.clear();
    }

    // ArrayList<PhotoGridViewItem> AdPhotosGridViewItems
    String editAdTitle;
    String editAdDescription;
    String editAdCategory;
    String editAdPhoneNumber;
    String editAdSellerName;
    String editAdCityId;
    String editAdCityDistrictId;


    ArrayList<urlParameter> urlParametres = new ArrayList<>();
    EditText etSelectCity;
    public void btnSaveAd(View v){
        //Log.d(TAG, "Нажата кнопка сохранить объявление");

        //Log.d(TAG, "TARGET CATEGORY: " + targetCategory.name);

        // добавленные фото


        urlParametres.add(new urlParameter("adid", adId));

        // заголовок объявления
        EditText etAdTitle = (EditText)findViewById(R.id.etAdTitle);
        editAdTitle = etAdTitle.getText().toString();
        urlParametres.add(new urlParameter("data[title]", editAdTitle));

        // описание
        EditText etAdDescription = (EditText)findViewById(R.id.etAdDescription);
        editAdDescription = etAdDescription.getText().toString();
        urlParametres.add(new urlParameter("data[description]", editAdDescription));

        // номер телефона
        EditText etPhoneNumber = (EditText)findViewById(R.id.etPhoneNumber);
        editAdPhoneNumber = etPhoneNumber.getText().toString();
        urlParametres.add(new urlParameter("data[phone]", editAdPhoneNumber));

        // Контактное лицо
        EditText etSellerName = (EditText)findViewById(R.id.etSellerName);
        editAdSellerName = etSellerName.getText().toString();
        urlParametres.add(new urlParameter("data[person]", editAdSellerName));

        // текущая категория
        editAdCategory = adCategory;
        urlParametres.add(new urlParameter("data[category_id]", editAdCategory));

        // параметры из фрагмента
        urlParametres.addAll(adSettingsFragment.getUrlParametres());

        // город
        editAdCityId = adCityId;
        editAdCityDistrictId = adCityDistrictId;

        urlParametres.add(new urlParameter("data[city_id]", editAdCityId));
        urlParametres.add(new urlParameter("data[district_id]", editAdCityDistrictId));

        // фото
        urlParametres.add(new urlParameter("photos", AdPhotosGridViewItems));

        Log.d(TAG, "************************************************************************");
        for(int g=0; g<AdPhotosGridViewItems.size();g++){
            //Log.d(TAG, "P"+g+" "+AdPhotosGridViewItems.get(g).photo.toString());
            Log.d(TAG, "ID фото на сервере: " + AdPhotosGridViewItems.get(g).adPhotoId+ " Slot: " + AdPhotosGridViewItems.get(g).slot + " Старое фото: " + AdPhotosGridViewItems.get(g).oldPhoto + " Текущее фото " + AdPhotosGridViewItems.get(g).currentPhoto);
        }
        //Log.d(TAG, "************************************************************************");


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
            //Log.d(TAG, "Для пользователя: " + authToken + " обновляется объявление: " + gson.toJson(urlParametres));
        }catch (Exception ex){
            //Log.d(TAG, ex.getMessage());
            ex.printStackTrace();
        }

        //GsonBuilder builder2 = new GsonBuilder();
        //Gson gson2 = builder2.create();
        //Log.d(TAG, "JSON:: " + gson2.toJson(urlParametres));

        /*updateAdDialog = new ProgressDialog(EditAdActivity.this);
        updateAdDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        updateAdDialog.setCancelable(false);
        updateAdDialog.setTitle(R.string.edit_ad_progress_dialog_title);
        updateAdDialog.setMessage(getResources().getString(R.string.edit_ad_progress_dialog_message));
        updateAdDialog.show();*/

        /*
        intent.putExtra("adjson", gson.toJson(urlParametres));
        intent.putExtra("adtitle", adTitle);
         */
        String adJson = gson.toJson(urlParametres);
        //String editAdTitle = editAdTitle;


        //Log.d(TAG, "TITLE: " + editAdTitle);
        //Log.d(TAG, "JSON: " + adJson);
        //finish();
        /*String targetUrl = null;
        try {
            targetUrl = getResources().getString(R.string.host) + "/?act=updatead&authtoken=" + authToken + "&adid="+adId+"&title="+URLEncoder.encode(editAdTitle, "utf-8")+"&adjson=" + URLEncoder.encode(adJson, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        AsyncTaskUpdateAd task = new AsyncTaskUpdateAd(EditAdActivity.this, this);
        task.execute(targetUrl);*/
        //Log.d(TAG, adSettingsFragment.testGetData());

        /*Intent intent = new Intent();
        intent.putExtra("adjson", gson.toJson(urlParametres));
        intent.putExtra("adtitle", editAdTitle);
        setResult(RESULT_OK, intent);
        finish();*/

        //


        Intent intent = new Intent();
        intent.putExtra("adjson", gson.toJson(urlParametres));
        intent.putExtra("adtitle", adTitle);
        intent.putExtra("adid", adId);
        intent.putExtra("adcategory", adCategory);


        //intent.putExtra("targetAccountID", targetAccountID);
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



    @Override
    public void UpdateAdCompleted(String response) {
        updateAdDialog.dismiss();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();

    }





    @Override
    public void PhotoChangeCompleted(String response) {
        updatePhotoInfDialog.dismiss();
    }

    @Override
    public void LoadCategoriesForEditCompleted(String response) {
        Log.d(TAG, "Categories for edit LOADED FINISH");
    }

    @Override
    public void DeleteAdPhotoCompleted(String response) {
        deletePhotoDialog.dismiss();


        try {
            JSONObject dataJsonObj2 = null;
            dataJsonObj2 = new JSONObject(response);
            Log.d(TAG, "1111111111" + dataJsonObj2.getString("status"));
            if (dataJsonObj2.getString("status").equals("ok")) {
                AdPhotosGridViewItems.remove(GridViewClickedPosition);
                Log.d(TAG, "Фото статус ОК" +GridViewClickedPosition);
                adPhotoClass p = new adPhotoClass(this);
                p.oldPhoto = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.drawable.default_add_photo).toString();
                p.currentPhoto = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.drawable.default_add_photo).toString();
                p.slot = "";
                p.adPhotoId = "";
                AdPhotosGridViewItems.add(p);



// сортировка фото только со слотами
                ArrayList<adPhotoClass> photoListWithSlots = new ArrayList<>();
                ArrayList<adPhotoClass> photosResult = new ArrayList<>();
                int totaPhotos = AdPhotosGridViewItems.size();
                for(int u=0;u<AdPhotosGridViewItems.size(); u++){
                    if(AdPhotosGridViewItems.get(u).slot.toString().length()>0){
                        photoListWithSlots.add(AdPhotosGridViewItems.get(u));
                    }
                }
                int totalPhotosWithSlots = photoListWithSlots.size();

                photosResult = new ArrayList<adPhotoClass>(photoListWithSlots);
                /*Collections.sort(photosResult, new Comparator<adPhotoClass>() {
                    public int compare(adPhotoClass p1, adPhotoClass p2) {
                        return Integer.valueOf(Integer.parseInt(p1.getSlot())).compareTo(Integer.parseInt(p2.getSlot()));
                    }
                });*/
                //AdPhotosGridViewItems = photosResult;
                AdPhotosGridViewItems.clear();
                AdPhotosGridViewItems.addAll(photosResult);

                // добавление пустых элементов в список фото
                for(int s = AdPhotosGridViewItems.size(); s<totaPhotos; s++){
                    //Log.d(TAG, "111111" + adPhotoClassObj.getString("currentPhoto"));
                    AdPhotosGridViewItems.add(p);
                }


                updatePhotoInf();
                //после удаления сохранение в базу

                // уведомляем, что данные изменились
                photosAdapter.notifyDataSetChanged();
                photosList.setAdapter(photosAdapter);
                //photosAdapter= new AdPhotosAdapter(EditAdActivity.this, AdPhotosGridViewItems);
                //photosList.setAdapter(photosAdapter);
            }
        }catch (Exception ex){
            Log.e(TAG, "Фото не удалено");
        }
    }

    private void updatePhotoInf() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        //Log.d(TAG, "Нажата кнопка сохранить объявление");
        ArrayList<urlParameter> urlParametres2 = new ArrayList<>();
        try {
            JSONArray ad = new JSONArray(adData);
            for (int k = 0; k < ad.length(); k++) { // параметры объявления
                JSONObject ob;
                ob = new JSONObject(ad.get(k) + "");
                //Log.d(TAG, ">>>>>kex33y: " + ob.getString("key") + " value: " + ob.getString("value"));

                if (ob.getString("key").equals("photos")) {
                    JSONArray adPhotoClassArrs = new JSONArray(ob.getString("value"));


                    urlParametres2.add(new urlParameter("photos", gson.toJson(AdPhotosGridViewItems)));


                } else {
                    urlParametres2.add(new urlParameter(ob.getString("key"), ob.getString("value")));
                }

            }
            //Log.d(TAG, "TARGET CATEGORY: " + targetCategory.name);
            //Запись в базу изменений только фото !
            String newAdJson = gson.toJson(urlParametres2);
            dbHelper.updateAdJson(newAdJson, adId);

            // добавленные фото
        }catch (Exception ex){
            Log.e(TAG, "Ошибка удаления фото " + ex.getMessage());
        }
    }

    @Override
    public void UploadPhotoCompleted(String response) { //не выполняются несколько действий подряд и очередность фото при открытии редактирования
        uploadPhotoDialog.dismiss();

        JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(response);

            if(dataJsonObj.getString("status").equals("success")){
                for(int l=0;l<AdPhotosGridViewItems.size();l++){
                    // поиск свободного слота для вставки нового фото
                    if(AdPhotosGridViewItems.get(l).currentPhoto.equals(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.drawable.default_add_photo).toString())){
                        adPhotoClass p = new adPhotoClass(EditAdActivity.this);
                        p.slot = dataJsonObj.getString("slot");
                        p.adPhotoId = dataJsonObj.getString("apollo_id");
                        p.oldPhoto = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.drawable.default_add_photo).toString();
                        p.currentPhoto = dataJsonObj.getString("url_thumb");
                        Log.d(TAG, "SLOT " + dataJsonObj.getString("slot"));
                        Log.d(TAG, "adPhotoId " + dataJsonObj.getString("apollo_id"));
                        Log.d(TAG, "oldPhoto " + Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.drawable.default_add_photo).toString());
                        Log.d(TAG, "currentPhoto " + dataJsonObj.getString("url_thumb"));
                        Log.d(TAG, "Найдет слот для вставки нового фото " + l);
                        AdPhotosGridViewItems.set(l, p);
                        break;
                    }
                }




                adPhotoClass p2 = new adPhotoClass(this);
                p2.oldPhoto = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.drawable.default_add_photo).toString();
                p2.currentPhoto = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.drawable.default_add_photo).toString();
                p2.slot = "";
                p2.adPhotoId = "";

                // сортировка фото только со слотами
                ArrayList<adPhotoClass> photoListWithSlots = new ArrayList<>();
                ArrayList<adPhotoClass> photosResult = new ArrayList<>();
                int totaPhotos = AdPhotosGridViewItems.size();
                for(int u=0;u<AdPhotosGridViewItems.size(); u++){
                    if(AdPhotosGridViewItems.get(u).slot.toString().length()>0){
                        photoListWithSlots.add(AdPhotosGridViewItems.get(u));
                    }
                }
                int totalPhotosWithSlots = photoListWithSlots.size();

                //photosResult.clear();
                photosResult = new ArrayList<adPhotoClass>(photoListWithSlots);
                /*Collections.sort(photosResult, new Comparator<adPhotoClass>() {
                    public int compare(adPhotoClass p1, adPhotoClass p2) {
                        return Integer.valueOf(Integer.parseInt(p1.getSlot())).compareTo(Integer.parseInt(p2.getSlot()));
                    }
                });*/
                //AdPhotosGridViewItems.clear();
                AdPhotosGridViewItems.clear();
                AdPhotosGridViewItems.addAll(photosResult);
                //AdPhotosGridViewItems = photosResult;

                // добавление пустых элементов в список фото
                for(int s = AdPhotosGridViewItems.size(); s<totaPhotos; s++){
                    //Log.d(TAG, "111111" + adPhotoClassObj.getString("currentPhoto"));
                    AdPhotosGridViewItems.add(p2);
                }


                updatePhotoInf();
                photosAdapter.notifyDataSetChanged();
                photosList.setAdapter(photosAdapter);
                //photosAdapter= new AdPhotosAdapter(EditAdActivity.this, AdPhotosGridViewItems);
                //photosList.setAdapter(photosAdapter);

                //apollo_id = dataJsonObj.getString("apollo_id");
                //riak_key = dataJsonObj.getString("riak_key");
                //Log.d(TAG, "URL"+dataJsonObj.getString("url"));
                //Log.d(TAG, "uploadedphoto " + dataJsonObj.getString("url_thumb") + " slot " + dataJsonObj.getString("slot"));
                //AdPhotosGridViewItems.get(Integer.parseInt(dataJsonObj.getString("slot"))-1).photo = Uri.parse(dataJsonObj.getString("url_thumb"));
                //AdPhotosGridViewItems.get(Integer.parseInt(dataJsonObj.getString("slot"))-1).apollo_id = dataJsonObj.getString("apollo_id");
                //AdPhotosGridViewItems.get(Integer.parseInt(dataJsonObj.getString("slot"))-1).baseUriPath = Uri.parse(dataJsonObj.getString("baseUriPath"));



            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "EditAdActivity.java " + e.getMessage());
        }
/*
        // загрузить фото и получить урл, если не получилось загрузить то давать урл фото по умолчанию
        String photoUrl = "";
        try {
            adPhotoClass p = new adPhotoClass(this);
            p.oldPhoto = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString();
            p.currentPhoto = imageUri.toString();
            p.slot = AdPhotosGridViewItems.get(GridViewClickedPosition).slot;
            p.adPhotoId = AdPhotosGridViewItems.get(GridViewClickedPosition).adPhotoId;

            //AdPhotosGridViewItems.set(GridViewClickedPosition, p);

            // если клик по путому, то поиск пустого места

            for(int j=0;j<AdPhotosGridViewItems.size();j++){
                if(AdPhotosGridViewItems.get(j).currentPhoto.equals(Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.default_add_photo).toString())){
                    AdPhotosGridViewItems.set(j, p);
                    break;
                }else if(j>=(AdPhotosGridViewItems.size()-1)){
                    adPhotoClass p1 = new adPhotoClass(this);
                    p1.oldPhoto = AdPhotosGridViewItems.get(j).currentPhoto;
                    p1.currentPhoto = imageUri.toString();
                    p1.slot = AdPhotosGridViewItems.get(GridViewClickedPosition).slot;
                    p1.adPhotoId = AdPhotosGridViewItems.get(GridViewClickedPosition).adPhotoId;

                    AdPhotosGridViewItems.set(GridViewClickedPosition, p1);
                }
            }




            photosAdapter.notifyDataSetChanged();
            photosList.setAdapter(photosAdapter);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "EditAdActivity.java " + e.getMessage());
        }




        //task.execute("{\"riak_key\":\"" + riak_key + "\", \"token\":\"" + authToken + "\", \"idob\":\"" + newAdID + "\", \"pos\":\"" + GridViewClickedPosition + "\", \"type\":\"" + type + "\", \"type2\":\"" + type2 + "\", \"imguri\":\"" + imageUri + "\"}");

        //AdPhotosAdapter photosAdapter= new AdPhotosAdapter(AddNewAdActivity.this, AdPhotosGridViewItems);
        //photosList.setAdapter(photosAdapter);
        photosAdapter.notifyDataSetChanged();
        photosList.setAdapter(photosAdapter);

        //Log.d(TAG, ">>>>>>Фото выбрано");
        // Uri всех фото в GridView
        */
    }
}
