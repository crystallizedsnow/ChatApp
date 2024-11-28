package com.example.chatapp.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat; // 推荐用于兼容性处理的 ContextCompat 工具类
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Activity.ChatActivity;
import com.example.chatapp.R;
import com.example.chatapp.pojo.Message;
import com.example.chatapp.utils.DatabaseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private String username;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    private Context context;

    public MessageAdapter(List<Message> messages, String username, Context context) {
        this.messages = messages != null ? messages : new ArrayList<>();
        this.username=username;
        dbHelper= new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        this.context = context;
    }

    public List<Message> getMessages() {
        return messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(view);

    }
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        // 设置消息内容和时间
        if (message.isImage()!=0) {
            // 显示图片消息
            holder.tvMessageContent.setVisibility(View.GONE);
            holder.ivMessageImage.setVisibility(View.VISIBLE);
            // 检查权限
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 请求权限，需要强转为 Activity 来使用 requestPermissions
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
                }
            }
            File file = new File(message.getContent());
            Log.d("FileExists", "File exists: " + file.exists());
            Glide.with(context)
                    .load(message.getContent()) // 图片 URL
                    .placeholder(R.drawable.picture_failed)
                    .into(holder.ivMessageImage);

        } else {
            holder.tvMessageContent.setVisibility(View.VISIBLE);
            holder.tvMessageContent.setText(message.getContent());
            holder.ivMessageImage.setVisibility(View.GONE);
        }
        holder.tvMessageTimestamp.setText(message.getTimestamp());
        int screenWidth = holder.itemView.getContext().getResources().getDisplayMetrics().widthPixels;

        // 设置气泡最大宽度为屏幕宽度的3/4
        int maxWidth = (int) (screenWidth * 0.75);

        // 动态设置最大宽度
        holder.tvMessageContent.setMaxWidth(maxWidth);
        // 根据发送者调整位置和背景
        if (message.getSender().equals(username)) {
            // 自己发送的消息
            holder.tvMessageContent.setBackgroundResource(R.drawable.message_bubble_sent);
            ((LinearLayout.LayoutParams) holder.tvMessageContent.getLayoutParams()).gravity = Gravity.END;
        } else {
            // 对方发送的消息
            holder.tvMessageContent.setBackgroundResource(R.drawable.message_bubble_received);
            ((LinearLayout.LayoutParams) holder.tvMessageContent.getLayoutParams()).gravity = Gravity.START;
        }

        // 设置长按事件
        holder.itemView.setOnLongClickListener(v -> {
            // 显示删除按钮
            new AlertDialog.Builder(v.getContext())
                    .setMessage("确定删除这条消息吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        // 确保删除的消息是当前长按的消息
                        db.delete("messages", "timestamp=? AND sender=?",
                                new String[]{message.getTimestamp(), message.getSender()});

                        // 从消息列表中移除并更新视图
                        messages.remove(position);
                        notifyItemRemoved(position); // 通知适配器移除此项
                        notifyItemRangeChanged(position, messages.size()); // 更新剩余项的位置
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });
    }



    @Override
    public int getItemCount() {
        return messages.size();
    }

     static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageContent, tvMessageTimestamp;
        ImageView ivMessageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            tvMessageTimestamp = itemView.findViewById(R.id.tvMessageTimestamp);
        }
    }
}

