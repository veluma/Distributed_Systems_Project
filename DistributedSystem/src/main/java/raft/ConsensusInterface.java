package raft;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ConsensusInterface extends Remote {

    Boolean addRequest(ConsensusAdd request) throws RemoteException;
    Boolean voteRequest(ConsensusVote request) throws RemoteException;
}

