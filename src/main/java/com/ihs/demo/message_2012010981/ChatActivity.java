package com.ihs.demo.message_2012010981;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.TextOptions;
import com.ihs.account.api.account.HSAccountManager;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.message_2012010981.R;
import com.ihs.message_2012010981.managers.HSMessageChangeListener;
import com.ihs.message_2012010981.managers.HSMessageManager;
import com.ihs.message_2012010981.types.HSAudioMessage;
import com.ihs.message_2012010981.types.HSBaseMessage;
import com.ihs.message_2012010981.types.HSImageMessage;
import com.ihs.message_2012010981.types.HSMessageType;
import com.ihs.message_2012010981.types.HSOnlineMessage;
import com.ihs.message_2012010981.types.HSTextMessage;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import android.app.Activity;
import java.io.File;
import android.os.Environment;

public class ChatActivity extends Activity implements HSMessageChangeListener, View.OnClickListener, HSMessageManager.SendMessageCallback, MediaPlayer.OnCompletionListener {
    String my_mid;
//    TextView textView;
    String mid;
    String name;
    ListView listView;
    ArrayList<HSBaseMessage> messages;
    ChatAdapter chatAdapter;
    EditText textMsg;
    Button btnSend;
    Button btnPhotoSend;
    Button btnCameraSend;
    Button btnAudioSend;
    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;
    private File outputImage;
    private Uri imageUri;
    private MediaRecorder mRecorder;
    private Date startDateAudio, stopDateAudio;
    String AudioFileName = null;
    Toast toastSpeaking;

    public void reset() {
        ArrayList<HSBaseMessage> ordMessages = (ArrayList<HSBaseMessage>) HSMessageManager.getInstance().queryMessages(mid, 0, -1).getMessages();
        chatAdapter.getMsg().clear();
        for (int i = (ordMessages.size()-1); i >= 0; i--) {
            chatAdapter.addMsg(ordMessages.get(i));
        }
        chatAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        TextView textView1 = (TextView)findViewById(R.id.contactInfo);
        //显示联系人信息
        String [] contactInfos = (String [])getIntent().getExtras().get("contactInfo");
        textView1.setText("  " + contactInfos[0] + "  " + contactInfos[1]);
        name = contactInfos[0];
        mid = contactInfos[1];
        my_mid = HSAccountManager.getInstance().getMainAccount().getMID();
        HSMessageManager.getInstance().addListener(this, new Handler());
        HSMessageManager.getInstance().markRead(mid);
        //从数据库取出与这人相关信息
        ArrayList<HSBaseMessage> ordMessages = (ArrayList<HSBaseMessage>) HSMessageManager.getInstance().queryMessages(mid, 0, -1).getMessages();
        messages = new ArrayList<>();
        for (int i = (ordMessages.size()-1); i >= 0; i--) {
            messages.add(ordMessages.get(i));
        }

        listView = (ListView)findViewById(R.id.list_view);
        //点击语音播放、图片显示大图
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HSBaseMessage hsBaseMessage = (HSBaseMessage)listView.getAdapter().getItem(position);
                switch (hsBaseMessage.getType()) {
                    case TEXT:
                        break;
                    case AUDIO:
//                        chatAdapter.startAnimation();
                        MediaPlayer mPlayer = new MediaPlayer();
                        try{
                            HSAudioMessage hsAudioMessage = (HSAudioMessage)hsBaseMessage;
                            mPlayer.setDataSource(hsAudioMessage.getAudioFilePath());
                            mPlayer.prepare();
                            mPlayer.start();
                            mPlayer.setOnCompletionListener(ChatActivity.this);
                        }catch(IOException e){
//                            Log.e(LOG_TAG,"播放失败");
                        }
//                        System.out.println("正在播放！！！！");
                        break;
                    case IMAGE:
                        Intent intent = new Intent(ChatActivity.this, ImageActivity.class);
                        String filePath = ((HSImageMessage)hsBaseMessage).getNormalImageFilePath();
                        intent.putExtra("filePath", filePath);
                        startActivity(intent);
                        break;
                    case LOCATION:
                        break;
                    default:
                        break;
                }
            }
        });
        //长按信息删除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ChatActivity.this, "delete", Toast.LENGTH_SHORT).show();
                List<HSBaseMessage> deleteMsg = new ArrayList<>();
                deleteMsg.add((HSBaseMessage) listView.getAdapter().getItem(position));
                HSMessageManager.getInstance().deleteMessages(deleteMsg);
                deleteMsg.clear();
                reset();
                return false;
            }
        });

        //根据数据库信息构造adaper并与listView相关联
        chatAdapter = new ChatAdapter(this, messages, my_mid);
        listView.setAdapter(chatAdapter);
//        reset();
        listView.setSelection(listView.getAdapter().getCount() - 1);

        textMsg = (EditText)findViewById(R.id.text_msg);
        btnSend = (Button)findViewById(R.id.btn_send);
        btnPhotoSend = (Button)findViewById(R.id.btn_photo_send);
        btnCameraSend = (Button)findViewById(R.id.btn_camera_send);
        btnAudioSend = (Button)findViewById(R.id.btn_audio_send);
        btnSend.setOnClickListener(this);
        btnPhotoSend.setOnClickListener(this);
        btnCameraSend.setOnClickListener(this);

        //长按语音发送键响应
        AudioFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        AudioFileName += "/audiorecordtest.3gp";
        btnAudioSend.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (toastSpeaking != null)
                        toastSpeaking.cancel();
                    toastSpeaking = Toast.makeText(getApplicationContext(),
                            "讲话结束", Toast.LENGTH_SHORT);
//                    toastSpeaking.setGravity(Gravity.CENTER, 0, 0);
                    toastSpeaking.show();

                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                    stopDateAudio = new Date();
                    audioSend(mid, AudioFileName, (stopDateAudio.getTime() - startDateAudio.getTime()) / 1000.0);
//                    UnreadMessageProcess.getInstance().setLatestText(userInfo[1],"你发送了一条语音");
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    toastSpeaking = Toast.makeText(getApplicationContext(),
                            "正在讲话", Toast.LENGTH_SHORT);
//                    toastSpeaking.setGravity(Gravity.CENTER, 0, 0);
                    toastSpeaking.show();

                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setOutputFile(AudioFileName);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    try {
                        mRecorder.prepare();
                    } catch (IOException e) {
                    }
                    mRecorder.start();
                    startDateAudio = new Date();
                }
                return false;
            }
        });


//      从非通知中心界面进入，要取消通知
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        int ID = Integer.parseInt(mid);
        notificationManager.cancel(ID);
    }

    //发送文字信息
    public void textSend(String msg) {
        HSBaseMessage sendMsg = new HSTextMessage(mid, msg);
        HSMessageManager.getInstance().send(sendMsg, this, new Handler());
    }
    //发送图片信息
    public void imageSend(String to, String path) {
        HSMessageManager.getInstance().send(new HSImageMessage(to, path), new HSMessageManager.SendMessageCallback() {
            @Override
            public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {
//                HSLog.d(TAG, "success: "+success);
            }
        }, new Handler());
    }
    //发送语音信息
    public void audioSend(String toMid, String fileName, double duration) {
        HSMessageManager.getInstance().send(new HSAudioMessage(toMid, fileName, duration), new HSMessageManager.SendMessageCallback() {
            @Override
            public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {
//                HSLog.d(TAG, "success: " + success);
            }
        }, new Handler());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //有消息改变，刷新聊天窗口
    @Override
    public void onMessageChanged(HSMessageChangeType changeType, List<HSBaseMessage> messages) {
        reset();
        listView.setSelection(listView.getAdapter().getCount() - 1);
    }

    @Override
    protected void onPause() {
        HSMessageManager.getInstance().markRead(mid);
        super.onPause();
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

    @Override
    protected void onDestroy() {
        HSMessageManager.getInstance().removeListener(this);
        super.onDestroy();
    }

    //点击事件响应
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            //文字消息发送
            case R.id.btn_send:
                String msg = textMsg.getText().toString();
                if (msg.equals(""))
                    break;
                textSend(msg);
                textMsg.setText("");
//                Toast.makeText(this, "fuck", Toast.LENGTH_LONG).show();
                break;
            //打开照片库
            case R.id.btn_photo_send:
                outputImage = new File(Environment.getExternalStorageDirectory(),"output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                //intent.putExtra("crop", true);
                //intent.putExtra("scale", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CROP_PHOTO);
                break;
            //打开摄像头
            case R.id.btn_camera_send:
                outputImage = new File(Environment.getExternalStorageDirectory(),"output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(outputImage);
                intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
                break;
        }
    }
    //拍照或者调用相册数据
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    imageSend(mid, outputImage.getAbsolutePath());
                }
                break;
            case CROP_PHOTO:

                Bitmap bm = null;
                ContentResolver resolver = getContentResolver();
                Uri originalUri = data.getData();        //获得图片的uri
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                imageSend(mid, path);

                break;
        }
    }

    @Override
    public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {

    }

    //语音播放完的响应
    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(this, "播放完毕", Toast.LENGTH_SHORT).show();
//        chatAdapter.stopAnimation();
    }
}
