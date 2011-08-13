package br.com.fiap.blind;

import com.google.android.maps.GeoPoint;

import android.location.Address;

/**
 * Classe global com a localização do usuário
 *
 */
final public class Gps {

	private static Double latitude;
	private static Double longitude;
	private static Address enderecoOrigem;
	private static Address enderecoDestino;
	
	public static Double getLatitude() {
		return latitude;
	}
	public static void setLatitude(Double paramLatitude) {
		latitude = paramLatitude;
	}
	public static Double getLongitude() {
		return longitude;
	}
	public static void setLongitude(Double paramLongitude) {
		longitude = paramLongitude;
	}
	public static Address getEnderecoOrigem() {
		return enderecoOrigem;
	}
	public static void setEnderecoOrigem(Address enderecoOrigem) {
		Gps.enderecoOrigem = enderecoOrigem;
	}
	public static Address getEnderecoDestino() {
		return enderecoDestino;
	}
	public static void setEnderecoDestino(Address enderecoDestino) {
		Gps.enderecoDestino = enderecoDestino;
	}
	
	public static String getStrEnderecoDestino() {
		String rua = enderecoDestino.getThoroughfare();
		String bairro = enderecoDestino.getLocality();
		String uf = enderecoDestino.getAdminArea();
		return rua + " - " + bairro + " - " + uf;
	}
	
	public static String getStrEnderecoOrigem() {
		String rua = enderecoOrigem.getThoroughfare();
		String bairro = enderecoOrigem.getLocality();
		String uf = enderecoOrigem.getAdminArea();
		return rua + " - " + bairro + " - " + uf;
	}

	public static GeoPoint getPonto() {
		int moveToLat = (int) (Gps.getLatitude()*1E6);
		int moveToLong = (int) (Gps.getLongitude()*1E6);
		return new GeoPoint(moveToLat, moveToLong);
	}
}
