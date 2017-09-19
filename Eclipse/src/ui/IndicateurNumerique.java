package ui;

import javax.swing.JLabel;

public class IndicateurNumerique extends JLabel {
	private static final long serialVersionUID = -3715801319809119957L;
	
	String etiquette;

	public IndicateurNumerique(String mon_etiquette){
		etiquette = mon_etiquette;
		this.setText(etiquette + " : ");
		this.metValeur(0);
	}
	
	public void metValeur(int ma_valeur){
		this.setText(etiquette + " : " + ma_valeur);
	}
}
