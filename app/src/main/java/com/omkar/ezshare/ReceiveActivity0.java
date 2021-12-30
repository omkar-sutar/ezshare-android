package com.omkar.ezshare;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ReceiveActivity0 extends AppCompatActivity {

    private final static int RECEIVE_SOCKET_CREATED_UPDATE_UI=1;

    private SocketRecv skr;
    private String hostIP;
    private int port;
    private boolean socketCreatedFlag=false;
    private boolean cancel=false;
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
                    //If user is not connected to wifi, ask to connect
                    if(hostIP.equals("0.0.0.0")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveActivity0.this);
                        builder.setTitle("Wi-Fi unavailable")
                                .setMessage("Please connect the device to a Wi-Fi network")
                                .setPositiveButton("Ok", (dialogInterface, i) -> {
                                    //Return from receive activity
                                    finish();
                                });
                        AlertDialog alertDialog=builder.create();
                        alertDialog.show();
                        return;
                    }
                    TextView textViewIP=findViewById(R.id.textViewIP);
                    textViewIP.setText(hostIP);
                    TextView textViewPort=findViewById(R.id.textViewPort);
                    textViewPort.setText(String.valueOf(port));

                    TextView textViewReceptionInfo=findViewById(R.id.textViewReceptionStatus);
                    FileReceptionThread fileReceptionThread=new FileReceptionThread();
                    fileReceptionThread.start();
                }
            });
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
          String filename,fileSize;
          byte[] buffer = new byte[1000000];
          int bytesReceived=0;
          File path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
          try {
              DataInputStream dataInputStream=skr.getDataInputStream();
              filename=dataInputStream.readUTF();
              fileSize=dataInputStream.readUTF();   //in MB
              File file=new File(path,filename);
              try {
                  boolean res=file.createNewFile();
                  if(!res){
                      //File already exists
                      res=file.delete();
                      if(!res){
                          runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  toast(getApplicationContext(),"The file already exists in the directory and couldn't be overwritten");
                              }
                          });
                          skr.close();
                          finish();
                      }
                  }
              } catch (Exception e){
                  e.printStackTrace();
                  return;
              }
              FileOutputStream fileOutputStream=new FileOutputStream(file,false);
              //update ui
              runOnUiThread(new Runnable() {
                  @SuppressLint("SetTextI18n")
                  @Override
                  public void run() {
                      TextView textViewReceptionStatus=findViewById(R.id.textViewReceptionStatus);
                      textViewReceptionStatus.setText("Receiving file: "+filename);
                      ProgressBar progressBarCircle=findViewById(R.id.progressBarCircle);
                      progressBarCircle.setVisibility(View.INVISIBLE);
                      ProgressBar progressBarHorizontal=findViewById(R.id.progressBarHorizontal);
                      progressBarHorizontal.setVisibility(View.VISIBLE);
                      TextView textViewProgressBarPercentage = findViewById(R.id.textViewProgressBarPercentage);
                      textViewProgressBarPercentage.setVisibility(View.VISIBLE);
                      updateProgress(0,Float.parseFloat(fileSize));
                  }
              });
              float megaBytesReceived=0;
              float totalMegaBytes=Float.parseFloat(fileSize);
              while(true){
                  try{
                      bytesReceived=dataInputStream.read(buffer);
                  } catch (IOException e){
                      runOnUiThread(new Runnable() {
                          @SuppressLint("SetTextI18n")
                          @Override
                          public void run() {
                              TextView textViewReceptionStatus=findViewById(R.id.textViewReceptionStatus);
                              textViewReceptionStatus.setText("Reception Failed");
                              Button buttonCancel=findViewById(R.id.buttonCancelReceiveActivity0);
                              buttonCancel.setText("Back");
                          }
                      });
                      return;
                  }
                  megaBytesReceived+=((float)bytesReceived/1048576);
                  updateProgress(megaBytesReceived,totalMegaBytes);
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
              updateProgress(totalMegaBytes,totalMegaBytes);
              runOnUiThread(new Runnable() {
                  @SuppressLint("SetTextI18n")
                  @Override
                  public void run() {
                      TextView textViewReceptionStatus=findViewById(R.id.textViewReceptionStatus);
                      textViewReceptionStatus.setText("File Received: "+filename);
                      TextView textViewReceivedFilePath=findViewById(R.id.textViewReceivedFilePath);
                      textViewReceivedFilePath.setText("File saved at: "+path+"/"+filename);
                      textViewReceivedFilePath.setVisibility(View.VISIBLE);
                      Button buttonCancel=findViewById(R.id.buttonCancelReceiveActivity0);
                      buttonCancel.setText("Back");
                  }
              });
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
      private void updateProgress(float megaBytesReceived,float totalMegaBytes){
          runOnUiThread(new Runnable() {
              @SuppressLint("SetTextI18n")
              @Override
              public void run() {
                  int percentageReceived=(int)(megaBytesReceived*100/totalMegaBytes);
                  String megabytesCount=String.valueOf((int)megaBytesReceived)+"/"+String.valueOf((int)totalMegaBytes);
                  ProgressBar progressBarHorizontal=findViewById(R.id.progressBarHorizontal);
                  progressBarHorizontal.setProgress((percentageReceived));
                  TextView textViewProgressBarPercentage=findViewById(R.id.textViewProgressBarPercentage);
                  textViewProgressBarPercentage.setText(String.valueOf(percentageReceived)+"% "+"(z MB)".replace("z",megabytesCount));
              }
          });
      }
    }
}
