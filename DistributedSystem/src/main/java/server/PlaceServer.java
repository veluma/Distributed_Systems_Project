package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import places.Place;
import raft.*;
import request.Request;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceServer extends UnicastRemoteObject implements ConsensusInterface, Serializable {

    //attribute
    @Expose private ArrayList<Place> places = new ArrayList<>();
    private ConcurrentHashMap<String, Date> replicas = new ConcurrentHashMap<String, Date>();
    @Expose private List<PlaceServer> replicaList = new ArrayList<>();
    @Expose private String localAddress;
    @Expose private Integer localPort;
    @Expose private Integer frontEndComPort;

    private Integer currentTerm;

    private Long lastTime;
    private Long currentTimeout;
    private String leaderAddress;
    private Integer leaderPort;
    private String candidateAddress;
    private Integer candidatePort;
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();


    @Expose Constants.Roles currentRole = Constants.Roles.Follower;
    public PlaceServer() throws RemoteException{
    }
    //constructor
    public PlaceServer(Integer port) throws RemoteException, UnknownHostException {
        // PlaceManager
        super(0);
        this.localAddress = InetAddress.getLocalHost().getHostAddress();
        this.localPort = port;

        // Consensus Server Attributes
        this.currentRole = Constants.Roles.Follower;
        this.currentTerm = 0;
        this.currentTimeout = (long) (Math.random() * 5000 + 5000) * 1000000;

        // Follower Attributes
        this.lastTime = System.nanoTime();
        this.leaderAddress = null;
        this.leaderPort = null;
        this.candidateAddress = null;
        this.candidatePort = null;
    }

    public Boolean addRequest(ConsensusAdd request) throws RemoteException {
        if (this.getCurrentTerm() > request.getLeaderSession()) {
            return false;
        }

        if (this.getCurrentTerm() < request.getLeaderSession()) {

            this.newTimeout();
            this.setCandidateAddress(null);
            this.setCandidatePort(null);
            this.setCurrentTerm(request.getLeaderSession());
            this.setLeaderAddress(request.getLeaderAddress());
            this.setLeaderPort(request.getLeaderPort());
            this.setCurrentRole(Constants.Roles.Follower);

            return true;
        }

        if (this.getLeaderAddress() == null || this.getLeaderPort() == null) {

            this.newTimeout();
            this.setCandidateAddress(null);
            this.setCandidatePort(null);
            this.setCurrentTerm(request.getLeaderSession());
            this.setLeaderAddress(request.getLeaderAddress());
            this.setLeaderPort(request.getLeaderPort());
            this.setCurrentRole(Constants.Roles.Follower);

            return true;
        }

        this.newTimeout();
        this.setCurrentRole(Constants.Roles.Follower);

        return true;
    }

    public Boolean voteRequest(ConsensusVote request) throws RemoteException {
        if (this.getCurrentTerm() > request.getCandidateSession()) {
            return false;
        }

        if (this.getCurrentTerm() < request.getCandidateSession()) {
            this.setLeaderAddress(null);
            this.setLeaderPort(null);
            this.setCandidateAddress(request.getCandidateAddress());
            this.setCandidatePort(request.getCandidatePort());

            return true;
        }

        return false;
    }

    public void newTimeout() {
        this.setLastTime(System.nanoTime());
        this.setCurrentTimeout((long) (Math.random() * 5000 + 5000) * 1000000);
    }

    public synchronized void addReplica(String replicaServer) throws RemoteException {
        replicas.put(replicaServer, new Date());
    }

    public synchronized void removeReplica(String replicaAddress) throws RemoteException {
        replicas.remove(replicaAddress);
    }

    public synchronized void addAllReplicas(ConcurrentHashMap<String, Date> replicas)
            throws RemoteException {
        this.replicas = replicas;
    }

    public synchronized void removeAllReplicas() throws RemoteException {
        replicas.clear();
    }

    public synchronized void emptyReplicas(Integer maximumReplicaAge) throws RemoteException {
        try {
            ConcurrentHashMap<String, Date> replicas = this.getAllReplicas();
            Iterator<Map.Entry<String, Date>> it = replicas.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Date> pair = it.next();
                if (new Date().getTime() - pair.getValue().getTime() > maximumReplicaAge) {
                    it.remove();
                }
            }

        } catch (RemoteException e) {
            System.out.println(e.getMessage());
        }
    }

    public void coachCurrentRole(){
        switch (getCurrentRole()){
            case Candidate: new Candidate().coach(this);
                break;
            case Leader: new Leader().coach(this);
                break;
            case Follower: new Follower().coach(this);
                break;
        }
    }

    public void processReplicaResponse(String replicaResponse) {
        try {
            PlaceServer responsePlaceServer = gson.fromJson(replicaResponse, PlaceServer.class);
            // add the replica which is sending the response to the replica list if not present
            this.addReplica(responsePlaceServer.getLocalAddress()+":" + responsePlaceServer.getLocalPort());
            // If the received response is from the leader update the current logs of the server to that of the leader server
            if(responsePlaceServer.getCurrentRole() == Constants.Roles.Leader && this.getCurrentRole() != Constants.Roles.Leader){
                this.places = responsePlaceServer.places;
                this.replicaList = responsePlaceServer.replicaList;
            }
        }catch (IOException e) {
          Log.printLog(e.getMessage());
        }
    }

    public String processClientRequest(String request){
        String response = "";
        int errorCode = -1;
        System.out.println("Json to be casted "+ request);
        Request req = gson.fromJson(request, Request.class);
        switch (req.getReqType()){
            case 1: for(Place place : places){
                if(place.getPostalCode().equals(req.getPostalCode())){
                    response = "The Location Corresponding to " +req.getPostalCode() + " is "+ place.getLocationName();
                    errorCode = 0;
                }
            }
            if(errorCode == -1)  response = "The Location Corresponding to " +req.getPostalCode() + " doesn't exist";
            errorCode = 0;
            break;
            case 2: Place newPlace = new Place();
                    newPlace.setLocationName(req.getLocationName());
                    newPlace.setPostalCode(req.getPostalCode());
                    places.add(newPlace);
                    response = "Added new place with postal code = " + newPlace.getPostalCode() + " and location name "+ newPlace.getLocationName() ;
                    errorCode = 0;
                    break;
            case 3:for(Place place : new ArrayList<>(places)){
                if(place.getPostalCode().equals(req.getPostalCode())){
                    places.remove(place);
                    errorCode = 0;
                    response = "removed the information about place with postal code = "+ req.getPostalCode();
                }
                if(errorCode == -1) response = "place not found in the place list : Couldn't delete ";
                errorCode = 0;
                break;
            }
        }
       if(errorCode == 0) return  response;
       else return "error : could not perform operation";
    }

    public synchronized ConcurrentHashMap<String, Date> getAllReplicas() throws RemoteException {
        return replicas;
    }

    public synchronized Constants.Roles getCurrentRole() {
        return currentRole;
    }

    public synchronized void setCurrentRole(Constants.Roles currentRole) {
        this.currentRole = currentRole;
    }

    public synchronized Integer getCandidatePort() {
        return candidatePort;
    }

    public synchronized void setCandidatePort(Integer candidatePort) {
        this.candidatePort = candidatePort;
    }

    public synchronized String getCandidateAddress() {
        return candidateAddress;
    }

    public synchronized void setCandidateAddress(String candidateAddress) {
        this.candidateAddress = candidateAddress;
    }

    public synchronized Integer getLeaderPort() {
        return leaderPort;
    }

    public synchronized void setLeaderPort(Integer leaderPort) {
        this.leaderPort = leaderPort;
    }

    public synchronized String getLeaderAddress() {
        return leaderAddress;
    }

    public synchronized void setLeaderAddress(String leaderAddress) {
        this.leaderAddress = leaderAddress;
    }

    public synchronized Long getCurrentTimeout() {
        return currentTimeout;
    }

    public synchronized void setCurrentTimeout(Long currentTimeout) {
        this.currentTimeout = currentTimeout;
    }

    public synchronized Long getLastTime() {
        return lastTime;
    }

    public synchronized void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public synchronized Integer getCurrentTerm() {
        return currentTerm;
    }

    public synchronized void setCurrentTerm(Integer currentTerm) {
        this.currentTerm = currentTerm;
    }

    public synchronized Integer getLocalPort() {
        return localPort;
    }

    public synchronized void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public synchronized String getLocalAddress() {
        return localAddress;
    }

    public synchronized void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public synchronized ArrayList<Place> getPlaces() {
        return places;
    }

    public synchronized void setPlaces(ArrayList<Place> places) {
        this.places = places;
    }

    public synchronized ConcurrentHashMap<String, Date> getReplicas() {
        return replicas;
    }

    public synchronized void setReplicas(ConcurrentHashMap<String, Date> replicas) {
        this.replicas = replicas;
    }

    public String toString() {
        return "\nSession: " + this.getCurrentTerm() +", Current Role :"+ this.getCurrentRole() +
                "\nCurrent Server Address: rmi://localhost:"+ this.getLocalPort() + "/server "
                + "\nLeader Server Address: \t rmi://localhost:" + this.getLeaderPort() + "/server";
    }
}
