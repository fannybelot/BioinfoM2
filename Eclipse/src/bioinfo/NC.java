package bioinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.Download;

public class NC {

	private File fichier;
	private List<CDS> listeCDS;
	private int[][] frequencePreferentielle2;
	private int[][] frequencePreferentielle3;
	private int[][] sumCountPhase2;
	private int[][] sumCountPhase3;
	private int numberCDS;
	private int numberInvalidCDS;
	private String type;
	private int [] [] trinucleoPhase;
	private int [] [] dinucleoPhase;
	private String filePath;
	private String name;
	private String id; //NC's id that is needed to download it with the url :
					   //https://www.ncbi.nlm.nih.gov/sviewer/viewer.cgi?tool=portal&save=file&log$=seqview&db=nuccore&report=gbwithparts&sort=&from=begin&to=end&maxplex=3&id=<ID>

	public NC() {
		numberCDS = 0;
		numberInvalidCDS = 0;
	}
	
	public NC(String id) {
		this.numberCDS = 0;
		this.numberInvalidCDS = 0;
		this.id = id;
	}
	
	public NC(String id, String filePath) {
		this.numberCDS = 0;
		this.numberInvalidCDS = 0;
		this.id = id;
		this.filePath = filePath;
	}
	
	public void ncStatistique() {
		sumCountPhase2 = new int[2][16];
		sumCountPhase3 = new int[3][64];
		Iterator<CDS> it = this.listeCDS.iterator();
		while (it.hasNext()) {
			CDS gene = it.next();
			if (!gene.verification()) {
				numberInvalidCDS += 1;
				it.remove();
			} else {
				gene.geneStatistique();
				addSumCountPhase2(gene.getCountPhase2());
				addSumCountPhase3(gene.getCountPhase3());
//				File f = new File ("testBrugia");
//				try
//				{
//				    FileWriter fw = new FileWriter (f,true);
//			        fw.write (gene.getChaine());
//			        fw.write (" \n");
//				    fw.close();
//				}
//				catch (IOException exception)
//				{
//				    System.out.println ("Erreur lors de la lecture : " + exception.getMessage());
//				}
			}
		}
		this.frequencePreferentielle2 = frequencePref(this, 2);
		this.frequencePreferentielle3 = frequencePref(this, 3);
		this.trinucleoPhase = new int[3][64];
		this.dinucleoPhase = new int[2][16];
		for (CDS cds : getCDS()) {
			trinucleoPhase = addition(cds.getCountPhase3(), trinucleoPhase);
			dinucleoPhase = addition(cds.getCountPhase2(), dinucleoPhase);
		}
	}

	public void parse() throws IOException {
		this.listeCDS = new ArrayList<CDS>();
		FileReader fr = new FileReader(fichier);
		BufferedReader br = new BufferedReader(fr);
		String line;
		while ((line = br.readLine()) != null ) {
			if (line.startsWith("     CDS")) {
				CDS cds = new CDS(this.filePath, numberCDS);
				cds.addRawCDS(line);
				while (line.endsWith(",")) {
					line = br.readLine();
					cds.addRawCDS(line);
				}
				listeCDS.add(cds);
				numberCDS += 1;
			}
			if (line.startsWith("DEFINITION")){
				line = line.toLowerCase();
				if (line.contains("chromosome")){
					this.type = "chromosome";
				}
				else if (line.contains("mitochondrion")){
					this.type = "mitochondrion";
				}
				else if (line.contains("plast")){
					this.type = "plast";
				}
				else if (line.contains("plasmid")){
					this.type = "plasmid";
				}
				else if (line.contains("linkage")){
					this.type = "linkage";
				}
				else if (line.contains("dna")){
					this.type = "DNA";
				}
				else {
					this.type = "others";
				}
				this.name = this.type + "_" + this.fichier.getName();
			}
			if (line.contains("ORIGIN")) {
				break;
			}
		}
		// On a tous les CDS il faut maintenant récup la chaine
		Iterator<CDS> it = listeCDS.iterator();
		while (it.hasNext()){
			CDS gene = it.next();
			if (! gene.calculCDS()){
				numberInvalidCDS += 1;
				it.remove();
			}
		}
		Map<Integer, List<CDS>>[] startAndStopMap = calculStartAndStop();
		Map<Integer, List<CDS>> startMap = startAndStopMap[0];
		Map<Integer, List<CDS>> stopMap = startAndStopMap[1];

		Integer numero_de_nucleotide = 0;
		List<CDS> CDSActifs = new ArrayList<CDS>();
		while ((line = br.readLine()) != null && !line.contains("//")) {
			String nucleotides = line.substring(10).replaceAll(" ", "");
			for (int i = 0; i < nucleotides.length(); i++) {
				char nucleotide = nucleotides.charAt(i);
				numero_de_nucleotide += 1;
				if (startMap.containsKey(numero_de_nucleotide)){
					for (CDS cds: startMap.get(numero_de_nucleotide)){
						CDSActifs.add(cds);
					}
				}
				if (stopMap.containsKey(numero_de_nucleotide)){
					for (CDS cds: stopMap.get(numero_de_nucleotide)){
						CDSActifs.remove(cds);
					}
				}
				for (CDS cds : CDSActifs) {
					cds.ajouterNucleotide(nucleotide);
				}
			}
		}
		br.close();
		fr.close();
		for (CDS cds : listeCDS) {
			cds.finParsing();
		}
	}


	public int[][] addition(int[][] A, int[][] B) {
		try {
			int[][] add = A;
			for (int i=0; i< B.length; i++) {
				for (int j=0; j< B[0].length; j++) {
					add[i][j] += B[i][j];
				}
			}
			return add;
		}
		catch (Exception e) {
			System.out.println("add 2 tab with different lengths !");
		}
		return A;
	}

	public void addSumCountPhase2(int[][] count) {
		for (int i=0; i<2; i++) {
			for (int j=0; j<16; j++) {
				sumCountPhase2[i][j]+=count[i][j];
			}
		}
	}
	
	public void addSumCountPhase3(int[][] count) {
		for (int i=0; i<3; i++) {
			for (int j=0; j<64; j++) {
				sumCountPhase3[i][j]+=count[i][j];
			}
		}
	}

	private Map<Integer, List<CDS>>[] calculStartAndStop(){
		@SuppressWarnings("unchecked")
		Map<Integer, List<CDS>>[] startAndStopMap = new Map[2];
		Map<Integer,List<CDS>> startMap = new Hashtable<Integer, List<CDS>>();
		Map<Integer,List<CDS>> stopMap = new Hashtable<Integer, List<CDS>>();
		for(CDS cds: getCDS()){
			List<Integer[]> intCDS = cds.getIntCDS();
			for (Integer[] startAndStop: intCDS){
				if (! startMap.containsKey(startAndStop[0])){
					startMap.put(startAndStop[0], new ArrayList<CDS>());
				}
				startMap.get(startAndStop[0]).add(cds);
				if (! stopMap.containsKey(startAndStop[1]+1)){
					stopMap.put(startAndStop[1]+1, new ArrayList<CDS>());
				}
				stopMap.get(startAndStop[1]+1).add(cds);
			}
		}
		startAndStopMap[0] = startMap;
		startAndStopMap[1] = stopMap;
		return startAndStopMap;
	}
	
	public void deleteDechet() {
		this.fichier.delete();
	}
	
	public void download() throws Exception {
		this.fichier = new File("genomes" + File.separator + this.filePath + File.separator + id + ".txt");
		Download.getNC(this.id, this.fichier);
	}
	
	private int[][] frequencePref(NC G, int n) {
		int preft[][];
		preft = new int[n][(int)Math.pow(4, n)];
		for (CDS g : G.listeCDS) {
			if (n==3) {
				float freq[][] = g.getFrequencePhase3();
				for (int i = 0; i < 64; i++) {
					if (freq[0][i] >= freq[1][i] && freq[0][i] >= freq[2][i]) {
						preft[0][i] += 1;
					}
					if (freq[1][i] >= freq[0][i] && freq[1][i] >= freq[2][i]) {
						preft[1][i] += 1;
					}
					if (freq[2][i] >= freq[0][i] && freq[2][i] >= freq[1][i]) {
						preft[2][i] += 1;
					}
				}
			}
			else if (n==2) {
				float freq[][] = g.getFrequencePhase2();
				for (int i = 0; i < 16; i++) {
					if (freq[0][i] >= freq[1][i]) {
						preft[0][i] += 1;
					}
					if (freq[1][i] >= freq[0][i]) {
						preft[1][i] += 1;
					}
				}
			}
		}
		return preft;
	}

	public List<CDS> getCDS() {
		return listeCDS;
	}

	public int[][] getDinucleoPhase() {
		return dinucleoPhase;
	}

	public int[][] getFrequencePreferentielle2() {
		return frequencePreferentielle2;
	}

	public int[][] getFrequencePreferentielle3() {
		return frequencePreferentielle3;
	}

	public String getName() {
		return name;
	}

	public int getNumberCDS() {
		return numberCDS;
	}
	


	public int getNumberInvalidCDS() {
		return numberInvalidCDS;
	}

	public int[][] getSumCountPhase2() {
		return sumCountPhase2;
	}

	public int[][] getSumCountPhase3() {
		return sumCountPhase3;
	}
	
	public int[][] getTrinucleoPhase() {
		return trinucleoPhase;
	}
	
	public String getType() {
		return type;
	}

	public void setFichier(File fichier) {
		this.fichier = fichier;
	}
}
