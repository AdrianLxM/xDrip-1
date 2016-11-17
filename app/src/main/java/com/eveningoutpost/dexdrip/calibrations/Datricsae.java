package com.eveningoutpost.dexdrip.calibrations;

import android.util.Log;

import com.eveningoutpost.dexdrip.Models.Calibration;
import com.eveningoutpost.dexdrip.Models.Forecast;
import com.eveningoutpost.dexdrip.Models.Forecast.PolyTrendLine;
import com.eveningoutpost.dexdrip.Models.Forecast.TrendLine;
import com.eveningoutpost.dexdrip.utils.DexCollectionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamorham on 04/10/2016.
 * <p>
 * Maintained by jamorham
 * <p>
 * If you would like to modify a calibration plugin,
 * please create a new one and make the modifications there
 */

public class Datricsae extends CalibrationAbstract {

    private static final String TAG = "Datricsae";

    @Override
    public String getAlgorithmName() {
        return TAG;
    }

    @Override
    public String getAlgorithmDescription() {
        return "pronounced: da-trix-ee - place holder only - do not use";
    }

    @Override
    public CalibrationData getCalibrationData() {

        CalibrationData cd = loadDataFromCache(TAG);
        if (cd == null) {

            // first is most recent
            final List<Calibration> calibrations = Calibration.latestValid(6);
            if (calibrations == null) return null;
            // have we got enough data to have a go
            if (calibrations.size() < 4) {
                // just use whatever xDrip original would have come up with at this point
                cd = new CalibrationData(calibrations.get(0).slope, calibrations.get(0).intercept);
            } else {
                // TODO sanity checks
                final TrendLine bg_to_raw = new Forecast.PolyTrendLine(1);

                final List<Double> raws = new ArrayList<>();
                final List<Double> bgs = new ArrayList<>();
                final boolean adjust_raw = !DexCollectionType.hasLibre();
                for (Calibration calibration : calibrations) {
                    // sanity check?
                    // weighting!
                    final double raw = adjust_raw ? calibration.adjusted_raw_value : calibration.raw_value;
                    Log.d(TAG, "Calibration: " + raw + " -> " + calibration.bg);
                    raws.add(raw);
                    bgs.add(calibration.bg);
                }

                bg_to_raw.setValues(PolyTrendLine.toPrimitiveFromList(bgs), PolyTrendLine.toPrimitiveFromList(raws));
                Log.d(TAG, "Error Variance: " + bg_to_raw.errorVarience());
                final double intercept = bg_to_raw.predict(0);
                Log.d(TAG, "Intercept: " + intercept);
                final double one = bg_to_raw.predict(1);
                Log.d(TAG, "One: " + one);
                final double slope = one - intercept;
                Log.d(TAG, "Slope: " + slope);
                cd = new CalibrationData(slope, intercept);
            }
        }
        return cd; // null if invalid
    }

    @Override
    public boolean invalidateCache() {
        return saveDataToCache(TAG, null);
    }

    // this means we invalidate the cache close to a calibration if our alg
    // makes use of this updated data as the xDrip original algorithm does
    @Override
    public boolean newCloseSensorData() {
        return invalidateCache();
    }
}
