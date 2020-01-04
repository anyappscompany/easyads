package ua.com.anyapps.easyads.easyads;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdSettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
class PhotoGridViewItem{
    public Uri photo;
}
public class AdSettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CATEGORY_ID = "categoryid";
    private static final String MAX_PHOTOS = "maxphotos";

    private static final String TAG = "debapp";
    private String targetCategory = "-1";
    private String maxPhotos = "0";

    // TODO: Rename and change types of parameters
    private String paramCategoryId;
    private String paramMaxPhotos;

    private OnFragmentInteractionListener mListener;

    public AdSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * param param1 Parameter 1.
     * param param2 Parameter 2.
     * @return A new instance of fragment AdSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdSettingsFragment newInstance(String categoryid, String maxphotos) {
        AdSettingsFragment fragment = new AdSettingsFragment();
        Bundle args = new Bundle();
        args.putString(CATEGORY_ID, categoryid);
        args.putString(MAX_PHOTOS, maxphotos);
        fragment.setArguments(args);
        return fragment;
    }

    ArrayList<urlParameter> urlParametres = new ArrayList<>();
    public ArrayList<urlParameter> getUrlParametres(){
        //

        urlParametres.clear();
        // определение цены
        RadioGroup rgPrice = (RadioGroup) getView().findViewById(R.id.rgPrice);
        int radioButtonID = rgPrice.getCheckedRadioButtonId();
        View radioButton = rgPrice.findViewById(radioButtonID);
        int idx = rgPrice.indexOfChild(radioButton);


        switch(idx){
            case 0:
                urlParametres.add(new urlParameter("data[param_price][0]", "free"));
                break;
            case 1:
                urlParametres.add(new urlParameter("data[param_price][0]", "exchange"));
                break;
            case 2:
                urlParametres.add(new urlParameter("data[param_price][0]", "price"));

                EditText etPrice = (EditText) getView().findViewById(R.id.etPrice);
                urlParametres.add(new urlParameter("data[param_price][1]", etPrice.getText().toString()));

                Spinner spPriceCurrency = (Spinner) getView().findViewById(R.id.spPriceCurrency);
                String keyvalue = getResources().getStringArray(R.array.data_param_price_currency_values)[spPriceCurrency.getSelectedItemPosition()];
                urlParametres.add(new urlParameter("data[param_price][currency]", keyvalue));

                CheckBox cbPriceArranged = (CheckBox)getView().findViewById(R.id.cbPriceArranged);
                if(cbPriceArranged.isChecked()){
                    urlParametres.add(new urlParameter("data[param_price][0]", "arranged"));
                }
                break;
            default:
                break;
        }

        switch (targetCategory){
            case "541":
            case "70":
            case "540":
                // размер в этих категориях
                EditText etSize = (EditText) getView().findViewById(R.id.etSize);
                urlParametres.add(new urlParameter("data[param_size]", etSize.getText().toString()));
                break;
            case "893":
                break;
            case "5414":
            case "888":
                //
                break;
            default:
                break;
        }

        // состояние
        Spinner spState = (Spinner) getView().findViewById(R.id.spState);
        String stateValue = getResources().getStringArray(R.array.data_param_state_values)[spState.getSelectedItemPosition()];
        urlParametres.add(new urlParameter("data[param_state]", stateValue));



        // частное или бизнес
        Spinner spPrivateBusiness = (Spinner) getView().findViewById(R.id.spPrivateBusiness);
        String privateBusinessValue = getResources().getStringArray(R.array.data_private_business_values)[spPrivateBusiness.getSelectedItemPosition()];
        urlParametres.add(new urlParameter("data[private_business]", privateBusinessValue));

        return urlParametres;
    }

    //AdPhotosAdapter photosAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            paramCategoryId = targetCategory= getArguments().getString(CATEGORY_ID);
            paramMaxPhotos = maxPhotos = getArguments().getString(MAX_PHOTOS);

            /*photosAdapter = new AdPhotosAdapter(getContext(), AdPhotosGridViewItems);

            GridView photosList = null;
            photosList = (GridView) getView().findViewById(R.id.gvPhotos);
            photosList.setAdapter(photosAdapter);*/
            //EditText et = getView().findViewById(R.id.trrrrrr);
        }
        //Log.d(TAG, "-----------onCreate");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d(TAG, "-----------onCreateView");
        // Inflate the layout for this fragment
        int settingsLayout = R.layout.fragment_ad_settings;
        //Log.d(TAG, targetCategory);
        switch (targetCategory){
            case "541":
            case "70":
            case "540":
                settingsLayout = R.layout.settings_layout1;
                // Цена, Состояние, Размер, Частное лицо / Бизнес
                break;
            case "893":
                settingsLayout = R.layout.settings_layout2;
                // Цена, Состояние, Размер, Частное лицо / Бизнес
                break;
            case "5414":
            case "888":
                //
                break;
            default:
                settingsLayout = R.layout.fragment_ad_settings;
                break;
        }

        View view = inflater.inflate(settingsLayout, container, false);
        // установка подгружаемых параметров объявления
        boolean arranged = false;

        if(getArguments().getString("data")!=null){
            String data = getArguments().getString("data");

            try {
                    JSONArray jsonArr = new JSONArray(data);
                    //Log.d(TAG, "Всего параметров объявления: " + jsonArr.length());
                        for (int k = 0; k < jsonArr.length(); k++){

                            JSONObject jsonObj = jsonArr.getJSONObject(k);
                            //Log.d(TAG, "OBject: " + jsonObj.get("key"));
                            String key = (String)jsonObj.get("key");

                            switch(key){
                                case "data[param_price][1]": {
                                    EditText etPrice = (EditText) view.findViewById(R.id.etPrice);
                                    etPrice.setText((String)jsonObj.get("value"));
                                    }
                                    break;
                                case "data[param_size]": {
                                    EditText etSize = (EditText) view.findViewById(R.id.etSize);
                                    etSize.setText((String)jsonObj.get("value"));
                                }
                                break;
                                case "data[param_state]": {
                                    Spinner spState = (Spinner) view.findViewById(R.id.spState);
                                    //String[] data_param_state_values = getResources().getStringArray(R.array.data_param_state_values);
                                    ArrayList<String> data_param_state_values = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.data_param_state_values)));
                                    ArrayList<String> data_param_state_keys = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.data_param_state_keys)));

                                    int pos = data_param_state_values.indexOf((String)jsonObj.get("value"));

                                    ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, data_param_state_keys);
                                    stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spState.setAdapter(stateAdapter);
                                    spState.setSelection(pos);

                                }
                                break;
                                case "data[private_business]": {
                                    Spinner spPrivateBusiness = (Spinner) view.findViewById(R.id.spPrivateBusiness);
                                    //String[] data_param_state_values = getResources().getStringArray(R.array.data_param_state_values);
                                    ArrayList<String> data_private_business_values = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.data_private_business_values)));
                                    ArrayList<String> data_private_business_keys = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.data_private_business_keys)));

                                    int pos = data_private_business_values.indexOf((String)jsonObj.get("value"));

                                    ArrayAdapter<String> private_businessAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, data_private_business_keys);
                                    private_businessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spPrivateBusiness.setAdapter(private_businessAdapter);
                                    spPrivateBusiness.setSelection(pos);

                                }
                                break;
                                case "data[param_price][0]": {
                                    RadioGroup rgPrice = (RadioGroup) view.findViewById(R.id.rgPrice);

                                    switch((String)jsonObj.get("value")){
                                        case "free":
                                            //Log.d(TAG, "PRICE free");
                                            rgPrice.check(R.id.radioFree);
                                            break;
                                        case "exchange":
                                            //Log.d(TAG, "PRICE exchange");
                                            rgPrice.check(R.id.radioExchange);
                                            break;
                                        case "price":
                                            //Log.d(TAG, "PRICE price");
                                            rgPrice.check(R.id.radioPrice);
                                            break;
                                        default:
                                            break;
                                    }

                                    if(arranged ==false && jsonObj.get("value").equals("arranged")){
                                        arranged = true;
                                        CheckBox cbPriceArranged = (CheckBox) view.findViewById(R.id.cbPriceArranged);
                                        cbPriceArranged.setChecked(true);
                                    }
                                }
                                break;
                                case "data[param_price][currency]": {
                                    Spinner spPriceCurrency = (Spinner) view.findViewById(R.id.spPriceCurrency);
                                    //String[] data_param_state_values = getResources().getStringArray(R.array.data_param_state_values);
                                    ArrayList<String> data_param_price_currency_values = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.data_param_price_currency_values)));
                                    ArrayList<String> data_param_price_currency_keys = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.data_param_price_currency_keys)));

                                    int pos = data_param_price_currency_values.indexOf((String)jsonObj.get("value"));

                                    ArrayAdapter<String> price_currencyAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, data_param_price_currency_keys);
                                    price_currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spPriceCurrency.setAdapter(price_currencyAdapter);
                                    spPriceCurrency.setSelection(pos);
                                }
                                break;
                                default:
                                    break;
                            }

                        }
            }catch ( Exception ex){
                //Log.d(TAG, "Ошибка во время редактирования объявления в фрагменте: " + ex.getMessage() + ex.toString());
            }


        }else{
            //Log.d(TAG, "Показан фрагмент во время создания объявления");
        }

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            /*throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");*/
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
