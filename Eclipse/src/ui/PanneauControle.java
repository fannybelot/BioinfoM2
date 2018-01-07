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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import bioinfo.Orchestreur;
import bioinfo.Organism;
import bioinfo.OrganismHierarchy;


//TODO Ajouter bouton sauvegarder genes et sauvegarder genomes

public class PanneauControle {
	private static Boolean dejaCree = false; //indique si le panneau a été créé
	private static JPanel panneauControle;
	
	private static FournisseurDePause fdp;
	private static Orchestreur orch;
	private static Thread thread_orch;
	
	private static JPanel panneauOptions;
	private static ButtonGroup quoiTraiter;
	private static JPanel panneauLancement;
	private static JButton boutonStart;
	private static JProgressBar barreChargement;
	private static JButton boutonPause;
	private static JButton boutonStop;
	private static JButton boutonHardStop;
	private static JCheckBox genomeSauv;
	private static JCheckBox geneSauv;
	
	
	private static Boolean pause;
	
	public static JPanel obtientPanneauControle(){
		if(!dejaCree){
			InterfaceUtilisateur.journalise("DEBUG", "obtientPanneauControle appelée alors que le panneau n'a pas été initialisé.");
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
	 * Cette fonction doit être appelée par "construitInterface"
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
		
		geneSauv = new JCheckBox("Sauvegarder les gènes");
		genomeSauv = new JCheckBox("Sauvegarder les génomes");
		
		//mise en place des alignements
		toutTraiter.setAlignmentX(Component.LEFT_ALIGNMENT);
		traiterPasTraites.setAlignmentX(Component.LEFT_ALIGNMENT);
		traiterCoches.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		//insertion des options
		panneauOptions.add(toutTraiter);
		panneauOptions.add(traiterPasTraites);
		panneauOptions.add(traiterCoches);
		panneauOptions.add(Box.createVerticalStrut(20));
		panneauOptions.add(geneSauv);
		panneauOptions.add(genomeSauv);
		panneauOptions.add(Box.createVerticalGlue());
		//scrollPane.setPreferredSize(new Dimension(5, 5));
	}

	/**
	 * Crée et configure le panneau de lancement.
	 * Cette fonction doit être appelée par "construitInterface".
	 */
	private static void creePanneauLancement() {
		//création du panneau
		panneauLancement = new JPanel();
		panneauLancement.setLayout(new BorderLayout());
		panneauControle.add(panneauLancement, BorderLayout.PAGE_END);
	
		//insertion de la barre de chargement
		barreChargement = new JProgressBar();
		barreChargement.setMinimum(0);
		barreChargement.setStringPainted(true);
		panneauLancement.add(barreChargement, BorderLayout.SOUTH);
		
		//création d'un conteneur pour le bouton pause et le bouton stop
		JPanel conteneurBoutons = new JPanel();
		conteneurBoutons.setLayout(new BorderLayout());
		panneauLancement.add(conteneurBoutons, BorderLayout.CENTER);

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
					orch = new Orchestreur(fdp, orgas, genomeSauv.isSelected(), geneSauv.isSelected());
					thread_orch = new Thread(orch);
					thread_orch.start();
					
					//met a jour les boutons
					boutonStart.setEnabled(false);
					boutonStop.setEnabled(true);
					boutonPause.setEnabled(true);
					boutonHardStop.setEnabled(true);
				}
			});
		conteneurBoutons.add(boutonStart, BorderLayout.NORTH);
	
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
					boutonHardStop.setEnabled(true);
				}
				else {
					fdp.pause();
					pause = true;
					
					//met a jour les boutons
					boutonPause.setText("Reprendre");
					boutonStop.setEnabled(false);
					boutonPause.setEnabled(true);
					boutonStart.setEnabled(false);
					boutonHardStop.setEnabled(false);
				}
			}
		});
		//conteneurBoutons.add(boutonPause, BorderLayout.CENTER);
	
		//insertion du bouton Stop
		boutonStop = new JButton("Stop");
		boutonStop.setEnabled(false);
		boutonStop.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					orch.killWorkers();
					InterfaceUtilisateur.journalise("INFO", "Abandon des traitements");
					
					//met a jour les boutons
					boutonStart.setEnabled(true);
					boutonStop.setEnabled(false);
					boutonPause.setEnabled(false);
					boutonHardStop.setEnabled(true);
				}
			});
		//conteneurBoutons.add(boutonStop, BorderLayout.SOUTH);

		//insertion du bouton HardStop
		boutonHardStop = new JButton("Stop");
		boutonHardStop.setEnabled(false);
		boutonHardStop.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					orch.hardKillWorkers();
					InterfaceUtilisateur.journalise("INFO", "Abandon des traitement, attention si une �criture de fichier �tait en cours, ce fichier est peut-�tre corrompu");

					//met a jour les boutons
					boutonStart.setEnabled(true);
					boutonStop.setEnabled(false);
					boutonHardStop.setEnabled(false);
					boutonPause.setEnabled(false);
				}
			});
		conteneurBoutons.add(boutonHardStop, BorderLayout.SOUTH);

		
		//initialise les boutons
		boutonStop.setEnabled(false);
		boutonHardStop.setEnabled(false);
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
