package br.com.fiap.simuladorblind;

public class PosicaoOnibus {
	
	private String idPosicaoOnibus;
	private String linha;
	private String onibus;
	private String coordenadaX;
	private String coordenadaY;
	
	public String getIdPosicaoOnibus() {
		return idPosicaoOnibus;
	}
	public void setIdPosicaoOnibus(String idPosicaoOnibus) {
		this.idPosicaoOnibus = idPosicaoOnibus;
	}
	
	public String getLinha() {
		return linha;
	}
	public void setLinha(String linha) {
		this.linha = linha;
	}
	
	public String getOnibus() {
		return onibus;
	}
	public void setOnibus(String onibus) {
		this.onibus = onibus;
	}
	
	public String getCoordenadaX() {
		return coordenadaX;
	}
	public void setCoordenadaX(String coordenadaX) {
		this.coordenadaX = coordenadaX;
	}
	
	public String getCoordenadaY() {
		return coordenadaY;
	}
	public void setCoordenadaY(String coordenadaY) {
		this.coordenadaY = coordenadaY;
	}
	
	public double getLatitude() {
		return new Double(coordenadaX);
	}
	public double getLongitude() {
		return new Double(coordenadaY);
	}
	
	public PosicaoOnibus() {
		super();
	}
	public PosicaoOnibus(String idPosicaoOnibus, String linha, String onibus,
			String coordenadaX, String coordenadaY) {
		super();
		this.idPosicaoOnibus = idPosicaoOnibus;
		this.linha = linha;
		this.onibus = onibus;
		this.coordenadaX = coordenadaX;
		this.coordenadaY = coordenadaY;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((coordenadaX == null) ? 0 : coordenadaX.hashCode());
		result = prime * result
				+ ((coordenadaY == null) ? 0 : coordenadaY.hashCode());
		result = prime * result
				+ ((idPosicaoOnibus == null) ? 0 : idPosicaoOnibus.hashCode());
		result = prime * result + ((linha == null) ? 0 : linha.hashCode());
		result = prime * result + ((onibus == null) ? 0 : onibus.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PosicaoOnibus other = (PosicaoOnibus) obj;
		if (coordenadaX == null) {
			if (other.coordenadaX != null)
				return false;
		} else if (!coordenadaX.equals(other.coordenadaX))
			return false;
		if (coordenadaY == null) {
			if (other.coordenadaY != null)
				return false;
		} else if (!coordenadaY.equals(other.coordenadaY))
			return false;
		if (idPosicaoOnibus == null) {
			if (other.idPosicaoOnibus != null)
				return false;
		} else if (!idPosicaoOnibus.equals(other.idPosicaoOnibus))
			return false;
		if (linha == null) {
			if (other.linha != null)
				return false;
		} else if (!linha.equals(other.linha))
			return false;
		if (onibus == null) {
			if (other.onibus != null)
				return false;
		} else if (!onibus.equals(other.onibus))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "PosicaoOnibus [coordenadaX=" + coordenadaX + ", coordenadaY="
				+ coordenadaY + ", idPosicaoOnibus=" + idPosicaoOnibus
				+ ", linha=" + linha + ", onibus=" + onibus + "]";
	}
}