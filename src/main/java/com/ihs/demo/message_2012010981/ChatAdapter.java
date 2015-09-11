package com.ihs.demo.message_2012010981;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.message_2012010981.R;
import com.ihs.message_2012010981.types.HSAudioMessage;
import com.ihs.message_2012010981.types.HSBaseMessage;
import com.ihs.message_2012010981.types.HSImageMessage;
import com.ihs.message_2012010981.types.HSMessageType;
import com.ihs.message_2012010981.types.HSTextMessage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashMap;

/**
 * Created by anthony on 9/6/15.
 */
public class ChatAdapter extends BaseAdapter{
    private ArrayList<HSBaseMessage> messages;
    private Context context;
    private String my_mid;
    private LayoutInflater mInflater;
    private final String sending = "[sending]", sent = "[Sent]", failed = "[failed]";
//    private ArrayList<boolean> animation;


    public ChatAdapter(Context context, ArrayList<HSBaseMessage> messages, String my_mid) {
        this.messages = messages;
        this.context = context;
        this.my_mid = my_mid;
        mInflater = LayoutInflater.from(context);
//        animation = new ArrayList<>();
    }

    public void setMsg(ArrayList<HSBaseMessage> msg) {
        this.messages = msg;
    }

    public ArrayList<HSBaseMessage> getMsg() { return this.messages; }

    public void addMsg(HSBaseMessage msg) {
        this.messages.add(msg);
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HSBaseMessage message = messages.get(position);
        View view;
        ViewHolder viewHolder;

        int isSend = (my_mid.equals(message.getFrom())) ? 0 : 1;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.chat_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.leftLayout = (LinearLayout)view.findViewById(R.id.left_layout);
            viewHolder.rightLayout = (LinearLayout)view.findViewById(R.id.right_layout);

            viewHolder.leftMsg = (TextView)view.findViewById(R.id.left_view);
            viewHolder.leftMsgStatus = (TextView)view.findViewById(R.id.left_view_status);
            viewHolder.leftImage = (ImageView)view.findViewById(R.id.left_image_view);
            viewHolder.leftAudio = (ImageView)view.findViewById(R.id.left_audio_view);
            viewHolder.leftLayoutAudio = (LinearLayout)view.findViewById(R.id.left_layout_audio_view);
            viewHolder.leftAudioText = (TextView)view.findViewById(R.id.left_audio_text);

            viewHolder.rightMsg = (TextView)view.findViewById(R.id.right_view);
            viewHolder.rightMsgStatus = (TextView)view.findViewById(R.id.right_view_status);
            viewHolder.rightImage = (ImageView)view.findViewById(R.id.right_image_view);
            viewHolder.rightAudio = (ImageView)view.findViewById(R.id.right_audio_view);
            viewHolder.rightLayoutAudio = (LinearLayout)view.findViewById(R.id.right_layout_audio_view);
            viewHolder.rightAudioText = (TextView)view.findViewById(R.id.right_audio_text);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");

        String msgTime = sdf.format(message.getTimestamp());
        TextView textView;
        ImageView imageView;
        ImageView audioView;
        LinearLayout layoutAudio;
        TextView audioText;
        if (isSend == 0) {
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            viewHolder.leftLayout.setVisibility(View.GONE);
            textView = viewHolder.rightMsg;
            imageView = viewHolder.rightImage;
            audioView = viewHolder.rightAudio;
            audioText = viewHolder.rightAudioText;
            layoutAudio = viewHolder.rightLayoutAudio;
            //在发送方显示发送中、送达、发送失败
            String suffix = "";
            switch (message.getStatus()) {
                case SENDING:
                    suffix = sending;
                    break;
                case SENT:
                    suffix = sent;
                    break;
                case FAILED:
                    suffix = failed;
            }
            viewHolder.rightMsgStatus.setText(msgTime + suffix);

        } else {
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.rightLayout.setVisibility(View.GONE);
            textView = viewHolder.leftMsg;
            imageView = viewHolder.leftImage;
            audioView = viewHolder.leftAudio;
            audioText = viewHolder.leftAudioText;
            layoutAudio = viewHolder.leftLayoutAudio;
            viewHolder.leftMsgStatus.setText(msgTime);
        }

        //不同类型信息显示不同内容
        switch (message.getType()) {
            case TEXT:
                textView.setVisibility(View.VISIBLE);
                layoutAudio.setVisibility(View.GONE);
//                audioView.setVisibility(View.GONE);

                imageView.setVisibility(View.GONE);

                textView.setText(((HSTextMessage) message).getText());
                break;
            case AUDIO:
                textView.setVisibility(View.GONE);
//                audioView.setVisibility(View.VISIBLE);
                layoutAudio.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
//                if (animation.get(position)) {
//                    AnimationDrawable mAnimationDrawable = (AnimationDrawable) audioView.getBackground();
//                    mAnimationDrawable.start();
//                }
//                else {
//                    AnimationDrawable mAnimationDrawable = (AnimationDrawable) audioView.getBackground();
//                    mAnimationDrawable.stop();
//                }
//                textView.setText("Audio Message");

                AnimationDrawable mAnimationDrawable = (AnimationDrawable) audioView.getBackground();
                mAnimationDrawable.stop();
                double time = ((HSAudioMessage)message).getDuration();
                audioText.setText("[" + new Double(time).toString() + "]");
                break;
            case IMAGE:
                textView.setVisibility(View.GONE);
                layoutAudio.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                HSImageMessage imageMsg = (HSImageMessage)message;
                Bitmap bitmap = getLocalBitmap(imageMsg.getThumbnailFilePath());
                imageView.setImageBitmap(bitmap);
                break;
            case LOCATION:
                textView.setVisibility(View.VISIBLE);
                layoutAudio.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                textView.setText("Location Msg Type");
                break;
            default:
                textView.setVisibility(View.VISIBLE);
                layoutAudio.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                textView.setText("Unsupport Msg Type");
                break;
        }
        return view;
    }

    public Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    class ViewHolder {
        //左右布局对应收到和发送的消息
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        TextView leftMsgStatus;
        TextView rightMsgStatus;
        ImageView rightImage;
        ImageView leftImage;
        ImageView rightAudio;
        ImageView leftAudio;
        LinearLayout leftLayoutAudio;
        LinearLayout rightLayoutAudio;
        TextView leftAudioText;
        TextView rightAudioText;
    }


}

