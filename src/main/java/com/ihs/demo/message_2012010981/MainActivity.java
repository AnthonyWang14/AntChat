package com.ihs.demo.message_2012010981;

import test.contacts.demo.friends.api.HSContactFriendsMgr;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.utils.HSLog;
import com.ihs.message_2012010981.R;
import com.ihs.message_2012010981.managers.HSMessageChangeListener;
import com.ihs.message_2012010981.managers.HSMessageManager;
import com.ihs.message_2012010981.types.HSBaseMessage;
import com.ihs.message_2012010981.types.HSMessageType;
import com.ihs.message_2012010981.types.HSOnlineMessage;
import com.ihs.message_2012010981.types.HSTextMessage;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

public class MainActivity extends HSActionBarActivity implements HSMessageChangeListener {

    private final static String TAG = MainActivity.class.getName();
    private Tab tabs[];
    private String my_mid;
    private NotificationManager nm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = this.getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        int[] tabNames = { R.string.contacts, R.string.messages, R.string.settings, R.string.sample };
        tabs = new Tab[4];
        for (int i = 0; i < 4; i++) {
            Tab tab = bar.newTab();
            tabs[i] = tab;
            tab.setText(tabNames[i]);
            tab.setTabListener(new TabListener() {

                @Override
                public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
                    HSLog.d(TAG, "unselected " + arg0);
                }

                @Override
                public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
                    HSLog.d(TAG, "selected " + arg0);
                    if (tabs[0] == arg0) {
                        Fragment f = new ContactsFragment();
                        arg1.replace(android.R.id.content, f);
                    } else if (tabs[1] == arg0) {
                        Fragment f = new MessagesFragment();
                        arg1.replace(android.R.id.content, f);
                    } else if (tabs[2] == arg0) {
                        Fragment f = new SettingsFragment();
                        arg1.replace(android.R.id.content, f);
                    } else if (tabs[3] == arg0) {
                        Fragment f = new SampleFragment();
                        arg1.replace(android.R.id.content, f);
                    }
                }

                @Override
                public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
                    HSLog.d(TAG, "reselected " + arg0);
                }
            });
            bar.addTab(tab);
        }

        setContentView(R.layout.activity_main);
        HSMessageManager.getInstance().addListener(this, new Handler());
        if (HSAccountManager.getInstance().getSessionState() == HSAccountManager.HSAccountSessionState.INVALID) {
            Toast toast = Toast.makeText(this, "请先登录", Toast.LENGTH_LONG);
            toast.show();
        }
        else {
            my_mid = HSAccountManager.getInstance().getMainAccount().getMID();
        }

        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        HSMessageManager.getInstance().pullMessages();
        HSContactFriendsMgr.startSync(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onMessageChanged(HSMessageChangeType changeType, List<HSBaseMessage> messages) {
        if (changeType == HSMessageChangeType.ADDED) {
            for (HSBaseMessage message : messages) {
                if (!my_mid.equals(message.getFrom())) {
                    //收到信息的提醒
                    MediaPlayer player = MediaPlayer.create(this, R.raw.message_ringtone_received);
                    player.start();
                    //通知中心界面
                    if (HSSessionMgr.getTopActivity() == null) {
                        Notification notification = new Notification();
                        notification.icon = R.drawable.notification_icon;
                        String nfStr = "";
                        String mid = message.getFrom();
                        if (FriendManager.getInstance().getFriend(mid)!=null) {
                            String name = FriendManager.getInstance().getFriend(mid).getName();
                            int unRead = HSMessageManager.getInstance().queryUnreadCount(mid);
                            nfStr = name + ": ";
                            switch (message.getType()) {
                                case TEXT:
                                    nfStr += ((HSTextMessage) message).getText();
                                    break;
                                case AUDIO:
                                    nfStr += "[Audio Message]";
                                    break;
                                case IMAGE:
                                    nfStr += "[Image Message]";
                                    break;
                                case LOCATION:
                                    nfStr += "[Location Message]";
                                    break;
                                default:
                                    nfStr += "[Unknown Type Message]";
                            }
                            notification.tickerText = nfStr;
                            notification.flags |= Notification.FLAG_AUTO_CANCEL;
                            Intent intent = new Intent(this, ChatActivity.class);
                            String data[] = new String[]{name, mid};
                            intent.putExtra("contactInfo", data);
                            int ID = Integer.parseInt(mid);
                            //每个用户都有不同的数字ID
                            PendingIntent pd = PendingIntent.getActivity(this, ID, intent, 0);
                            if (unRead > 1) {
                                notification.setLatestEventInfo(this, unRead + " new messages", nfStr, pd);
                            }
                            else {
                                notification.setLatestEventInfo(this, unRead + " new message", nfStr, pd);
                            }
                            nm.notify(ID, notification);
                        }
                    }
                } else {
                    MediaPlayer player = MediaPlayer.create(this, R.raw.message_ringtone_sent);
                    player.start();
                }
            }
        }

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
