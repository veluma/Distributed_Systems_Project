package FrontEnd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import raft.RaftCommunication;
import request.Request;
import server.Constants;
import server.Log;
import server.PlaceServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static java.lang.reflect.Modifier.TRANSIENT;

/**
 * This class acts as a middle ware between the client and the servers. It listens to the clients request and forwards it to the server
 * the server responds to the request and the response is sent back to the client by Frontend
 */
public class FrontEnd {
    private static List<PlaceServer> replicaList = new ArrayList<>();
    private static Map<String, Constants.Roles> replicaAddressRoleMap = new HashMap<>();
    private static GsonBuilder builder = new GsonBuilder();
    private static Gson gson =  new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .excludeFieldsWithModifiers(TRANSIENT) // STATIC|TRANSIENT in the default configuration
            .create();
        public FrontEnd(){

    }
    public static void startServer() {
        Log.printLog("FrontEnd Started");
        new Thread() {
            public void run() {
                // Listens multi cast message from servers
                while (true) {
                    try {
                        String response = RaftCommunication.listenMultiCast(Constants.MULTI_CAST_IP_ADDRESS, Constants.MULTI_CAST_PORT);
                        PlaceServer responsePlaceServer = gson.fromJson(response, PlaceServer.class);
                        //mapping all the servers in the cluster with their Roles
                        if (replicaAddressRoleMap.keySet().contains(responsePlaceServer.getLocalAddress() + ":" + responsePlaceServer.getLocalPort())) {
                            if (responsePlaceServer.getCurrentRole() != replicaAddressRoleMap.get(responsePlaceServer.getLocalAddress() + ":" + responsePlaceServer.getLocalPort())) {
                                replicaAddressRoleMap.replace(responsePlaceServer.getLocalAddress() + ":" + responsePlaceServer.getLocalPort(), responsePlaceServer.getCurrentRole());
                            }
                        } else {
                            replicaAddressRoleMap.put(responsePlaceServer.getLocalAddress() + ":" + responsePlaceServer.getLocalPort(), responsePlaceServer.getCurrentRole());
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }.start();

        new Thread() {
            public void run() {
                // Listens message from the client
                while (true) {
                    try {
                        ServerSocket serverSocket = new ServerSocket(4554);
                        Socket clientSocket = serverSocket.accept();
                        DataInputStream dataInputStream=new DataInputStream(clientSocket.getInputStream());
                        String  request=(String)dataInputStream.readUTF();
                        System.out.println("Request received from Client: "+request);

                        sendServerResponse(request);
                        serverSocket.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }.start();

        new Thread() {
            public void run() {
                // Listens responses from the server
                while (true) {
                    try {
                        ServerSocket serverSocket = new ServerSocket(Constants.FRONT_END_PORT);
                        Socket clientSocket = serverSocket.accept();
                        DataInputStream dataInputStream=new DataInputStream(clientSocket.getInputStream());
                        String  response=(String)dataInputStream.readUTF();
                        System.out.println("Response received from Server: "+response);
                        FrontEndRequest.sendClientResponse(response);
                        serverSocket.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }.start();
    }



    public static void sendServerResponse(String request){
        String response;
        Request requestReceived = gson.fromJson(request, Request.class);
           switch (requestReceived.getReqType()){
               case 1:  FrontEndRequest.fetchFromServer(requestReceived, replicaAddressRoleMap);
                   break;
               case 2:  FrontEndRequest.addToServer(requestReceived, replicaAddressRoleMap);
                   break;
               case 3:  FrontEndRequest.deleteFromServer(requestReceived, replicaAddressRoleMap);
                   break;
           }
    }



}
