package raft;

import server.Constants;
import server.PlaceServer;

public class Follower implements ConsensusCoach{
    public void coach(PlaceServer server) {
        while (System.nanoTime() - server.getLastTime() < server.getCurrentTimeout()) {

        }
        server.setLeaderAddress(null);
        server.setLeaderPort(null);
        server.setCandidateAddress(null);
        server.setCandidatePort(null);
        server.setCurrentRole(Constants.Roles.Candidate);
        System.out.println(server);
    }
}
