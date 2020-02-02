package raft;

import java.io.Serializable;

public class ConsensusAdd implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer leaderSession;
    private String leaderAddress;
    private Integer leaderPort;

    public ConsensusAdd(Integer leaderSession, String leaderAddress, Integer leaderPort) {
        this.leaderSession = leaderSession;
        this.leaderAddress = leaderAddress;
        this.leaderPort = leaderPort;
    }

    // Getters & Setters
    public Integer getLeaderSession() {
        return leaderSession;
    }
    public void setLeaderSession(Integer leaderSession) {
        this.leaderSession = leaderSession;
    }
    public String getLeaderAddress() {
        return leaderAddress;
    }
    public void setLeaderAddress(String leaderAddress) {
        this.leaderAddress = leaderAddress;
    }
    public Integer getLeaderPort() {
        return leaderPort;
    }
    public void setLeaderPort(Integer leaderPort) {
        this.leaderPort = leaderPort;
    }
}
