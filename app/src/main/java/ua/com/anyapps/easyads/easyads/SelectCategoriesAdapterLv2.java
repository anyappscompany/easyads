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

public class SelectCategoriesAdapterLv2 extends BaseAdapter {
    Context context;
    LayoutInflater lInflater;
    ArrayList<OlxCategory> objOlxCategories = new ArrayList<OlxCategory>();
    private static final String TAG = "debapp";

    private ArrayList<String> items = new ArrayList<String>();

    public SelectCategoriesAdapterLv2(Context context, ArrayList<OlxCategory> objOlxCategories) {
        this.context = context;
        this.objOlxCategories = objOlxCategories;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return objOlxCategories.size();
    }

    @Override
    public Object getItem(int position) {
        return objOlxCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    OlxCategory getOlxCategory(int position){
        return ((OlxCategory)getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = lInflater.inflate(R.layout.select_category_item_layout, parent, false);
        }

        OlxCategory olxCategory = getOlxCategory(position);

        //CheckBox cbAdTitle = (CheckBox)view.findViewById(R.id.cbAdTitle);
        TextView tvCategoryTitle = (TextView)view.findViewById(R.id.tvCategoryTitle);

        tvCategoryTitle.setText(olxCategory.name);


        //tvCategoryTitle.setTag(olxCategory.id);


        return view;
    }

    public String getSelectedItems(){
        String result = "";

        JSONArray jsArray = new JSONArray(items);
        result = jsArray.toString();

        return result;
    }
}
