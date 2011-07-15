package br.com.fiap.blind;

public class Transporte {
	
	private String tipo;
	private String embarque;
	private String desembarque;
	private String linha;
	
	public Transporte(String tipo) {
		this.tipo = tipo;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getEmbarque() {
		return embarque;
	}
	public void setEmbarque(String embarque) {
		this.embarque = embarque;
	}
	public String getDesembarque() {
		return desembarque;
	}
	public void setDesembarque(String desembarque) {
		this.desembarque = desembarque;
	}
	public String getLinha() {
		return linha;
	}
	public void setLinha(String linha) {
		this.linha = linha;
	}
	@Override
	public String toString() {
		return "Transporte [tipo=" + tipo + "\n linha=" 
		+ linha + "\n embarque=" 
		+ embarque + "\n desembarque="
		+ desembarque + "]";
	}
	
	

}
