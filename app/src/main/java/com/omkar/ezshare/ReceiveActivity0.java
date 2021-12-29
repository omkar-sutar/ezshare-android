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
    private String fileReceptionState="NotStarted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive0);
        SocketCreationThread socketCreationThread=new SocketCreationThread();
        socketCreationThread.start();

        //Create socket skr
//        socketCreationTask=new SocketCreationTask();
//        fileReceiveTask=new FileReceiveTask();
//        socketCreationTask.execute();
//        fileReceiveTask.execute();

    }

    public void onButtonCancelClick(View view){
        Log.i("mymsg","Reception cancelled");
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
                    textViewPort.setText(String.valueOf(port));
                    FileReceptionThread fileReceptionThread=new FileReceptionThread();
                    fileReceptionThread.start();
                }
            });

            //Now update the ui using UiHandler
            //Message msg=new Message();
            //msg.what=RECEIVE_SOCKET_CREATED_UPDATE_UI;
            //UiHandler.sendMessage(msg);
        }
    }

    private class FileReceptionThread extends Thread{
      @Override
      public void run(){
          try {
              skr.accept();
          } catch (Exception e) {
              e.printStackTrace();
              return;
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
                  boolean res=file.createNewFile();
                  if(!res){

                      return;
                  }
              } catch (Exception e){
                  e.printStackTrace();
                  return;
              }
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
              runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      toast(getApplicationContext(),"File received");
                  }
              });
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
    }
}
