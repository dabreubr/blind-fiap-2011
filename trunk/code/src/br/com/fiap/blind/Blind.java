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
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

public class Blind extends Activity implements LocationListener, TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener  {

    private TextView txtlatitude = null;
    private TextView txtlongitude = null;
    private TextToSpeech mTts;
    private ReentrantLock waitForInitLock = new ReentrantLock();
    private boolean ttsInitialized = false;
    
	/** Chamado quando a aplicação é iniciada. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        txtlatitude = (TextView) findViewById(R.id.latitude);
        txtlongitude = (TextView) findViewById(R.id.longitude);
     
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
   	    		mTts.speak(this.getString(R.string.gps_desativado), TextToSpeech.QUEUE_FLUSH, null);
    			alertaGPSDesativado();
    		}
    		else {
    			getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    			getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    			Intent itVoz = new Intent(this, ReconhecimentoVoz.class);
    			startActivity(itVoz);
    		}
    	}
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	getLocationManager().removeUpdates(this);
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
    
    private LocationManager getLocationManager() {
    	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	return locationManager;
    }

	@Override
	public void onLocationChanged(Location location) {
		txtlatitude.setText(((Double) location.getLatitude()).toString());
		txtlongitude.setText(((Double) location.getLongitude()).toString());
		
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
    
    

}