package raft;

import server.Constants;
import server.PlaceServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Candidate implements ConsensusCoach{
    public void coach(PlaceServer replica) {
        Integer Votes = 0;
        replica.newTimeout();

        Thread t = new Thread() {
            public void run() {
                while (System.nanoTime() - replica.getLastTime() < replica.getCurrentTimeout()) {
                }
            }
        };
        t.start();

        try {
            ConcurrentHashMap<String, Date> replicas = replica.getAllReplicas();
            Iterator<Map.Entry<String, Date>> it = replicas.entrySet().iterator();
            while (it.hasNext() && t.isAlive()) {
                Map.Entry<String, Date> pair = it.next();

                ConsensusInterface cu =
                        (ConsensusInterface) Naming.lookup("rmi://" + pair.getKey() + "/PlaceServer");

                if (cu.voteRequest(new ConsensusVote(replica.getCurrentTerm() + 1,
                        replica.getLocalAddress(), replica.getLocalPort())) == true) {
                    Votes++;
                }
            }
            if (Votes > replicas.size() / 2) {
                replica.setCandidateAddress(null);
                replica.setCandidatePort(null);
                replica.setLeaderAddress(replica.getLocalAddress());
                replica.setLeaderPort(replica.getLocalPort());
                replica.setCurrentTerm(replica.getCurrentTerm() + 1);
                replica.setCurrentRole(Constants.Roles.Leader);
            } else {
                // Volta a ser follower
                replica.newTimeout();
                replica.setCandidateAddress(null);
                replica.setCandidatePort(null);
                replica.setCurrentRole(Constants.Roles.Follower);
            }
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
