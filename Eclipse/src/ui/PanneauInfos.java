package ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class PanneauInfos extends JPanel{
	private static final long serialVersionUID = -9177852484092098397L;
	
	private IndicateurNumerique indicNbGenomeTotal;
	private IndicateurNumerique indicNbGenomeTraites;
	private IndicateurNumerique indicNbGenomeReussis;
	private IndicateurNumerique indicNbGenomeRates;
	
	public PanneauInfos()
	{
	    this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Panneau d'informations"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		indicNbGenomeTotal = new IndicateurNumerique("Nombre de génomes à traiter");
		indicNbGenomeTraites = new IndicateurNumerique("Génomes traités");
		indicNbGenomeReussis = new IndicateurNumerique("Génomes réussis");
		indicNbGenomeRates = new IndicateurNumerique("Génomes ratés");
		this.add(indicNbGenomeTotal);
		this.add(indicNbGenomeTraites);
		this.add(indicNbGenomeReussis);
		this.add(indicNbGenomeRates);
	}
	
	public void metNbGenomeTotal(int nb){
		indicNbGenomeTotal.metValeur(nb);
	}
	
	public void metNbGenomeTraites(int nb){
		indicNbGenomeTraites.metValeur(nb);
	}
	
	public void metNbGenomeReussis(int nb){
		indicNbGenomeReussis.metValeur(nb);
	}
	
	public void metNbGenomeRates(int nb){
		indicNbGenomeRates.metValeur(nb);
	}
}
