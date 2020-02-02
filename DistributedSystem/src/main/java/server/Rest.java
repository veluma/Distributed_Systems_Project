package server;

import java.io.*;

public class Rest {
    public static void main(String[] args) throws IOException {
        MultiCastCommunication multiCastServer =  new MultiCastCommunication();
        multiCastServer.establishConnection();
        multiCastServer.startCommunication();
    }
}