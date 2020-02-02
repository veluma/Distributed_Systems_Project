package raft;

import java.util.ArrayList;
import java.util.List;

public class HeartBeatMessage {
    private String address;
    private Integer port;
    List<Integer> listArray;

    public HeartBeatMessage(String address, Integer port, List<Integer> listArray) {
        this.port = port;
        this.address = address;
        this.listArray = listArray;
    }
}
