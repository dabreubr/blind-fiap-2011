package br.com.fiap.blind;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.widget.ListView;

public class ReconhecimentoVoz {

	private ArrayList<String> enderecos;
	
	public ReconhecimentoVoz() {
		super();
	}
	
    public ArrayList<String> getEnderecos() {
		return enderecos;
	}
	public void setEnderecos(ArrayList<String> enderecos) {
		this.enderecos = enderecos;
	}

	/**
     * Inicia o reconhecimento de voz
     */
    public void iniciarReconhecimentoVoz(ListView mList) {
		
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
