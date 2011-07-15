package br.com.fiap.blind;

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
	private final String metodo = "TracarRota";
	private final String metodoSOAPAction = "http://tempuri.org/TracarRota";
	
	public SPTransWS(String url) {
		this.url = url;
	}
	
	public String calcularRota(String origem, String destino) throws IOException, XmlPullParserException{
		Object rota;
		
		SoapObject soap = new SoapObject(nameSpace, metodo);
		soap.addProperty("enderecoOrigem", origem);
		soap.addProperty("enderecoDestino", destino);
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
