package com.fabianosdev.help24hplus;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.os.AsyncTask;
import android.util.Log;



class SocketTask extends AsyncTask<String, String, Boolean> {
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private String host;
    private int port;
    private int timeout;
    //  private String user;

    /**
     * Construtor com host, porta e timeout
     *
     * @param host
     *            host para conexão
     * @param port
     *            porta para conexão
     *
     *
     */
    public SocketTask(String host, int port, int timeout) {
        super();
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        //   this.user = user;
    }

    /**
     * Envia dados adicionais se estiver conectado
     *
     * @param data
     *            dados addicionais
     * @throws IOException
     */
    public void sendData(String data) throws IOException {
        if (socket != null && socket.isConnected()) {
            os.write(data.getBytes());
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } /*finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        return sb.toString();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = false;
        try {
            SocketAddress sockaddr = new InetSocketAddress( host, port );
            socket = new Socket();
            Log.i( "Status : ", "Send Request to Server..." );
            socket.connect( sockaddr, timeout );
            socket.setOOBInline( true );
            Log.i( "Tcp receiver ", "" );
            if (socket.isConnected()) {
                publishProgress( "CONNECTED" );      Log.i( "Status : ", "Connected to Server " + host + ":" + port );
                is = socket.getInputStream();                Log.i( "Get data : ", "Receiving response from Server" );
                os = socket.getOutputStream();               Log.i( "Send data : ", "Send data to Server" );
                for (String p : params) {
                    os.write( p.getBytes() );
                }
                os.flush();
                byte[] buff = new byte[2048];
                int buffData = is.read( buff, 0, 2048 );

                while (buffData != -1) {
                    String response = new String( buff );
                    // Envia os dados
                    publishProgress( response );          Log.i( "Server Response: ", "" + response );
                    buffData = is.read( buff, 0, 2048 );            Log.i( "Entries : ", "" + buffData );
                }
            }
        } catch (IOException e) {
            publishProgress( "ERROR" );         Log.e( "SocketAndroid", "input and output error", e );
            result = true;
        } catch (Exception e) {
            publishProgress( "ERROR" );         Log.e( "SocketAndroid", "Generic error", e );
            result = true;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
                if (socket != null) {
                    socket.close();
                    Log.i( "Socket : ", "Disconnected" );
                }
                publishProgress( "DISCONNECTED" );
            } catch (Exception e) {
                Log.e( "SocketAndroid", "Error closing connection - ", e );
            }
        }
        return result;
    }

}