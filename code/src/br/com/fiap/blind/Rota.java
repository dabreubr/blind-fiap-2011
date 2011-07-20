package br.com.fiap.blind;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Rota extends Activity implements Runnable {
	
	private String origem, destino;
	private Handler handler = new Handler();
	private ProgressDialog dialog;
	private static final String URL = "http://goomedia.com.br/sptrans/ServicoRotas.asmx";
	private TextView txtRota;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent it = getIntent();
        if (it != null) {
        	Bundle params = it.getExtras();
        	if (params != null) {
        		destino = params.getString("destino");
        		origem = params.getString("origem");
        	}
        }
        txtRota = new TextView(this);
    }
	
    @Override
    public void onStart() {
    	super.onStart();
    	
    	dialog = ProgressDialog.show(this, "Aguarde", "Calculando rota");
    	
    	new Thread(this).start();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

	@Override
	public void run() {
		try {
			SPTransWS sptrans = new SPTransWS(URL);
			final String rota = sptrans.calcularRota(origem, destino);
			handler.post(new Runnable() {

				@Override
				public void run() {
					tratarResposta(rota);
				}
				
			});
		} catch (Exception e) {
			Log.e("Rota", e.getMessage(), e);
		} finally {
			dialog.dismiss();
		}
	}

	protected void tratarResposta(String rota) {
		Integer quantidadeConducao = new Integer(rota.substring(rota.indexOf("quantidadeConducao=") + 19, rota.indexOf("quantidadeConducao=") + 20));
		String tempoViagem = rota.substring(rota.indexOf("tempoViagem=") + 12, rota.indexOf("tempoViagem=") + 17);
		String valorTotalTarifa = rota.substring(rota.indexOf("valorTotalTarifa=") + 17, rota.indexOf("valorTotalTarifa=") + 24);
		String enderecoFinal = tratarTexto(rota.substring(rota.indexOf("enderecoFinal=") + 14, rota.indexOf("; }")));
		ArrayList<Transporte> transportes = new ArrayList<Transporte>();
		Integer i=1;
		while (quantidadeConducao >= i) {
			Transporte transporte = new Transporte("Ônibus");
			transporte.setEmbarque(tratarTexto(rota.substring(rota.indexOf("enderecoEmbarque" + i + "=") + 18, rota.indexOf(";", rota.indexOf("enderecoEmbarque" + i + "=") + 19))));
			transporte.setDesembarque(tratarTexto(rota.substring(rota.indexOf("enderecoDesembarque" + i + "=") + 21, rota.indexOf(";", rota.indexOf("enderecoDesembarque" + i + "=") + 21))));
			transporte.setLinha(tratarTexto(rota.substring(rota.indexOf("linha" + i + "=") + 7, rota.indexOf(";", rota.indexOf("linha" + i + "=") + 7))));
			transportes.add(transporte);
			i++;
		}
		
		rota = "\n origem: " + origem;
		rota += "\n destino: " + destino;
		rota += "\n quantidadeConducao: " + quantidadeConducao;
		rota += "\n tempoViagem: " + tempoViagem;
		rota += "\n valorTotalTarifa: " + valorTotalTarifa;
		rota += "\n enderecoFinal: " + enderecoFinal;
		rota += "\n transportes: " + transportes;
		
		txtRota.setText("Rota: " + rota);
		txtRota.setVisibility(View.VISIBLE);
		setContentView(txtRota);
		
	}

	private String tratarTexto(String texto) {
		if (texto.endsWith("."))
			texto = texto.substring(0, texto.length()-1);
		if (texto.indexOf("REF.: ") > 0) 
			texto = texto.substring(0, texto.indexOf("REF.: ") - 1);
		texto = texto.replace("AV. ", "AVENIDA ");
		texto = texto.replace("S. ", "SÃO ");
		texto = texto.replace("R. ", "RUA ");
		texto = texto.replace("PCA ", "PRAÇA ");
		texto = texto.replace("PCA. ", "PRAÇA ");
		return texto;
	}

}
