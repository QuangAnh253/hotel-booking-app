package com.nhom4.hotelbooking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.User;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.List;

public class AdminAccountAdapter extends RecyclerView.Adapter<AdminAccountAdapter.AccountViewHolder> {

    private List<User> userList;
    private OnAccountActionListener listener;

    public interface OnAccountActionListener {
        void onEdit(User user);
        void onDelete(User user);
    }

    public AdminAccountAdapter(List<User> userList, OnAccountActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvAccountName.setText(user.getName());
        holder.tvAccountEmail.setText(user.getEmail());
        holder.tvAccountPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "N/A");
        
        String role = user.getRole();
        holder.tvAccountRole.setText(role != null ? role.toUpperCase() : "USER");
        
        if (Constants.ROLE_ADMIN.equals(role)) {
            holder.tvAccountRole.setTextColor(android.graphics.Color.parseColor("#C62828"));
            holder.tvAccountRole.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
        } else {
            holder.tvAccountRole.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
            holder.tvAccountRole.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
        }

        holder.btnAccountMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Chỉnh sửa");
            popup.getMenu().add("Xoá tài khoản");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Chỉnh sửa")) {
                    listener.onEdit(user);
                } else {
                    listener.onDelete(user);
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountName, tvAccountEmail, tvAccountPhone, tvAccountRole;
        ImageButton btnAccountMenu;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountName = itemView.findViewById(R.id.tvAccountName);
            tvAccountEmail = itemView.findViewById(R.id.tvAccountEmail);
            tvAccountPhone = itemView.findViewById(R.id.tvAccountPhone);
            tvAccountRole = itemView.findViewById(R.id.tvAccountRole);
            btnAccountMenu = itemView.findViewById(R.id.btnAccountMenu);
        }
    }
}