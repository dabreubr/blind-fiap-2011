package br.com.fiap.blind;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapRouteActivity extends MapActivity implements Runnable, LocationListener {
	
	private Handler handler = new Handler();
	private ProgressDialog dialog;
	private static final String URL = "http://goomedia.com.br/sptrans/ServicoRotas.asmx";
	private TextView txtRota, txtDescription;
	private MapView mapView;
	private Rota mRota; 
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);
        
        txtRota = (TextView) findViewById(R.id.rota);
        txtDescription = (TextView) findViewById(R.id.description);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
    }
	
    @Override
    public void onStart() {
    	super.onStart();
    	
        getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		
		Location loc = getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (loc != null) {
			setNewLocation(loc);
		}
		
    	dialog = ProgressDialog.show(this, "Aguarde", "Calculando rota");
    	
    	new Thread(this).start();
    	
    }
    
    @Override
    public void onStop() {
        super.onStop();
        
        getLocationManager().removeUpdates(this);
    }

	private LocationManager getLocationManager() {
    	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	return locationManager;
    }

	@Override
	public void run() {
		try {
			SPTransWS sptrans = new SPTransWS(URL);
			final String rota = sptrans.calcularRota(Gps.getStrEnderecoOrigem(), Gps.getStrEnderecoDestino());
			handler.post(new Runnable() {
				@Override
				public void run() {
					double fromLat = Gps.getEnderecoOrigem().getLatitude(), 
					fromLon = Gps.getEnderecoOrigem().getLongitude(), 
					toLat = Gps.getEnderecoDestino().getLatitude(), 
					toLon = Gps.getEnderecoDestino().getLongitude();
					String url = GerarRota
					.getUrl(fromLat, fromLon, toLat, toLon);
					InputStream is = getConnection(url);
					mRota = GerarRota.getRoute(is);
					txtDescription.setText(mRota.mName + " " + mRota.mDescription);
					RouteOverlay routeOverlay = new RouteOverlay(mRota, mapView);
					List<Overlay> listOfOverlays = mapView.getOverlays();
					listOfOverlays.clear();
					listOfOverlays.add(routeOverlay);
					mapView.invalidate();
					tratarResposta(rota);
				}
				
			});
		} catch (Exception e) {
			Log.e("Rota", e.getMessage(), e);
		} finally {
			dialog.dismiss();
		}
	}

	protected void tratarResposta(String rota) {
		Integer quantidadeConducao = new Integer(rota.substring(rota.indexOf("quantidadeConducao=") + 19, rota.indexOf("quantidadeConducao=") + 20));
		String tempoViagem = rota.substring(rota.indexOf("tempoViagem=") + 12, rota.indexOf("tempoViagem=") + 17);
		String valorTotalTarifa = rota.substring(rota.indexOf("valorTotalTarifa=") + 17, rota.indexOf("valorTotalTarifa=") + 24);
		String enderecoFinal = tratarTexto(rota.substring(rota.indexOf("enderecoFinal=") + 14, rota.indexOf("; }")));
		ArrayList<Transporte> transportes = new ArrayList<Transporte>();
		Integer i=1;
		while (quantidadeConducao >= i) {
			Transporte transporte = new Transporte("Ônibus");
			transporte.setEmbarque(tratarTexto(rota.substring(rota.indexOf("enderecoEmbarque" + i + "=") + 18, rota.indexOf(";", rota.indexOf("enderecoEmbarque" + i + "=") + 19))));
			transporte.setDesembarque(tratarTexto(rota.substring(rota.indexOf("enderecoDesembarque" + i + "=") + 21, rota.indexOf(";", rota.indexOf("enderecoDesembarque" + i + "=") + 21))));
			transporte.setLinha(tratarTexto(rota.substring(rota.indexOf("linha" + i + "=") + 7, rota.indexOf(";", rota.indexOf("linha" + i + "=") + 7))));
			transportes.add(transporte);
			i++;
		}
		
		rota = "\n origem: " + Gps.getStrEnderecoOrigem();
		rota += "\n destino: " + Gps.getStrEnderecoDestino();
		/*rota += "\n quantidadeConducao: " + quantidadeConducao;
		rota += "\n tempoViagem: " + tempoViagem;
		rota += "\n valorTotalTarifa: " + valorTotalTarifa;
		rota += "\n enderecoFinal: " + enderecoFinal;
		rota += "\n transportes: " + transportes;*/
		
		txtRota.setText("Rota: " + rota);
		txtRota.setVisibility(View.VISIBLE);
	}

	private String tratarTexto(String texto) {
		if (texto.endsWith("."))
			texto = texto.substring(0, texto.length()-1);
		if (texto.indexOf("REF.: ") > 0) 
			texto = texto.substring(0, texto.indexOf("REF.: ") - 1);
		texto = texto.replace("AV. ", "AVENIDA ");
		texto = texto.replace("S. ", "SÃO ");
		texto = texto.replace("R. ", "RUA ");
		texto = texto.replace("PCA ", "PRAÇA ");
		texto = texto.replace("PCA. ", "PRAÇA ");
		texto = texto.replace("ESTR. ", "ESTRADA ");
		texto = texto.replace("GEN. ", "GENERAL ");
		return texto;
	}
	
	private InputStream getConnection(String url) {
		InputStream is = null;
		try {
			URLConnection conn = new URL(url).openConnection();
			is = conn.getInputStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}    
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		setNewLocation(location);
	}

	private void setNewLocation(Location location) {
		Gps.setLatitude((Double) location.getLatitude());
		Gps.setLongitude((Double) location.getLongitude());
		
		int latitude = (int) (location.getLatitude()*1E6);
		int longitude = (int) (location.getLongitude()*1E6);
		
		if (mRota != null) {
			mapView.getOverlays().clear();
			mapView.getOverlays().add(new RouteOverlay(mRota, mapView));
			mapView.getOverlays().add(new PositionOverlay(latitude, longitude));
			mapView.getController().setCenter(new GeoPoint(latitude, longitude));
			mapView.invalidate();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}


class PositionOverlay extends com.google.android.maps.Overlay {
	
	private int latitude, longitude;
	private GeoPoint geoPoint;
	private Paint paint = new Paint();
	
	public PositionOverlay(int latitude, int longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		geoPoint = new GeoPoint(this.latitude, this.longitude);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mv, boolean shadow) {
		super.draw(canvas, mv, shadow);
		if (geoPoint != null) {
			paint.setColor(Color.BLUE);
			Point ponto = mv.getProjection().toPixels(geoPoint, null);
			Bitmap bitmap = BitmapFactory.decodeResource(mv.getResources(), R.drawable.marker2);
			RectF r = new RectF(ponto.x, ponto.y, ponto.x+bitmap.getWidth(), ponto.y+bitmap.getHeight());
			canvas.drawBitmap(bitmap, null, r, paint);
		}
		
	}
}

class RouteOverlay extends com.google.android.maps.Overlay {
	Rota rota;
	ArrayList<GeoPoint> mPoints;

	public RouteOverlay(Rota rota, MapView mv) {
		this.rota = rota;
		if (rota.mRoute.length > 0) {
			mPoints = new ArrayList<GeoPoint>();
			for (int i = 0; i < rota.mRoute.length; i++) {
				mPoints.add(new GeoPoint((int) (rota.mRoute[i][1] * 1000000),
						(int) (rota.mRoute[i][0] * 1000000)));
			}
			
			MapController mapController = mv.getController();
			mapController.animateTo(Gps.getPonto());
			mapController.setZoom(21);
		}
	}

	@Override
	public boolean draw(Canvas canvas, MapView mv, boolean shadow, long when) {
		super.draw(canvas, mv, shadow);
		drawPath(mv, canvas);
		return true;
	}

	public void drawPath(MapView mv, Canvas canvas) {
		int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(3);
		for (int i = 0; i < mPoints.size(); i++) {
			Point point = new Point();
			mv.getProjection().toPixels(mPoints.get(i), point);
			x2 = point.x;
			y2 = point.y;
			if (i > 0) {
				canvas.drawLine(x1, y1, x2, y2, paint);
			}
			x1 = x2;
			y1 = y2;
		}
	}
}
