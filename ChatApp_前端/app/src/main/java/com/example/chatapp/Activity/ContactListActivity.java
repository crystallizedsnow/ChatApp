package com.example.chatapp.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Adapter.ContactAdapter;
import com.example.chatapp.R;
import com.example.chatapp.pojo.Contact;
import com.example.chatapp.pojo.Message;
import com.example.chatapp.service.WebSocketService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;

public class ContactListActivity extends AppCompatActivity {
    private RecyclerView rvContacts;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();
    private String currentUser; // 当前登录用户名
    private static final String PREFS_NAME = "ChatAppPrefs";
    public List<Contact> contacts = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        // 获取从 LoginActivity 传入的用户名
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUser = preferences.getString("username", null);

        loadContacts(); // 加载联系人列表

        rvContacts = findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        Toolbar toolbar = findViewById(R.id.contactList_toolbar);
        toolbar.setTitle("通讯录"); // 设置标题
        setSupportActionBar(toolbar); // 设置Toolbar为ActionBar
        if (getSupportActionBar() != null) {
            // 显示返回按钮
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        contactAdapter = new ContactAdapter(contactList, new ContactAdapter.OnItemClickListener() {
            @Override
            public void onChatClick(Contact contact) {
                contact.setHasUnreadMessages(false);
                Intent intent = new Intent(ContactListActivity.this, ChatActivity.class);
                updateUnreadIndicators();
                intent.putExtra("username", contact.getUsername());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Contact contact) {
                deleteContact(contact);
            }
        });
        rvContacts.setAdapter(contactAdapter);

        findViewById(R.id.btnAddFriend).setOnClickListener(v -> addFriend());

        // 更新联系人列表的红点状态
        updateUnreadIndicators();
    }

    private void loadContacts() {
        String url = "http://10.0.2.2:8081/contractList/get?username=" + currentUser;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ContactListActivity.this, "加载好友失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();

                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<String>>() {}.getType(); // 解析为字符串列表
                    List<String> usernames = gson.fromJson(jsonData, listType);

                    for (String username : usernames) {
                        contacts.add(new Contact(username, false)); // 默认没有未读消息
                    }

                    runOnUiThread(() -> {
                        contactList.clear();
                        contactList.addAll(contacts);
                        contacts.clear();
                        contactAdapter.notifyDataSetChanged();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ContactListActivity.this, "获取好友列表失败", Toast.LENGTH_SHORT).show());
                }
            }

        });
    }

    private void addFriend() {
        String friendname = ((EditText) findViewById(R.id.etSearch)).getText().toString();
        if (!friendname.isEmpty()) {
            String url = "http://10.0.2.2:8081/contractList/add?username=" + currentUser + "&friendname=" + friendname;
            Request request = new Request.Builder().url(url).post(RequestBody.create("", null)).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ContactListActivity.this, "添加好友失败", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(ContactListActivity.this, "好友添加成功", Toast.LENGTH_SHORT).show();
                            loadContacts(); // 重新加载联系人列表
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(ContactListActivity.this, "添加失败", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        }
    }

    private void deleteContact(Contact contact) {
        String url = "http://10.0.2.2:8081/contractList/delete?username=" + currentUser + "&friendname=" + contact.getUsername();
        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ContactListActivity.this, "删除好友失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        contacts.clear();
                        contactList.remove(contact);
                        contactAdapter.notifyDataSetChanged();
                        Toast.makeText(ContactListActivity.this, "好友已删除", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ContactListActivity.this, "删除失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateUnreadIndicators() {
        for (Contact contact : contacts) {
            contact.setHasUnreadMessages( contact.hasUnreadMessages());
        }
        contactAdapter.notifyDataSetChanged();
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String username = preferences.getString("username", "");

        if (username.isEmpty()) {
            Toast.makeText(ContactListActivity.this, "未找到用户信息", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2:8081/api/auth/logout?username=" + username;

        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request request = new Request.Builder()
                .url(url)
                .post(emptyBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                startActivity(new Intent(ContactListActivity.this, LoginActivity.class));
                finish();
                runOnUiThread(() ->
                        Toast.makeText(ContactListActivity.this, "服务器未收到退出信息", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ContactListActivity.this, "已退出ChatApp", Toast.LENGTH_SHORT).show();
                        // 清除登录状态
                        editor.putString("username", null);
                        editor.putBoolean("is_logged_in", false);
                        editor.apply();
//                        Intent stopIntent = new Intent(this, WebSocketService.class);
                        //调用stopService()方法-传入Intent对象,以此停止服务
//                        stopService(stopIntent);
                        startActivity(new Intent(ContactListActivity.this, LoginActivity.class));
                        finish();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(ContactListActivity.this, "退出失败，请稍后再试", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    @Override
    protected void onResume() {//切到通讯录界面时调用
        super.onResume();
        IntentFilter filter = new IntentFilter("NEW_MESSAGE_RECEIVED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(newMessageReceiver, filter, Context.RECEIVER_EXPORTED);
        }else{
            registerReceiver(newMessageReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(newMessageReceiver);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 返回上一页面
            logout();
        }
        return true;
    }
    private final BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isFinishing() || isDestroyed()) {
                Log.i("BroadcastReceiver", "ContactListActivity is not active. Ignoring update.");
                return;
            }
            String messageJson = intent.getStringExtra("message");
            Message message = new Gson().fromJson(messageJson, Message.class);

            // Update the unread status for the relevant contact
            for (Contact contact : contacts) {
                if (contact.getUsername().equals(message.getSender())) {
                    contact.setHasUnreadMessages(true);
                    break;
                }
            }

            // Refresh the UI to show the red dot
            updateUnreadIndicators();
        }
    };
}

