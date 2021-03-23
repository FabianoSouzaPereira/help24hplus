package com.fabianosdev.help24hplus;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static com.fabianosdev.help24hplus.MainActivity.chave;
import static com.fabianosdev.help24hplus.MainActivity.servidor1;
import static com.fabianosdev.help24hplus.MainActivity.servidor2;

public class ConectionActivity extends AppCompatActivity {
    private static final String FILE_NAME = "ConectionConfig.txt";
    private TextView mEditChave;
    private TextView mEditServidor1;
    private TextView mEditServidor2;
    private TextView mEditConta;
    private TextView mEditParticao;
    private TextView mEditPorta1;
    private TextView mEditPorta2;
    private Button btnOk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conection);
        setTitle("Conexão");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mEditChave = findViewById( R.id.editChave );
        mEditServidor1 = findViewById( R.id.editServidor1 );
        mEditServidor2 = findViewById( R.id.editServidor2 );
        mEditPorta1 = findViewById( R.id.editPorta1 );
        mEditPorta2 = findViewById( R.id.editPorta2 );
        mEditConta = findViewById( R.id.editConta );
        mEditParticao = findViewById( R.id.editParticao );
        btnOk = findViewById( R.id.btnOk );
        btnOk.setOnClickListener( btnOkListener);
        loadFile();
    }

    public View.OnClickListener btnOkListener  = new View.OnClickListener(){
        public void onClick(View v) {
            saveFile(v);
        }
    };

    public void saveFile(View view){
        String chave = mEditChave.getText().toString().trim();
        String servidor1 = mEditServidor1.getText().toString().trim();
        String servidor2 = mEditServidor2.getText().toString().trim();
        String conta = mEditConta.getText().toString().trim();
        String porta1 = mEditPorta1.getText().toString().trim();
        String porta2 = mEditPorta2.getText().toString().trim();
        String particao = mEditParticao.getText().toString().trim();

        if(servidor1.length() == 0 && servidor2.length() == 0 ){
            Snackbar.make(view, "É necessário preencher um servidor.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }else  {
            if (servidor1.length() != 0) {
                if (servidor1.length() < 7) {
                    Snackbar.make(view, "Servidor 1 requer ao menos 7 caracteres/dígitos", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
            }
            if (servidor2.length() != 0) {
                if (servidor2.length() < 7) {
                    Snackbar.make(view, "Servidor 2 requer ao menos 7 caracteres/dígitos", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
            }
        }


        if(!particao.isEmpty() && !conta.isEmpty() && !chave.isEmpty() && !servidor1.isEmpty() || !servidor2.isEmpty()) {
            String text = ( conta + "," + particao + ","  + chave + "," + servidor1 + "," + porta1 + "," + servidor2 + "," + porta2 + "," );
            FileOutputStream fos = null;

            try {
                fos = openFileOutput( FILE_NAME, MODE_PRIVATE );
                fos.write( text.getBytes() );
                mEditChave.setText( "" );
                mEditParticao.setText( "" );
                mEditServidor1.setText( "" );
                mEditServidor2.setText( "" );
                mEditConta.setText( "" );
                mEditPorta1.setText( "" );
                mEditPorta2.setText( "" );
/*                MainActivity.chave = chave;
                MainActivity.conta = conta;
                MainActivity.particao = particao;
                MainActivity.servidor1 = servidor1;
                MainActivity.servidor2 = servidor2;
                MainActivity.porta1 = porta1;
                MainActivity.porta2 = porta2;*/
                Intent config = new Intent( ConectionActivity.this, MainActivity.class );
                startActivity( config );
                finish();
                Toast.makeText( this, "Save to " + getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_LONG).show();
                Log.i( "Configuração ", "Save to " + getFilesDir() + "/" + FILE_NAME );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{

            Snackbar.make(view, "Deve ser preenchido todos os campos", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }
    }

    public void loadFile() {
        FileInputStream fis = null;
        try {
                File file = new File(getFilesDir() + "/" + FILE_NAME);
                if (file.exists()) {

                    fis = openFileInput(FILE_NAME);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String text;

                    while ((text = br.readLine()) != null) {
                        sb.append(text).append("\n");
                    }

                    String read = sb.toString();
                    String[] v = read.split(",");
                    String conta = v[0];
                    String particao = v[1];
                    String chave = v[2];
                    String servidor1 = v[3];
                    String porta1 = v[4];
                    String servidor2 = v[5];
                    String porta2 = v[6];
                    mEditConta.setText( conta );
                    mEditParticao.setText( particao);
                    mEditChave.setText( chave );
                    mEditServidor1.setText( servidor1 );
                    mEditPorta1.setText( porta1 );
                    mEditServidor2.setText( servidor2 );
                    mEditPorta2.setText( porta2 );
/*                    MainActivity.chave = chave;
                    MainActivity.conta = conta;
                    MainActivity.particao = particao;
                    MainActivity.servidor1 = servidor1;
                    MainActivity.servidor2 = servidor2;
                    MainActivity.porta1 = porta1;
                    MainActivity.porta2 = porta2;*/
                }
        } catch(FileNotFoundException e){
                e.printStackTrace();
        } catch(IOException e){
                e.printStackTrace();
        } finally{
                try {
                    if (fis != null ){ fis.close();}
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

    }
}