package br.com.fiap.blind;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapRouteActivity extends MapActivity implements Runnable, LocationListener, OnInitListener {
	
	private int indiceOnibus;
	private ProgressDialog dialog;
	private static final String URL = "http://goomedia.com.br/sptrans/ServicoRotas.asmx";
	private TextView txtRota, txtDescription;
	private MapView mapView;
	private Rota mRota;
	private String enderecoFinal;
	private boolean trocouOnibus, chegouAoPonto, aguardarOnibus, noOnibus, chegouAoDestino, obteveRotaOnibus;
	private Transporte transporte;
	private ArrayList<Transporte> transportes = new ArrayList<Transporte>();
	private TextToSpeech mTts;
	private ReentrantLock waitForInitLock = new ReentrantLock();
	private boolean ttsInitialized = false;
	private Geocoder geocoder;
	private List<Address> listAddressDesembarque = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize text-to-speech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization completes.
		mTts = new TextToSpeech(this,
				this  // TextToSpeech.OnInitListener
		);
		//don't do speak until initing 
		waitForInitLock.lock(); 
    }
	
    @Override
    public void onStart() {
    	super.onStart();
    	iniciar();
    }
    
    public void iniciar() {
    	if (ttsInitialized) {
            setContentView(R.layout.mapa);
            txtRota = (TextView) findViewById(R.id.rota);
            txtDescription = (TextView) findViewById(R.id.description);
            mapView = (MapView) findViewById(R.id.mapview);
            mapView.setBuiltInZoomControls(true);
			geocoder = new Geocoder(this);
			trocouOnibus = true;
			aguardarOnibus = false;
			obteveRotaOnibus = false;
    		getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    		getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    		Location loc = getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
    		if (loc != null) {
    			setNewLocation(loc);
    		}

    		dialog = ProgressDialog.show(this, "Aguarde", "Calculando rota");

    		new Thread(this).start();
    	}
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
			tratarResposta(rota);		
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
		enderecoFinal = tratarTexto(rota.substring(rota.indexOf("enderecoFinal=") + 14, rota.indexOf("; }")));
		Integer i=1;
		while (quantidadeConducao >= i) {
			Transporte transporte = new Transporte("Ônibus");
			transporte.setEmbarque(tratarTexto(rota.substring(rota.indexOf("enderecoEmbarque" + i + "=") + 18, rota.indexOf(";", rota.indexOf("enderecoEmbarque" + i + "=") + 19))));
			transporte.setDesembarque(tratarTexto(rota.substring(rota.indexOf("enderecoDesembarque" + i + "=") + 21, rota.indexOf(";", rota.indexOf("enderecoDesembarque" + i + "=") + 21))));
			transporte.setLinha(tratarTexto(rota.substring(rota.indexOf("linha" + i + "=") + 7, rota.indexOf(";", rota.indexOf("linha" + i + "=") + 7))));
			transportes.add(transporte);
			i++;
		}
		obteveRotaOnibus = true;
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
	
	public void falar(String fala) {
		fala = tratarTexto(fala);
		txtDescription.setText(fala);
		fala = fala.replace("\n", "");
		mTts.speak(fala, TextToSpeech.QUEUE_ADD, null);
	}
	
	public void obterMapaeRota(double fromLat, double fromLon, double toLat, double toLon) {
		String url = GerarRota.getUrl(fromLat, fromLon, toLat, toLon);
		InputStream is = getConnection(url);
		if (is!=null) { // alterar
			mRota = GerarRota.getRoute(is);
			txtDescription.setText(mRota.mName + " " + mRota.mDescription);
			RouteOverlay routeOverlay = new RouteOverlay(mRota, mapView);
			List<Overlay> listOfOverlays = mapView.getOverlays();
			listOfOverlays.clear();
			listOfOverlays.add(routeOverlay);
			mapView.invalidate();
		}
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
		atualizaRota();
	}

	private void setNewLocation(Location location) {
		Gps.setLatitude((Double) location.getLatitude());
		Gps.setLongitude((Double) location.getLongitude());
	}
	
	private void atualizaRota() {
		try {
			if (obteveRotaOnibus) {
				txtRota.setText("Origem: " + Gps.getStrEnderecoOrigem() + " Destino: " + enderecoFinal);
				double latitude = Gps.getLatitude();
				double longitude = Gps.getLongitude();
				int latitudeInt = (int) (latitude*1E6);
				int longitudeInt = (int) (longitude*1E6);
				int latitudeAprox = (int) (latitude*1E4);
				int longitudeAprox = (int) (longitude*1E4);

				if (trocouOnibus) {
					aguardarOnibus = false;
					List<Address> listAddress = null;
					if (transportes.size() > indiceOnibus) {
						transporte = transportes.get(indiceOnibus);
						listAddress = geocoder.getFromLocationName(transporte.getEmbarque(), 1);
					} else {
						listAddress = geocoder.getFromLocationName(enderecoFinal, 1);
					}
					double toLat = listAddress.get(0).getLatitude();
					double toLon = listAddress.get(0).getLongitude();
					obterMapaeRota(latitude, longitude, toLat, toLon);
					trocouOnibus = false;
				}	

				if (mRota != null) {
					mapView.getOverlays().clear();
					mapView.getOverlays().add(new RouteOverlay(mRota, mapView));
					mapView.getOverlays().add(new PositionOverlay(latitudeInt, longitudeInt));
					mapView.getController().setCenter(new GeoPoint(latitudeInt, longitudeInt));
					mapView.getController().setZoom(21);
					mapView.invalidate();
					for (int i=0; i<mRota.mPoints.length; i++) {
						if (((int) (mRota.mPoints[i].getmLatitude()*1E4) == latitudeAprox) &&
								((int) (mRota.mPoints[i].getmLongitude()*1E4) == longitudeAprox)) {
							String fala = null;
							if (mRota.mPoints[i].getmName() != null)
								fala = mRota.mPoints[i].getmName();
							if (mRota.mPoints[i].getmDescription() != null)
								fala = fala + " \n" + mRota.mPoints[i].getmDescription();
							if (fala != null) {
								falar(fala);
								if (fala.startsWith("Chegar em:"))
									if (transportes.size() > indiceOnibus) 
										chegouAoPonto = true;
									else
										chegouAoDestino = true;
							}
							break;
						}
					}
				} else {
					mapView.getOverlays().clear();
					mapView.getOverlays().add(new PositionOverlay(latitudeInt, longitudeInt));
					mapView.getController().setCenter(new GeoPoint(latitudeInt, longitudeInt));
					mapView.getController().setZoom(21);
					mapView.invalidate();
				}

				if (chegouAoPonto) {
					String fala;
					fala = "Você chegou ao ponto de " + transporte.getTipo() + 
					" \nPegue o " + transporte.getTipo() + 
					" " + transporte.getLinha();
					falar(fala);
					mRota = null;
					chegouAoPonto = false;
					aguardarOnibus = true;
				}

				if (chegouAoDestino) {
					falar("Você chegou ao seu destino \n" + enderecoFinal);
					mRota = null;
					chegouAoDestino = false;
				}

				if (aguardarOnibus) {
					// Espera onibus chegar via WebService
					aguardarOnibus = false;
					noOnibus = true;
				}

				if (noOnibus) {
					if (listAddressDesembarque == null)
						listAddressDesembarque = geocoder.getFromLocationName(transporte.getDesembarque(), 1);
					else {
						double toLat = listAddressDesembarque.get(0).getLatitude();
						double toLon = listAddressDesembarque.get(0).getLongitude();
						Location localAtual = new Location("reverseGeocoded");
						Location localDesembarque = new Location("reverseGeocoded");
						localAtual.setLatitude(latitude);
						localAtual.setLongitude(longitude);
						localDesembarque.setLatitude(toLat);
						localDesembarque.setLongitude(toLon);
						double distance = localAtual.distanceTo(localDesembarque);
						if (distance <= 100.00) {
							falar("A distância para o desembarque é de " + 
									(int) distance + " metros.");
							if ((int) distance <= 5) { // se distancia menor que 5 metros
								falar("Desembarque no próximo ponto.");
								if ((int) distance <= 2) { // se distancia menor que 5 metros
									trocouOnibus = true;
									indiceOnibus++;
									noOnibus = false;
									listAddressDesembarque = null;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	@Override
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
			//unlock it so that speech will happen 
			waitForInitLock.unlock(); 
			ttsInitialized = true;
			iniciar();
		} else {
			// Initialization failed.
			Log.e("DesambiguaVoz", "Could not initialize TextToSpeech.");
		}
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
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
		if (mPoints != null) { // alterado colocar no blind tambem
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
}