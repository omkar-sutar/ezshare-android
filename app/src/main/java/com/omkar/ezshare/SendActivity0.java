package com.omkar.ezshare;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SendActivity0 extends AppCompatActivity {
    private static Uri fileUri;
    private static String filename;
    private static long fileSize;
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

        TextView textViewIP=findViewById(R.id.editTextIP);
        TextView textViewPort=findViewById(R.id.editTextPort);
        Button buttonNext=findViewById(R.id.buttonNext0);
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
    public void onButtonNextClick(View view){

        TextView textViewIP=findViewById(R.id.editTextIP);
        TextView textViewPort=findViewById(R.id.editTextPort);

        String hostIP=textViewIP.getText().toString();
        //Make sure port is integer

        int port;
        try {
            port=Integer.parseInt(textViewPort.getText().toString());
        }
        catch (NumberFormatException e){
            Toast.makeText(this,"Port should be a number!",Toast.LENGTH_SHORT).show();
            return;
        }

        //Start SendActivity1
        Intent intent=new Intent(this,SendActivity1.class);
        intent.setData(fileUri);
        intent.putExtra("hostIP",hostIP);
        intent.putExtra("port",port);
        intent.putExtra("filename",filename);
        intent.putExtra("fileSize",fileSize);
        startActivity(intent);
    }
}