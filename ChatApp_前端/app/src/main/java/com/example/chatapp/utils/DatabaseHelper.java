package com.example.chatapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.chatapp.pojo.Message;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ChatApp.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE contacts (username TEXT PRIMARY KEY)");
        db.execSQL("CREATE TABLE messages (sender TEXT, receiver TEXT, content TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,isread Integer Default 1,sentSuccess Integer DEFAULT 0,isImage Int DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS contacts");
        db.execSQL("DROP TABLE IF EXISTS messages");
        onCreate(db);
    }
    public void insertMessage(SQLiteDatabase db, Message message){
        ContentValues values=new ContentValues();
        values.put("sender",message.getSender());
        values.put("receiver",message.getReceiver());
        values.put("content",message.getContent());
        values.put("timestamp",message.getTimestamp());
        values.put("isImage",message.isImage());
        db.insert("messages",null,values);
    }
    public void clearMessage(SQLiteDatabase db, String friendName) {
        // 删除与指定好友相关的所有消息
        db.delete("messages", "username=?", new String[]{friendName});
    }

    public void deleteMessage(SQLiteDatabase db, Message message) {
        // 删除特定时间戳和发送者的消息
        db.delete("messages", "timestamp=? AND sender=?",
                new String[]{message.getTimestamp(), message.getSender()});
    }

    public void updateMessageURL(SQLiteDatabase db, Message message, String localUrl) {
        // 创建 ContentValues 存储要更新的字段和值
        ContentValues values = new ContentValues();
        values.put("content", localUrl); // 更新的字段和值

        // 执行更新操作
        db.update(
                "messages",               // 表名
                values,                   // 更新的数据
                "timestamp = ? AND sender = ?", // 条件
                new String[]{message.getTimestamp(), message.getSender()} // 条件对应的参数
        );
    }
}
