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
	static final int NOMBRE_DE_WORKERS = 10;
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
		for(File kingdom:results.listFiles(Writer.getFolderFilter())){
			createTotalsAux(kingdom);
		}
	}
	
	private void createTotalsAux(File folder){
		Writer w;
		if (folder.listFiles(Writer.getFolderFilter()).length == 0){//feuille de la hiérarchie
			w = new Writer();
			w.createTotalSubGroup(folder);
			w.close();
		}
		else{
			for(File subFolder:folder.listFiles(Writer.getFolderFilter())){
				createTotalsAux(subFolder);
			}
			w = new Writer();
			w.createTotalGroupOrKingdom(folder);
			w.close();
		}
	}

	public void killWorkers(){
		for(Worker w : workers){
			w.kill();
		}
	}
	
	public void hardKillWorkers(){
		for(Worker w : workers){
			w.hardKill();
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
