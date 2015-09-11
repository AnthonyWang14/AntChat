package com.ihs.demo.message_2012010981;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.message_2012010981.R;
import com.ihs.message_2012010981.managers.HSMessageChangeListener;
import com.ihs.message_2012010981.managers.HSMessageManager;
import com.ihs.message_2012010981.types.HSBaseMessage;
import com.ihs.message_2012010981.types.HSOnlineMessage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment implements HSMessageChangeListener{

    private ListView listView;
    private MessageAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSMessageManager.getInstance().addListener(this, new Handler());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.contact_list);
        final List<Contact> contacts = new ArrayList<Contact>();
        adapter = new MessageAdapter(this.getActivity(), R.layout.cell_item_contact, contacts);
        listView.setAdapter(adapter);

        //点击聊天
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mid = contacts.get(position).getMid();
                String name = contacts.get(position).getName();
//                Toast.makeText(getActivity(), "你点击了名字为：" + name + " mid为：" + mid + "的联系人，需要在这里跳转到同此人的聊天界面（一个Activity）", Toast.LENGTH_LONG).show();
                if (HSAccountManager.getInstance().getSessionState() == HSAccountManager.HSAccountSessionState.INVALID) {
                    Toast toast = Toast.makeText(getActivity(), "请先登录", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    String data[] = new String[]{name, mid};
                    intent.putExtra("contactInfo", data);
//                  startActivity(intent);
                    startActivityForResult(intent, 0);
                }
            }
        });
        //长按删除与该联系人信息
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HSMessageManager.getInstance().deleteMessages(adapter.getContacts().get(position).getMid());
                refresh();
//                Toast.makeText(MessagesFragment.this.getActivity(), "message refresh", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        refresh();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refresh();
    }

    void refresh() {
        adapter.getContacts().clear();
        List<Contact> friends = FriendManager.getInstance().getAllFriends();
        for (int i = 0; i < friends.size(); i++) {
            String mid = friends.get(i).getMid();
            List<HSBaseMessage> hsBaseMessages = HSMessageManager.getInstance().queryMessages(mid, 0, -1).getMessages();
            if (hsBaseMessages.size() > 0) {
                adapter.getContacts().add(friends.get(i));
            }
        }
//        adapter.getContacts().addAll(FriendManager.getInstance().getAllFriends());
//        Toast.makeText(this.getActivity(),"refresh", Toast.LENGTH_LONG).show();
        adapter.sortContact();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMessageChanged(HSMessageChangeType changeType, List<HSBaseMessage> messages) {
        refresh();
    }

    @Override
    public void onTypingMessageReceived(String fromMid) {

    }

    @Override
    public void onOnlineMessageReceived(HSOnlineMessage message) {

    }

    @Override
    public void onUnreadMessageCountChanged(String mid, int newCount) {

    }

    @Override
    public void onReceivingRemoteNotification(JSONObject pushInfo) {

    }
}
