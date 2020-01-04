package ua.com.anyapps.easyads.easyads;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccountsListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater lInflater;
    ArrayList<OlxAccount> objOlxAccounts;
    private static final String TAG = "debapp";
    private ArrayList<String> selectedItems = new ArrayList<String>();

    private ArrayList<AcToAd> bonding = new ArrayList<AcToAd>();

    public AccountsListAdapter(Context context, ArrayList<OlxAccount> objOlxAccounts) {
        this.context = context;
        this.objOlxAccounts = objOlxAccounts;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return objOlxAccounts.size();
    }

    @Override
    public Object getItem(int position) {
        return objOlxAccounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    Integer selectedPosition = -1;
    Spinner spin1 = null;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = lInflater.inflate(R.layout.account_one_item_layout, parent, false);
        }

        OlxAccount olxAc = getOlxAccount(position);
        TextView tvAccountTitle = (TextView) view.findViewById(R.id.tvAccountTitle);
        Button btnNewAd = (Button) view.findViewById(R.id.btnNewAd);
        Button btnDeleteAccountAd = (Button) view.findViewById(R.id.btnDeleteAccountAd);
        Button btnEditAd = (Button) view.findViewById(R.id.btnEditAd);
        Button btnUpdateCookies = (Button) view.findViewById(R.id.btnUpdateCookies);
        TextView tvAdTitle = (TextView) view.findViewById(R.id.tvAdTitle);
        ImageView ivFirstAdPhoto = (ImageView) view.findViewById(R.id.ivFirstAdPhoto);
        tvAccountTitle.setText(olxAc.email);
        tvAccountTitle.setTag(olxAc.accountid);

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

        btnUpdateCookies.setTag(olxAc.accountid);
        btnDeleteAccountAd.setTag(olxAc.accountid);
        btnNewAd.setTag(olxAc.accountid);
        btnEditAd.setTag(olxAc.currentadid);

        if(olxAc.currentadid!=null) {


            btnNewAd.setVisibility(View.GONE);
            btnDeleteAccountAd.setVisibility(View.VISIBLE);
            btnEditAd.setVisibility(View.VISIBLE);
        }else{

            btnNewAd.setVisibility(View.VISIBLE);
            btnEditAd.setVisibility(View.GONE);
            btnDeleteAccountAd.setVisibility(View.GONE);
        }


        if(olxAc.currentadtitle!=null && olxAc.currentadtitle.length()>0){
            tvAdTitle.setText(olxAc.currentadtitle);

            btnNewAd.setVisibility(View.GONE);
            btnDeleteAccountAd.setVisibility(View.VISIBLE);
            btnEditAd.setVisibility(View.VISIBLE);
        }else{
            btnNewAd.setVisibility(View.VISIBLE);
            btnEditAd.setVisibility(View.GONE);
            btnDeleteAccountAd.setVisibility(View.GONE);
        }

        if(olxAc.currentadphoto!=null && olxAc.currentadphoto.length()>0){
            ivFirstAdPhoto.setVisibility(View.VISIBLE);
            //Log.d(TAG, "))))"+olxAc.currentadphoto);
            Picasso.get().load(Uri.parse(olxAc.currentadphoto)).into(ivFirstAdPhoto);

            //ivFirstAdPhoto.setImageBitmap(BitmapFactory.decodeFile(olxAc.currentadphoto));
        }else{
            ivFirstAdPhoto.setVisibility(View.GONE);
        }



        return view;
    }

    OlxAccount getOlxAccount(int position){
        return ((OlxAccount)getItem(position));
    }

    public String getSelectedItems(){
        String result = "";

        JSONArray jsArray = new JSONArray(selectedItems);
        result = jsArray.toString();

        return result;
    }

    public Integer toBondingTotalItems(){
        return bonding.size();
    }

    public String getDataForBonding(){
        //Log.d(TAG, "1111111111111111");
        ArrayList<AcToAd> resultBonding = new ArrayList<AcToAd>();
        //Log.d(TAG, "Размер списка: " + bonding.size());
        for(int i=bonding.size()-1; i>=0; i--){
            //Log.d(TAG, "TEST: " + bonding.get(i).currentadid);

            // проверка, если id ака уже существует, то пропуск
            boolean proverka = false;
            for(int k=0; k<resultBonding.size(); k++){
                if(resultBonding.get(k).accountid.equals(bonding.get(i).accountid)){
                    proverka = true;
                    break;
                }
            }
            if(!proverka) {
                resultBonding.add(bonding.get(i));
            }
        }
        //Log.d(TAG, "WWWWWWWWWWW: " + resultBonding.size());

        String result = "[";
        Iterator<AcToAd> iter = resultBonding.iterator();
        int pos=0;
        AcToAd curItem=new AcToAd();
        ArrayList<String> bounds = new ArrayList<String>();
        Integer totalBounds = 0;
        while ( iter .hasNext() == true )
        {
            pos=pos+1;
            curItem =(AcToAd) iter .next();
            //Log.d(TAG, "<" + curItem.accountid + ">");
            //Log.d(TAG, "<" + curItem.currentadid + ">");
            bounds.add("{\"accountid\":\"" + curItem.accountid + "\", \"adid\":\"" + curItem.currentadid + "\"}");
            //Отправка на сервер json строки
            totalBounds++;
        }
        result += TextUtils.join(",", bounds);
        result += "]";
        if(totalBounds>0) {
            return result;
        }else{
            return null;
        }
    }
}
