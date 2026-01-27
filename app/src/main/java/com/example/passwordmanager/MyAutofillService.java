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

public class MyAutofillService extends AutofillService {

    @Override
    public void onFillRequest(@NonNull FillRequest request, @NonNull android.os.CancellationSignal cancellationSignal, @NonNull FillCallback callback) {
        List<FillContext> contexts = request.getFillContexts();
        AssistStructure structure = contexts.get(contexts.size() - 1).getStructure();

        AutofillIds ids = new AutofillIds();
        traverseStructure(structure, ids);

        if (ids.usernameId == null && ids.passwordId == null) {
            callback.onSuccess(null);
            return;
        }

        new Thread(() -> {
            // Veritabanından tüm hesapları çek
            List<Account> accounts = AppDatabase.getInstance(this).accountDao().getAll();

            if (accounts == null || accounts.isEmpty()) {
                callback.onSuccess(null);
                return;
            }

            FillResponse.Builder responseBuilder = new FillResponse.Builder();

            for (Account account : accounts) {
                Dataset.Builder datasetBuilder = new Dataset.Builder();

                RemoteViews presentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
                presentation.setTextViewText(android.R.id.text1, account.title + " (" + account.username + ")");

                // Kullanıcı adını yerleştir
                if (ids.usernameId != null) {
                    datasetBuilder.setValue(ids.usernameId, AutofillValue.forText(account.username), presentation);
                }

                // ŞİFRE ÇÖZME: Singleton CryptoHelper kullanımı
                if (ids.passwordId != null) {
                    String decryptedPass = CryptoHelper.getInstance().decrypt(account.password);
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

    private void traverseStructure(AssistStructure structure, AutofillIds ids) {
        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            traverseNode(windowNode.getRootViewNode(), ids);
        }
    }

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