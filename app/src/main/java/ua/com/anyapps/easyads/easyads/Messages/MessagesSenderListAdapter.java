package ua.com.anyapps.easyads.easyads.Messages;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ua.com.anyapps.easyads.easyads.R;

public class MessagesSenderListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater lInflater;
    ArrayList<SenderInfo> senders;
    private static final String TAG = "debapp";

    public MessagesSenderListAdapter(Context context, ArrayList<SenderInfo> _senders) {
        this.context = context;
        this.senders = _senders;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for(int f=0; f<senders.size();f++) {
            Log.d(TAG, "SENDERS COUNT " + senders.get(f).adTitle + " " + senders.get(f).lastMessage + " " + senders.get(f).date + " " + senders.get(f).sender);
            //Log.d(TAG, "SENDERS COUNT " + senders.size() + " " + senders.get(f).sender + " " + senders.get(f).lastMessage);
        }

    }

    @Override
    public int getCount() {
        return senders.size();
    }

    @Override
    public Object getItem(int position) {
        return senders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = lInflater.inflate(R.layout.messages_sender_list_one_item, parent, false);
        }

        SenderInfo oneSender = getSender(position);

        Log.d(TAG, "1111111111111 " + oneSender.adTitle + " " + oneSender.lastMessage + " " + oneSender.date + " " + oneSender.sender);

        TextView tvSender = (TextView) view.findViewById(R.id.tvSender);
        TextView tvDate = (TextView) view.findViewById(R.id.tvDate);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvLastMessage);
        TextView tvAdTitle = (TextView) view.findViewById(R.id.tvAdTitle);
        //LinearLayout layoutMessage = (LinearLayout)view.findViewById(R.id.layoutMessage);
        ConstraintLayout constLayout = (ConstraintLayout)view.findViewById(R.id.constLayout);

        constLayout.setTag(oneSender.uniqueChat);
        Integer defColor = constLayout.getSolidColor();
        // если не прочитано сообщение, то выделить красным
        if(oneSender.viewed.equals("0")) {
            constLayout.setBackgroundColor(Color.RED);
        }else{
            constLayout.setBackgroundColor(defColor);
        }

        tvSender.setText(oneSender.sender);
        tvDate.setText(oneSender.date);
        tvMessage.setText(oneSender.lastMessage);
        tvAdTitle.setText(oneSender.adTitle);

        return view;
    }

    SenderInfo getSender(int position){
        return ((SenderInfo)getItem(position));
    }
}
