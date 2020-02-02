package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import raft.RaftCommunication;

import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import static java.lang.reflect.Modifier.TRANSIENT;

public class MultiCastCommunication {
    Registry registry = null;
    PlaceServer server = null;
    public MulticastSocket socket = null;
    public byte[] buffer = null;
    public DatagramSocket incomingPacket = null;
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson =  new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .excludeFieldsWithModifiers(TRANSIENT) // STATIC|TRANSIENT in the default configuration
            .create();
    public void establishConnection() throws RemoteException, UnknownHostException{
        Constants.LOCAL_PORT = getFreePort();
        if(Constants.LOCAL_PORT == 9876){
            Log.printLog("All available Ports already in use cannot establish connection ");
        }else{
            try {
                Log.printLog("Registry on Port : " + Constants.LOCAL_PORT);
                registry = LocateRegistry.createRegistry(Constants.LOCAL_PORT);
            } catch (RemoteException a) {
                Log.printLog(Constants.LOCAL_PORT + " is already in use.");
            }
            server = new PlaceServer(Constants.LOCAL_PORT);
            registry.rebind("PlaceServer", server);
            Log.printLog("Connection Established -> Using Port " + Constants.LOCAL_PORT + " for Place Manager");
        }
    }

    public void startCommunication(){
        // @todo
        new Thread() {
            public void run() {
                while (true) {
                    // Send message
                    try {
                        System.out.println( gson.toJson(server));
                        RaftCommunication.sendMultiCastMessage(Constants.MULTI_CAST_IP_ADDRESS, Constants.MULTI_CAST_PORT,
                                gson.toJson(server));

                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }.start();

        new Thread() {
            public void run() {
                // Listens message
                while (true) {
                    try {
                        String listenMultiCast = RaftCommunication.listenMultiCast(Constants.MULTI_CAST_IP_ADDRESS, Constants.MULTI_CAST_PORT);
                        // change the current replica logs as per the response received
                        server.processReplicaResponse(listenMultiCast);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }.start();

        new Thread() {
            public void run() {
                // Listens uni cast message from FrontEnd
                while (true) {
                    try {
                        String clientRequest = RaftCommunication.ListenFrontEnd();
                        System.out.println("received from frontend ->"+ clientRequest);
                        // change the current replica logs as per the response received
                        String response = server.processClientRequest(clientRequest);
                        RaftCommunication.sendResponse(response);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }.start();

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        server.emptyReplicas(2 * 3000);
                    } catch (RemoteException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }.start();

        // Handles consensus
        new Thread() {
            public void run() {
                while (true) {
                    server.coachCurrentRole();
                }
            }
        }.start();

        new Thread() {
            public void run() {
                while (true) {
                    Log.printLog(server.toString());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }.start();
    }

    public int getFreePort(){
        for(int port : Constants.LOCAL_PORT_LIST) {
            try {
                ServerSocket s = new ServerSocket(port);
                s.close();
                return port;
            } catch (IOException ex) {
                continue; // try next port
            }
        }
        return 9876;
    }
}
