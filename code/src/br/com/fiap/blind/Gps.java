package br.com.fiap.blind;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class Gps {

	private LocationManager locationManager;
	private LocationListener locationListener;
	private Double latitude;
	private Double longitude;
	
	public LocationManager getLocationManager() {
		return locationManager;
	}
	public void setLocationManager(LocationManager locationManager) {
		this.locationManager = locationManager;
	}
	public LocationListener getLocationListener() {
		return locationListener;
	}
	public void setLocationListener(LocationListener locationListener) {
		this.locationListener = locationListener;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * Criar gps e definir o gerenciador de localiza��o
	 * @param locationManager
	 */
	public Gps(LocationManager locationManager) {
        // Adquire referencia ao sistema de gerenciamento de localiza��o
    	this.locationManager = locationManager;
	}
	
	/**
	 * Gps esta ativo
	 * @return true se gps ativado, false se gps desativado
	 */
	public boolean gpsAtivado() {
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))  
			return false;
		else
			return true;
	}
	
	/**
	 * parar o Gps
	 */
	public void pararGps() {
		locationManager.removeUpdates(locationListener);
	}
	
	/**
	 * Obtem localiza��o do usuario
	 */
	public void obterLocalizacao() {

		// Define um ouvidor que recebe as atualiza��es de localiza��o
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Chamado quando uma localiza��o � encontrada
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

		// Registra o ouvidor com o gerenciamento de localiza��o para receber as atualiza��es de localiza��o
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
   
}
