package ua.com.anyapps.easyads.easyads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;

import ua.com.anyapps.easyads.easyads.AdsList.AdSelectedCallback;

public class AdsListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater lInflater;
    ArrayList<OlxAd> objOlxAds;
    private static final String TAG = "debapp";

    private ArrayList<String> items = new ArrayList<String>();


    private AdSelectedCallback adSelected;

    public AdsListAdapter(Context context, ArrayList<OlxAd> objOlxAds) {
        this.context = context;
        this.objOlxAds = objOlxAds;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        adSelected = (AdSelectedCallback)context;
    }

    @Override
    public int getCount() {
        return objOlxAds.size();
    }

    @Override
    public Object getItem(int position) {
        return objOlxAds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    OlxAd getOlxAd(int position){
        return ((OlxAd)getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = lInflater.inflate(R.layout.ad_one_item_layout, parent, false);
        }

        OlxAd olxAd = getOlxAd(position);

        CheckBox cbAdTitle = (CheckBox)view.findViewById(R.id.cbAdTitle);
        //TextView tvUpdateDate = (TextView)view.findViewById(R.id.tvUpdateDate);

        cbAdTitle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    //Log.d(TAG, "+" + (String) v.getTag());
                    items.add((String) v.getTag());


                }
                else{
                    //Log.d(TAG, "-" + (String) v.getTag());

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
                adSelected.AdSelectedCallback(items.size());
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
        cbAdTitle.setTag(olxAd.id);


        return view;
    }

    public String getSelectedItems(){
        String result = "";

        JSONArray jsArray = new JSONArray(items);
        result = jsArray.toString();

        return result;
    }

    public String getSelectedId(){
        //Log.d(TAG, "SSSSSS:" + items.get(0));
        return ""+items.get(0);
    }

    public int getSelectedTotal(){
        return items.size();
    }
}
