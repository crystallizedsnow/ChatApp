// ChatActivity.java
package com.example.chatapp.Activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.example.chatapp.Adapter.MessageAdapter;
import com.example.chatapp.R;
import com.example.chatapp.pojo.Contact;
import com.example.chatapp.pojo.Message;
import com.example.chatapp.utils.DatabaseHelper;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private String friendName;
    private String username;
    private final OkHttpClient client= new OkHttpClient();
    private static final String PREFS_NAME = "ChatAppPrefs";
    private MessageAdapter messageAdapter;
    private RecyclerView chatRecyclerView;
    private List<Message> messageList;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private ImageButton btnSendImage;
    private ActivityResultLauncher<Intent> selectImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        dbHelper= new DatabaseHelper(ChatActivity.this);
        db = dbHelper.getWritableDatabase();
        messageInput = findViewById(R.id.message_input);
        chatRecyclerView=findViewById(R.id.rvMessages);
        messageInput = findViewById(R.id.message_input);
        btnSendImage = findViewById(R.id.btn_send_image);

        // 检查和请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            }
        }

        // 注册结果处理器
        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData(); // 获取图片的 Uri
                        if (imageUri != null) {
                            // 调试日志，打印 Uri
                            Log.d("ImageSelector", "Selected Image Uri: " + imageUri.toString());

                            // 获取真实路径
                            String realPath = getRealPathFromUri(this, imageUri);
                            if (realPath != null) {
                                Log.d("ImageSelector", "Real Path: " + realPath);

                                // 创建文件对象并发送
                                File imageFile = new File(realPath);
                                sendImage(imageFile, username, friendName);
                            } else {
                                Log.e("ImageSelector", "Failed to resolve real path.");
                            }
                        }
                    }
                });

        // 设置按钮点击事件启动图片选择器
        btnSendImage.setOnClickListener(v -> {
            // 优先尝试 ACTION_GET_CONTENT
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            // 检查是否存在处理的应用程序
            PackageManager packageManager = getPackageManager();
            if (intent.resolveActivity(packageManager) != null) {
                selectImageLauncher.launch(Intent.createChooser(intent, "Select Image"));
            } else {
                // 如果 ACTION_GET_CONTENT 不支持，降级使用 ACTION_PICK
                Intent fallbackIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                fallbackIntent.setType("image/*");
                selectImageLauncher.launch(Intent.createChooser(fallbackIntent, "Select Image"));
            }
        });



        Intent intent1=getIntent();
        friendName= intent1.getStringExtra("username");
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        toolbar.setTitle(getString(R.string.chat_friend, friendName)); // 设置标题
        setSupportActionBar(toolbar); // 设置Toolbar为ActionBar
        SharedPreferences  sharedPreferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        username=sharedPreferences.getString("username"," ");
        if (getSupportActionBar() != null) {
            // 显示返回按钮
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList,username,ChatActivity.this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        messageInput.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        messageInput.setImeOptions(EditorInfo.IME_ACTION_SEND);
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                String messageText = messageInput.getText().toString().trim();  // 去掉前后空白字符
                if (!messageText.isEmpty()) {  // 仅在消息不为空时发送
                    sendMessage(messageText);
                    messageInput.setText("");
                }
                return true;
            }
            return false;
        });
        loadHistoryMessages();
//        findViewById(R.id.btnReturn).setOnClickListener(v -> toContactList());
    }
    private String getRealPathFromUri(Context context, Uri uri) {
        String result = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            if (idx != -1) {
                result = cursor.getString(idx);
            }
            cursor.close();
        }
        return result;
    }

    private void sendMessage(String content) {
        // Send message to the server and update UI
        // Example: update chatHistory with new message
        String url="http://10.0.2.2:8081/api/messages/send";
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String timestamp = formatter.format(date);

        JSONObject json=new JSONObject();
        try{
            json.put("sender",username);
            json.put("receiver",friendName);
            json.put("content",content);
            json.put("timestamp",timestamp);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // 插入消息到数据库
        Message message = new Message(username,friendName,content,timestamp);
        dbHelper.insertMessage(db,message);

        RequestBody body=RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request=new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("sendText","Send Message Failed");
                runOnUiThread(() ->
                        Toast.makeText(ChatActivity.this, "send failed", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body() != null ? response.body().string() : "";
                    Message message = new Message(username,friendName,content,timestamp);

                    runOnUiThread(() -> {
                        messageList.add(message);
                        messageAdapter.notifyItemInserted( messageList.size() - 1);
                        chatRecyclerView.scrollToPosition( messageList.size() - 1);//滚动到底部
                        Toast.makeText(ChatActivity.this, responseData, Toast.LENGTH_SHORT).show();

                        ContentValues values = new ContentValues();
                        values.put("sentSuccess", 1);
                        db.update("messages", values, "timestamp = ? AND sender = ?", new String[]{timestamp, username});

                    });
                } finally {
                    response.close();
                }
            }
        });

    }
    private void sendImage(File imageFile, String sender, String receiver) {
        String uploadUrl = "http://10.0.2.2:8081/api/image";
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String timestamp = formatter.format(date);

        OkHttpClient client = new OkHttpClient();
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), imageFile);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(), fileBody)
                .addFormDataPart("sender", sender)
                .addFormDataPart("receiver", receiver)
                .addFormDataPart("timestamp", timestamp)
                .build();

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "图片上传失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // 图片上传成功后，保存图片到本地
                    String localPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/";  // 获取外部存储的图片目录
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());  // 将文件转换为 Bitmap
                    File localImageFile = new File(localPath, imageFile.getName());

                    // 保存图片到外部存储
                    try {
                        saveImageToExternalStorage(bitmap, localImageFile.getName());  // 保存图片到外部存储
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // 插入消息到数据库，保存图片路径
                    Message message = new Message(sender, receiver, localImageFile.getAbsolutePath(), timestamp, 1);
                    dbHelper.insertMessage(db, message);

                    runOnUiThread(() -> {
                        messageList.add(message); // 添加消息到列表
                        messageAdapter.notifyItemInserted(messageList.size() - 1); // 通知适配器更新
                        chatRecyclerView.scrollToPosition(messageList.size() - 1); // 滚动到底部
                        Toast.makeText(getApplicationContext(), "图片上传成功", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "图片上传失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void saveImageToExternalStorage(Bitmap bitmap, String imageName) throws IOException {
        // 使用 getExternalFilesDir 替代 Public Directory（更安全，适合现代 Android）
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // 确保目录存在
        if (directory != null && !directory.exists()) {
            boolean isCreated = directory.mkdirs();
            if (!isCreated) {
                throw new IOException("无法创建目录: " + directory.getAbsolutePath());
            }
        }

        // 定义保存文件
        File file = new File(directory, imageName);

        // 创建文件输出流，将图片保存
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        }

        // 日志记录文件路径
        Log.d("SaveImage", "图片保存路径: " + file.getAbsolutePath());
    }


    private void toContactList() {
        finish();
    }
    // 从数据库加载历史消息的方法
    private void loadHistoryMessages() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("messages", null, "sender = ? AND receiver=? OR sender = ? AND receiver=?",
                new String[]{username, friendName, friendName, username}, null, null, "timestamp ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String sender = cursor.getString(cursor.getColumnIndexOrThrow("sender"));
                String receiver = cursor.getString(cursor.getColumnIndexOrThrow("receiver"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
                int isImage=cursor.getInt(cursor.getColumnIndexOrThrow("isImage"));
                Message message = new Message(sender, receiver, content, timestamp,isImage);
                messageList.add(message);
            }
            cursor.close();
        }

        // 通知适配器数据集已更改
        messageAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("NEW_MESSAGE_RECEIVED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i("BroadcastReceiver", "broadcast created");
            registerReceiver(newMessageReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
    }

    @Override
    protected void onPause() {
        Log.i("BroadcastReceiver", "broadcast unregister");
        super.onPause();
        unregisterReceiver(newMessageReceiver);
    }

    private final BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("BroadcastReceiver", "Receive broadcast");
            if (isFinishing() || isDestroyed()) {
                Log.i("BroadcastReceiver", "ChatActivity is not active. Ignoring update.");
                return;
            }
            String messageJson = intent.getStringExtra("message");
            Message message = new Gson().fromJson(messageJson, Message.class);
            String url="http://10.0.2.2:8081/api/messages/delete?timestamp="+message.getTimestamp();
            Request request=new Request.Builder().url(url).delete().build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    Log.e("deleteText","Server Delete Message Failed");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.i("deleteText","Server Delete Message Succeed");
                }
            });
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1); // 通知适配器更新
            chatRecyclerView.scrollToPosition(messageList.size() - 1); // 滚动到底部
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==android.R.id.home)
        {
            // 返回上一页面
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_clear_chat) {
            // 清空所有消息
            db.delete("messages", "(sender=? And receiver=?) ", new String[]{username, friendName});
            db.delete("messages", "(sender=? And receiver=?)",new String[]{friendName,username});
            messageList.clear();
            messageAdapter.notifyDataSetChanged();
            Toast.makeText(this, "聊天记录已清空", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
