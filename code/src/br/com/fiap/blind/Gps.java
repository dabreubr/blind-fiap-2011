package br.com.fiap.blind;
/**
 * Classe global com a localização do usuário
 *
 */
final public class Gps {

	private static Double latitude;
	private static Double longitude;
	
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
	
	
}
