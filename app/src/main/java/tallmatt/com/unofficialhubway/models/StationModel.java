package tallmatt.com.unofficialhubway.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by matthewmichaud on 10/27/14.
 */
@Root(name="station")
public class StationModel {
    @Element
    int id;
    @Element
    String name;
    @Element
    String terminalName;
    @Element
    long lastCommWithServer;
    @Element
    double lat;
    @Element(name="long")
    double lon;
    @Element
    boolean installed;
    @Element
    boolean locked;
    @Element(name="installDate", required=false)
    long installDate;
    @Element(name="removalDate", required=false)
    long removalDate;
    @Element
    boolean temporary;
    @Element(name="public")
    boolean pub;
    @Element
    int nbBikes;
    @Element
    int nbEmptyDocks;
    @Element
    long latestUpdateTime;

    public StationModel(int id, String name, String terminalName, long lastCommWithServer, double lat, double lon, boolean installed, boolean locked, long intallDate, long removeDate, boolean temporary, boolean pub, int nbBikes, int nbEmptyDocks, long latestUpdateTime) {
        this.id = id;
        this.name = name;
        this.terminalName = terminalName;
        this.lastCommWithServer = lastCommWithServer;
        this.lat = lat;
        this.lon = lon;
        this.installed = installed;
        this.locked = locked;
        this.installDate = intallDate;
        this.removalDate = removeDate;
        this.temporary = temporary;
        this.pub = pub;
        this.nbBikes = nbBikes;
        this.nbEmptyDocks = nbEmptyDocks;
        this.latestUpdateTime = latestUpdateTime;
    }

    public StationModel() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTerminalName() {
        return terminalName;
    }

    public void setTerminalName(String terminalName) {
        this.terminalName = terminalName;
    }

    public long getLastCommWithServer() {
        return lastCommWithServer;
    }

    public void setLastCommWithServer(long lastCommWithServer) {
        this.lastCommWithServer = lastCommWithServer;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public long getInstallDate() {
        return installDate;
    }

    public void setInstallDate(long installDate) {
        this.installDate = installDate;
    }

    public long getRemovalDate() {
        return removalDate;
    }

    public void setRemovalDate(long removalDate) {
        this.removalDate = removalDate;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public boolean isPub() {
        return pub;
    }

    public void setPub(boolean pub) {
        this.pub = pub;
    }

    public int getNbBikes() {
        return nbBikes;
    }

    public void setNbBikes(int nbBikes) {
        this.nbBikes = nbBikes;
    }

    public int getNbEmptyDocks() {
        return nbEmptyDocks;
    }

    public void setNbEmptyDocks(int nbEmptyDocks) {
        this.nbEmptyDocks = nbEmptyDocks;
    }

    public long getLatestUpdateTime() {
        return latestUpdateTime;
    }

    public void setLatestUpdateTime(long latestUpdateTime) {
        this.latestUpdateTime = latestUpdateTime;
    }
}
