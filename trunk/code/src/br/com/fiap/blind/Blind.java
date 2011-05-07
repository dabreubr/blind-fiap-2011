package br.com.fiap.blind;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

public class Blind extends Activity implements LocationListener {

    private TextView txtlatitude = null;
    private TextView txtlongitude = null;
    
	/** Chamado quando a aplicação é iniciada. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        txtlatitude = (TextView) findViewById(R.id.latitude);
        txtlongitude = (TextView) findViewById(R.id.longitude);
        
        if (!getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER))
        	alertaGPSDesativado();
        else {
        	getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        	getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            
        	Intent itVoz = new Intent(this, ReconhecimentoVoz.class);
            startActivity(itVoz);
        }
    }
    
    public void onDestroy() {
    	super.onDestroy();
    	getLocationManager().removeUpdates(this);
    }
    
    private void alertaGPSDesativado(){  
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	builder.setTitle("O GPS está desabilitado")
    	.setMessage("Mostrar configurações de local?")
    	.setCancelable(false)  
    	.setPositiveButton("OK",  
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
    
    

}