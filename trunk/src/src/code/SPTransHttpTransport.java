package src.code;

import java.io.IOException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Log;

public class SPTransHttpTransport extends HttpTransportSE {
	
	private static final String TAG = "SPTransHttpTransport";
	
	public SPTransHttpTransport(String s) {
		super(s);
	}
	
	@Override
	public void call(String s, SoapEnvelope soapenvelope) throws IOException, XmlPullParserException {
		// Apenas para logar o xml elemento envelope do SOAP
		byte bytes[] = createRequestData(soapenvelope);
		String envelope = new String(bytes);
		Log.i(TAG, "Envelope: " + envelope);
		super.call(s, soapenvelope);
	}

}
