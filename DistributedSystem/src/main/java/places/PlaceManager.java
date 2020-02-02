package places;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class PlaceManager extends UnicastRemoteObject implements PlacesListInterface {
    // to save all the places
    private List<Place> allPlaces = new ArrayList<Place>();

    public PlaceManager() throws RemoteException {
    }

    public void removePlace(Place place) throws RemoteException{
        allPlaces.remove(place);
    }
    public void addPlace(Place place) throws RemoteException {
        allPlaces.add(place);
    }
    public Place getPlace(String postalCode) throws RemoteException {
        for(Place places : allPlaces){
            if (places.getPostalCode().equals(postalCode))
                return places;
        }
        return new Place("0000", "No Location Found");
    }

    public List<Place> getAllPlaces() throws RemoteException {
        return allPlaces;
    }
}
