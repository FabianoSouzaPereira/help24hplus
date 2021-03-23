package com.fabianosdev.help24hplus;


import android.content.SharedPreferences;
import android.os.Bundle;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    private static final String FILE_NAME = "ClientConfig.txt";
    private TextView mEditTelefone;
    private TextView mEditServidor1;
    private TextView mEditServidor2;
    private TextView mEditConta;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setTitle("Configuração");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this.getContext());
            String tentativas = sharedPreferences.getString("tentativas", "");
            Toast.makeText(getActivity(), " "+tentativas+" ",
                    Toast.LENGTH_SHORT).show();
        }
    }

}