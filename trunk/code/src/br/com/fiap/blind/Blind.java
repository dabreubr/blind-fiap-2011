package br.com.fiap.blind;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class Blind extends Activity {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private ListView mList = null;
    private Button btnFechar = null;
    private TextView txtlatitude = null;
    private TextView txtlongitude = null;
    private Gps gps = null;
    private ReconhecimentoVoz voz = null;
	private ArrayList<String> enderecos;    
    
	/** Chamado quando a aplicação é iniciada. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(br.com.fiap.blind.R.layout.main);
        
        gps = new Gps((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
        voz = new ReconhecimentoVoz();
        
        txtlatitude = (TextView) findViewById(R.id.latitude);
        txtlongitude = (TextView) findViewById(R.id.longitude);
        mList = (ListView) findViewById(R.id.list);
        btnFechar = (Button) findViewById(R.id.btnFechar);
        btnFechar.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		//gps.pararGps();
        		finish();
        	}
        });
        
        if (voz.pacoteInstalado(getPackageManager())) {
        	//voz.iniciarReconhecimentoVoz(mList);
        	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga o endereço");
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } else {
            alertaSemReconhecimentoVoz();
        }
        
  /*      if (gps.gpsAtivado()) {
        	gps.obterLocalizacao();
        	txtlatitude.setText(gps.getLatitude().toString());
        	txtlongitude.setText(gps.getLongitude().toString());
        }
        else
        	alertaGPSDesativado();
*/
    }
    
    /**
     * Trata os dados de reconhecimento de voz 
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
        	enderecos = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
            		enderecos));
        }
 
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void alertaGPSDesativado(){  
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	builder.setMessage("Seu GPS está desativado. Gostaria de ativá-lo agora?")  
    	.setCancelable(false)  
    	.setPositiveButton("Ativar GPS",  
    			new DialogInterface.OnClickListener(){  
    		public void onClick(DialogInterface dialog, int id){  
    			Intent gpsOptionsIntent = new Intent(  
    					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
    			startActivity(gpsOptionsIntent);  
    		}  
    	});  
    	builder.setNegativeButton("Não fazer nada",  
    			new DialogInterface.OnClickListener(){  
    		public void onClick(DialogInterface dialog, int id){  
    			dialog.cancel();  
    		}  
    	});  
    	AlertDialog alert = builder.create();  
    	alert.show();  
    }
    
    private void alertaSemReconhecimentoVoz(){  
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	builder.setMessage("Por favor instale o reconhecimento de voz.");
    	builder.setNegativeButton("Sair",  
    			new DialogInterface.OnClickListener(){  
    		public void onClick(DialogInterface dialog, int id){  
    			dialog.cancel();
    		}  
    	});  
    	AlertDialog alert = builder.create();  
    	alert.show();  
    }

}