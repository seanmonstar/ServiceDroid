package com.monstarlab.servicedroid.util;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationUtil extends java.util.Observable implements
		LocationListener {

	private static final double RADIUS = 3963.1;
	private static final int DUE_N = 0;
	private static final int DUE_S = 180;
	private static final int DUE_E = 90;
	private static final int DUE_W = 270;

	private Geocoder geo;
	private Location lastlocation;

	public LocationUtil(Context ctx) {
		LocationManager lm = (LocationManager) ctx
				.getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f,
				this);

		Criteria criteria = new Criteria();
		String bestProvider = lm.getBestProvider(criteria, false);

		lastlocation = lm.getLastKnownLocation(bestProvider);

		geo = new Geocoder(ctx, Locale.getDefault());

	}

	public String getLatLong(String address) {
		List<Address> locations = null;

		double bounding[] = this.getBoundingBox(1.0);

		try {
			if (bounding == null) {
				locations = geo.getFromLocationName(address, 1);

			} else {
				locations = geo.getFromLocationName(address, 1, bounding[0],
						bounding[2], bounding[1], bounding[3]);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (locations.size() > 0) {
			Address address1 = locations.get(0);
			String lat = Double.toString(address1.getLatitude());
			String lon = Double.toString(address1.getLongitude());
			return new String(lat + ',' + lon);
		} else {
			return null;
		}
	}

	/**
	 * Return the distance in miles from current location to given latitude and
	 * longitude
	 * 
	 * @param lat
	 * @param lon
	 * @return double Miles to distance
	 */
	public double getDistanceToLocation(double lat, double lon) {
		double dist;
		if (lastlocation != null) {
			Location dest = new Location(lastlocation.getProvider());
			dest.setLatitude(lat);
			dest.setLongitude(lon);
			dist = lastlocation.distanceTo(dest);
		} else {
			return -1;
		}
		if (dist != 0) {
			dist = dist / 1609.344;
		}
		return dist;
	}

	public double[] getBoundingBox(int distance_in_miles) {
		if (lastlocation == null) {
			return null;
		}

		double lat = lastlocation.getLatitude();
		double lon = lastlocation.getLongitude();

		double lat_r = (Math.PI / 180) * lat;
		double lon_r = (Math.PI / 180) * lon;

		double northmost = Math.asin(Math.sin(lat_r)
				* Math.cos(distance_in_miles / RADIUS) + Math.cos(lat_r)
				* Math.sin(distance_in_miles / RADIUS) * Math.cos(DUE_N));

		double southmost = Math.sin(Math.sin(lat_r)
				* Math.cos(distance_in_miles / RADIUS) + Math.cos(lat_r)
				* Math.sin(distance_in_miles / RADIUS) * Math.cos(DUE_S));

		double eastmost = lon_r
				+ Math.atan2(
						Math.sin(DUE_E) * Math.sin(distance_in_miles / RADIUS)
								* Math.cos(lat_r),
						Math.cos(distance_in_miles / RADIUS) - Math.sin(lat_r)
								* Math.sin(lat_r));

		double westmost = lon_r
				+ Math.atan2(
						Math.sin(DUE_W) * Math.sin(distance_in_miles / RADIUS)
								* Math.cos(lat_r),
						Math.cos(distance_in_miles / RADIUS) - Math.sin(lat_r)
								* Math.sin(lat_r));

		double lat1, lat2, lon1, lon2;
		if (northmost > southmost) {
			lat1 = southmost;
			lat2 = northmost;

		} else {
			lat1 = northmost;
			lat2 = southmost;
		}

		if (eastmost > westmost) {
			lon1 = westmost;
			lon2 = eastmost;

		} else {
			lon1 = eastmost;
			lon2 = westmost;
		}
		double[] bounding = { lat1, lat2, lon1, lon2 };
		return bounding;

	}

	public double[] getBoundingBox(double degrees) {
		if (lastlocation == null) {
			return null;
		}
		double lat = lastlocation.getLatitude();
		double lon = lastlocation.getLongitude();

		double lat1, lat2, lon1, lon2;
		lat1 = lat - degrees;
		lat2 = lat + degrees;

		lon1 = lon - degrees;
		lon2 = lon + degrees;

		double[] bounding = { lat1, lat2, lon1, lon2 };
		return bounding;

	}

	/* This method is called when position is changed */
	public void onLocationChanged(Location location) {
		lastlocation = location;
		setChanged();
		notifyObservers();
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	protected boolean isRouteDisplayed() {
		return false;
	}

}
