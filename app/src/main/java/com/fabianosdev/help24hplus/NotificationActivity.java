package com.fabianosdev.help24hplus;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.TextView;

public class NotificationActivity extends AppCompatActivity {

    private TextView tipo;
    private TextView estado;
    private TextView inf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        setTitle("Notificação");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tipo = findViewById(R.id.txtTipo);
        estado = findViewById(R.id.lbStatus);
        inf = findViewById(R.id.txtData);

  /*      tipo.setText("Pânico");
        estado.setText("Evento enviado com sucesso!");
        inf.setText("datahora");*/

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}