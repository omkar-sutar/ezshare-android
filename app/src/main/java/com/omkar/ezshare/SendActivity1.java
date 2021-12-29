package com.omkar.ezshare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class SendActivity1 extends AppCompatActivity {
    private static Uri fileUri;
    private static String filename;
    private static long fileSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send1);
        Intent intent=getIntent();
        fileUri=intent.getData();
        filename=intent.getStringExtra("filename");
        fileSize=intent.getLongExtra("fileSize",0);
        Toast.makeText(this,filename,Toast.LENGTH_SHORT).show();
        return;
    }
}