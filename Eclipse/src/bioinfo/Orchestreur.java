package bioinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import excel.Writer;
import ui.InterfaceUtilisateur;
import ui.PanneauControle;
import ui.PanneauControle.FournisseurDePause;

public class Orchestreur implements Runnable {
	static final int NOMBRE_DE_WORKERS = 16;
	FournisseurDePause p;
	Vector<Organism> genomes;
	List<Worker> workers;
	
	public Orchestreur(FournisseurDePause pause, Vector<Organism> genomes){
		p = pause;
		Worker w;
		this.workers = new ArrayList<Worker>();
		this.genomes = genomes;
		this.p = pause;
		InterfaceUtilisateur.donneNbGenomeTotal(genomes.size());
		for(int i=0; i<NOMBRE_DE_WORKERS; i++){
			w = new Worker(genomes,i);
			workers.add(w);
		}
	}
	
	public void createTotals(){
		File results = new File("Results");
		Writer w;
		for(File kingdom:results.listFiles(Writer.getFolderFilter())){
			for(File group:kingdom.listFiles(Writer.getFolderFilter())){
				for(File subGroup:group.listFiles(Writer.getFolderFilter())){
					w = new Writer();
					w.createTotalSubGroup(subGroup);
					w.close();
				}
				w = new Writer();
				w.createTotalGroupOrKingdom(group);
				w.close();
			}
			w = new Writer();
			w.createTotalGroupOrKingdom(kingdom);
			w.close();
		}
	}

	public void killWorkers(){
		for(Worker w : workers){
			w.interrupt();
		}
	}
	
	@Override
	public void run() {
		for (Worker w : workers){
			w.start();
		}
		for (Worker w: workers){
			try {
				w.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		InterfaceUtilisateur.journalise("INFO", "Calcul et écriture des totaux.");
		createTotals();
		PanneauControle.toutFiniCallback();
		InterfaceUtilisateur.journalise("Info", "Tout est fini !");
	}
}
