package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Constants {
    public static String MULTI_CAST_IP_ADDRESS = "228.5.6.9";
    public static int MULTI_CAST_PORT = 5555;
    public static int LOCAL_PORT;
    public static int[] LOCAL_PORT_LIST = new int[]{4000,4001,4002,4003,4004,4005,4006,4007,4008,4009};
    public static int FRONT_END_PORT = 9999;

    public static Thread thread;
    public static Socket socket;
    public static DataOutputStream dataOutputStream;
    public static DataInputStream dataInputStream;
    public static ServerSocket serverSocket;
    public static enum Roles {Candidate, Follower, Leader};
}
