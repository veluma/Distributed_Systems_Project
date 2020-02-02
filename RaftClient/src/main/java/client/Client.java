package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.reflect.Modifier.TRANSIENT;

public class Client {
    private static GsonBuilder builder = new GsonBuilder();
    private static Gson gson =  new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .excludeFieldsWithModifiers(TRANSIENT) // STATIC|TRANSIENT in the default configuration
            .create();
    public static void main(String[] args) throws IOException {
        new Thread() {
            public void run() {
                while (true) {
                    Request request ;
                    System.out.println("Send Request to Server ? Y/N ");
                    String str = new Scanner(System.in).nextLine();
                    if (str.toLowerCase().equals("y")) {
                        request = new Request();
                        System.out.println("1. Fetch Request \n2. Add Request \n3. Delete Request");
                        int reqType = Integer.parseInt(new Scanner(System.in).nextLine());
                        if (reqType == 1) {
                            System.out.println("Input the postal code of the area to be fetched");
                            request.setPostalCode(new Scanner(System.in).nextLine());
                            request.setReqType(1);
                        }
                        if (reqType == 2) {
                            System.out.println("Input the postal code to be added to server");
                            request.setPostalCode(new Scanner(System.in).nextLine());
                            System.out.println("Input the Location name of the postal code");
                            request.setLocationName(new Scanner(System.in).nextLine());
                            request.setReqType(2);
                        }
                        if (reqType == 3) {
                            System.out.println("Input the postal code to be deleted from server");
                            request.setPostalCode(new Scanner(System.in).nextLine());
                            request.setReqType(3);
                        }
                        try {
                            String jsonRequest = gson.toJson(request);
                            Socket s = new Socket("localhost", 4554);
                            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
                            dout.writeUTF(jsonRequest);
                            dout.flush();
                            dout.close();
                            s.close();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }.start();

        new Thread(){
            public void run() {
                while (true) {
                    try{
                        ServerSocket serverSocket = new ServerSocket(6969);
                        Socket clientSocket = serverSocket.accept();
                        DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                        String response = (String) dataInputStream.readUTF();
                        System.out.println("Response received from FrontEnd: " + response);
                        serverSocket.close();
                    } catch (IOException e){
                        System.out.println(e);
                    }
                }
            }
        }.start();
    }
}
