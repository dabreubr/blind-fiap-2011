package src.code;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapPrimitive;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CadastroRotasActivity extends Activity implements OnClickListener, LocationListener {

	private final String url = "http://goomedia.com.br/sptrans/ServicoRotas.asmx";
	private final String metodoInserePosicaoOnibus = "InserePosicaoOnibus";
	//private final String metodoLimparPosicaoOnibus  = "LimparPosicaoOnibus";
	//private final String metodoRetornaPosicaoOnibus  = "RetornaPosicaoOnibus";
	private final String metodoRetornaPosicaoOnibusNovo  = "RetornaPosicaoOnibusNovo";
	private TextView txtLinha, txtOnibus, txtResultado;
	private Button btnIniciar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main);
			
			txtResultado = (TextView) findViewById(R.id.resultado);
			txtLinha = (TextView) findViewById(R.id.txtLinha);
			txtOnibus = (TextView) findViewById(R.id.txtOnibus);
			btnIniciar = (Button) findViewById(R.id.btnIniciar);
			
			btnIniciar.setOnClickListener(this);
		}
		catch(Exception ex)
		{
			Log.i("Erro: ", ex.getMessage());

		}
	}

    @Override
    public void onStop() {
        super.onStop();
        getLocationManager().removeUpdates(this);
    }
	
	@Override
	public void onClick(View view) {
		getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
	
	private LocationManager getLocationManager() {
    	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	return locationManager;
    }

	public void inserePosicaoOnibus(String linha, String onibus, String coordenadaX, String coordenadaY)
	{
		try
		{
			SPTransWS spTransWs = new SPTransWS(url, metodoInserePosicaoOnibus);
			final String retorno = spTransWs.inserePosicaoOnibus(linha, onibus, coordenadaX, coordenadaY);
			
			txtResultado.setText("Retorno: " + retorno + "\nlinha: " + linha + "\nonibus:" + onibus + "\ncoordenadaX:" + coordenadaX + "\ncoordenadaY:" + coordenadaY);
			txtResultado.setVisibility(View.VISIBLE);
		}
		catch(Exception ex)
		{
			Log.i("Erro: ", ex.getMessage());
			txtResultado.setText("Erro: " + ex.getMessage());
			txtResultado.setVisibility(View.VISIBLE);
		}
	}
	
	public void retornaPosicaoOnibus(String linha, String onibus)
	{
		try
		{

			SPTransWS spTransWs = new SPTransWS(url, metodoRetornaPosicaoOnibusNovo);
			final Object retorno = spTransWs.retornaPosicaoOnibus(linha, onibus);

			// Resultado do método do webservice           
			SoapPrimitive s = (SoapPrimitive)retorno;                    

			// Cria um array JSON para percorrer os registros
			JSONArray jaOnibus = new JSONArray(s.toString());
			
			String resultado = "";
			
			for (int i=0; i < jaOnibus.length(); i++) {
				
				if (jaOnibus.getJSONObject(i) != null) {
					JSONObject json = jaOnibus.getJSONObject(i);
					
					PosicaoOnibus posicaoOnibus = new PosicaoOnibus();
					posicaoOnibus.setIdPosicaoOnibus(json.getString("idPosicaoOnibus"));
					posicaoOnibus.setLinha(json.getString("linha"));
					posicaoOnibus.setOnibus(json.getString("onibus"));
					posicaoOnibus.setCoordenadaX(json.getString("coordenadaX"));
					posicaoOnibus.setCoordenadaY(json.getString("coordenadaY"));
					
					resultado += posicaoOnibus.toString();
				}
			}

			txtResultado.setText("Retorno\n" + resultado);
			txtResultado.setVisibility(View.VISIBLE);
		}
		catch(Exception ex)
		{
			Log.i("Erro: ", ex.getMessage());
			txtResultado.setText("Erro: " + ex.getMessage());
			txtResultado.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {
		inserePosicaoOnibus(txtLinha.getText().toString(), txtOnibus.getText().toString(), String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
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