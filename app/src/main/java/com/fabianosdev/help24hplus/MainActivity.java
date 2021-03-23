package com.fabianosdev.help24hplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "";
    private static final String CHANNEL_ID = "Pânico";
    private static final String FILE_NAME = "ConectionConfig.txt";
    private static final String FILE_LOG = "LogEvent.txt";
    public String datahora;
    public static String servidor1 = "";
    public static String servidor2 = "";
    public static String conta = "";
    public static String particao = "";
    public static String phoneNumber = "";
    public static String chave = "";
    static String porta1= "";
    static String porta2 = "";
    static String varprogress = "";
    public static double latitude = 0;
    public static double longitude = 0;
    static final String[] evento = {"EPAN", "ECHE", "ESAI"};
    static String ev = "";
    public static String status = "0";
    public static int phoneState = -1;

    public static final int PRIORITY_HIGTH_ACCURACY = 100;
    public static final int PERMISSION_CODE = 3;

    FusedLocationProviderClient client;
    GeofencingClient geofencingClient;
    LocationCallback locationCallback;

    ListView listView;
    ArrayList<View> arrayList = new ArrayList<View>();
    ArrayAdapter<String> arrayAdapter;
    private TextView tvCoodinate;
    private View btnItemView;
    private View itemposition;
    private View itemmap;

    private static Button btnPanico;
    private TextView lbStatus;
    private TextView txtValor;
    private TextView txtHostPort;
    private String user = "samsung";
    private SocketTask st;
    int Permission_All = 1;
    String[] Permissions = {Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    String[] Permission = { Manifest.permission.READ_PHONE_NUMBERS };

    public TextView som;
    public static String tentativas; //tentativas de comunicação
    static int atemptcount = 0;
    static int a = 0, b = 0; //controle cadencia de servidores
    public static String tempo; //tempo entre tentativas de comunicação
    public String intervalo; //intervalo localizador
    public String locatefast; //captura de localização previa
    private static ProgressBar progressbar;
    private TextView lbReturn;
    private static int count = 0;
    private static Boolean senderror=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mainloadFile();
        createNotificationChannel();
        client = LocationServices.getFusedLocationProviderClient( this );
        geofencingClient = LocationServices.getGeofencingClient( this );
        btnPanico = (Button) findViewById( R.id.btnPanico );
        btnPanico.setOnClickListener( btnConnectListener );
        lbStatus = findViewById( R.id.lbStatus);
        lbReturn = findViewById(R.id.lbReturn);
        progressbar = findViewById(R.id.progressBar);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                notificationDisplay();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mainloadFile();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        tentativas = sharedPreferences.getString("tentativas", "");
        tempo = sharedPreferences.getString("tempotentativas", "");
        intervalo = sharedPreferences.getString("intervalo", "");
        locatefast = sharedPreferences.getString("locatefast", "");

        Toast.makeText(getApplicationContext(), " " + tentativas + " , "+ tempo +"," +intervalo+","+locatefast,
                Toast.LENGTH_SHORT).show();


    }

    /** Como é necessário criar o canal de notificação antes de postar notificações no Android 8.0
     *  e versões mais recentes, execute esse código assim que o app for iniciado.  Mas ainda é
     *  necessário definir a prioridade com setPriority() para oferecer compatibilidade ao
     *  Android 7.1 e versões anteriores.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void notificationDisplay() {
        Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int id = (int)(1);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_stat_priority);
        builder.setContentTitle("Pânico");
        builder.setContentText("ENVIADO PÂNICO COM SUCESSO!");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(id, builder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        datahora();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPhonePermissions( this, Permissions )) {
                ActivityCompat.requestPermissions( this,Permissions,Permission_All );
            }
        }
        int errorcode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable( MainActivity.this );
        switch (errorcode) {
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
            case ConnectionResult.SERVICE_DISABLED:
                Log.i( "Teste", "Show dialog =======" );
                GoogleApiAvailability.getInstance().getErrorDialog( MainActivity.this, errorcode, 0, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                } ).show();
                break;
            case ConnectionResult.SUCCESS:
                Log.i( "Teste", "Google Play Services up-to-date =======" );
                break;
        }
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        client.getLastLocation().addOnSuccessListener( new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.i( "Location: ", ""+location.getLatitude());
                    Log.i( "Longitude: ","" + location.getLongitude());
                    Log.i("Bearing: ","" + location.getBearing());
                    Log.i("Altitude: ","" + location.getAltitude());
                    Log.i("Speed: ","" + location.getSpeed());
                    Log.i("Provider: ","" + location.getProvider());
                    Log.i("Accuracy: ","" + location.getAccuracy());
                    Log.i("Hora: ","" + DateFormat.getTimeInstance().format( new Date() ) + "  ======= " );
                } else {
                    Log.i( "location - ", "null" );
                }
            }
        } ).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        } );
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval( 60 * 1000 );
        locationRequest.setFastestInterval( 30 * 1000 );
        locationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY ); //uso preciso com gps.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest( locationRequest );
        SettingsClient settingsClient = LocationServices.getSettingsClient( this );
        settingsClient.checkLocationSettings( builder.build() ).addOnSuccessListener( new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i( "teste", locationSettingsResponse.getLocationSettingsStates().isNetworkLocationPresent() + "" );
            }
        } ).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult( MainActivity.this, 10 );
                    } catch (IntentSender.SendIntentException el) {

                    }
                }
            }
        } );
        //Listener pega sempre a nova posição do provider. Kill app se local é nulo.
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.i( "Local position -> ", "local is null" );
                    return;
                }
                //procurar na lista de localição.
                for (Location location : locationResult.getLocations()) {
                    Log.i( "Location pos -> ", location.getLatitude() + " " + location.getLongitude() );
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                Log.i( "locationAvailability : ", locationAvailability.isLocationAvailable() + "" );
            }
        };
        client.requestLocationUpdates( locationRequest, locationCallback, null );

    }

    @SuppressLint("SimpleDateFormat")
    private View.OnClickListener btnConnectListener = new View.OnClickListener() {

        public void onClick(View v) {
            senderror=false;
            String servidor="";
            ev = evento[0];
            String porta = "0000";
            int limit = Integer.parseInt(tentativas);

            try {

                if(servidor1 == null && servidor2 == null){
                    Toast.makeText(getApplicationContext(), "Preencha o campo servidor.",
                            Toast.LENGTH_SHORT).show();

                }

                if(servidor1 != null && servidor2 != null && atemptcount <= limit ){
                    if(a == 0) {
                        a = 1;
                        atemptcount++;
                        servidor = servidor1;
                        porta = porta1;
                    }else{
                            a=0;
                            atemptcount++;
                            servidor = servidor2;
                            porta = porta2;
                    }
                    openSocket(servidor,porta);

                }
                if(servidor1 != null && servidor2 == null && atemptcount <= limit){

                    atemptcount++;
                    servidor = servidor1;
                    porta = porta1;

                    openSocket(servidor,porta);

                }
                if(servidor2 != null && servidor1 == null && atemptcount <= limit){

                    atemptcount++;
                    servidor = servidor2;
                    porta = porta2;

                    openSocket(servidor,porta);

                }


            } catch (Exception e) {
                Log.i( "Exeption - > ", "" + e );
            }
        }
    };

    @SuppressLint("StaticFieldLeak")
    public void openSocket(String servidor, String porta){
        final String host = String.valueOf( servidor ).trim();
        final int port = Integer.parseInt(porta);
        progressbar.setVisibility(View.VISIBLE);
        lbStatus.setText("Enviando...");
        // Instancia a classe de conexão com socket   "179.184.92.101", 5198
        st = new SocketTask( host, port, 10000 ) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onProgressUpdate(String... progress) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
                // Recupera o retorno
                lbReturn.setText( sdf.format( new Date() ) + " - " + progress[0] );
                varprogress = progress[0];
                if(varprogress.contains("@")){
                    notificationDisplay();
                    lbStatus.setText("Em Espera");
                    progressbar.setVisibility(View.GONE);
                }else{
                    if(count <= Integer.parseInt(tentativas.toString()) && senderror==false) {
                        progressbar.setVisibility(View.VISIBLE);
                        varprogress="";
                        count++;
                        Toast.makeText(getApplicationContext(), "Tentativa: "+ count, Toast.LENGTH_SHORT).show();
                      //  delayRetry();

                        //TODO   cancelar operacoes

                    }else {
                        progressbar.setVisibility(View.GONE);
                        senderror=true;
                        count = 0;
                        atemptcount=0;
                        Toast.makeText(getApplicationContext(), "Erro de comunicação com o servidor.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                lbStatus.setText("Em Espera");
                return;
            }
        };
        String dados = ("#" + conta + "," + latitude + "," + longitude + "," + ev + "," + status + particao + datahora + "$");
        st.execute( dados );

    }

/*    private static void delayRetry(){
        if(!varprogress.contains("@resposta")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                   Log.i("h","hello");
                }
            }, 5000);
        }
    }*/

    @SuppressLint("InlinedApi")
    private static boolean hasPhonePermissions(Context context, String... permissions) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null){
            for(String permission: permissions){
                if(ActivityCompat.checkSelfPermission( context, permission ) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText( MainActivity.this, "Permissão aceita" , Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText( MainActivity.this, "Permissão negada" , Toast.LENGTH_SHORT).show();
            }
        }
    }


    /** Inspection info:This check scans through your code and libraries and looks at the APIs being used,
     *  and checks this against the set of permissions required to access those APIs.
     *  If the code using those APIs is called at runtime, then the program will crash.   */
    @SuppressLint("HardwareIds")
    @SuppressWarnings("deprecation")
    public void getPHONElowerVer(){
        String[] permission = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            permission = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS};
        }
        try {
            //  String phoneNumber = "";
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    assert telephonyMgr != null;
                    phoneNumber = telephonyMgr.getLine1Number();  Log.i( "PhoneNumber: ","" + phoneNumber );
                    phoneState =  telephonyMgr.getSimState();                Log.i( "PhoneState:A ","" + phoneState );
                } else {
                    if (telephonyMgr != null) {
                        phoneNumber = telephonyMgr.getSubscriberId();        Log.i( "PhoneNumber: ","" + phoneNumber );
                    }

                    assert telephonyMgr != null;
                    phoneState =  telephonyMgr.getSimState();                Log.i( "PhoneState: B","" + phoneState );
                }
            }else{
                ActivityCompat.requestPermissions( MainActivity.this, permission,1);
                Log.i( "Manifest.permission: " ,"NOT PERMISSION_GRANTED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent config = new Intent( MainActivity.this, SettingsActivity.class );
            startActivity( config );
            return true;
        }
        if (id == R.id.action_connection) {
            Intent conection = new Intent( MainActivity.this, ConectionActivity.class );
            startActivity( conection );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void mainloadFile() {
        File file = new File( getFilesDir() + "/" + FILE_NAME );
        if (file.exists()) {
            FileInputStream fis = null;
            try {

                fis = openFileInput( FILE_NAME );
                InputStreamReader isr = new InputStreamReader( fis );
                BufferedReader br = new BufferedReader( isr );
                StringBuilder sb = new StringBuilder();
                String text;

                while ((text = br.readLine()) != null) {
                    sb.append( text ).append( "\n" );
                }

                String read = sb.toString();
                String [] v = read.split(",");
                String conta = v[0];
                String particao = v[1];
                String chave = v[2];
                String servidor1 = v[3];
                String porta1= v[4];
                String servidor2 = v[5];
                String porta2 = v[6];
                MainActivity.chave = chave;
                MainActivity.conta = conta;
                MainActivity.particao = particao;
                MainActivity.servidor1 = servidor1;
                MainActivity.porta1 = porta1;
                MainActivity.servidor2 = servidor2;
                MainActivity.porta2 = porta2;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    Objects.requireNonNull(fis).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void datahora(){
        SimpleDateFormat formataData = new SimpleDateFormat(getString(R.string._datahota));
        Date data = new Date();
        datahora = formataData.format(data);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}