package com.example.numberbook;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements ContactAdapter.OnContactActionListener {

    private Button btnLoadContacts, btnSyncContacts, btnSearch;
    private EditText etKeyword;
    private RecyclerView recyclerViewContacts;
    private ContactAdapter adapter;
    private List<Contact> contactList = new ArrayList<>();
    private ContactApi contactApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoadContacts      = findViewById(R.id.btnLoadContacts);
        btnSyncContacts      = findViewById(R.id.btnSyncContacts);
        btnSearch            = findViewById(R.id.btnSearch);
        etKeyword            = findViewById(R.id.etKeyword);
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);

        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(contactList, this);
        recyclerViewContacts.setAdapter(adapter);

        contactApi = RetrofitClient.getClient().create(ContactApi.class);

        btnLoadContacts.setOnClickListener(v -> checkPermissionAndLoadContacts());
        btnSyncContacts.setOnClickListener(v -> syncContactsToServer());
        btnSearch.setOnClickListener(v -> searchOnServer());

        // Filtrage local en temps réel pendant la frappe
        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                adapter.filterByKeyword(s.toString().trim());
            }
        });
    }

    // ── Permission ──────────────────────────────────────────────────────────

    private void checkPermissionAndLoadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private final androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) loadContacts();
                else Toast.makeText(this, "Permission refusee", Toast.LENGTH_SHORT).show();
            });

    // ── Chargement contacts téléphone ────────────────────────────────────────

    private void loadContacts() {
        contactList.clear();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name  = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactList.add(new Contact(name, normalizePhone(phone)));
            }
            cursor.close();
        }
        adapter.updateData(contactList);
        TextView tvCount = findViewById(R.id.tvContactCount);
        tvCount.setText(contactList.size() + " contacts");
        Toast.makeText(this, contactList.size() + " contacts charges", Toast.LENGTH_SHORT).show();
    }

    // Supprime espaces et tirets pour normaliser les numéros
    private String normalizePhone(String raw) {
        return raw.replaceAll("[\\s\\-()]", "");
    }

    // ── Synchronisation vers le serveur ─────────────────────────────────────

    private void syncContactsToServer() {
        if (contactList.isEmpty()) {
            Toast.makeText(this, "Chargez d abord les contacts", Toast.LENGTH_SHORT).show();
            return;
        }

        final int[] success = {0}, duplicates = {0}, errors = {0};
        final int total = contactList.size();

        for (Contact contact : contactList) {
            contactApi.insertContact(contact).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse> call,
                                       @NonNull Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse r = response.body();
                        if (r.isDuplicate())   duplicates[0]++;
                        else if (r.isSuccess()) success[0]++;
                        else                    errors[0]++;
                    } else {
                        errors[0]++;
                    }
                    checkSyncDone(total, success[0], duplicates[0], errors[0]);
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                    errors[0]++;
                    checkSyncDone(total, success[0], duplicates[0], errors[0]);
                }
            });
        }
    }

    private void checkSyncDone(int total, int success, int duplicates, int errors) {
        if (success + duplicates + errors < total) return;
        runOnUiThread(() -> {
            Toast.makeText(this,
                    success + " inseres, " + duplicates + " doublons, " + errors + " erreurs",
                    Toast.LENGTH_LONG).show();

            // Synchro bidirectionnelle : récupérer ce qui manque sur l'app
            fetchMissingContacts();
        });
    }

    // ── Synchro bidirectionnelle ─────────────────────────────────────────────

    private void fetchMissingContacts() {
        List<String> phones = new ArrayList<>();
        for (Contact c : contactList) phones.add(c.getPhone());

        Map<String, List<String>> body = new HashMap<>();
        body.put("phones", phones);

        contactApi.syncCheck(body).enqueue(new Callback<SyncCheckResponse>() {
            @Override
            public void onResponse(@NonNull Call<SyncCheckResponse> call,
                                   @NonNull Response<SyncCheckResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Contact> missing = response.body().getMissingOnApp();
                    if (missing != null && !missing.isEmpty()) {
                        for (Contact c : missing) {
                            contactList.add(c);
                            adapter.addContact(c);
                        }
                        Toast.makeText(MainActivity.this,
                                missing.size() + " contact(s) recuperes du serveur",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SyncCheckResponse> call, @NonNull Throwable t) {}
        });
    }

    // ── Recherche distante ───────────────────────────────────────────────────

    private void searchOnServer() {
        String keyword = etKeyword.getText().toString().trim();
        if (keyword.isEmpty()) {
            Toast.makeText(this, "Saisir un nom ou un numero", Toast.LENGTH_SHORT).show();
            return;
        }
        contactApi.searchContacts(keyword).enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(@NonNull Call<List<Contact>> call,
                                   @NonNull Response<List<Contact>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                    Toast.makeText(MainActivity.this,
                            response.body().size() + " resultat(s)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contact>> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur de recherche", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Affichage détaillé au clic ───────────────────────────────────────────

    @Override
    public void onContactClick(Contact contact, int position) {
        String details =
                "Nom    : " + contact.getName()  + "\n" +
                        "Numero : " + contact.getPhone() + "\n" +
                        "Source : " + (contact.getSource() != null ? contact.getSource() : "local") + "\n" +
                        "Ajoute : " + (contact.getCreated_at() != null ? contact.getCreated_at() : "-");

        new AlertDialog.Builder(this)
                .setTitle("Detail du contact")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .setNegativeButton("Supprimer", (d, w) -> deleteContact(contact, position))
                .show();
    }

    // ── Suppression au long clic ─────────────────────────────────────────────

    @Override
    public void onContactLongClick(Contact contact, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Supprimer " + contact.getName() + " du serveur ?")
                .setPositiveButton("Oui", (d, w) -> deleteContact(contact, position))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteContact(Contact contact, int position) {
        if (contact.getId() <= 0) {
            adapter.removeAt(position);
            contactList.remove(contact);
            return;
        }
        Map<String, Integer> body = new HashMap<>();
        body.put("id", contact.getId());

        contactApi.deleteContact(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call,
                                   @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    adapter.removeAt(position);
                    contactList.remove(contact);
                    Toast.makeText(MainActivity.this,
                            "Contact supprime", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Erreur suppression", Toast.LENGTH_SHORT).show();
            }
        });
    }
}