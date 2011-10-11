package br.com.fiap.blind;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapPrimitive;

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
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.TextView;
import br.com.fiap.blind.PosicaoOnibus;
import br.com.fiap.blind.SPTransWS;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapRouteActivity extends MapActivity implements Runnable, LocationListener, OnInitListener {
	
	private final String TAG = "MapRouteActivity";
	private int indiceOnibus;
	private ProgressDialog dialog = null;
	private static final String URL = "http://goomedia.com.br/sptrans/ServicoRotas.asmx";
	private TextView txtRota, txtDescription;
	private MapView mapView;
	private Rota mRota;
	private String enderecoFinal;
	private boolean trocouOnibus, chegouAoPonto, aguardarOnibus, 
	noOnibus, chegouAoDestino, obteveRotaOnibus, terminouAtualizaRota;
	private Transporte transporte;
	private ArrayList<Transporte> transportes = new ArrayList<Transporte>();
	private TextToSpeech mTts;
	private ReentrantLock waitForInitLock = new ReentrantLock();
	private boolean ttsInitialized = false;
	private Geocoder geocoder;
	private List<Address> listAddressDesembarque = null;
	private String metodoTracarRota="TracarRota";
	private String metodoRetornaPosicaoOnibusNovo="RetornaPosicaoOnibusNovo";
	private int geoPointSeguinte;
	private Double distanciaAnterior = null;
	
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
			terminouAtualizaRota = true;

			consumirSPTransWS();

			iniciarLocationListener();
    	}
    }
    
    private void iniciarLocationListener() {
    	getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
    	getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
    	Location loc = getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    	if (loc != null) {
    		setNewLocation(loc);
    	}
    }
    
    private void consumirSPTransWS() {
    	if (dialog == null)
    		dialog = ProgressDialog.show(this, "Aguarde", "Calculando rota");
		new Thread(this).start();
	}

	@Override
    public void onStop() {
        super.onStop();
        
        getLocationManager().removeUpdates(this);
        mTts.stop();
        mTts.shutdown();
    }

	private LocationManager getLocationManager() {
    	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	return locationManager;
    }

	@Override
	public void run() {
		try {
			if (!obteveRotaOnibus) {
				SPTransWS sptrans = new SPTransWS(URL, metodoTracarRota);
				//final String rota = sptrans.tracarRota(Gps.getStrEnderecoOrigem(), Gps.getStrEnderecoDestino());
				// xxxxx
				final String rota = sptrans.tracarRota("Rua Silvestre Lourenço da Silva - Osasco", 
				"Avenida Lins de Vasconcellos - São Paulo - São Paulo");
				tratarResposta(rota);
				obteveRotaOnibus = true;
				Log.i(TAG, rota);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
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
	}

	private String tratarTexto(String texto) {
		if (texto.endsWith("."))
			texto = texto.substring(0, texto.length()-1);
		if (texto.indexOf("REF.: ") > 0) 
			texto = texto.substring(0, texto.indexOf("REF.: ") - 1);
		texto = texto.replace("AV. ", "Avenida ");
		texto = texto.replace("S. ", "São ");
		texto = texto.replace("R. ", "Rua ");
		texto = texto.replace("PCA ", "Praça ");
		texto = texto.replace("PCA. ", "Praça ");
		texto = texto.replace("ESTR. ", "Estrada ");
		texto = texto.replace("GEN. ", "General ");
		return texto;
	}
	
	public void falar(String fala) {
		fala = tratarTexto(fala);
		txtDescription.setText(fala);
		String falaVetor[] = fala.split("\n");
		for (int i=0;i<falaVetor.length;i++) {
			mTts.speak(falaVetor[i], TextToSpeech.QUEUE_ADD, null);
		}
	}
	
	public void obterMapaeRota(double fromLat, double fromLon, double toLat, double toLon) {
		String url = GerarRota.getUrl(fromLat, fromLon, toLat, toLon);
		InputStream is = getConnection(url);
		if (is!=null) {
			Log.i(TAG, url);
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
		
		if (terminouAtualizaRota) {
			terminouAtualizaRota = false;
			atualizaRota();
		}
	}

	private void setNewLocation(Location location) {
		Gps.setLatitude((Double) location.getLatitude());
		Gps.setLongitude((Double) location.getLongitude());
		
		TextView txtPos = (TextView) findViewById(R.id.testePos);
		String teste = "";
		teste = "Lat:" + Gps.getLatitude().toString();
		teste += "\nLon:" + Gps.getLongitude().toString();
		Date d = new Date();
		teste += "\nData:" + d.toString(); 
		txtPos.setText(teste);
		Log.i(TAG, teste);
	}
	
	private void atualizaRota() {
		try {
			if (obteveRotaOnibus) {
				Log.i(TAG, "Inicia atualizaRota()");
				// txtRota.setText("Origem: " + Gps.getStrEnderecoOrigem() + " Destino: " + enderecoFinal);
				// xxxxx
				txtRota.setText("Origem: " + "Rua Silvestre Lourenço da Silva - Osasco"
						+ " Destino: " + enderecoFinal);
				double latitude = Gps.getLatitude();
				double longitude = Gps.getLongitude();
				int latitudeInt = (int) (latitude*1E6);
				int longitudeInt = (int) (longitude*1E6);
				int latitudeAprox = (int) (latitude*1E4);
				int longitudeAprox = (int) (longitude*1E4);

				// Blind
				if (mRota != null) { // se a pé recalculo rota, se necessario
					if ((!chegouAoPonto) || (!chegouAoDestino)) { // somente se nao chegou ao destino do subTrajeto
						Location localAtual = new Location("reverseGeocoded");
						Location localSeguinte = new Location("reverseGeocoded");
						localAtual.setLatitude(latitude);
						localAtual.setLongitude(longitude);
						localSeguinte.setLatitude(mRota.mPoints[geoPointSeguinte].getmLatitude());
						localSeguinte.setLongitude(mRota.mPoints[geoPointSeguinte].getmLongitude());
						Double distancia = new Double(localAtual.distanceTo(localSeguinte));
						if (distanciaAnterior != null) {
							if (distancia > distanciaAnterior) { // se aumentou a distancia recalcula a rota
								// sempre altera essas variaveis para poder recalcular rota
								trocouOnibus = true;
								mRota = null;
							}
						}
						distanciaAnterior = new Double(distancia);
					}
				} 
				
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
							geoPointSeguinte = i+1;
							
							Location localAtual = new Location("reverseGeocoded");
							Location localSeguinte = new Location("reverseGeocoded");
							localAtual.setLatitude(latitude);
							localAtual.setLongitude(longitude);
							localSeguinte.setLatitude(mRota.mPoints[geoPointSeguinte].getmLatitude());
							localSeguinte.setLongitude(mRota.mPoints[geoPointSeguinte].getmLongitude());
							distanciaAnterior = new Double(localAtual.distanceTo(localSeguinte));
	
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
					String linha = transporte.getLinha().substring(0, transporte.getLinha().indexOf(" "));
					if (retornaPosicaoOnibus(linha, "", latitude, longitude)) {
						aguardarOnibus = false;
						noOnibus = true;
					}
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
			terminouAtualizaRota = true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage(), e);
			terminouAtualizaRota = true;
		}
	}
	
	public boolean retornaPosicaoOnibus(String linha, String onibus, Double latitude, Double longitude)
	{
		ArrayList<PosicaoOnibus> posicaoOnibusList = new ArrayList<PosicaoOnibus>();
		boolean resultado = false;
		try
		{
			SPTransWS spTransWs = new SPTransWS(URL, metodoRetornaPosicaoOnibusNovo);
			final Object retorno = spTransWs.retornaPosicaoOnibus(linha, onibus);

			// Resultado do método do webservice           
			SoapPrimitive s = (SoapPrimitive)retorno;                    

			// Cria um array JSON para percorrer os registros
			JSONArray jaOnibus = new JSONArray(s.toString());

			for (int i=0; i < jaOnibus.length(); i++) {

				if (jaOnibus.getJSONObject(i) != null) {
					JSONObject json = jaOnibus.getJSONObject(i);

					PosicaoOnibus posicaoOnibus = new PosicaoOnibus();
					posicaoOnibus.setIdPosicaoOnibus(json.getString("idPosicaoOnibus"));
					posicaoOnibus.setLinha(json.getString("linha"));
					posicaoOnibus.setOnibus(json.getString("onibus"));
					posicaoOnibus.setCoordenadaX(json.getString("coordenadaX")); // latitude 
					posicaoOnibus.setCoordenadaY(json.getString("coordenadaY")); // longitude
					posicaoOnibusList.add(posicaoOnibus);
				}
			}
			ArrayList<Double> distancias = new ArrayList<Double>();
			double distanciaAnterior=-1;
			int menorDistancia = 0;
			for (int i=0; i < posicaoOnibusList.size(); i++) {
				PosicaoOnibus posicaoOnibus = posicaoOnibusList.get(i);
				double busLat = posicaoOnibus.getLatitude();
				double busLon = posicaoOnibus.getLongitude();
				Location localAtual = new Location("reverseGeocoded");
				Location localOnibus = new Location("reverseGeocoded");
				localAtual.setLatitude(latitude);
				localAtual.setLongitude(longitude);
				localOnibus.setLatitude(busLat);
				localOnibus.setLongitude(busLon);
				distancias.add(new Double(localAtual.distanceTo(localOnibus)));
			}
			if (distancias.size() > 0) {
				for(int i=0; i < distancias.size(); i++) {
					if (i==0) {
						distanciaAnterior = distancias.get(i);
					} else {
						if (distanciaAnterior > distancias.get(i)) {
							distanciaAnterior = distancias.get(i);
							menorDistancia = i;
						}
					}
				}
				PosicaoOnibus posicaoOnibus = posicaoOnibusList.get(menorDistancia);
				onibus = posicaoOnibus.getOnibus();
				PosicaoOnibus posicaoOnibusMaisPerto = null;
				while (true) {
					final Object retornoOnibus = spTransWs.retornaPosicaoOnibus(linha, onibus);

					// Resultado do método do webservice           
					s = (SoapPrimitive)retornoOnibus;                    

					// Cria um array JSON para percorrer os registros
					jaOnibus = new JSONArray(s.toString());
					
					for (int i=0; i < jaOnibus.length(); i++) {

						if (jaOnibus.getJSONObject(i) != null) {
							JSONObject json = jaOnibus.getJSONObject(i);

							posicaoOnibusMaisPerto = new PosicaoOnibus();
							posicaoOnibusMaisPerto.setIdPosicaoOnibus(json.getString("idPosicaoOnibus"));
							posicaoOnibusMaisPerto.setLinha(json.getString("linha"));
							posicaoOnibusMaisPerto.setOnibus(json.getString("onibus"));
							posicaoOnibusMaisPerto.setCoordenadaX(json.getString("coordenadaX")); // latitude
							posicaoOnibusMaisPerto.setCoordenadaY(json.getString("coordenadaY")); // longitude
						}
					}
					double busLat = posicaoOnibusMaisPerto.getLatitude();
					double busLon = posicaoOnibusMaisPerto.getLongitude();
					Location localAtual = new Location("reverseGeocoded");
					Location localOnibus = new Location("reverseGeocoded");
					localAtual.setLatitude(latitude);
					localAtual.setLongitude(longitude);
					localOnibus.setLatitude(busLat);
					localOnibus.setLongitude(busLon);
					Double distanciaDoOnibus = new Double(localAtual.distanceTo(localOnibus));
					if (distanciaDoOnibus < 100.00) {
						falar("O " + transporte.getTipo() + " está a " + distanciaDoOnibus + " metros de distância");
						if (distanciaDoOnibus < 10.00) {
							falar("O " + transporte.getTipo() + " da linha " + transporte.getLinha() + " chegou ao ponto");
							resultado = true;
							break;
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			Log.i("Erro: ", ex.getMessage());
		}
		return resultado;
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
		if (mPoints != null) { 
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