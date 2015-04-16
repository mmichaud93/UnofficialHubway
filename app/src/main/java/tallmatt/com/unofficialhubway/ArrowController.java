package tallmatt.com.unofficialhubway;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import tallmatt.com.unofficialhubway.models.StationModel;

/**
 * Created by michaudm3 on 11/11/2014.
 */
public class ArrowController {

    public static Bitmap[] getBitmapArrows(StationModel[] models) {
        Bitmap[] bitmaps = new Bitmap[models.length];

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);

        for(int i = 0; i < models.length; i++) {
            Bitmap bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(new RectF(0, 28-4, 64, 36), paint);
            bitmaps[i] = bitmap;
        }

        return bitmaps;
    }

    public static StationModel[] getClosestStations(LatLng point, ArrayList<StationModel> ogModels, int count) {
        StationModel[] closestModels = new StationModel[count];
        ArrayList<StationModel> models = (ArrayList<StationModel>) ogModels.clone();
        for(int i = 0; i < count; i++) {
            double minDistance = Double.MAX_VALUE;
            StationModel closestModel = null;
            for(StationModel model : models) {
                double distance = getDistance(point.latitude, point.longitude, model.getLat(), model.getLon());
                if(distance == 0) {
                    continue;
                }
                if(distance < minDistance) {
                    minDistance = distance;
                    closestModel = model;
                }
            }
            //Log.d("TM", "distance: " + minDistance + ", name: " + closestModel.getName());
            closestModels[i]=closestModel;
            models.remove(closestModel);
        }



        return closestModels;
    }

    private static double getDistance(double lat1, double lon1, double lat2, double lon2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = deg2rad(lat2 - lat1);
        Double lonDistance = deg2rad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // convert to meters
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
}
