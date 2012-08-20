package org.azavea.map;

import java.io.InputStream;
import java.net.URL;

import org.azavea.otm.App;
import org.azavea.otm.rest.handlers.TileHandler;
import org.azavea.map.TileRequestQueue;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class WMSClient {
	public static Bitmap getTile(int width, int height) {
		try {
			URL url = new URL("http://phillytreemap.org/geoserver/wms?LAYERS=ptm_trees&TRANSPARENT=true&FORMAT=image/gif&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&SRS=EPSG:4326&BBOX=-75.301720,39.834114,-74.972816,40.254116&WIDTH=480&HEIGHT=800");
			InputStream input = url.openStream();
			return BitmapFactory.decodeStream(input);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Bitmap getTile(double top, double left, double bottom, double right) {
		try {
			String urlString = String.format("http://phillytreemap.org/geoserver/wms?LAYERS=ptm_trees&TRANSPARENT=true&FORMAT=image/gif&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&SRS=EPSG:4326&BBOX=%f,%f,%f,%f&WIDTH=480&HEIGHT=800", top, left, bottom, right);
			Log.d("WMSClient", urlString);
			URL url = new URL(urlString);
			InputStream input = url.openStream();
			return BitmapFactory.decodeStream(input);
		} catch (Exception e) {
			Log.d("WMSClient", "Exception: " + e.getMessage());
			return null;
		}
	}
	
	public static Bitmap getTile(double top, double left, double bottom, double right, int height, int width) {
		try {
			String urlString = String.format("http://phillytreemap.org/geoserver/wms?LAYERS=ptm_trees&TRANSPARENT=true&FORMAT=image/png8&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&SRS=EPSG:4326&BBOX=%f,%f,%f,%f&WIDTH=%d&HEIGHT=%d", top, left, bottom, right, width, height);
			Log.d("WMSClient", urlString);
			URL url = new URL(urlString);
			InputStream input = url.openStream();
			return BitmapFactory.decodeStream(input);
		} catch (Exception e) {
			Log.d("WMSClient", "Exception: " + e.getMessage());
			return null;
		}
	}
	
	// Asynchronous version of the above
	public static void getTile(double top, double left, double bottom, double right, int height, int width, TileHandler response) {
		GeoRect boundingBox = new GeoRect(top, left, bottom, right);
		
		getTile(boundingBox, height, width, response);
	}
	
	public static void getTile(GeoRect boundingBox, int height, int width, TileHandler response) {
		TileRequestQueue tileQueue = App.getTileRequestQueue();
		
		// Look up in queue to see if this has already been requested
		TileHandler th = tileQueue.getTileRequestHandler(boundingBox);

		// If it has, see if screen-coords are what we want
		if (th != null) {
			Log.d("WMSClient", "Existing request");
			// If they aren't, get rid of this request - it is no longer needed
			if (th.getX() != response.getX() && th.getY() != response.getY()) {
				// Change screen-coords and make this request current
				th.setX(response.getX());
				th.setY(response.getY());
			} else {
				Log.d("WMSClient", "Existing request usable");
				// The existing request will do what we need so
				// give it the latest sequence-id
				th.setSeqId(App.getTileRequestSeqId());
			}
		} else {
			Log.d("WMSClient", "Nothing found. Creating new request");
			// Nothing found for the given bounding-box so make a new request
			AsyncHttpClient client = new AsyncHttpClient();
			String urlString = String.format("http://phillytreemap.org/geoserver/wms?LAYERS=ptm&TRANSPARENT=true&FORMAT=image/png8&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&SRS=EPSG:4326&BBOX=%f,%f,%f,%f&WIDTH=%d&HEIGHT=%d", boundingBox.getLeft(), boundingBox.getTop(), boundingBox.getRight(), boundingBox.getBottom(), width, height);
			Log.d("WMSClient", urlString);
			client.get(urlString, response);
			tileQueue.addTileRequest(boundingBox, response);
		}

	}
}
