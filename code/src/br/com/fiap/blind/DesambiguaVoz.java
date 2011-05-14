package br.com.fiap.blind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class DesambiguaVoz extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

	private TextToSpeech mTts;
	private boolean ttsInitialized;
	private ArrayList<String> lista;
	private static final int DESAMBIGUAVOZ = 12;
	private String resultado = "";
	private boolean pararPerguntar = false;
	private ReentrantLock waitForInitLock = new ReentrantLock();
	private Iterator<String> itLista;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        ttsInitialized = false;
        
        lista = new ArrayList<String>();
        Intent it = getIntent();
        if (it != null) {
        	Bundle params = it.getExtras();
        	if (params != null) {
        		lista = params.getStringArrayList("lista");
        	}
        }
        itLista = lista.iterator();
        
        // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        mTts = new TextToSpeech(this,
            this  // TextToSpeech.OnInitListener
            );
        //don't do speak until initing 
        waitForInitLock.lock(); 
    }
    
    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }

        super.onDestroy();
    }

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
    	// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
    	if (status == TextToSpeech.SUCCESS) {
    		//unlock it so that speech will happen 
            waitForInitLock.unlock(); 
    		ttsInitialized = true;
    		if (itLista.hasNext()) {
    			desambiguarVoz(itLista.next());
    		}
       		
    	} else {
    		// Initialization failed.
    		Log.e("DesambiguaVoz", "Could not initialize TextToSpeech.");
    	}
    }

    private void desambiguarVoz(String texto) {
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
    		mTts.speak("Você quis dizer?", TextToSpeech.QUEUE_FLUSH, null);
    		HashMap<String, String> myHashText = new HashMap<String, String>();
    		myHashText.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Vocedisse");
    		resultado = texto;
    		mTts.speak(texto, TextToSpeech.QUEUE_ADD, myHashText);
    		try {
    			Thread.sleep(10000);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	} else {
    		Log.e("DesambiguaVoz", "TextToSpeech not initialized");
    	}

    }
    
    @Override
	public void onUtteranceCompleted(String utteranceId) {
	    if (utteranceId.equals("Vocedisse")) {
        	
	    	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, resultado);
            startActivityForResult(intent, DESAMBIGUAVOZ);
	    }
		
	}

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DESAMBIGUAVOZ && resultCode == RESULT_OK) {
        	ArrayList<String> listaResp;
        	
			listaResp = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
			
			Iterator<String> it = listaResp.iterator();
			
			while (it.hasNext()) {
				String resposta = (String) it.next();
				if (resposta.equalsIgnoreCase("sim")) {
					pararPerguntar = true;
					break;
				}
			}
			
			if (pararPerguntar == false) {
				if (itLista.hasNext()) {
					desambiguarVoz(itLista.next());
				}
			}
			
			Intent itVoltar = new Intent();
	    	itVoltar.putExtra("texto", resultado);
	    	setResult(1, itVoltar);
	    	finish();
		}
	}
	
	
}
