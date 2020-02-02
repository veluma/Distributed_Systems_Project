package places;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PlacesListInterface extends Remote {
    void addPlace(Place place) throws RemoteException;
    Place getPlace(String postalCode) throws RemoteException;
    List<Place> getAllPlaces() throws RemoteException;
    void removePlace(Place place) throws RemoteException;

}
