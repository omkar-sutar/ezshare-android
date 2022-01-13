package com.omkar.ezshare;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

public class SendActivity0 extends AppCompatActivity {
    private static Uri fileUri;
    private static String hostIP;
    private static int port;
    private SocketSend sks;
    private SendDataThread sendDataThread;
    private String filename;
    private long fileSize;
    private static String sendingStatus;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send0);

        fileUri=getIntent().getData();
        Cursor cursor=getContentResolver().query(fileUri,null,null,null,null);
        int colIndexName=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int colIndexSize=cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        filename=cursor.getString(colIndexName);
        fileSize=cursor.getLong(colIndexSize);
        cursor.close();

        //Initialize variables
        sendingStatus="notStarted";

        //Put instructions on textViewSendStatus
        TextView textViewSendStatus=findViewById(R.id.textViewSendInstructions);
        textViewSendStatus.setText("Enter the IP address of the receiving device and click 'Next'.");

        TextView textViewIP=findViewById(R.id.editTextIP);
        TextView textViewPort=findViewById(R.id.editTextPort);
        Button buttonNext=findViewById(R.id.buttonNextSendActivity0);
        buttonNext.setEnabled(false);
        textViewIP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(textViewIP.getText().length()==0){
                    buttonNext.setEnabled(false);
                }
                else{
                    if(textViewPort.getText().length()!=0){
                        buttonNext.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
        textViewPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(textViewPort.getText().length()==0){
                    buttonNext.setEnabled(false);
                }
                else{
                    if(textViewIP.getText().length()!=0){
                        buttonNext.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

    }
    @SuppressLint("SetTextI18n")
    public void onButtonNextClick(View view) {

        //If sending is yet to start, clicking next will initialize socket
        if (sendingStatus.equals("notStarted")) {
            TextView textViewIP = findViewById(R.id.editTextIP);
            TextView textViewPort = findViewById(R.id.editTextPort);
            hostIP = textViewIP.getText().toString();
            //Make sure port is integer
            try {
                port = Integer.parseInt(textViewPort.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Port should be a number!", Toast.LENGTH_SHORT).show();
                return;
            }
            TextView textViewSendStatus = findViewById(R.id.textViewSendStatus);
            textViewSendStatus.setText("Connecting to Receiver..");
            textViewSendStatus.setVisibility(View.VISIBLE);
            ProgressBar progressBarCircle=findViewById(R.id.progressBarSendCircle);
            progressBarCircle.setVisibility(View.VISIBLE);

            sendDataThread=new SendDataThread();
            sendDataThread.start();
        }
        else if(sendingStatus.equals("started")){
            android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(SendActivity0.this);
            builder.setTitle("Cancel Sending");
            builder.setMessage("Do you want to cancel Sending?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try{
                        sks.close();
                    }
                    catch (Exception e){
                        Log.i("mymsg","Socket Close Exception");
                    }
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Continue reception/Do nothing
                    return;
                }
            });
            builder.show();
        }
        else if(sendingStatus.equals("finished")){
            //Socket has been closed already, just finish the activity
            finish();
        }
    }
    private void toast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
    private class SendDataThread extends Thread{
        @SuppressLint("SetTextI18n")
        @Override
        public void run(){
            try{
                sendingStatus="connecting";
                sks=new SocketSend(hostIP,port);
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity0.this);
                        builder.setTitle("Connection failed");
                        builder.setMessage("Failed to connect to receiver. Check if the IP Address is correct and Receiver is listening.");
                        builder.setPositiveButton("Ok", null);
                        builder.show();
                    }
                });
                return;
            }
            float fileSizeMB=(float)fileSize/1048576;   //filesize in MB, float

            //File transfer starting
            sendingStatus="started";
            runOnUiThread(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    EditText editText=findViewById(R.id.editTextIP);
                    editText.setEnabled(false);
                    editText=findViewById(R.id.editTextPort);
                    editText.setEnabled(false);
                    TextView textViewSendStatus=findViewById(R.id.textViewSendStatus);
                    textViewSendStatus.setText("Sending file: "+filename);
                    ProgressBar progressBarCircle=findViewById(R.id.progressBarSendCircle);
                    progressBarCircle.setVisibility(View.INVISIBLE);
                    updateProgress(0,fileSizeMB);
                    ProgressBar progressBarHorizontal=findViewById(R.id.progressBarSendHorizontal);
                    progressBarHorizontal.setVisibility(View.VISIBLE);
                    TextView textViewProgressBarPercentage = findViewById(R.id.textViewProgressBarSendPercentage);
                    textViewProgressBarPercentage.setVisibility(View.VISIBLE);
                    Button buttonNext=findViewById(R.id.buttonNextSendActivity0);
                    buttonNext.setText("Cancel");
                }
            });

            try{
                InputStream inputStream=getContentResolver().openInputStream(fileUri);
                DataOutputStream dataOutputStream=sks.getDataOutputStream();
                dataOutputStream.writeUTF(filename);
                dataOutputStream.writeUTF(String.valueOf(fileSizeMB));    //send file size in MB

                byte[] bytes=new byte[1000000];
                int bytesRead;
                float megaBytesSent=0;
                while(true){
                    bytesRead=inputStream.read(bytes);
                    if(bytesRead==-1)break;
                    dataOutputStream.write(bytes,0,bytesRead);
                    dataOutputStream.flush();
                    megaBytesSent+=(float)bytesRead/1048576;
                    updateProgress(megaBytesSent, fileSizeMB);
                }
                //File transfer finished
                sendingStatus="finished";
                inputStream.close();
                dataOutputStream.close();
                sks.close();
                updateProgress(fileSizeMB, fileSizeMB);
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        TextView textViewSendStatus=findViewById(R.id.textViewSendStatus);
                        textViewSendStatus.setText("File sent: "+filename);
                        Button buttonNext=findViewById(R.id.buttonNextSendActivity0);
                        buttonNext.setText("Back");
                    }
                });

            } catch (SocketException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        TextView textViewSendStatus=findViewById(R.id.textViewSendStatus);
                        textViewSendStatus.setText("Transfer Failed");
                        Button buttonCancel=findViewById(R.id.buttonNextSendActivity0);
                        buttonCancel.setText("Back");
                        sendingStatus="finished";
                    }
                });
            } catch (IOException e) {
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
                    ProgressBar progressBarHorizontal=findViewById(R.id.progressBarSendHorizontal);
                    progressBarHorizontal.setProgress((percentageReceived));
                    TextView textViewProgressBarPercentage=findViewById(R.id.textViewProgressBarSendPercentage);
                    textViewProgressBarPercentage.setText(String.valueOf(percentageReceived)+"% "+"(z MB)".replace("z",megabytesCount));
                }
            });
        }
    }
}