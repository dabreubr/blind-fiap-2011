package br.com.fiap.simuladorblind;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapPrimitive;

import br.com.fiap.simuladorblind.PosicaoOnibus;
import br.com.fiap.simuladorblind.SPTransWS;

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

import br.com.fiap.simuladorblind.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapRouteActivity extends MapActivity implements LocationListener, Runnable, OnInitListener {

	private final String TAG = "MapRouteActivity";
	private int indiceOnibus;
	private MapView mapView;
	private Road mRoad;
	private Handler handler = new Handler();
	private int Tempo = 5000;
	private String enderecoFinal;
	private boolean trocouOnibus, chegouAoPonto, aguardarOnibus, noOnibus, chegouAoDestino, obteveRotaOnibus;
	private Transporte transporte;
	private ArrayList<Transporte> transportes = new ArrayList<Transporte>();
	private static final String URL = "http://goomedia.com.br/sptrans/ServicoRotas.asmx";
	private double[][] coordenadas = new double[][] { 
			{-46.794210,-23.603950}, // voz
			{-46.794250,-23.603850},
			{-46.794310,-23.603750},
			{-46.794350,-23.603650},
			{-46.794430,-23.603540}, // voz
			// {-46.795000,-23.604910} // Testes para recalcular rota
			{-46.794370,-23.603480},
			{-46.794270,-23.603380},
			{-46.794270,-23.603280},
			{-46.793770,-23.603180},
			{-46.791671,-23.602038}, // voz
			{-46.791672,-23.602039},
			{-46.791674,-23.602040},
			{-46.676890,-23.563560},{-46.675840,-23.562870},{-46.674960,-23.562190},{-46.674030,-23.561570},{-46.671960,-23.560300},{-46.668630,-23.558480},{-46.666330,-23.556870},{-46.665110,-23.556450},{-46.664100,-23.556210},{-46.664100,-23.556210},{-46.663780,-23.556090},{-46.662860,-23.555490},{-46.660940,-23.553890},{-46.658590,-23.552250},{-46.654710,-23.550170},{-46.652390,-23.548870},{-46.649010,-23.547780},{-46.648020,-23.547420},{-46.646650,-23.547130},{-46.645790,-23.547200},{-46.642780,-23.547790},
			{-46.642000,-23.547740}, // chegou ao enderecoDesembarque1
			{-46.642000,-23.547740},{-46.642600,-23.547780},{-46.642600,-23.547780},{-46.642780,-23.547790},{-46.642780,-23.547790},
			{-46.642950,-23.546980},
			{-46.643032,-23.546549}, // chegou ao enderecoEmbarque2
			{-46.632370,-23.565110},{-46.632300,-23.566050},{-46.632300,-23.566050},{-46.631370,-23.565390},{-46.631370,-23.565390},{-46.630040,-23.566820},{-46.629440,-23.567570},{-46.629440,-23.567570},{-46.629080,-23.567450},{-46.628330,-23.567870},{-46.625830,-23.569860},{-46.625830,-23.569860},{-46.624950,-23.570000},{-46.624950,-23.570000},{-46.625220,-23.571300},{-46.625220,-23.571300},{-46.624050,-23.571510},{-46.624050,-23.571510},
			{-46.624180,-23.572040}, // chegou ao enderecoDesembarque2
			{-46.624180,-23.572040},{-46.624460,-23.573200},{-46.624580,-23.573920},{-46.624580,-23.573920},{-46.624280,-23.573970},{-46.624090,-23.573900},{-46.623630,-23.573980},{-46.623490,-23.574100},
			{-46.623210,-23.574150} // chegou ao enderecoFinal */
	};
	private int posicao = 0;
	TextView textView;
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
			geocoder = new Geocoder(this);
			trocouOnibus = true;
			aguardarOnibus = false;
			obteveRotaOnibus = false;
			setContentView(R.layout.main);
			mapView = (MapView) findViewById(R.id.mapview);
			textView = (TextView) findViewById(R.id.description);
			mapView.setBuiltInZoomControls(true);
			getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
			getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

			consumirSPTransWS(); 

			handler.postDelayed(this, Tempo);
		}
	}
	
	private void consumirSPTransWS() {
		try {
			SPTransWS sptrans = new SPTransWS(URL, metodoTracarRota);
			final String rota = sptrans.tracarRota("Rua Dr. Oscar Tollens", "Avenida Lins de Vasconcellos");
			tratarResposta(rota);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void tratarResposta(String rota) {
		Integer quantidadeConducao = new Integer(rota.substring(rota.indexOf("quantidadeConducao=") + 19, rota.indexOf("quantidadeConducao=") + 20));
		String tempoViagem = rota.substring(rota.indexOf("tempoViagem=") + 12, rota.indexOf("tempoViagem=") + 17);
		String valorTotalTarifa = rota.substring(rota.indexOf("valorTotalTarifa=") + 17, rota.indexOf("valorTotalTarifa=") + 24);
		enderecoFinal = tratarTexto(rota.substring(rota.indexOf("enderecoFinal=") + 14, rota.indexOf("; }")));
		Integer i=1;
		while (quantidadeConducao >= i) {
			transporte = new Transporte("Ônibus");
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
		texto = texto.replace("AV. ", "Avenida ");
		texto = texto.replace("S. ", "São ");
		texto = texto.replace("R. ", "Rua ");
		texto = texto.replace("PCA ", "Praça ");
		texto = texto.replace("PCA. ", "Praça ");
		texto = texto.replace("ESTR. ", "Estrada ");
		texto = texto.replace("GEN. ", "General ");
		texto = texto.replace("Av. ", "Avenida ");
		texto = texto.replace("S. ", "São ");
		texto = texto.replace("R. ", "Rua ");
		return texto;
	}


	@Override
	public void run() {
		try {
			if (obteveRotaOnibus) {
				if (posicao == coordenadas.length)
					posicao = 0;
				double latitude = (coordenadas[posicao][1]);
				double longitude = (coordenadas[posicao][0]);
				int latitudeInt = (int) (latitude*1E6);
				int longitudeInt = (int) (longitude*1E6);
				int latitudeAprox = (int) (latitude*1E4);
				int longitudeAprox = (int) (longitude*1E4);

				if (mRoad != null) { // se a pé recalculo rota, se necessario
					if ((!chegouAoPonto) || (!chegouAoDestino)) { // somente se nao chegou ao destino do subTrajeto
						Location localAtual = new Location("reverseGeocoded");
						Location localSeguinte = new Location("reverseGeocoded");
						localAtual.setLatitude(latitude);
						localAtual.setLongitude(longitude);
						localSeguinte.setLatitude(mRoad.mPoints[geoPointSeguinte].getmLatitude());
						localSeguinte.setLongitude(mRoad.mPoints[geoPointSeguinte].getmLongitude());
						Double distancia = new Double(localAtual.distanceTo(localSeguinte));
						if (distanciaAnterior != null) {
							if (distancia > distanciaAnterior) { // se aumentou a distancia recalcula a rota
								// sempre altera essas variaveis para poder recalcular rota
								falar("Recalculando rota...");
								trocouOnibus = true;
								mRoad = null;
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

				if (mRoad != null) {
					mapView.getOverlays().clear();
					mapView.getOverlays().add(new RouteOverlay(mRoad, mapView));
					mapView.getOverlays().add(new PositionOverlay(latitudeInt, longitudeInt));
					mapView.getController().setCenter(new GeoPoint(latitudeInt, longitudeInt));
					mapView.getController().setZoom(20);
					mapView.invalidate();
					for (int i=0; i<mRoad.mPoints.length; i++) {
						if (((int) (mRoad.mPoints[i].getmLatitude()*1E4) == latitudeAprox) &&
								((int) (mRoad.mPoints[i].getmLongitude()*1E4) == longitudeAprox)) {

							geoPointSeguinte = i+1;
							
							Location localAtual = new Location("reverseGeocoded");
							Location localSeguinte = new Location("reverseGeocoded");
							localAtual.setLatitude(latitude);
							localAtual.setLongitude(longitude);
							localSeguinte.setLatitude(mRoad.mPoints[geoPointSeguinte].getmLatitude());
							localSeguinte.setLongitude(mRoad.mPoints[geoPointSeguinte].getmLongitude());
							distanciaAnterior = new Double(localAtual.distanceTo(localSeguinte));
							
							String fala = null;
							if (mRoad.mPoints[i].getmName() != null)
								fala = mRoad.mPoints[i].getmName();
							if (mRoad.mPoints[i].getmDescription() != null)
								fala = fala + " \n" + mRoad.mPoints[i].getmDescription();
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
					mapView.getController().setZoom(20);
					mapView.invalidate();
				}

				if (chegouAoPonto) {
					String fala;
					fala = "Você chegou ao ponto de " + transporte.getTipo() + 
					" \nPegue o " + transporte.getTipo() + 
					" " + transporte.getLinha();
					falar(fala);
					mRoad = null;
					chegouAoPonto = false;
					aguardarOnibus = true;
				}

				if (chegouAoDestino) {
					falar("Você chegou ao seu destino \n" + enderecoFinal);
					mRoad = null;
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

				posicao++;
			} else {
				consumirSPTransWS();
			}
			if (posicao < coordenadas.length)
				handler.postDelayed(this, Tempo);
		} catch (Exception e) {
			e.printStackTrace();
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
	
	public void falar(String fala) {
		fala = tratarTexto(fala);
		textView.setText(fala);
		String falaVetor[] = fala.split("\n");
		for (int i=0;i<falaVetor.length;i++) {
			mTts.speak(falaVetor[i], TextToSpeech.QUEUE_ADD, null);
		}
	}

	public void obterMapaeRota(double fromLat, double fromLon, double toLat, double toLon) {
		String url = RoadProvider.getUrl(fromLat, fromLon, toLat, toLon);
		InputStream is = getConnection(url);
		if (is!=null) {
			Log.i(TAG, url);
			mRoad = RoadProvider.getRoute(is);
			textView.setText(mRoad.mName + " " + mRoad.mDescription);
			RouteOverlay routeOverlay = new RouteOverlay(mRoad, mapView);
			List<Overlay> listOfOverlays = mapView.getOverlays();
			listOfOverlays.clear();
			listOfOverlays.add(routeOverlay);
			mapView.invalidate();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTts.shutdown();
	}

	@Override
	protected void onPause() {
		super.onPause();
		getLocationManager().removeUpdates(this);
		mTts.stop();
		finish();
	}

	private LocationManager getLocationManager() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return locationManager;
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
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

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
	Road mRoad;
	ArrayList<GeoPoint> mPoints;

	public RouteOverlay(Road road, MapView mv) {
		mRoad = road;
		if (road.mRoute.length > 0) {
			mPoints = new ArrayList<GeoPoint>();
			for (int i = 0; i < road.mRoute.length; i++) {
				mPoints.add(new GeoPoint((int) (road.mRoute[i][1] * 1000000),
						(int) (road.mRoute[i][0] * 1000000)));
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