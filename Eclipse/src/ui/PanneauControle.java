package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import bioinfo.Orchestreur;
import bioinfo.Organism;
import bioinfo.OrganismHierarchy;

public class PanneauControle {
	private static Boolean dejaCree = false; //indique si le panneau a été créé
	private static JPanel panneauControle;
	
	private static FournisseurDePause fdp;
	private static Orchestreur orch;
	private static Thread thread_orch;
	
	private static JPanel panneauOptions;
	private static ButtonGroup quoiTraiter;
	private static JTextArea canaux;
	private static JPanel panneauLancement;
	private static JButton boutonStart;
	private static JProgressBar barreChargement;
	private static JButton boutonPause;
	private static JButton boutonStop;
	
	private static Boolean pause;
	
	public static JPanel obtientPanneauControle(){
		if(!dejaCree){
			InterfaceUtilisateur.journalise("DEBUG", "obtientPanneauControle appellée alors que le panneau n'a pas été initialisé.");
		}
		return panneauControle;
	}
	
	public static void creePanneauControle(){
		panneauControle = new JPanel();
		fdp = new FournisseurDePause();
		pause = false;
		
		panneauControle.setBorder(BorderFactory.createCompoundBorder(
		                BorderFactory.createTitledBorder("Panneau de contrôle"),
		                BorderFactory.createEmptyBorder(5,5,5,5)));
		panneauControle.setLayout(new BorderLayout());
		
		//création du panneau des options
		creePanneauOptions();
		
		//création du panneau de lancement (2 boutons + une barre de chgmt)
		creePanneauLancement();
		
		dejaCree = true;
	}
	
	/**
	 * Crée et configure le panneau des options.
	 * Cette fonction doit être appellée par "construitInterface"
	 */
	private static void creePanneauOptions(){
		//Création du panneau
		panneauOptions = new JPanel();
		panneauOptions.setLayout(new BoxLayout(panneauOptions, BoxLayout.PAGE_AXIS));
		panneauControle.add(panneauOptions);
		
		//création des options
		JRadioButton toutTraiter = new JRadioButton("Traiter tous les génomes");
		toutTraiter.setActionCommand("toutTraiter");
		JRadioButton traiterPasTraites = new JRadioButton("Traiter les génomes pas encore traités");
		traiterPasTraites.setActionCommand("traiterPasTraites");
		JRadioButton traiterCoches = new JRadioButton("Traiter les génomes cochés");
		traiterCoches.setActionCommand("traiterCoches");
		traiterPasTraites.setSelected(true);
		quoiTraiter = new ButtonGroup();
		quoiTraiter.add(toutTraiter);
		quoiTraiter.add(traiterPasTraites);
		quoiTraiter.add(traiterCoches);
		
		JLabel label = new JLabel("Canaux de journalisation activés :");
		canaux = new JTextArea();
		JScrollPane panneauDefilementCanaux = new JScrollPane(canaux); 
		canaux.setEditable(true);
		canaux.append("debug\ninfo\nwarning\nerror");
		
		//mise en place des alignements
		toutTraiter.setAlignmentX(Component.LEFT_ALIGNMENT);
		traiterPasTraites.setAlignmentX(Component.LEFT_ALIGNMENT);
		traiterCoches.setAlignmentX(Component.LEFT_ALIGNMENT);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		panneauDefilementCanaux.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		//insertion des options
		panneauOptions.add(toutTraiter);
		panneauOptions.add(traiterPasTraites);
		panneauOptions.add(traiterCoches);
		panneauOptions.add(label);
		panneauOptions.add(panneauDefilementCanaux);
		panneauOptions.add(Box.createVerticalGlue());
		//scrollPane.setPreferredSize(new Dimension(5, 5));
	}
	
	/**
	 * Renvoie Vrai si le canal passé en paramètre doit être affiché.
	 * @param canal
	 * @return
	 */
	public static Boolean estCanalAutorise(String canal){
		return canaux.getText().toLowerCase().contains(canal.toLowerCase());
	}
	
	/**
	 * Crée et configure le panneau de lancement.
	 * Cette fonction doit être appellée par "construitInterface".
	 */
	private static void creePanneauLancement() {
		//création du panneau
		panneauLancement = new JPanel();
		panneauLancement.setLayout(new BorderLayout());
		panneauControle.add(panneauLancement, BorderLayout.PAGE_END);
	
		//insertion du bouton Start
		boutonStart = new JButton("Start");
		boutonStart.addActionListener(new ActionListener()
			{
				Vector<Organism> orgas = null;
				public void actionPerformed(ActionEvent e)
				{
					orgas = null;
					InterfaceUtilisateur.reset();
					switch(quoiTraiter.getSelection().getActionCommand()){
						case "toutTraiter":
							orgas = OrganismHierarchy.getAsList();
							if(orgas == null){
								InterfaceUtilisateur.journalise("ERROR", "Vous devez d'abord obtenir la liste des organismes");
								return;
							}
							if(orgas.size() == 0){
								InterfaceUtilisateur.journalise("ERROR", "Il n'y a pas d'organisme dans la liste.");
								return;
							}
							break;
						case "traiterPasTraites":
							orgas = OrganismHierarchy.getAsList();
							PanneauHierarchie.getListAlreadyTreated()
								.forEach(str -> orgas.removeIf(orga -> orga.getName().equals(str)));
							if(orgas.size() == 0){
								InterfaceUtilisateur.journalise("ERROR", "Il n'y a pas d'organisme pas traités.");
								return;
							}
							break;
						case "traiterCoches":
							orgas = PanneauHierarchie.obtientOrganismesCoches();
							if(orgas == null){
								InterfaceUtilisateur.journalise("ERROR", "Vous devez sélectionner au moins 1 organisme avant de lancer le traitement.");
								return;
							}
							if(orgas.size() == 0){
								InterfaceUtilisateur.journalise("ERROR", "Vous devez sélectionner au moins 1 organisme avant de lancer le traitement.");
								return;
							}
							break;
						default: 
							InterfaceUtilisateur.journalise("ERROR", "Option pas reconnue");
							return;
					}
					
					if(orgas == null) return;

					InterfaceUtilisateur.donneNbGenomeTotal(orgas.size());
					orch = new Orchestreur(fdp, orgas);
					thread_orch = new Thread(orch);
					thread_orch.start();
					
					//met a jour les boutons
					boutonStart.setEnabled(false);
					boutonStop.setEnabled(true);
					boutonPause.setEnabled(true);
				}
			});
		panneauLancement.add(boutonStart, BorderLayout.WEST);
	
		//insertion de la barre de chargement
		barreChargement = new JProgressBar();
		barreChargement.setMinimum(0);
		barreChargement.setStringPainted(true);
		panneauLancement.add(barreChargement, BorderLayout.CENTER);
		
		//création d'un conteneur pour le bouton pause et le bouton stop
		JPanel conteneurBoutons = new JPanel();
		conteneurBoutons.setLayout(new BorderLayout());
		panneauLancement.add(conteneurBoutons, BorderLayout.EAST);

		//insertion du bouton pause
		boutonPause = new JButton("Pause");
		boutonPause.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(pause){
					fdp.reprend();
					pause = false;
					
					//met a jour les boutons
					boutonPause.setText("Pause");
					boutonStart.setEnabled(false);
					boutonStop.setEnabled(true);
					boutonPause.setEnabled(true);
				}
				else {
					fdp.pause();
					pause = true;
					
					//met a jour les boutons
					boutonPause.setText("Reprendre");
					boutonStop.setEnabled(false);
					boutonPause.setEnabled(true);
					boutonStart.setEnabled(false);
				}
			}
		});
		//conteneurBoutons.add(boutonPause, BorderLayout.WEST);
	
		//insertion du bouton Stop
		boutonStop = new JButton("Stop");
		boutonStop.setEnabled(false);
		boutonStop.addActionListener(new ActionListener()
			{
				@SuppressWarnings("deprecation")
				public void actionPerformed(ActionEvent e)
				{
					orch.killWorkers();
					thread_orch.interrupt(); //does not work
					thread_orch.stop();
					InterfaceUtilisateur.journalise("INFO", "Abandon des traitements");
					
					//met a jour les boutons
					boutonStart.setEnabled(true);
					boutonStop.setEnabled(false);
					boutonPause.setEnabled(false);
				}
			});
		conteneurBoutons.add(boutonStop, BorderLayout.EAST);
		
		//initialise les boutons
		boutonStop.setEnabled(false);
		boutonPause.setEnabled(false);
	}
	
	public static void toutFiniCallback(){
		//met a jour les boutons
		boutonStart.setEnabled(true);
		boutonStop.setEnabled(false);
		boutonPause.setEnabled(false);
	}
	
	public static JProgressBar obtientBarre(){
		return barreChargement;
	}
	
	public static void blockStart(){
		boutonStart.setEnabled(false);
	}
	
	public static void unblockStart(){
		boutonStart.setEnabled(true);
	}
	
	/**
	 * Permet d'implémenter la fonction pause/reprend.
	 * L'UI instancie cette objet et le donne a l'orchestrateur au
	 * moment d'instancier ce dernier. À interval régulier,
	 * l'orchestrateur appelle "pointDePause()" pour permettre
	 * de se pauser si nécéssaire. Quand l'UI souhaite pauser le
	 * traitement en cours, il appelle "pause()". Pour reprendre, il
	 * appelle "reprend()".
	 * @author sindarus
	 */
	public static class FournisseurDePause {
	    private boolean besoinDePause;

	    public synchronized void pointDePause() throws InterruptedException {
	        while (besoinDePause) {
	            wait();
	        }
	    }

	    public synchronized void pause() {
	    	besoinDePause = true;
	    }

	    public synchronized void reprend() {
	    	besoinDePause = false;
	        this.notifyAll();
	    }
	}
}
