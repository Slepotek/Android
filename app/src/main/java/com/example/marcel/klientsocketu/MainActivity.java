package com.example.marcel.klientsocketu;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;

public class MainActivity extends Activity {

    private Button tempPlus;
    private TextView viewTempRzecz;
    private Socket socket;
    private PrintWriter out;
    private SocketClient socketClient;
    private Button connect;
    private TextView viewTempZad;
    private Button tempMinus;
    private int tempZ = 600;
    private int indicator = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempPlus = (Button) findViewById(R.id.tempPlus);
        viewTempRzecz = (TextView) findViewById(R.id.viewTempRzecz);
        connect = (Button) findViewById(R.id.connect);
        tempMinus = (Button) findViewById(R.id.tempMinus);
        viewTempZad = (TextView) findViewById(R.id.viewTempZad);

    }


    public void connectButton (View v){
        if(indicator == 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connect();
                    indicator = 1;
                }
            }).start();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        sendMessage();
                        waiter();
                    }
                }
            }).start();
        }else{
            disconnect();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

    }

    public void tempUp (View v){
        tempZ=tempZ+11;
        double msg = tempZ;
        final double tempMsg = ((((msg/4096)*3.3)*100)-24.5);
        viewTempZad.post(new Runnable() {
            @Override
            public void run() {
                viewTempZad.setText(String.format(Locale.CANADA,"%.0f",tempMsg));
            }
        });
    }

    public void tempDown (View v){
        tempZ=tempZ-11;
        double msg = tempZ;
        final double tempMsg = ((((msg/4096)*3.3)*100)-24.5);
        viewTempZad.post(new Runnable() {
            @Override
            public void run() {
                viewTempZad.setText(String.format(Locale.CANADA,"%.0f",tempMsg));
            }
        });
    }

    private void connect(){
        if(socket!=null){
            disconnect();
            return;
        }
        String ip = "192.168.43.54";
        int port = 32000;
        try{
            Log.d("Klient Socketu","Connecting to socket...");
            socket = new Socket(ip,port);
        }catch (IOException e){
            e.printStackTrace();
            Log.d("Klient Socketu","Nie utworzono socketu...");
        }
        socketClient = SocketClient.handle(this, socket);
    }

    public void sendMessage(){
        try{
            if(out==null){
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),true);
            }
            out.print(String.valueOf(tempZ)+"\r\n");
            out.flush();
        }catch (IOException e){
            Log.d("Klient Socketu","Error Sending Message");
            disconnect();
        }
    }

    public void disconnect(){
        try{
            socketClient.setDesonnected(true);
            socket.close();
        }catch (Exception e){
            Log.d("Klient Socketu", "Error closing socket : "+e);
        }
        socket=null;
        out=null;
    }

    public void setMessage (String s){
        double msg = Integer.parseInt(s);
        final double tempMsg = ((((msg/4096)*3.3)*100)-24.5);

        viewTempRzecz.post(new Runnable() {
            @Override
            public void run() {
                viewTempRzecz.setText(String.format(Locale.CANADA,"%.0f",tempMsg));
            }
        });
    }

    private void waiter (){
        try{
            Thread.sleep(3000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
