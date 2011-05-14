package br.com.fiap.blind;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ReconhecimentoVoz extends Activity {

	private ArrayList<String> lista;
	private static int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private ListView mList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		
		mList = (ListView) findViewById(R.id.list);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		if (pacoteInstalado(getPackageManager())) {
        	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga o endereço");
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } else {
            alertaSemReconhecimentoVoz();
        }

   }
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
        	lista = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
            		lista));
            
            
            
        	Intent itEndereco = new Intent(this, Endereco.class);
            itEndereco.putExtra("endereco", "Avenida Paulista");
            startActivity(itEndereco);
        }
 
    }
	
	private void alertaSemReconhecimentoVoz(){  
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	builder.setMessage("Por favor instale o reconhecimento de voz.");
    	builder.setNegativeButton("Sair",  
    			new DialogInterface.OnClickListener(){  
    		public void onClick(DialogInterface dialog, int id){  
    			finish();
    		}  
    	});  
    	AlertDialog alert = builder.create();  
    	alert.show();  
    }
  
    /**
     * Verifica se possui o pacote de reconhecimento de voz instalado
     * @return se true instalado, se false não instalado
     */
    public boolean pacoteInstalado(PackageManager pm) {
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0)
        	return true;
        else 
        	return false;
    }
}
