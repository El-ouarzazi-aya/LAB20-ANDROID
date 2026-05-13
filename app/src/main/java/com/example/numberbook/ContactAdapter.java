package com.example.numberbook;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface OnContactActionListener {
        void onContactClick(Contact contact, int position);
        void onContactLongClick(Contact contact, int position);
    }

    private List<Contact> contacts;
    private List<Contact> allContacts;
    private OnContactActionListener listener;

    public ContactAdapter(List<Contact> contacts, OnContactActionListener listener) {
        this.contacts    = new ArrayList<>(contacts);
        this.allContacts = new ArrayList<>(contacts);
        this.listener    = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact c = contacts.get(position);
        holder.tvName.setText(c.getName());
        holder.tvPhone.setText(c.getPhone());

        String name = c.getName() != null ? c.getName().trim() : "?";
        String[] parts = name.split(" ");
        String initials = parts.length >= 2
                ? String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[1].charAt(0))
                : name.length() >= 2 ? name.substring(0, 2) : name;
        holder.tvAvatar.setText(initials.toUpperCase());

        holder.syncDot.setVisibility(c.getId() > 0 ? View.VISIBLE : View.INVISIBLE);

        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
                .alpha(1f)
                .translationYBy(-10f)
                .setDuration(300)
                .setStartDelay(position * 40L)
                .start();

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onContactClick(c, position);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onContactLongClick(c, position);
            return true;
        });
    }

    @Override
    public int getItemCount() { return contacts.size(); }

    public void updateData(List<Contact> newList) {
        this.contacts    = new ArrayList<>(newList);
        this.allContacts = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void addContact(Contact c) {
        contacts.add(c);
        allContacts.add(c);
        notifyItemInserted(contacts.size() - 1);
    }

    public void removeAt(int position) {
        Contact c = contacts.get(position);
        contacts.remove(position);
        allContacts.remove(c);
        notifyItemRemoved(position);
    }

    public Contact getAt(int position) {
        return contacts.get(position);
    }

    public void filterByKeyword(String keyword) {
        contacts.clear();
        if (keyword.isEmpty()) {
            contacts.addAll(allContacts);
        } else {
            String lower = keyword.toLowerCase();
            for (Contact c : allContacts) {
                if (c.getName().toLowerCase().contains(lower)
                        || c.getPhone().toLowerCase().contains(lower)) {
                    contacts.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAvatar;
        View syncDot;

        ContactViewHolder(@NonNull View v) {
            super(v);
            tvName   = v.findViewById(R.id.tvName);
            tvPhone  = v.findViewById(R.id.tvPhone);
            tvAvatar = v.findViewById(R.id.tvAvatar);
            syncDot  = v.findViewById(R.id.syncDot);
        }
    }
}