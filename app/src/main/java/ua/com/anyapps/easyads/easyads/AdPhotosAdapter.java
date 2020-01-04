package ua.com.anyapps.easyads.easyads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;

public class AdPhotosAdapter extends BaseAdapter {


    private Context context;
    private ArrayList<adPhotoClass> AdPhotosGridViewItems = new ArrayList<adPhotoClass>();
    private static final String TAG = "debapp";
    static final int PICK_SELECT_PHOTO_REQUEST = 103;

    public AdPhotosAdapter(Context c, ArrayList<adPhotoClass> AdPhotosGridViewItems)
    {
        context = c;
        this.AdPhotosGridViewItems = AdPhotosGridViewItems;
    }

    //---returns the number of images---
    public int getCount() {
        return AdPhotosGridViewItems.size();
    }

    //---returns the ID of an item---
    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    //---returns an ImageView view---
    //private int photoPosition;
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        //photoPosition = position;
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setPadding(5, 5, 5, 5);
        } else {
            imageView = (ImageView) convertView;
        }
        if(imageView.getDrawable() == null){
            imageView.setImageURI(null);
            //imageView.setImageURI(AdPhotosGridViewItems.get(position).photo);
            //Log.d(TAG, "SS" + AdPhotosGridViewItems.get(position).currentPhoto);

            Picasso.get().load(Uri.parse(AdPhotosGridViewItems.get(position).currentPhoto)).into(imageView);

            //Log.d(TAG, "Photo---" + AdPhotosGridViewItems.get(position).photo);
        }

        //Log.d(TAG, "Drawable" + imageView.getDrawable().toString());

        /*imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Position:" + position);
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                ((Activity) context).startActivityForResult(photoPickerIntent, PICK_SELECT_PHOTO_REQUEST);
                //startActivityForResult(photoPickerIntent, "sd");
            }
        });*/

        return imageView;
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
        }
    }*/
}
