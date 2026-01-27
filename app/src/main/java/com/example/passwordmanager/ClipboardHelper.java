package com.example.passwordmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ClipboardHelper {
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable clearRunnable;
    private static final String LABEL = "PasswordManager";
    private static final int CLEAR_DELAY = 180000; // 3 dakika

    public static void copyToClipboard(Context context, String text, String toastMessage) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(LABEL, text);

        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, toastMessage + " (3 dk sonra silinecek)", Toast.LENGTH_SHORT).show();

            if (clearRunnable != null) {
                handler.removeCallbacks(clearRunnable);
            }

            clearRunnable = () -> {
                ClipData currentClip = clipboard.getPrimaryClip();
                if (currentClip != null && currentClip.getDescription().getLabel() != null &&
                        currentClip.getDescription().getLabel().equals(LABEL)) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
                    Toast.makeText(context, "Güvenlik için pano temizlendi", Toast.LENGTH_SHORT).show();
                }
            };

            handler.postDelayed(clearRunnable, CLEAR_DELAY);
        }
    }
}