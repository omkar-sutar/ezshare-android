package com.omkar.ezshare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ReceiveActivity0 extends AppCompatActivity {

    private final static int RECEIVE_SOCKET_CREATED_UPDATE_UI=1;

    private SocketRecv skr;
    private String hostIP;
    private int port;
    private boolean socketCreatedFlag=false;
    private boolean cancel=false;
    private SocketCreationTask socketCreationTask;
    private FileReceiveTask fileReceiveTask;
    private Handler UiHandler=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive0);
        createUiHandler();
        SocketCreationThread t=new SocketCreationThread();
        t.start();
        //Create socket skr
//        socketCreationTask=new SocketCreationTask();
//        fileReceiveTask=new FileReceiveTask();
//        socketCreationTask.execute();
//        fileReceiveTask.execute();

    }

    private void createUiHandler() {
        UiHandler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                Log.i("mymsg","here");
            }
        };
    }

    public void onButtonCancelClick(View view){
        toast(this,"Reception cancelled");
        Log.i("mymsg","Reception cancelled");
        socketCreationTask.cancel(true);
        fileReceiveTask.cancel(true);
        socketCreationTask=null;
        fileReceiveTask=null;
        try{
            skr.close();
        }
        catch (Exception e){
            Log.i("mymsg","Socket Close Exception");
        }
        finish();
    }
    public static void toast(Context context, String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }

    private class SocketCreationThread extends Thread{
        @Override
        public void run(){
            try {
                skr=new SocketRecv();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("mymsg",e.getMessage());
                return;
            }
            Log.i("mymsg","socketCreated");
            try {
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                hostIP= Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                port=skr.getHostPort();
                Log.i("mymsg",hostIP);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            socketCreatedFlag=true;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView textViewIP=findViewById(R.id.textViewIP);
                    textViewIP.setText(hostIP);
                    TextView textViewPort=findViewById(R.id.textViewPort);
                    textViewPort.setText(port);
                }
            });

            //Now update the ui using UiHandler
            //Message msg=new Message();
            //msg.what=RECEIVE_SOCKET_CREATED_UPDATE_UI;
            //UiHandler.sendMessage(msg);
        }
    };

    private class SocketCreationTask extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.i("mymsg","Socket Creation started");
            try {
                skr=new SocketRecv();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("mymsg",e.getMessage());
                return false;
            }
            Log.i("mymsg","socketCreated");
            try {
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                hostIP= Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                port=skr.getHostPort();
                Log.i("mymsg",hostIP);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            socketCreatedFlag=true;
            return true;
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            Log.i("mymsg","SocketCreationTaskCancelled");
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Boolean result){
            if(!result) return;
            TextView textViewIP=findViewById(R.id.textViewIP);
            TextView textViewPort=findViewById(R.id.textViewPort);
            textViewIP.setText("Ipv4: " + hostIP);
            textViewPort.setText("Port: " + port);
        }
    }



    private class FileReceiveTask extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                skr.accept();
            } catch (Exception e) {
                e.printStackTrace();
                return false;   //Task failed
            }
            String filename;
            byte[] buffer = new byte[1000000];
            int bytesReceived=0;
            File path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            try {
                DataInputStream dataInputStream=skr.getDataInputStream();
                //get filename
                filename=dataInputStream.readUTF();
                File file=new File(path,filename);
                try {
                    file.createNewFile();
                } catch (Exception e){};
                FileOutputStream fileOutputStream=new FileOutputStream(file,false);
                while(true){
                    bytesReceived=dataInputStream.read(buffer);
                    if(bytesReceived==-1) break;
                    else if(bytesReceived!=1000000){
                        fileOutputStream.write(Arrays.copyOf(buffer,bytesReceived));
                        fileOutputStream.flush();
                    }
                    else{
                        fileOutputStream.write(buffer);
                    }
                }
                fileOutputStream.close();
                Log.i("mymsg",path.getPath());
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("mymsg",e.getMessage());
            }
            return true;
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            Log.i("mymsg","FileReceieveTaskCancelled");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            boolean flag=aBoolean;
            if(!flag) return;

        }
    }
}
