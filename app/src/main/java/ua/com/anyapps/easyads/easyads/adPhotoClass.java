package ua.com.anyapps.easyads.easyads;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

public class adPhotoClass{
    public String oldPhoto = "";
    public String currentPhoto = "";
    public String slot="";
    public String adPhotoId = "";
    public adPhotoClass(Context ctxt){
        oldPhoto = Uri.parse("android.resource://"+ctxt.getPackageName()+"/" + R.drawable.default_add_photo).toString();
        currentPhoto = Uri.parse("android.resource://"+ctxt.getPackageName()+"/" + R.drawable.default_add_photo).toString();
    }
    public String getSlot(){
        return this.slot;
    }
}
