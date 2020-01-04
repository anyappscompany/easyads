package ua.com.anyapps.easyads.easyads;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;

public class CityListAdapter extends BaseAdapter{
    Context context;
    LayoutInflater lInflater;
    ArrayList<City> objCities;
    private static final String TAG = "debapp";

    private ArrayList<String> items = new ArrayList<String>();

    public CityListAdapter(Context context, ArrayList<City> objCities) {
        this.context = context;
        this.objCities = objCities;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objCities.size();
    }

    @Override
    public Object getItem(int position) {
        return objCities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    City getCity(int position){
        return ((City)getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = lInflater.inflate(R.layout.cities_list_item, parent, false);
        }

        City city = getCity(position);
        TextView tvCity = (TextView)view.findViewById(R.id.tvCity);

        tvCity.setText(city.city);


        //CheckBox cbAdTitle = (CheckBox)view.findViewById(R.id.cbAdTitle);
        //TextView tvUpdateDate = (TextView)view.findViewById(R.id.tvUpdateDate);

        /*cbAdTitle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    Log.d(TAG, "+" + (String) v.getTag());
                    items.add((String) v.getTag());
                }
                else{
                    Log.d(TAG, "-" + (String) v.getTag());

                    Iterator<String> iter = items.iterator();
                    int pos=0;
                    String curItem="";
                    while ( iter .hasNext() == true )
                    {
                        pos=pos+1;
                        curItem =(String) iter .next();
                        if (curItem.equals((String) v.getTag())  ) {
                            items.remove(pos-1);
                            break;
                        }
                    }
                }
            }
        });

        if(!olxAd.activeinaccount.equals("null")){
            //cbAdTitle.setTextColor(Color.rgb(153,153,153));
            cbAdTitle.setTextColor(Color.rgb(153,153,153));
        }else {
            cbAdTitle.setTextColor(Color.BLACK);
        }
        cbAdTitle.setText(olxAd.title);
        //tvUpdateDate.setText(olxAd.createdate);
        cbAdTitle.setTag(olxAd.id);*/


        return view;
    }

    public String getSelectedItems(){
        String result = "";

        JSONArray jsArray = new JSONArray(items);
        result = jsArray.toString();

        return result;
    }
}
