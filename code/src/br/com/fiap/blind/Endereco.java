package br.com.fiap.blind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

public class Endereco extends Activity {

	private List<Address> address, addressOrigem;
	private ArrayList<String> enderecos;
	private String paramEndereco;
	private static final int DESAMBIGUA_VOZ = 1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        enderecos = new ArrayList<String>();
        Intent it = getIntent();
        if (it != null) {
        	Bundle params = it.getExtras();
        	if (params != null) {
        		paramEndereco = params.getString("endereco");
        	}
        }
        
        Geocoder geocoder = new Geocoder(this);
        try {
        	addressOrigem = geocoder.getFromLocation(Gps.getLatitude(), Gps.getLongitude(), 1);
    		Gps.setEnderecoOrigem(addressOrigem.get(0));
    		
		} catch (IOException e1) {
			Log.e("GeocoderLog", "Deu erro o geo coder origem - " + e1.getMessage());
		}
		
        try {
        	address = geocoder.getFromLocationName(paramEndereco + " - SP", 3);
        	for (int i=0; i < address.size(); i++) {
        		if (address.get(i).getThoroughfare() != null) {
        			String rua = tratarTexto(address.get(i).getThoroughfare());
        			String bairro = address.get(i).getLocality();
        			String uf = address.get(i).getAdminArea();
        			enderecos.add(rua + " - " + bairro + " - " + uf);
        		}
        	}
   	
        	Intent itDesambiguaVoz = new Intent(this, DesambiguaVoz.class);
            itDesambiguaVoz.putExtra("lista", enderecos);
            startActivityForResult(itDesambiguaVoz, DESAMBIGUA_VOZ);
            
		} catch (IOException e) {
			Log.e("GeocoderLog", "Deu erro o geo coder - " + e.getMessage());
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if (requestCode == DESAMBIGUA_VOZ) {
			 if (resultCode == 1) {
				 String endereco = data.getStringExtra("texto");
				 if (endereco != null)
					 if (!endereco.equals("")) {
						 Intent itRota = new Intent(this, MapRouteActivity.class);
						 Gps.setEnderecoDestino(address.get(enderecos.indexOf(endereco)));
				         startActivity(itRota);
				         finish();
					 } else 
						 finish();
				 else
					 finish();
			 }
		 }
	}

	
    private String tratarTexto(String texto) {
		texto = texto.replace("Av. ", "Avenida ");
		texto = texto.replace("R. ", "Rua ");
		texto = texto.replace("Estr. ", "Estrada ");
		texto = texto.replace("Gen. ", "General ");
		return texto;
    }
	
}
