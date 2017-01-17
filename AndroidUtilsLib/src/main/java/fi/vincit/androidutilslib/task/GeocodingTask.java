/*
 * Copyright (C) 2017 Vincit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.vincit.androidutilslib.task;

import java.util.List;

import android.location.Address;
import android.location.Geocoder;

import fi.vincit.androidutilslib.context.WorkContext;

/**
 * Task for geocoding and reverse geocoding. Currently Android class Geocoder is
 * used under the hood.
 */
public abstract class GeocodingTask extends WorkAsyncTask {
    
    /**
     * Returns true if a Geocoder is implemented on the device.
     */
    public static boolean isGeocoderPresent() {
        return Geocoder.isPresent();
    }
    
    private List<Address> mResults;
    private int mMaxResults;
    //Geocoding
    private double mLatitude;
    private double mLongitude;
    //Reverse geocoding
    private String mLocationName;
    private Double mLeftLongitude;
    private Double mTopLatitude;
    private Double mRightLongitude;
    private Double mBottomLatitude;
    
    /**
     * Reverse geocode latitude and longitude.
     */
    public GeocodingTask(WorkContext context, double latitude, double longitude, int maxResults) {
        super(context);
        
        mLatitude = latitude;
        mLongitude = longitude;
        mMaxResults = maxResults;
    }
    
    /**
     * Geocode a location name into a latitude and longitude.
     */
    public GeocodingTask(WorkContext context, String locationName, int maxResults) {
        super(context);
        
        mLocationName = locationName;
        mMaxResults = maxResults;
    }
    
    /**
     * Geocode a location name into a latitude and longitude with a bounding box.
     */
    public GeocodingTask(WorkContext context, String locationName, int maxResults, 
            double leftLongitude, double topLatitude, double rightLongitude, double bottomLatitude) {
        super(context);
        
        mLocationName = locationName;
        mMaxResults = maxResults;
        mLeftLongitude = leftLongitude;
        mTopLatitude = topLatitude;
        mRightLongitude = rightLongitude;
        mBottomLatitude = bottomLatitude;
    }
    
    private List<Address> geocodeReverse(Geocoder geocoder) throws Exception {
        return geocoder.getFromLocation(mLatitude, mLongitude, mMaxResults);
    }
    
    private List<Address> geocode(Geocoder geocoder) throws Exception {
        if (mLeftLongitude != null) {
            return geocoder.getFromLocationName(mLocationName, mMaxResults, 
                    mBottomLatitude, mLeftLongitude, mTopLatitude, mRightLongitude);
        }
        else {
            return geocoder.getFromLocationName(mLocationName, mMaxResults);
        }
    }
    
    @Override
    protected final void onAsyncRun() throws Exception {
        if (isGeocoderPresent()) {
            Geocoder geocoder = new Geocoder(getWorkContext().getContext());
            if (mLocationName != null) {
                mResults = geocode(geocoder);
            }
            else {
                mResults = geocodeReverse(geocoder);
            }
            
            if (mResults == null || mResults.size() == 0) {
                throw new RuntimeException("No geocoding results found");
            }
        }
        else {
            throw new RuntimeException("Geocoder is not present");
        }
    }

    @Override
    protected final void onFinish() {
        onFinishGeocoding(mResults);
    }
    
    /**
     * Called with the results if geocoding was successful.
     */
    protected abstract void onFinishGeocoding(List<Address> results);
}
