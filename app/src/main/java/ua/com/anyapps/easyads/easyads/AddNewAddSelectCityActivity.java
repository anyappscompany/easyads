package ua.com.anyapps.easyads.easyads;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class AddNewAddSelectCityActivity extends AppCompatActivity {

    private static final String TAG = "debapp";
    SQLiteDatabase db = null;
    DBHelper dbHelper = null;
    CityListAdapter cityListAdapter;
    ArrayList<City> autosuggest = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_add_select_city);

        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getDatabase();
        }catch (Exception ex){
            //Log.d(TAG, "Ошибка при получение бд: " + ex.getMessage());
        }
        //Log.d(TAG, " --- db v." + db.getVersion() + " --- ");

        ListView lvSelectCity = (ListView) findViewById(R.id.lvSelectCity);
        cityListAdapter = new CityListAdapter(AddNewAddSelectCityActivity.this, autosuggest);
        lvSelectCity.setAdapter(cityListAdapter);


        EditText etSelectCity = findViewById(R.id.etSelectCityInActivity);
        etSelectCity.addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                // Прописываем то, что надо выполнить после изменения текста
                if(s.toString().length()>=3) {
                    //Log.d(TAG, "Editable: " + s.toString());

                    autosuggest.clear();
                    autosuggest.addAll(dbHelper.autosuggest(s.toString()));
                    cityListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        lvSelectCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.setSelected(true);
                //Log.d(TAG, "CITY: " + position + " " + autosuggest.get(position).city);

                Intent intent = new Intent();
                intent.putExtra("_id", autosuggest.get(position)._id);
                intent.putExtra("city", autosuggest.get(position).city);
                intent.putExtra("district", autosuggest.get(position).district);
                intent.putExtra("id", autosuggest.get(position).id);
                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }

    public void btnSetCityClick(View v){
        Intent intent = new Intent();
        intent.putExtra("cityinfo", "{json}");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void btnSetCityClickCancel(View v){
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent );
        finish();
    }
}
