package FrontEnd;

import server.Constants;
import java.net.UnknownHostException;
import java.rmi.RemoteException;


public class Server {
    public static void main(String[] args) throws RemoteException, UnknownHostException {
        Constants.MULTI_CAST_IP_ADDRESS = "228.5.6.9";
        Constants.MULTI_CAST_PORT = 5555;
        FrontEnd.startServer();
    }
}
