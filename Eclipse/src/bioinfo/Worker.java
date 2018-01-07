package bioinfo;

import java.util.Vector;

import excel.Writer;
import ui.InterfaceUtilisateur;

public class Worker extends Thread {
	
	private Vector<Organism> joblist;
	private int numero_de_thread;
	private volatile boolean exit = false;
	private boolean saveGenome = false;
	private boolean saveGene = false;


	Worker(Vector<Organism> joblist, int num) {
		this.joblist = joblist;
		this.numero_de_thread = num;
	}
	
	public void setSave(Boolean saveGenome, Boolean saveGene){
		this.saveGenome = saveGenome;
		this.saveGene = saveGene;
	}

	@Override
	public void run() {
		InterfaceUtilisateur.journalise("Info","Starting worker "+numero_de_thread);
		Writer w;
		while (!joblist.isEmpty() && !exit){
			try{
				Organism orga = joblist.remove(0);
				try {
					InterfaceUtilisateur.journalise("Info", "Thread "+numero_de_thread + 
						" : Téléchargement de "+orga.getName());
					orga.downloadNCs(this.saveGene);
					InterfaceUtilisateur.journalise("Info", "Thread "+numero_de_thread + 
						" : Fin du téléchargement, début du parsing de "+orga.getName());
					orga.parseAndStat();
					InterfaceUtilisateur.journalise("Info", "Thread "+numero_de_thread + 
						" : Fin du parsing, début de l'écriture de "+orga.getName());
					w = new Writer();
					w.write(orga);
					InterfaceUtilisateur.journalise("Info", "Thread "+numero_de_thread + 
						" : Fin de l'écriture de "+orga.getName());
					InterfaceUtilisateur.unGenomeReussi(orga.getName());
				}
				catch(Exception e){
					System.out.println("Erreur dans worker : " + e);
					if (e.getMessage().contains("Server returned HTTP response code: 502 for URL")) {
						joblist.insertElementAt(orga, 0);
						InterfaceUtilisateur.journalise("Warning", "Thread "+numero_de_thread + 
							" : Traitement de "+orga.getName()+" raté" + " (502 Bad Gateway). Nouvel essai.");
					}
					else {
						InterfaceUtilisateur.journalise("Warning", "Thread "+numero_de_thread + 
							" : Traitement de "+orga.getName()+" raté" + ". Exception "+e);
						InterfaceUtilisateur.unGenomeRate();
						e.printStackTrace();
					}
				}
				if (this.saveGenome == false){
					orga.deleteDechet();
				}
			}
			catch (ArrayIndexOutOfBoundsException e) {
				//this error sometimes arises on joblist.remove(0); 
				//when there's only one organism to analyse.
				//Maybe because the workers both try to remove(0) at the same time.
			}
		}
		InterfaceUtilisateur.journalise("Info","Finishing worker "+numero_de_thread);
	}
	
	public void kill() {
		exit = true;
	}
	
	@SuppressWarnings("deprecation")
	public void hardKill() {
		this.stop();
	}
	
}
