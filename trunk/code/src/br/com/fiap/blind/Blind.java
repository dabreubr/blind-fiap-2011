package br.com.fiap.blind;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class Blind extends Activity implements LocationListener, TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener  {

    private TextToSpeech mTts;
    private ReentrantLock waitForInitLock = new ReentrantLock();
    private boolean ttsInitialized = false;
    
	/** Chamado quando a aplicação é iniciada. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
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
    	iniciarAplicacao();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        
        getLocationManager().removeUpdates(this);
    }
    
    public void iniciarAplicacao() {
    	if (ttsInitialized) {
    		if (!getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER)) {
    			if (waitForInitLock.isLocked()) 
    	    	{ 
    	    		try 
    	    		{ 
    	    			waitForInitLock.tryLock(180, TimeUnit.SECONDS); 
    	    		} 
    	    		catch (InterruptedException e) 
    	    		{ 
    	    			Log.e("speaker", "interruped"); 
    	    		} 
    	    		//unlock it here so that it is never locked again 
    	    		waitForInitLock.unlock(); 
    	    	}
    			if (!internetDisponivel()) {
	   	    		mTts.speak(this.getString(R.string.conexao_dados_desativada), TextToSpeech.QUEUE_FLUSH, null);
	   	    		alertaDadosDesativado();
    			} else if (!getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	   	    		mTts.speak(this.getString(R.string.gps_desativado), TextToSpeech.QUEUE_ADD, null);
	    			alertaGPSDesativado();
    			}
    		}
    		else {
    			getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    			getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    			
    			Location loc = getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
    			if (loc != null) {
    				Gps.setLatitude((Double) loc.getLatitude());
    				Gps.setLongitude((Double) loc.getLongitude());
    			}
    			Intent itVoz = new Intent(this, ReconhecimentoVoz.class);
    			startActivity(itVoz);
    		}
    	}
    }
    
    private void alertaGPSDesativado(){  
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	builder.setTitle(this.getString(R.string.gps_desativado))
    	.setMessage(this.getString(R.string.config_gps))
    	.setCancelable(false)  
    	.setPositiveButton(this.getString(R.string.OK),  
    			new DialogInterface.OnClickListener(){  
    		public void onClick(DialogInterface dialog, int id){  
    			Intent gpsOptionsIntent = new Intent(  
    					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
    			startActivity(gpsOptionsIntent);
    			finish();
    		}  
    	});  
    	AlertDialog alert = builder.create();  
    	alert.show();  
    }
    
    private boolean internetDisponivel() {

        try {

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                Log.d("Blind", "Internet Disponivel:true");
                return true;
            } else {
                Log.d("Blind", "Internet Disponivel:false");
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    
    private void alertaDadosDesativado(){  
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	builder.setTitle(this.getString(R.string.conexao_dados))
    	.setMessage(this.getString(R.string.conexao_dados_desativada))
    	.setCancelable(false)
    	.setNegativeButton(this.getString(R.string.sair),  
    			new DialogInterface.OnClickListener(){  
    		public void onClick(DialogInterface dialog, int id){  
    			finish();
    		}  
    	});  
    	AlertDialog alert = builder.create();  
    	alert.show();  
    }
    
    private LocationManager getLocationManager() {
    	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	return locationManager;
    }

	@Override
	public void onInit(int status) {
    	// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
    	if (status == TextToSpeech.SUCCESS) {
    		//unlock it so that speech will happen 
            waitForInitLock.unlock(); 
    		ttsInitialized = true;
    		iniciarAplicacao();
    	} else {
    		// Initialization failed.
    		Log.e("DesambiguaVoz", "Could not initialize TextToSpeech.");
    	}
		
	}

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location location) {
		Gps.setLatitude((Double) location.getLatitude());
		Gps.setLongitude((Double) location.getLongitude());
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