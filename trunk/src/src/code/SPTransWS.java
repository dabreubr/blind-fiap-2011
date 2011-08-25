package src.code;

import java.io.IOException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Log;

public class SPTransWS {
	private final String TAG = "SPTransWS";
	private final String url;
	private final String nameSpace = "http://tempuri.org/";
	private final String metodo;
	private final String metodoSOAPAction;
	
	public SPTransWS(String url, String metodo) {
		this.url = url;
		this.metodo = metodo;
		this.metodoSOAPAction = "http://tempuri.org/" + metodo;
	}

	public String inserePosicaoOnibus(String linha, String onibus, String coordenadaX, String coordenadaY) throws IOException, XmlPullParserException
	{
		Object rota;
		
		SoapObject soap = new SoapObject(nameSpace, metodo);
		soap.addProperty("linha", linha);
		soap.addProperty("onibus", onibus);
		soap.addProperty("coordenadaX", coordenadaX);
		soap.addProperty("coordenadaY", coordenadaY);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(soap);
		Log.i(TAG, "Chamando WebService " + url);
		HttpTransportSE httpTransport = new HttpTransportSE(url);
		httpTransport.call(metodoSOAPAction, envelope);
		rota = envelope.getResponse();
		Log.i(TAG, "Retorno: " + rota.toString());
		
		return rota.toString();
	}

	public String limparPosicaoOnibus() throws IOException, XmlPullParserException{
		Object rota;
		
		SoapObject soap = new SoapObject(nameSpace, metodo);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(soap);
		Log.i(TAG, "Chamando WebService " + url);
		HttpTransportSE httpTransport = new SPTransHttpTransport(url);
		httpTransport.call(metodoSOAPAction, envelope);
		rota = envelope.getResponse();
		Log.i(TAG, "Retorno: " + rota.toString());
		
		return rota.toString();
	}

	public String retornaPosicaoOnibus(String linha, String onibus) throws IOException, XmlPullParserException{
		Object rota;
		
		SoapObject soap = new SoapObject(nameSpace, metodo);
		soap.addProperty("linha", linha);
		soap.addProperty("onibus", onibus);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(soap);
		Log.i(TAG, "Chamando WebService " + url);
		HttpTransportSE httpTransport = new SPTransHttpTransport(url);
		httpTransport.call(metodoSOAPAction, envelope);
		rota = envelope.getResponse();
		Log.i(TAG, "Retorno: " + rota.toString());
		
		return rota.toString();
	}
	
	public String tracarRota(String enderecoOrigem, String enderecoDestino) throws IOException, XmlPullParserException{
		Object rota;
		
		SoapObject soap = new SoapObject(nameSpace, metodo);
		soap.addProperty("enderecoOrigem", enderecoOrigem);
		soap.addProperty("enderecoDestino", enderecoDestino);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(soap);
		Log.i(TAG, "Chamando WebService " + url);
		HttpTransportSE httpTransport = new SPTransHttpTransport(url);
		httpTransport.call(metodoSOAPAction, envelope);
		rota = envelope.getResponse();
		Log.i(TAG, "Rota: " + rota.toString());
		
		return rota.toString();
	}
}
