package bioinfo;

import java.util.Vector;

import excel.Writer;
import ui.InterfaceUtilisateur;

public class Worker extends Thread {
	
	private Vector<Organism> joblist;
	private int numero_de_thread;

	Worker(Vector<Organism> joblist, int num) {
		this.joblist = joblist;
		this.numero_de_thread = num;
	}

	@Override
	public void run() {
		InterfaceUtilisateur.journalise("Info","Starting worker "+numero_de_thread);
		Writer w;
		while (!joblist.isEmpty()){
			try{
				Organism orga = joblist.remove(0);
				try {
					InterfaceUtilisateur.journalise("Info", "Téléchargement de "+orga.getName()+
							" dans le thread "+numero_de_thread);
					orga.downloadNCs();
					InterfaceUtilisateur.journalise("Info", "Fin du téléchargement, début du parsing de "+orga.getName()+
							" dans le thread "+numero_de_thread);
					orga.parse();
					InterfaceUtilisateur.journalise("Info", "Fin du parsing, début de l'analyse de "+orga.getName()+
							" dans le thread "+numero_de_thread);
					orga.statistique();
					InterfaceUtilisateur.journalise("Info", "Fin de l'analyse, début de l'écriture de "+orga.getName()+
							" dans le thread "+numero_de_thread);
					w = new Writer();
					w.write(orga);
					InterfaceUtilisateur.journalise("Info", "Fin de l'écriture de "+orga.getName()+
							" dans le thread "+numero_de_thread);
					InterfaceUtilisateur.unGenomeReussi(orga.getName());
				}
				catch(Exception e ){
					InterfaceUtilisateur.journalise("Warning", "Traitement de "+orga.getName()+" raté"+
							" dans le thread "+numero_de_thread);
					InterfaceUtilisateur.unGenomeRate();
				}
				orga.deleteDechet();
			}
			catch (ArrayIndexOutOfBoundsException e) {
				//this error sometimes arises on joblist.remove(0); 
				//when there's only one organism to analyse.
				//Maybe because the workers both try to remove(0) at the same time.
			}
		}
		InterfaceUtilisateur.journalise("Info","Finishing worker "+numero_de_thread);
	}
}
