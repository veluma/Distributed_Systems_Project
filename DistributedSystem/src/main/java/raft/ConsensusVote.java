package raft;

import java.io.Serializable;

public class ConsensusVote implements Serializable {
    private static final long serialVersionUID = 1L;

    // Attributes
    private Integer candidateSession;
    private String candidateAddress;
    private Integer candidatePort;

    // Constructor
    public ConsensusVote(Integer candidateSession, String candidateAddress, Integer candidatePort) {
        this.candidateSession = candidateSession;
        this.candidateAddress = candidateAddress;
        this.candidatePort = candidatePort;
    }

    // Getters & Setters
    public Integer getCandidateSession() {
        return candidateSession;
    }
    public void setCandidateSession(Integer candidateSession) {
        this.candidateSession = candidateSession;
    }
    public String getCandidateAddress() {
        return candidateAddress;
    }
    public void setCandidateAddress(String candidateAddress) {
        this.candidateAddress = candidateAddress;
    }
    public Integer getCandidatePort() {
        return candidatePort;
    }
    public void setCandidatePort(Integer candidatePort) {
        this.candidatePort = candidatePort;
    }
}
