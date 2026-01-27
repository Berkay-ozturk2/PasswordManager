package com.example.passwordmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private List<Account> accountList;

    public AccountAdapter(List<Account> accountList) {
        this.accountList = accountList;
    }

    public void updateAccounts(List<Account> newAccounts) {
        this.accountList = newAccounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.tvSiteName.setText(account.title);
        holder.tvUsername.setText(account.username);

        // Detay sayfasına gitme
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("id", account.id);
            intent.putExtra("title", account.title);
            intent.putExtra("username", account.username);
            intent.putExtra("password", account.password);
            intent.putExtra("category", account.category);
            v.getContext().startActivity(intent);
        });

        // Hızlı şifre kopyalama işlemi
        // onBindViewHolder içindeki ilgili kısım:
        holder.btnQuickCopy.setOnClickListener(v -> {
            CryptoHelper crypto = new CryptoHelper();
            // Veritabanındaki şifreli metni çözüp panoya öyle gönderiyoruz
            String decryptedPassword = crypto.decrypt(account.password);

            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Password", decryptedPassword);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(v.getContext(), "Şifre kopyalandı", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return accountList != null ? accountList.size() : 0;
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvSiteName, tvUsername;
        ImageButton btnQuickCopy;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSiteName = itemView.findViewById(R.id.tvSiteName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnQuickCopy = itemView.findViewById(R.id.btnQuickCopy);
        }
    }
}