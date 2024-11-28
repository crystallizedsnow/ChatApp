package com.example.chatapp.service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.example.chatapp.Adapter.MessageAdapter;
import com.example.chatapp.pojo.Message;
import com.example.chatapp.utils.DatabaseHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebSocketService extends Service {
    private static final String SERVER_URL = "ws://10.0.2.2:8081/ws";  // Using Android emulator's IP
    private StompClient stompClient;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private boolean errorFlag = false; // Stomp error flag
    private int reconnectionNum = 0; // Reconnection attempts
    private String username;
    private MessageAdapter messageAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("WebSocket", "StartService");
        dbHelper = new DatabaseHelper(WebSocketService.this);
        db = dbHelper.getWritableDatabase();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 从启动服务的 Intent 中获取 username
        if (intent != null && intent.hasExtra("username")) {
            username = intent.getStringExtra("username");
        }

        // 初始化 WebSocket 或其他逻辑
        initialize(username);

        // 确保服务继续运行直到明确停止
        return START_STICKY;
    }

    public void initialize(String username) {
        this.username = username;
        connectWebSocket();
    }

    private void connectWebSocket() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SERVER_URL);
        resetSubscriptions();

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("username", this.username));

        stompClient.withClientHeartbeat(1000).withServerHeartbeat(1000);

        // Listen for lifecycle events
        Disposable dispLifecycle = stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d("WebSocket", "Stomp connection opened");
                            fetchUnreadMessages();
                            break;
                        case ERROR:
                            errorFlag = true;
                            Log.e("WebSocket", "Stomp connection error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.d("WebSocket", "Stomp connection closed");
                            if (errorFlag && reconnectionNum < 11) {
                                reconnectionNum++;
                                Log.i("WebSocket", "Attempting to reconnect (" + reconnectionNum + ")");
                                connectWebSocket();
                            }
                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            Log.d("WebSocket", "Stomp server heartbeat failed");
                            break;
                    }
                }, throwable -> Log.e("WebSocket", "Lifecycle subscription error", throwable));

        // Add to composite disposables
        Disposable dispTopic =stompClient.topic("/topic/user/"+username+"/sendToUser")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stompMessage -> {
                    String payload = stompMessage.getPayload();
                    Log.d("WebSocket", "Received message: " + payload);

                    if (payload == null || payload.isEmpty()) {
                        Log.w("WebSocket", "Received empty payload");
                    } else {
                        // Parse and handle the message
                        Type listType = new TypeToken<List<Message>>() {}.getType();
                        List<Message> messageList = new Gson().fromJson(payload, listType);
                        for (Message message : messageList) {
                            try {
                                handleIncomingMessage(message);
                            } catch (Exception e) {
                                Log.e("IncomingMessage", "Error handling message: " + e.getMessage(), e);
                            }

                        }
                        // 向服务器发送确认收到消息的信号
                        sendAcknowledgmentToServer();
                    }
                }, throwable -> Log.e("WebSocket", "Topic subscription error", throwable));

        stompClient.connect(headers);
    }

        public String downloadImg(String url) throws ClientException, ServiceException {
            // 获取文件名和本地存储路径
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            String localPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath(); // 应用私有存储路径
            File localFile = new File(localPath + File.separator + fileName);

            // 确保父目录存在
            File parentDir = localFile.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    Log.e("downloadImg", "Failed to create directory: " + parentDir.getAbsolutePath());
                    return null; // 目录创建失败，停止下载
                }
            }

            // 初始化 OSS 客户端
            OSSCredentialProvider ossCredentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);
            OSS oss = new OSSClient(getApplicationContext(), "https://oss-cn-hangzhou.aliyuncs.com", ossCredentialProvider);
            String bucketName = 

            // 构造同步下载文件请求
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, fileName);
            try {
                // 同步获取文件
                GetObjectResult result = oss.getObject(getObjectRequest);
                if (result.getObjectContent() == null) {
                    Log.e("downloadImg", "文件内容为空，下载失败。");
                    return null;
                }
                try (InputStream inputStream = result.getObjectContent();
                     FileOutputStream outputStream = new FileOutputStream(localFile)) {
                    // 将下载的内容写入本地文件
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    Log.d("downloadImg", "图片下载成功：" + localFile.getAbsolutePath());
                    return localFile.getAbsolutePath(); // 返回本地路径
                }catch (IOException e) {
                    Log.e("downloadImg", "文件写入失败：" + e.getMessage());
                }
            } catch (ClientException clientException) {
                Log.e("downloadImg", "客户端异常：" + clientException.getMessage());
            } catch (ServiceException serviceException) {
                Log.e("downloadImg", "服务端异常：" + serviceException.getErrorCode());
                Log.e("downloadImg", "RequestId：" + serviceException.getRequestId());
                Log.e("downloadImg", "HostId：" + serviceException.getHostId());
                Log.e("downloadImg", "RawMessage：" + serviceException.getRawMessage());
            }

            return null; // 下载失败
        }

        private void resetSubscriptions() {
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
        }
    }

    private void handleIncomingMessage(Message message) throws ServiceException, ClientException {
        // Save message to database
        Log.i("WebSocket","new message come");
        if(message.isImage()!=0){
            message.setContent("[图片]");
            message.setIsImage(0);
        }

        dbHelper.insertMessage(db, message);
        // Broadcast the message to update UI
        Intent intent = new Intent("NEW_MESSAGE_RECEIVED");
        intent.putExtra("message", new Gson().toJson(message));
        Log.d("WebSocket", "Broadcasting new message");
        sendBroadcast(intent);
    }

    private void sendAcknowledgmentToServer() {
        String payload = "{\"username\": \"" + username + "\"}";
        Log.i("confirm",payload);
        // 发送消息
        stompClient.send("/app/topic/acknowledgment", payload).subscribe();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    // 请求未读消息
    private void fetchUnreadMessages() {
        String url = "http://10.0.2.2:8081/api/auth/fetchMessages?username=" + username;

        // 发起 GET 请求获取未读消息
        Request request = new Request.Builder().url(url).build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("WebSocket","Message received successfully.");
                }else{
                   Log.e("WebSocket","Message received failed.");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FetchMessages", "Failed to fetch messages", e);
            }
        });
    }
}//oss下载图片,读文件时出错


