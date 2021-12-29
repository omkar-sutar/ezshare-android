package com.omkar.ezshare;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SocketRecv {
    public ServerSocket ssk=null;
    private Socket sk;
    public SocketRecv() throws Exception {
        for(int i=0;i<100;i++){
            try{
                ssk=new ServerSocket(9999+i);
                ssk.setReuseAddress(true);
                break;
            }
            catch (Exception e){
                Log.i("mymsg",e.getMessage());
            }
            if(i==99){
                throw new BindException();
            }
        }

    }
    public int getHostPort(){
        return ssk.getLocalPort();
    }
    public String getHostIP() throws UnknownHostException {
        return Inet4Address.getLocalHost().toString();
    }
    public void accept() throws IOException {
        sk=ssk.accept();
    }
    public int recvByte() throws IOException {
        return sk.getInputStream().read();
    }
    //Use stream instead of direct recv() func for large data
    public DataInputStream getDataInputStream() throws IOException {
        return new DataInputStream(sk.getInputStream());
    }
    public void close() throws IOException {
        sk.close();
    }
}
