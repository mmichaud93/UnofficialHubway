package tallmatt.com.unofficialhubway.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by matthewmichaud on 10/27/14.
 */
@Root(name="stations")
public class HubwayResponseModel {

    @ElementList(entry = "station", inline = true)
    List<StationModel> stations;
    @Attribute(name = "lastUpdate", empty = "-1", required = false)
    private String lastUpdate;
    @Attribute(name = "version", empty = "-1", required = false)
    private String version;

    public HubwayResponseModel(List<StationModel> stations, String lastUpdate, String version) {
        this.stations = stations;
        this.lastUpdate = lastUpdate;
        this.version = version;
    }

    public HubwayResponseModel() {

    }

    public List<StationModel> getStations() {
        return stations;
    }

    public void setStations(List<StationModel> stations) {
        this.stations = stations;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}