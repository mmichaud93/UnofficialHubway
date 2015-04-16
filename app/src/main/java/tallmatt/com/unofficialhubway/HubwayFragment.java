package tallmatt.com.unofficialhubway;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tallmatt.com.unofficialhubway.api.HubwayService;
import tallmatt.com.unofficialhubway.api.SimpleXMLConverter;
import tallmatt.com.unofficialhubway.models.HubwayResponseModel;
import tallmatt.com.unofficialhubway.models.StationModel;

/**
 * Created by matthewmichaud on 10/26/14.
 */
public class HubwayFragment extends Fragment {
    public static HubwayFragment newInstance(GoogleMap gMap) {
        HubwayFragment fragment = new HubwayFragment();
        fragment.setMap(gMap);
        return fragment;
    }

    GoogleMap gMap;
    boolean animatingDown = false;
    public void setMap(final GoogleMap gMap) {
        this.gMap = gMap;
        gMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if(lastMarker!=null && !animatingDown) {
                    //Log.d("TM", (gMap.getProjection().toScreenLocation(cameraPosition.target).x-gMap.getProjection().toScreenLocation(lastMarker.getPosition()).x)+", "+(gMap.getProjection().toScreenLocation(cameraPosition.target).y-gMap.getProjection().toScreenLocation(lastMarker.getPosition()).y));
                    if(
                            Math.abs(gMap.getProjection().toScreenLocation(cameraPosition.target).x-gMap.getProjection().toScreenLocation(lastMarker.getPosition()).x) >
                                    fragmentRoot.getWidth()/2.25  ||
                            Math.abs(gMap.getProjection().toScreenLocation(cameraPosition.target).y - gMap.getProjection().toScreenLocation(lastMarker.getPosition()).y) >
                                    fragmentRoot.getHeight()/2.25
                            ) {
                        if(onScreenRoot!=null && onScreenRoot.getVisibility()==View.VISIBLE) {
                            animatingDown=true;
                            animateDown(onScreenRoot);
                        } else if(offScreenRoot!=null && offScreenRoot.getVisibility()==View.VISIBLE) {
                            animatingDown=true;
                            animateDown(offScreenRoot);
                        }
                    }
                }
            }
        });
    }

    @InjectView(R.id.fragment_hubway_root)
    RelativeLayout fragmentRoot;

    @InjectView(R.id.station_one_detail)
    LinearLayout stationOneDetail;
    @InjectView(R.id.station_one_name)
    TextView stationOneName;
    @InjectView(R.id.station_one_bikes)
    TextView stationOneBikes;
    @InjectView(R.id.station_one_docks)
    TextView stationOneDocks;
    @InjectView(R.id.station_one_map)
    ImageButton stationOneMap;
    @InjectView(R.id.station_one_star)
    ImageButton stationOneStar;

    @InjectView(R.id.station_two_detail)
    LinearLayout stationTwoDetail;
    @InjectView(R.id.station_two_name)
    TextView stationTwoName;
    @InjectView(R.id.station_two_bikes)
    TextView stationTwoBikes;
    @InjectView(R.id.station_two_docks)
    TextView stationTwoDocks;
    @InjectView(R.id.station_two_map)
    ImageButton stationTwoMap;
    @InjectView(R.id.station_two_star)
    ImageButton stationTwoStar;
    @InjectView(R.id.hubway_progress_bar)
    SmoothProgressBar progressBar;

    View onScreenRoot;
    View offScreenRoot;

    HubwayService service;
    HubwayResponseModel stationModel;
    HashMap<Marker, StationModel> markerMap;

    Style customStyle;
    Configuration customConfig;

    Marker lastMarker;
    StationModel currentStation = null;

    Set<String> favoritesStation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createRestService();

        refreshStations(false);

        SharedPreferences settings = getActivity().getSharedPreferences("favorite stations", 0);
        favoritesStation = settings.getStringSet("favorites", new HashSet<String>());
        Log.d("TM", "length: "+favoritesStation.size());
    }

    private void createRestService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://www.thehubway.com")
                .setConverter(new SimpleXMLConverter())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        service = restAdapter.create(HubwayService.class);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        customStyle = new Style.Builder().setBackgroundColor(R.color.app_primary_light).build();
        customConfig = new Configuration.Builder().setDuration(3000).build();
    }

    @Override
    public void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        Log.d("TM", "length: "+favoritesStation.size());
        getActivity().getSharedPreferences("favorite stations", 0).edit().clear().putStringSet("favorites", favoritesStation).commit();
    }

    public void refreshStations(final boolean clearMap) {
        if(gMap!=null && clearMap) {
            gMap.clear();
            lastMarker = null;
            animateDown(onScreenRoot);
        }
        if(progressBar!=null) {
            progressBar.progressiveStart();
        }
        markerMap = new HashMap<Marker, StationModel>();
        if(service==null) {
            createRestService();
        }
        if(service!=null) {
            service.getStations(new Callback<HubwayResponseModel>() {
                @Override
                public void success(HubwayResponseModel stationModel, Response response) {
                    if (progressBar != null) {
                        progressBar.progressiveStop();
                    }
                    if (stationModel != null) {
                        HubwayFragment.this.stationModel = stationModel;
                        if (gMap != null) {
                            if (!clearMap) {
                                gMap.clear();

                            }
                            gMap.setOnMarkerClickListener(onMarkerClickListener);
                            if (!favoritesShown) {
                                drawMarkers(stationModel.getStations());
                            } else {
                                ArrayList<StationModel> favoriteModels = new ArrayList<StationModel>();
                                for (StationModel model : stationModel.getStations()) {
                                    if (favoritesStation.contains(Integer.toString(model.getId()))) {
                                        favoriteModels.add(model);
                                    }
                                }
                                drawMarkers(favoriteModels, R.drawable.ic_maps_place_gold, currentStation);
                            }
                            if (currentStation != null) {
                                lastMarker = getKeyByValue(markerMap, currentStation);
                                drawLines();
                            }
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("UH", error.toString());
                    if (progressBar != null) {
                        progressBar.progressiveStop();
                    }
                    Crouton.makeText(getActivity(), "Could not get bike stations", customStyle, fragmentRoot).setConfiguration(customConfig).show();
                }
            });
        }
    }

    private void drawMarkers(List<StationModel> models) {
        for (StationModel model : models) {
            Marker marker = gMap.addMarker(new MarkerOptions()
                    .position(new LatLng(model.getLat(), model.getLon()))
                    .title(model.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_place)));
            markerMap.put(marker, model);
        }
    }

    public static <T, E> T getKeyByValue(HashMap<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
                if (value.equals(entry.getValue())) {
                    return entry.getKey();
                }
        }
        return null;
    }

    private void drawMarkers(List<StationModel> models, int drawableResource, StationModel currentStation) {
        for (StationModel model : models) {
            if(model==currentStation) {
                continue;
            }
            Marker marker = gMap.addMarker(new MarkerOptions()
                    .position(new LatLng(model.getLat(), model.getLon()))
                    .title(model.getName())
                    .icon(BitmapDescriptorFactory.fromResource(drawableResource)));
            markerMap.put(marker, model);
        }
    }

    public void getMyLocation() {
        if(gMap.getMyLocation()==null) {
            // no location got
            Crouton.makeText(getActivity(), "Location not available", customStyle, fragmentRoot).setConfiguration(customConfig).show();
        } else {
            float zoom = 14f;
            if(gMap.getCameraPosition().zoom>zoom) {
                zoom = gMap.getCameraPosition().zoom;
            }
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(gMap.getMyLocation().getLatitude(),gMap.getMyLocation().getLongitude()), zoom));
        }
    }

    boolean favoritesShown = false;
    public boolean toggleFavorites() {
        if(stationModel==null || gMap==null) {
            return favoritesShown;
        }
        favoritesShown=!favoritesShown;
        markerMap = new HashMap<Marker, StationModel>();
        gMap.clear();
        drawLines();
        if(favoritesShown) {
            ArrayList<StationModel> favoriteModels = new ArrayList<StationModel>();
            for(StationModel model : this.stationModel.getStations()) {
                if(favoritesStation.contains(Integer.toString(model.getId()))) {
                    favoriteModels.add(model);
                }
            }
            drawMarkers(favoriteModels, R.drawable.ic_maps_place_gold, currentStation);
        } else {
            drawMarkers(stationModel.getStations());
        }
        if(lastMarker!=null && currentStation!=null) {
            if(favoritesStation.contains(Integer.toString(currentStation.getId())) || !favoritesShown) {
                lastMarker = gMap.addMarker(new MarkerOptions()
                        .position(lastMarker.getPosition())
                        .title(lastMarker.getTitle())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_place_selected)));
                markerMap.put(lastMarker, currentStation);
            } else {
                lastMarker=null;
                animateDown(onScreenRoot);
            }
        }
        return favoritesShown;
    }
    ArrayList<Polyline> lines = new ArrayList<Polyline>();
    GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            StationModel stationModel = markerMap.get(marker);
            if(stationModel==currentStation && onScreenRoot!=null) {
                return false;
            }
            if(stationModel!=null && marker!=lastMarker) {
                Tracker tracker = ((HubwayApplication)(getActivity().getApplication())).getTracker(
                        HubwayApplication.TrackerName.APP_TRACKER);
                tracker.setScreenName("HubwayFragment");
                tracker.send(new HitBuilders.EventBuilder()
                        .setAction("Marker Click")
                        .setLabel(stationModel.getName())
                        .build());
                if(lastMarker!=null) {
                    if(favoritesShown) {
                        if(!favoritesStation.contains(Integer.toString(currentStation.getId()))) {
                            lastMarker.remove();
                            lastMarker=null;
                        } else {
                            lastMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_place_gold));
                        }
                    } else {
                        lastMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_place));
                    }
                }
                currentStation = stationModel;

                drawLines();

                lastMarker = marker;
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_place_selected));
                if(onScreenRoot==null && offScreenRoot==null) {
                    // put content in screen 1 and animate up
                    fillScreenOne(stationModel);
                    onScreenRoot = stationOneDetail;
                    animateCamera(stationModel);
                    animateUp(onScreenRoot);
                } else {
                    if(onScreenRoot==stationOneDetail) {
                        // fill screen2 with the station
                        fillScreenTwo(stationModel);
                        offScreenRoot = stationTwoDetail;
                        onScreenRoot = stationOneDetail;
                    } else {
                        // fill screen1 with the station
                        fillScreenOne(stationModel);
                        offScreenRoot = stationOneDetail;
                        onScreenRoot = stationTwoDetail;
                    }
                    animateCamera(stationModel);
                    if(stationModel.getLon() > gMap.getCameraPosition().target.longitude) {
                        // animate detail from right to left
                        animateDetailsLeft();
                    } else {
                        // animate detail from left to right
                        animateDetailsRight();
                    }
                }

                return true;
            } else {
                stationOneDetail.setVisibility(View.GONE);
            }
            return false;
        }
    };

    private void drawLines() {
        if(currentStation==null) {
            return;
        }
        for(Polyline line: lines) {
            line.remove();
        }

        StationModel[] closestModels = null;
        int color = Color.rgb(71, 170, 66);
        if(favoritesShown) {
            ArrayList<StationModel> favoriteModels = new ArrayList<StationModel>();
            for(StationModel model : HubwayFragment.this.stationModel.getStations()) {
                if(favoritesStation.contains(Integer.toString(model.getId()))) {
                    favoriteModels.add(model);
                }
            }
            if(favoriteModels.contains(currentStation)) {
                int count = 3;
                if (favoriteModels.size() - 1 < 3) {
                    count = favoriteModels.size() - 1;
                }
                closestModels = ArrowController.getClosestStations(
                        new LatLng(currentStation.getLat(), currentStation.getLon()),
                        favoriteModels, count);
            }
        } else {
            closestModels = ArrowController.getClosestStations(
                    new LatLng(currentStation.getLat(), currentStation.getLon()),
                    (ArrayList<StationModel>) HubwayFragment.this.stationModel.getStations(), 3);
        }
        if(closestModels!=null) {
            for (int i = 0; i < closestModels.length; i++) {
                StationModel model = closestModels[i];
                PolylineOptions line = new PolylineOptions()
                        .add(new LatLng(model.getLat(), model.getLon()),
                                new LatLng(currentStation.getLat(), currentStation.getLon()))
                        .color(color);
                lines.add(gMap.addPolyline(line));
            }
        }
    }

    public void fillScreenOne(StationModel stationModel) {
        stationOneName.setText(stationModel.getName());
        stationOneBikes.setText("Available Bikes: "+stationModel.getNbBikes());
        stationOneDocks.setText("Available Docks: "+stationModel.getNbEmptyDocks());
        if(favoritesStation.contains(Integer.toString(stationModel.getId()))) {
            stationOneStar.setBackgroundResource(R.drawable.ripple_enabled);
        } else {
            stationOneStar.setBackgroundResource(R.drawable.ripple);
        }
    }

    public void fillScreenTwo(StationModel stationModel) {
        stationTwoName.setText(stationModel.getName());
        stationTwoBikes.setText("Available Bikes: "+stationModel.getNbBikes());
        stationTwoDocks.setText("Available Docks: "+stationModel.getNbEmptyDocks());
        if(favoritesStation.contains(Integer.toString(stationModel.getId()))) {
            stationTwoStar.setBackgroundResource(R.drawable.ripple_enabled);
        } else {
            stationTwoStar.setBackgroundResource(R.drawable.ripple);
        }
    }

    public void animateCamera(StationModel stationModel) {
        float zoom = 14f;
        if(gMap.getCameraPosition().zoom>zoom) {
            zoom = gMap.getCameraPosition().zoom;
        }
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(stationModel.getLat(), stationModel.getLon()),
                zoom
        ), 500, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                
            }

            @Override
            public void onCancel() {

            }
        });
    }
    public void animateUp(View view) {
        if(view.getVisibility()==View.GONE) {
            view.setVisibility(View.VISIBLE);
            Animation slideOnUp = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_on_up);
            slideOnUp.setInterpolator(new DecelerateInterpolator());
            view.startAnimation(slideOnUp);
        }
    }
    public void animateDown(final View view) {
        if(currentStation==null) {
            return;
        }
        Animation slideOffDown = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_off_down);
        slideOffDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
                onScreenRoot=null;
                offScreenRoot=null;
                animatingDown = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideOffDown.setInterpolator(new DecelerateInterpolator());
        if(view!=null) {
            view.startAnimation(slideOffDown);
        }
    }
    public void animateDetailsRight() {
        Animation slideOffRight = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_off_right);
        slideOffRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onScreenRoot.setVisibility(View.GONE);
                View temp = offScreenRoot;
                offScreenRoot = onScreenRoot;
                onScreenRoot = temp;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideOffRight.setInterpolator(new DecelerateInterpolator());
        onScreenRoot.startAnimation(slideOffRight);

        Animation slideOnRight = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_on_right);
        slideOnRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                offScreenRoot.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideOnRight.setInterpolator(new DecelerateInterpolator());
        offScreenRoot.startAnimation(slideOnRight);
    }
    public void animateDetailsLeft() {
        Animation slideOffLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_off_left);
        slideOffLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onScreenRoot.setVisibility(View.GONE);
                View temp = offScreenRoot;
                offScreenRoot = onScreenRoot;
                onScreenRoot = temp;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideOffLeft.setInterpolator(new DecelerateInterpolator());
        onScreenRoot.startAnimation(slideOffLeft);

        Animation slideOnLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_on_left);
        slideOnLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                offScreenRoot.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideOnLeft.setInterpolator(new DecelerateInterpolator());
        offScreenRoot.startAnimation(slideOnLeft);
    }

    View.OnClickListener mapButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(currentStation!=null) {
                Uri uri = Uri.parse("geo:"+currentStation.getLat()+","+currentStation.getLon());
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        }
    };

    View.OnClickListener starButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!favoritesStation.contains(Integer.toString(currentStation.getId()))) {
                favoritesStation.add(Integer.toString(currentStation.getId()));
                if(onScreenRoot==stationOneDetail) {
                    stationOneStar.setBackgroundResource(R.drawable.ripple_enabled);
                } else if(onScreenRoot==stationTwoDetail) {
                    stationTwoStar.setBackgroundResource(R.drawable.ripple_enabled);
                }
            } else {
                favoritesStation.remove(Integer.toString(currentStation.getId()));
                if(onScreenRoot==stationOneDetail) {
                    stationOneStar.setBackgroundResource(R.drawable.ripple);
                } else if(onScreenRoot==stationTwoDetail) {
                    stationTwoStar.setBackgroundResource(R.drawable.ripple);
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hubway, container, false);

        ButterKnife.inject(this, rootView);

        stationOneMap.setOnClickListener(mapButtonClickListener);
        stationOneStar.setOnClickListener(starButtonClickListener);
        stationTwoMap.setOnClickListener(mapButtonClickListener);
        stationTwoStar.setOnClickListener(starButtonClickListener);

        return rootView;
    }
}
