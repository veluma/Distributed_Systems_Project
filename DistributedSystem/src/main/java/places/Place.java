package places;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Place class contains the structure of the requrements of any place that is their postal code and location name
 */
public class Place implements Serializable {

    @Expose private String postalCode;
    @Expose private String locationName;

    //default constructor
    public Place(){}

    public Place(String postalCode, String locationName){
        this.postalCode = postalCode;
        this.locationName = locationName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public String toString(){
        return "Postal Code : " + postalCode +
                " Location Name : " + locationName;
    }
}
