package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;

import net.schema.Primary;

/**
 * Classe principale du programme, qui contient le main.
 * Cette classe permet aussi de créer l'UI.
 * @author sindarus
 */
//TODO: il faudra peut etre réusiner cette classe dans le futur,
// et insérer la gestion de l'UI dans une nouvelle classe
public class InterfaceUtilisateur {
	private static Boolean dejaCree = false; //indique si l'interface a été créée
	private static JFrame cadre;
	private static TextArea panneauLog;
	
	private static JPanel panneauActions;
	private static JPanel panneauHierarchie;
	private static JSplitPane panneau;
	private static JSplitPane panneau2;
	private static PanneauInfos panneauInfos;
	
	private static int nbGenomeTotal;
	private static int nbGenomeTraites;
	private static int nbGenomeReussis;
	private static int nbGenomeRates;
	
	private InterfaceUtilisateur() {
		
	}
	
	/**
	 * À appeler pour créer et montrer l'interface au début du programme
	 */
	public static void CreeEtMontreFenetre() {
		if(dejaCree){
			journalise("DEBUG", "La fonction CreeEtMontreFenetre a été appelée alors que l'interface est déja créée.");
			return;
		}
		construitInterface();
		cadre.pack();
		cadre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dejaCree = true;
		Primary primary = new Primary();
		primary.start();
		JFrame f = new JFrame();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(400, 100));
		//f.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		f.setResizable(false);
		f.setTitle("Téléchargement de la hiérarchie des génomes");
		
		JLabel l = new JLabel("Téléchargement de la hiérarchie des génomes en cours");
		
		JProgressBar pb = new JProgressBar();
		pb.setIndeterminate(true);
		pb.setPreferredSize(new Dimension(200, 30));
		
		
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.add(l);
		p.add(pb);
		
		
		f.add(p);
	    f.pack();
		try {
			primary.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		f.dispose();
		PanneauHierarchie.organismListDownloadEnd(Primary.organisms());
		
		//Look inside folder /Results and set organisms that have a XLSX as "treated"
		//these organisms will appear green in the hierarchy panel
		PanneauHierarchie.updateColorsAlreadyTreated();
		
		cadre.setVisible(true);
	}
	
	/**
	 * Construit toute l'interface.
	 * Attention : ce n'est pas elle qui donne aux boutons leur action
	 * et qui donne vie au panneau des infos. Elle construit juste la
	 * structure avec les panneaux et autres objets d'UI.
	 */
	private static void construitInterface() {
		//Creation de la fenêtre
		cadre = new javax.swing.JFrame("Bio - info");
		cadre.setLocation(0, 0);
		Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().		        getMaximumWindowBounds();
		cadre.setPreferredSize(new Dimension(maxBounds.width, maxBounds.height));


		//Création du panneau intérieur
		//(il faut d'abord créer le panneau de gauche et de droite)
			//Création du panneau de logs
		creePanneauLogs();
		
			//Création du panneau d'actions
		panneauActions = new JPanel();
		panneauActions.setBackground(Color.blue);
		panneauActions.setLayout(new BorderLayout());

		//Création du panneau repertoire
		PanneauHierarchie.creePanneauHierarchie();
		panneauHierarchie = PanneauHierarchie.getPanneauHierarchie();
        
		//Creation du panneau intérieur
		panneau = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panneauActions, panneauLog);
		panneau.setDividerLocation(600);
		panneau2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panneauHierarchie , panneauActions);
		panneau2.setDividerLocation(280);
		panneau.setPreferredSize(new Dimension(900, 500));
		cadre.setContentPane(panneau);
		cadre.add(panneau2);
		
		//Construction du panneau d'action
	    	//création du panneau de contrôle
		PanneauControle.creePanneauControle();
		panneauActions.add(PanneauControle.obtientPanneauControle(), BorderLayout.CENTER);

			//création du panneau d'info
		panneauInfos = new PanneauInfos();
		panneauActions.add(panneauInfos, BorderLayout.SOUTH);
	}
	
	/**
	 * Crée et configure le panneau des logs.
	 * Cette fonction doit être appelée par "construitInterface"
	 */
	private static void creePanneauLogs() {
		panneauLog = new TextArea("Bonjour.\n", 5, 10);
		panneauLog.setEditable(false);
		panneauLog.append("Ceci est un affichage pour tous les logs de l'application.\n");
	}
		
	public static void journalise(String canal, String message){
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		panneauLog.append(sdf.format(cal.getTime()) + " " + canal.toUpperCase() + " : " + message + "\n");
	}
	
	/**
	 * permet d'indiquer a l'UI le nombre total de génomes qu'on
	 * prévoit de traiter
	 * @param nombre
	 * 
	 */
	public static void donneNbGenomeTotal(int nombre) {
		nbGenomeTotal = nombre;
		panneauInfos.metNbGenomeTotal(nombre);
		PanneauControle.obtientBarre().setMaximum(nombre);
	}
	
	public static void unGenomeReussi(String orgaName) {
		nbGenomeReussis += 1;
		unGenomeTraite();
		panneauInfos.metNbGenomeReussis(nbGenomeReussis);
		PanneauHierarchie.setOrganismColor(orgaName, getTreatedColor());
	}
	
	public static void unGenomeRate() {
		nbGenomeRates += 1;
		unGenomeTraite();
		panneauInfos.metNbGenomeRates(nbGenomeRates);
	}
	
	private static void unGenomeTraite() {
		nbGenomeTraites += 1;
		PanneauControle.obtientBarre().setValue(nbGenomeTraites);
		panneauInfos.metNbGenomeTraites(nbGenomeTraites);
	}
	
	public static void reset() {
		PanneauControle.obtientBarre().setValue(0);
		nbGenomeTotal = 0;
		nbGenomeTraites = 0;
		nbGenomeReussis = 0;
		nbGenomeRates = 0;
		panneauInfos.metNbGenomeTotal(0);
		panneauInfos.metNbGenomeTraites(0);
		panneauInfos.metNbGenomeReussis(0);
		panneauInfos.metNbGenomeRates(0);
	}
	
	public static Color getTreatedColor() {
		return new Color(0, 200, 0);
	}
}
