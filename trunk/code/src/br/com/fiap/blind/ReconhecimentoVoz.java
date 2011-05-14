package br.com.fiap.blind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ReconhecimentoVoz extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

	private ArrayList<String> lista;
	private static int VOICE_RECOGNITION_REQUEST_CODE = 1;
	private static int DESAMBIGUA_VOZ = 2;
	private ListView mList;
	private TextToSpeech mTts;
	private boolean ttsInitialized;
	private ReentrantLock waitForInitLock = new ReentrantLock();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
		mList = (ListView) findViewById(R.id.list);
		
        ttsInitialized = false;
		mTts = new TextToSpeech(this, this);
        waitForInitLock.lock();
        
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
        	
            // Fill the list view with the strings the recognizer thought it could have heard
        	lista = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,lista));
            
            Intent itDesambigua = new Intent(this, DesambiguaVoz.class);
            itDesambigua.putExtra("lista", lista);
            startActivityForResult(itDesambigua, DESAMBIGUA_VOZ);
            
        }
		if (requestCode == DESAMBIGUA_VOZ) {
			
			 if (resultCode == 1) {
				 
				String endereco = data.getStringExtra("texto");
	            Intent itEndereco = new Intent(this, Endereco.class);
	            itEndereco.putExtra("endereco", endereco);
	            startActivity(itEndereco);
			 }
			 
		 }
 
        super.onActivityResult(requestCode, resultCode, data);
        
    }
    
    public void onInit(int status) {
    	// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
    	
    	if (status == TextToSpeech.SUCCESS) {
    		
    		//unlock it so that speech will happen 
            waitForInitLock.unlock(); 
    		ttsInitialized = true;
    		
    		if (waitForInitLock.isLocked()) 
        	{ 
        		try 
        		{ 
        			waitForInitLock.tryLock(180, TimeUnit.SECONDS); 
        		} 
        		catch (InterruptedException e) 
        		{ 
        			Log.e("speaker", "interruped"); 
        		} 
        		//unlock it here so that it is never locked again 
        		waitForInitLock.unlock(); 
        	} 
        	if (ttsInitialized) {
        		int result = mTts.setOnUtteranceCompletedListener(this); 
        		if (result == TextToSpeech.ERROR) 
        		{ 
        			Log.e("speaker", "failed to add utterance listener"); 
        		} 

        		HashMap<String, String> myHashText = new HashMap<String, String>();
        		myHashText.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "DigaEndereco");
        		mTts.speak("Diga o endereço de destino", TextToSpeech.QUEUE_FLUSH, myHashText);
        		
        		try {
        			Thread.sleep(10000);
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
        	} else {
        		Log.e("ReconhecimentoVoz", "TextToSpeech not initialized");
        	}
    		
    		
    	} else {
    		// Initialization failed.
    		Log.e("ReconhecimentoVoz", "Could not initialize TextToSpeech.");
    	}
    }
    
    @Override
	public void onUtteranceCompleted(String utteranceId) {
    	if (utteranceId.equals("DigaEndereco")) {
    		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga o endereço de destino");
    		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    	}
	}
}
