package com.ihs.demo.message_2012010981;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.message_2012010981.R;
import com.ihs.message_2012010981.managers.HSMessageManager;
import com.ihs.message_2012010981.types.HSBaseMessage;
import com.ihs.message_2012010981.types.HSTextMessage;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by anthony on 9/6/15.
 */

public class MessageAdapter extends ArrayAdapter<Contact> {

    private List<Contact> contacts;
    private Context context;

    DisplayImageOptions options;

    private class ViewHolder {
        ImageView avatarImageView;
        TextView titleTextView;
        TextView detailTextView;
    }

    public void sortContact() {
        Collections.sort(contacts, new ComparatorDate());
    }

    class ComparatorDate implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            Contact c1 = (Contact)lhs;
            Contact c2 = (Contact)rhs;
            if (c1.getLastTimeStamp().before(c2.getLastTimeStamp()))
                return 1;
            else if (c1.getLastTimeStamp().after(c2.getLastTimeStamp()))
                return -1;
            return 0;
        }
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contactList) { contacts = contactList; }

    public MessageAdapter(Context context, int resource, List<Contact> objects) {
        super(context, resource, objects);
        this.contacts = objects;
        this.context = context;

        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.chat_avatar_default_icon).showImageForEmptyUri(R.drawable.chat_avatar_default_icon)
                .showImageOnFail(R.drawable.chat_avatar_default_icon).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.cell_item_contact, parent, false);
            TextView titleView = (TextView) convertView.findViewById(R.id.title_text_view);
            TextView detailView = (TextView) convertView.findViewById(R.id.detail_text_view);
            holder.titleTextView = titleView;
            holder.detailTextView = detailView;
            holder.avatarImageView = (ImageView) convertView.findViewById(R.id.contact_avatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = contacts.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
        String lastTime = sdf.format(contact.getLastTimeStamp());
        int unRead = HSMessageManager.getInstance().queryUnreadCount(contact.getMid());
        holder.titleTextView.setText("" + contact.getName() + " " + lastTime);
        HSBaseMessage lastMsg = HSMessageManager.getInstance().queryMessages(contact.getMid(), 0, -1).getMessages().get(0);
        String firstLine = "未读消息：" + unRead + "\n";
        String lastStr = "";
        switch (lastMsg.getType()) {
            case TEXT:
                lastStr += ((HSTextMessage)lastMsg).getText();
                break;
            case AUDIO:
                lastStr += "[Audio Message]";
                break;
            case IMAGE:
                lastStr = "[Image Message]";
                break;
            case LOCATION:
                lastStr = "[Location Message]";
                break;
            default:
                break;
        }

        if (unRead > 0) {
            lastStr = "[Unread]" + lastStr;
            holder.detailTextView.setTextColor(Color.RED);
        } else {
            holder.detailTextView.setTextColor(Color.BLACK);
        }
        holder.detailTextView.setText(firstLine + lastStr);
        ImageLoader.getInstance().displayImage("content://com.android.contacts/contacts/" + contact.getContactId(), holder.avatarImageView, options);

        return convertView;
    }
}