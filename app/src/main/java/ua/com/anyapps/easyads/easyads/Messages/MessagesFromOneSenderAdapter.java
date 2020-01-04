package ua.com.anyapps.easyads.easyads.Messages;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ua.com.anyapps.easyads.easyads.OlxAccount;
import ua.com.anyapps.easyads.easyads.R;

public class MessagesFromOneSenderAdapter extends BaseAdapter {
    Context context;
    LayoutInflater lInflater;
    ArrayList<ChatMessage> senderMessages;
    private static final String TAG = "debapp";

    public MessagesFromOneSenderAdapter(Context context, ArrayList<ChatMessage> _senderMessages) {
        this.context = context;
        this.senderMessages = _senderMessages;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return senderMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return senderMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = lInflater.inflate(R.layout.message_list_item_from_one_sender, parent, false);
        }

        ChatMessage oneMessage = getSenderMessage(position);
        TextView tvSender = (TextView) view.findViewById(R.id.tvSender);
        TextView tvDate = (TextView) view.findViewById(R.id.tvDate);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        LinearLayout layoutMessage = (LinearLayout)view.findViewById(R.id.layoutMessage);

        tvSender.setText(oneMessage.sender);
        tvDate.setText(oneMessage.createdate);
        tvMessage.setText(oneMessage.message);

        if(oneMessage.sender.equals("Ваше сообщение")){
            layoutMessage.setBackgroundColor(Color.rgb(182, 228, 105));

            /*ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layoutMessage.getLayoutParams();
            params.leftMargin = 50;*/
            //params.rightMargin = 8;
            //layoutMessage.requestLayout();
        }else{
            layoutMessage.setBackgroundColor(Color.rgb(242,242,242));
            /*ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layoutMessage.getLayoutParams();
            //params.rightMargin = 50;
            params.leftMargin = 8;*/
        }




        return view;
    }

    ChatMessage getSenderMessage(int position){
        return ((ChatMessage)getItem(position));
    }
}
