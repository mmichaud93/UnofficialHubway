package tallmatt.com.unofficialhubway;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.logging.Logger;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import io.fabric.sdk.android.Fabric;

public class HubwayActivity extends ActionBarActivity {

    public static String PREFERENCES_NAME = "preferences";

    private Menu menu;

    private GoogleMap gMap;
    HubwayFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        ((HubwayApplication) getApplication()).getTracker(HubwayApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.activity_hubway);
        setUpMapIfNeeded();
        if(savedInstanceState!=null) {
            if(savedInstanceState.getParcelable("location")!=null && gMap!=null) {
                CameraPosition cameraPosition = savedInstanceState.getParcelable("location");
                gMap.moveCamera(CameraUpdateFactory.newLatLng(cameraPosition.target));
            }
        } else {
            SharedPreferences settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
            if(settings!=null) {
                if(settings.contains("lat") && settings.contains("lon") && settings.contains("zoom")) {
                    double lat = settings.getFloat("lat", 0);
                    double lon = settings.getFloat("lon", 0);
                    float zoom = settings.getFloat("zoom", 2);
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), zoom));
                } else {
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.350078, -71.077331), 12));
                }
            }
        }

        if (savedInstanceState == null) {
            getFragment();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    private void getFragment() {
        if(fragment==null) {
            fragment = HubwayFragment.newInstance(gMap);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if(fragment!=null) {
                    fragment.refreshStations(true);
                }
                break;
            case R.id.action_location:
                if(fragment!=null) {
                    fragment.getMyLocation();
                }
                break;
            case R.id.action_favorites:
                if(fragment!=null) {
                    boolean favorited = fragment.toggleFavorites();
                    if(favorited) {
                        item.setIcon(R.drawable.ic_toggle_star);
                    } else {
                        item.setIcon(R.drawable.ic_toggle_star_outline);
                    }
                    Tracker tracker = ((HubwayApplication)(this.getApplication())).getTracker(
                            HubwayApplication.TrackerName.APP_TRACKER);
                    tracker.setScreenName("HubwayActivity");
                    tracker.send(new HitBuilders.EventBuilder()
                            .setAction("Toggle Favorites")
                            .setValue((favorited ? 1 : 0))
                            .build());
                }
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        getFragment();
        fragment.refreshStations(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveMap();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

    private void saveMap() {
        SharedPreferences settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        settings.edit().
                putFloat("lat", (float) gMap.getCameraPosition().target.latitude).
                putFloat("lon", (float)gMap.getCameraPosition().target.longitude).
                putFloat("zoom", gMap.getCameraPosition().zoom).
                apply();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("location", gMap.getCameraPosition());
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (gMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            gMap.getUiSettings().setMyLocationButtonEnabled(false);
            gMap.getUiSettings().setZoomControlsEnabled(false);
            gMap.setMyLocationEnabled(true);
            gMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    if(menu!=null && menu.getItem(0)!=null) {
                        if(location!=null) {
                            menu.getItem(0).setIcon(R.drawable.ic_action_maps_my_location);
                        } else {
                            menu.getItem(0).setIcon(R.drawable.ic_action_device_location_searching);
                        }
                    }
                }
            });
            // Check if we were successful in obtaining the map.
            if (gMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

    }
}
