package FrontEnd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import raft.RaftCommunication;
import request.Request;
import server.Constants;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.reflect.Modifier.TRANSIENT;

public class FrontEndRequest {
    private static GsonBuilder builder = new GsonBuilder();
    private static Gson gson =  new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .excludeFieldsWithModifiers(TRANSIENT) // STATIC|TRANSIENT in the default configuration
            .create();
    /**
     * Handles the fetching of the data from the server
     */
    public static void fetchFromServer(Request request, Map<String, Constants.Roles> replicaAddressRoleMap){
        String replica = new ArrayList<>(replicaAddressRoleMap.keySet()).get(0);
        sendRequestToServer(replica, request);
    }

    /**
     * Handles the case in which new places needs to be added to the server
     */
    public static void addToServer(Request request, Map<String, Constants.Roles> replicaAddressRoleMap){
        String leaderAddress = "";
        for(Map.Entry<String, Constants.Roles> entry : replicaAddressRoleMap.entrySet()){
            if(entry.getValue().equals(Constants.Roles.Leader)){
                leaderAddress = entry.getKey();
                break;
            }
        }
        if(leaderAddress.equals("")){
            System.out.println("Leader Server not found");
        }else{
            sendRequestToServer(leaderAddress, request);
        }
    }

    /**
     * handles deletion of the place information from the server
     * @param request
     * @param replicaAddressRoleMap
     */
    public static void deleteFromServer(Request request, Map<String, Constants.Roles> replicaAddressRoleMap){
        String leaderAddress = "";
        for(Map.Entry<String, Constants.Roles> entry : replicaAddressRoleMap.entrySet()){
            if(entry.getValue().equals(Constants.Roles.Leader)){
                System.out.println(entry.getValue());
                leaderAddress = entry.getKey();
                break;
            }
        }
        if(leaderAddress.equals("")){
            System.out.println("Leader Server not found");
        }else{
            sendRequestToServer(leaderAddress, request);
        }
    }

    /**
     * Sends the request received to the desired server
     * @param replica desired server address
     * @param request clients request
     */
    public static void sendRequestToServer(String replica, Request request){
        try{
            String[] address = replica.split(":");
            String jsonRequest = gson.toJson(request);
            Socket s=new Socket(address[0], Integer.parseInt(address[1])+10);
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            dout.writeUTF(jsonRequest);
            dout.flush();
            dout.close();
            s.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }

    /**
     * send the response back to the client
     * @param response
     */
    public static void sendClientResponse(String response){
        try{
            Socket s=new Socket("localhost",6969);
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            dout.writeUTF(response);
            dout.flush();
            dout.close();
            s.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
