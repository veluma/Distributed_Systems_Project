package raft;

import server.PlaceServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Leader {
    public void coach(PlaceServer replica) {
        try {
            ConcurrentHashMap<String, Date> replicas = replica.getAllReplicas();

            for (Map.Entry<String, Date> pair : replicas.entrySet()) {
                if (pair.getKey().equals(replica.getLocalAddress() + ":" + replica.getLocalPort())) {
                    continue;
                }

                ConsensusInterface cu =
                        (ConsensusInterface) Naming.lookup("rmi://" + pair.getKey() + "/PlaceServer");

                cu.addRequest(new ConsensusAdd(replica.getCurrentTerm(),
                        replica.getLocalAddress(), replica.getLocalPort()));
            }
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
