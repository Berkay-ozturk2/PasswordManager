package com.example.passwordmanager;

import android.app.assist.AssistStructure;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import java.util.List;

/**
 * Otomatik Doldurma Servisi: Cihazdaki diğer uygulamaların giriş alanlarını algılar
 * ve veritabanındaki şifreli verileri çözerek kullanıcıya öneri olarak sunar.
 */
public class MyAutofillService extends AutofillService {

    @Override
    public void onFillRequest(@NonNull FillRequest request, @NonNull android.os.CancellationSignal cancellationSignal, @NonNull FillCallback callback) {
        // Formdaki alanları tespit etmek için en son ekran yapısını alıyoruz
        List<FillContext> contexts = request.getFillContexts();
        AssistStructure structure = contexts.get(contexts.size() - 1).getStructure();

        // Kullanıcı adı ve şifre alanlarının Autofill ID'lerini bulmak için ekranı tara
        AutofillIds ids = new AutofillIds();
        traverseStructure(structure, ids);

        // Eğer doldurulabilir bir alan tespit edilemediyse işlemi sonlandır
        if (ids.usernameId == null && ids.passwordId == null) {
            callback.onSuccess(null);
            return;
        }

        // Veritabanı işlemleri ana arayüzü kitlememek için arka planda yapılmalıdır
        new Thread(() -> {
            // DÜZELTME: AccountDao içindeki metod ismi 'getAll()' olarak güncellendi
            List<Account> accounts = AppDatabase.getInstance(this).accountDao().getAll();

            if (accounts == null || accounts.isEmpty()) {
                callback.onSuccess(null);
                return;
            }

            FillResponse.Builder responseBuilder = new FillResponse.Builder();
            CryptoHelper cryptoHelper = new CryptoHelper(); // Şifreleri çözmek için

            for (Account account : accounts) {
                Dataset.Builder datasetBuilder = new Dataset.Builder();

                // Kullanıcıya gösterilecek liste öğesi (Presentation)
                RemoteViews presentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
                presentation.setTextViewText(android.R.id.text1, account.title + " (" + account.username + ")");

                // 1. Kullanıcı Adını Yerleştir
                if (ids.usernameId != null) {
                    datasetBuilder.setValue(ids.usernameId, AutofillValue.forText(account.username), presentation);
                }

                // 2. Şifreyi Çöz (Decrypt) ve Yerleştir
                if (ids.passwordId != null) {
                    String decryptedPass = cryptoHelper.decrypt(account.password);
                    datasetBuilder.setValue(ids.passwordId, AutofillValue.forText(decryptedPass), presentation);
                }

                try {
                    responseBuilder.addDataset(datasetBuilder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            callback.onSuccess(responseBuilder.build());
        }).start();
    }

    /**
     * Ekran ağacını tarayan metod.
     */
    private void traverseStructure(AssistStructure structure, AutofillIds ids) {
        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            traverseNode(windowNode.getRootViewNode(), ids);
        }
    }

    /**
     * Düğümleri (Node) 'autofillHints' değerlerine göre kontrol eder.
     */
    private void traverseNode(AssistStructure.ViewNode node, AutofillIds ids) {
        String[] hints = node.getAutofillHints();
        if (hints != null) {
            for (String hint : hints) {
                if (hint.equalsIgnoreCase("username") || hint.equalsIgnoreCase("emailAddress")) {
                    ids.usernameId = node.getAutofillId();
                } else if (hint.equalsIgnoreCase("password")) {
                    ids.passwordId = node.getAutofillId();
                }
            }
        }

        int children = node.getChildCount();
        for (int i = 0; i < children; i++) {
            traverseNode(node.getChildAt(i), ids);
        }
    }

    private static class AutofillIds {
        AutofillId usernameId;
        AutofillId passwordId;
    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {
        callback.onSuccess();
    }
}