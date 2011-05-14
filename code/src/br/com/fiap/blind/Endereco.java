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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Endereco extends Activity {

    private ListView mList = null;
	private List<Address> address;
	private ArrayList<String> enderecos;
	private String paramEndereco;
	private static final int DESAMBIGUA_VOZ = 1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        
        mList = (ListView) findViewById(R.id.list);
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
        	address = geocoder.getFromLocationName(paramEndereco, 3);
        	for (int i=0; i < address.size(); i++) {
        		String rua = address.get(i).getThoroughfare();
        		String bairro = address.get(i).getLocality();
        		String uf = address.get(i).getAdminArea();
        		enderecos.add(rua + " - " + bairro + " - " + uf);
        	}
   	
            mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
            		enderecos));
            
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
				 TextView txtEndereco = new TextView(this);
				 txtEndereco.setText(endereco);
				 setContentView(txtEndereco);
			 }
		 }
	}

}
