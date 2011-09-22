package br.com.fiap.blind;

import java.io.IOException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.Marshal;
import org.ksoap2.serialization.MarshalFloat;
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
	
	public Object retornaPosicaoOnibus(String linha, String onibus){

		try
		{
			SoapObject soap = new SoapObject(nameSpace, metodo);
			soap.addProperty("linha", linha);
			soap.addProperty("onibus", onibus);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(soap);
			Log.i(TAG, "Chamando WebService " + url);
			HttpTransportSE httpTransport = new SPTransHttpTransport(url);
			httpTransport.debug = true;
			httpTransport.call(metodoSOAPAction, envelope);

			// Resultado do método do webservice           
			return envelope.getResponse();                    

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return ex.getMessage();
		}

	}
	
	public String tracarRota(String enderecoOrigem, String enderecoDestino) throws IOException, XmlPullParserException{
		Object rota;
		
		SoapObject soap = new SoapObject(nameSpace, metodo);
		soap.addProperty("enderecoOrigem", enderecoOrigem);
		soap.addProperty("enderecoDestino", enderecoDestino);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		
	    Marshal floatMarshal = new MarshalFloat();
	    floatMarshal.register(envelope);
		
		envelope.setOutputSoapObject(soap);
		Log.i(TAG, "Chamando WebService " + url);
		HttpTransportSE httpTransport = new SPTransHttpTransport(url);
		httpTransport.call(metodoSOAPAction, envelope);
		rota = envelope.getResponse();
		Log.i(TAG, "Rota: " + rota.toString());
		
		return rota.toString();
	}
}
