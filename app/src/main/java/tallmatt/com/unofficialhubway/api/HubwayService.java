package tallmatt.com.unofficialhubway.api;

import retrofit.Callback;
import retrofit.http.GET;
import tallmatt.com.unofficialhubway.models.HubwayResponseModel;

/**
 * Created by matthewmichaud on 10/27/14.
 */
public interface HubwayService {
    @GET("/data/stations/bikeStations.xml")
    void getStations(Callback<HubwayResponseModel> callback);
}
