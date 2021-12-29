package com.omkar.ezshare;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CHOOSE_FILE=1;
    public static final int REQUEST_ASK_PERMISSIONS=2;
    public static final int REQUEST_DOCUMENT_TREE=3;
    private static final String[] permissionsNeeded={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE};
    private static Uri destinationFolderUri=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonSendClick(View view){
        Toast.makeText(view.getContext(),"Send",Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent,REQUEST_CHOOSE_FILE);

    }

    public void onButtonReceiveClick(View view){
        boolean hasAccess=verifyPermissions();
        if(!hasAccess){
            toast(this,"Not enough permissions");
            return;
        }

        Intent intent=new Intent(this,ReceiveActivity0.class);
        try {
            startActivity(intent);
        }
        catch (Exception e){
            toast(this,e.getMessage());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CHOOSE_FILE && resultCode==RESULT_OK){
            //Start SendActivity0
            Intent intent=new Intent(this,SendActivity0.class);
            intent.setData(data.getData());
            startActivity(intent);
        }
        if(requestCode==REQUEST_DOCUMENT_TREE && resultCode==RESULT_OK){
            destinationFolderUri=data.getData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Permissions required")
                    .setMessage("Storage permission is needed to save received files. Please allow storage permission.")
                    .setPositiveButton("Ok", (dialogInterface, i) -> {
                        //Do nothing
                    });
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        }
        if(checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Permissions required")
                    .setMessage("Storage permission is needed to save received files. Please allow following permission(s): \n\n" +
                            "1)Allow management of all files.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(Intent.ACTION_SHOW_APP_INFO));
                                }
                            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    public static void toast(Context context, String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }
    private boolean verifyPermissions(){
        if(Build.VERSION.SDK_INT>=30){
            if(!Environment.isExternalStorageManager()){
                //If manage all files permission is unavailable
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Permissions required")
                        .setMessage("Storage permission is needed to save received files. Please allow following permission(s): \n\n" +
                                "1)Allow management of all files.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"));
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return false;
            }
            else{
                return true;    //Access is present
            }
        }

        //TODO
        //For devices running api<30
        //TODO end
        return false;
    }

}