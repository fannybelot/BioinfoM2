package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import bioinfo.Organism;
import bioinfo.OrganismHierarchy;
import net.Download;
import net.schema.Primary;
import ui.ConstructeurDeHierarchie.ExceptionFichierListeGenomeAbsent;

public class PanneauHierarchie {
	private static JPanel panneauHierarchie;
	private static CheckBoxJTree arbreHierarchie;
	private static JScrollPane hierarchieScroll;
	private static JPanel panneauChargement;
	private static JProgressBar barreChargementDonnees;
	private static Thread downloadListThread;
	
	public static JPanel getPanneauHierarchie(){
		return panneauHierarchie;
	}
	
	/**
     * Crée le panneau de la hierarchie avec un arbre vide
     */
    public static void creePanneauHierarchie()
    {
    	//Création du panneau
    	panneauHierarchie = new JPanel();
		panneauHierarchie.setBackground(Color.blue);
		panneauHierarchie.setLayout(new BorderLayout());
		
    	arbreHierarchie = new CheckBoxJTree(new CheckBoxTreeNode(new CheckBoxNode("Organismes disponibles à l'analyse", false)));
    	panneauHierarchie.add(arbreHierarchie);
    	
        //ajout de la barre de scroll
        hierarchieScroll = new JScrollPane(arbreHierarchie ,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		panneauHierarchie.add(hierarchieScroll, BorderLayout.CENTER);
    }
    
    private static void afficheBarreTelechargement() {
    	panneauChargement = new JPanel();
    	panneauChargement.setLayout(new BorderLayout());
    	panneauHierarchie.add(panneauChargement, BorderLayout.SOUTH);
    	
    	JPanel panneauChargementDonnees = new JPanel();
    	panneauChargementDonnees.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Téléchargement de la liste"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
    	panneauChargement.add(panneauChargementDonnees);
    	panneauChargementDonnees.setLayout(new BorderLayout());
    	
		barreChargementDonnees = new JProgressBar();
		barreChargementDonnees.setMinimum(0);
		barreChargementDonnees.setStringPainted(true);
		panneauChargementDonnees.add(barreChargementDonnees, BorderLayout.CENTER);
		
		JButton boutonStop = new JButton("Stop");
		boutonStop.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent e) {
				InterfaceUtilisateur.journalise("INFO", "Abandon de la mise a jour de la liste des organismes");
				Download.killDownloadThreads();
				downloadListThread.interrupt();
				downloadListThread.stop();
				supprimeBarreTelechargement();
				PanneauControle.unblockStart();
			}
		});
		panneauChargementDonnees.add(boutonStop, BorderLayout.EAST);
		
		panneauHierarchie.validate();
		panneauHierarchie.repaint();
    }
    
    public static void metNombrePageMaximum(int n) {
    	barreChargementDonnees.setMaximum(n);
    }
    
    public static void incrementeNombreDePageTraites(){
    	barreChargementDonnees.setValue(barreChargementDonnees.getValue() + 1);
    	if (barreChargementDonnees.getPercentComplete() == 1){
    		barreChargementDonnees.setValue(barreChargementDonnees.getMaximum() - 1);
    	}
    }
    
    private static void supprimeBarreTelechargement() {
    	panneauHierarchie.remove(panneauChargement);
		panneauHierarchie.validate();
		panneauHierarchie.repaint();
    }
    
    public static void organismListDownloadEnd(Vector<Organism> list){
    	majHierarchieListeOrganismes(list);
    	//supprimeBarreTelechargement();
    	PanneauControle.unblockStart();
    	return;
    }
    
    public static void majHierarchieListeOrganismes(Vector<Organism> listeOrgas){
    	//mise a jour de la hierarchie
    	OrganismHierarchy.createHierarchy(listeOrgas);
    	
    	//construction du JTree
    	arbreHierarchie = new CheckBoxJTree(
    						new CheckBoxTreeNode( OrganismHierarchy.getHierarchyRoot())
    					  );
    	
    	//Blink le panneau Scroll de la hierarchie
    	panneauHierarchie.remove(hierarchieScroll);
        hierarchieScroll = new JScrollPane(arbreHierarchie ,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		panneauHierarchie.add(hierarchieScroll, BorderLayout.CENTER);

		//refresh le panneau
    	panneauHierarchie.revalidate();
    	panneauHierarchie.repaint();
    }
    
    public static Vector<Organism> obtientOrganismesCoches(){
    	if(! OrganismHierarchy.isCreated()){
    		InterfaceUtilisateur.journalise("ERROR", "Vous devez obtenir la hierarchie des organismes avant de commencer le traitement");
    		return null;
    	}
    	
    	Vector<Organism> listeOrga = new Vector<Organism>();
    	Vector<String> nameCheckedNodes = arbreHierarchie.getCheckedNodes();
    	Organism curOrga;
    	for(String curOrgaName : nameCheckedNodes){
    		curOrga = OrganismHierarchy.findOrganismByName(curOrgaName);
    		if(curOrga == null){
    			//Checked node was a category.
    		}
    		else{
    			listeOrga.add(curOrga);
    		}
    	}
    	
		return listeOrga;
    }
    
    public static void setOrganismColor(String orgaName, Color color){
    	System.out.println(orgaName);
    	arbreHierarchie.setNodeColor(orgaName, color);
    }
    
    public static void updateColorsAlreadyTreated(){
    	ArrayList<String> list = getListAlreadyTreated();
    	for(String elt : list){
    		arbreHierarchie.setNodeColor(elt, InterfaceUtilisateur.getTreatedColor());
    	}
    }
    
    public static ArrayList<String> getListAlreadyTreated(){
    	File folder = new File("Results");
    	ArrayList<String> listFiles = getAllFileNames(folder);	//get list of files inside Results
    	listFiles.removeIf(s -> s.contains("Total_"));			//remove totals files
    	
    	for(int i = 0; i<listFiles.size(); i++){
    		listFiles.set(i, listFiles.get(i).replace(".xlsx", ""));	//remove file type
    	}
    	return listFiles;
    }
    
    public static ArrayList<String> getAllFileNames(File folder){
    	ArrayList<String> fileNames = new ArrayList<String>();
    	if(folder == null) return fileNames;
    	
    	File[] listOfFiles = folder.listFiles();
    	if(listOfFiles == null) return fileNames;
    	
    	for (final File fileEntry : listOfFiles) {
    		if (fileEntry.isDirectory()) {
    			fileNames.addAll(getAllFileNames(fileEntry));
    		} else {
    			fileNames.add(fileEntry.getName());
    		}
    	}
    	return fileNames;
    }
}
