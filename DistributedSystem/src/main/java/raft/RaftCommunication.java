package raft;

import server.Constants;
import server.Log;

import java.io.*;
import java.net.*;

public class RaftCommunication {
    public static void sendMultiCastMessage(String address, Integer port, String message)
            throws IOException {
        byte m[] = message.getBytes();

        DatagramSocket socket = new DatagramSocket();
        InetAddress host = InetAddress.getByName(address);
        DatagramPacket datagram = new DatagramPacket(m, m.length, host, port);
        socket.send(datagram);
        socket.close();

        Log.printLog("Sending MultiCast message" + new String(datagram.getData(), 0, datagram.getLength()));
        // Sends datagram
        socket.send(datagram);
        socket.close();
    }

    public static String listenMultiCast(String address, Integer port) throws IOException {
        byte[] buffer=new byte[1024];
        MulticastSocket socket=new MulticastSocket(port);
        InetAddress group=InetAddress.getByName(address);
        socket.joinGroup(group);
            DatagramPacket packet=new DatagramPacket(buffer,
                    buffer.length);
            socket.receive(packet);
            String msg =new String(packet.getData(), 0, packet.getLength());
            socket.leaveGroup(group);
        socket.close();
        return msg;
    }

    public static void sendResponse(String response) throws IOException {
        Socket s=new Socket("localhost",Constants.FRONT_END_PORT);
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());
        dout.writeUTF(response);
        System.out.println("Response sent to frontend : "+ response);
        dout.flush();
        dout.close();
        s.close();
    }

    public static String ListenFrontEnd() throws IOException {
        ServerSocket serverSocket = new ServerSocket(Constants.LOCAL_PORT + 10);
        Socket clientSocket = serverSocket.accept();
        DataInputStream dataInputStream=new DataInputStream(clientSocket.getInputStream());
        String request=(String)dataInputStream.readUTF();
        System.out.println("Request received from FrontEnd: "+request);
        serverSocket.close();
        return request;
    }
}
