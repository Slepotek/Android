package com.example.marcel.klientsocketu;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class SocketClient extends Thread {

    private static SocketClient socketClient=null;
    private Socket socket = null;
    private MainActivity parent;
    private BufferedInputStream in;
    private boolean desonnected = false;

    public synchronized void setDesonnected(boolean cr){
        desonnected=cr;
    }

    private SocketClient(MainActivity parent, Socket s){
        super("SocketClient");
        this.parent = parent;
        socket=s;
        setDesonnected(false);
        start();
    }

    public static synchronized SocketClient handle(MainActivity parent, Socket s) {
        if(socketClient==null){
            socketClient=new SocketClient(parent,s);
        }else{
            if(socketClient.socket!=null){
                try{
                    socketClient.socket.close();
                }catch (Exception e){
                    Log.d("Klient Socketu","Wyjątek w funkcji 'handle'");
                }
            }
            socketClient.socket=null;
            socketClient=new SocketClient(parent,s);
        }
        return socketClient;
    }

    public void run(){
        InputStream is = null;
        try{
            is = socket.getInputStream();
            in = new BufferedInputStream(is);
        }catch (IOException e){
            try{
                socket.close();
            }catch (IOException e2) {
                Log.d("Klient socketu","Socket not closed"+e2);
            }
            Log.d("Klient Socketu", "Could not open socket : "+e.getMessage());
            parent.disconnect();
            return;
        }

        while(!desonnected) {
            try{
                String got = readInputStream(in);
                if(got==null){
                    parent.disconnect();
                    break;
                }
                parent.setMessage(got);
            }catch (IOException e) {
                if(!desonnected){
                    Log.d("Klient socketu","Connection lost");
                    parent.disconnect();
                }
                break;
            }
        }//end of while
        try{
            is.close();
            in.close();
        }catch (Exception err){}
        socket=null;
    }//end of run

    private static String readInputStream(BufferedInputStream _in) throws IOException{
        String data = "";
        int s = _in.read();
        if(s==-1){
            return null;
        }
        data += ""+(char)s;
        int len = _in.available();
        Log.d("Klient Socketu", "Len got : "+len);
        if(len > 0){
            byte [] byteData = new byte[len];
            _in.read(byteData);
            data += new String(byteData);
        }
        return data;
    }









}
