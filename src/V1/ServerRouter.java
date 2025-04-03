package V1;

import java.io.*;
import java.net.*;

public class ServerRouter {
    ServerSocket ss;
    Socket s;
    DataInputStream dis;
    DataOutputStream dos;
    static String[][] routingTable = new String[3][2];
    boolean running = true; 
    public ServerRouter(){
        try{
            routingTable[0][1] = "192.168.0.27,50";
            System.out.println("SERVER ROUTER STARTED");
            ss = new ServerSocket(10);
            do{
                System.out.println("here");
                s = ss.accept();
                System.out.println(s);
                System.out.println("CLIENT RECEIVED");
                dis = new DataInputStream(s.getInputStream());
                dos = new DataOutputStream(s.getOutputStream());
                routeClientToServer();    
            }while(running);
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void routeClientToServer(){
        try{
            Boolean openSlot = false;
            String addr = dis.readUTF();
            for(int i = 0; i < routingTable.length; i++){
                if(routingTable[i][0] == null){
                    routingTable[i][0] = addr;
                    dos.writeUTF(routingTable[i][1]);
                    dos.flush();
                    openSlot = true;
                    break;
                }else if(routingTable[i][0].equals(addr)){
                    routingTable[i][0] = null;
                    running = false;
                }
            }
            if(openSlot){
                System.out.println("CLIENT ROUTED TO SERVER");
            }else{
                System.out.println("ALL SERVERS BUSY");
            }    
        }catch(Exception e){
            System.out.println(e);
        }
        
    }
    public static void main(String[] args) {
        new ServerRouter();
    }
}