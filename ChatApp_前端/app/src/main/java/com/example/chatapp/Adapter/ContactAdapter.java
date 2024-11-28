package com.example.chatapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.pojo.Contact;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<Contact> contacts;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onChatClick(Contact contact);
        void onDeleteClick(Contact contact);
    }

    public ContactAdapter(List<Contact> contacts, OnItemClickListener listener) {
        this.contacts = contacts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.bind(contact, listener);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUsername;
        public ImageView ivUnreadIndicator;
        public Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivUnreadIndicator = itemView.findViewById(R.id.ivUnreadIndicator);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Contact contact, OnItemClickListener listener) {
            tvUsername.setText(contact.getUsername());
            ivUnreadIndicator.setVisibility(contact.hasUnreadMessages() ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onChatClick(contact));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(contact));
        }
    }
}
