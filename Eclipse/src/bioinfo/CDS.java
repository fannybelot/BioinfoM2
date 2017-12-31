package bioinfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import excel.Writer;

public class CDS {
	private StringBuffer rawCDS;
	private Boolean needComplement;
	private List<Integer[]> intCDS;
	private StringBuffer rawChaine;
	private String chaine;
	private int[][] countPhase2;

	private float[][] frequencePhase2;

	private int[][] countPhase3;

	private float[][] frequencePhase3;
	
	private String filePath = "";
	private File fichier;
	private int CDSNumber = 1;
	

	public CDS() {
		this.rawCDS = new StringBuffer();
		this.rawChaine = new StringBuffer();
		this.needComplement = false;
		this.intCDS = new ArrayList<Integer[]>();
	}

	public CDS(String filePath, int CDSNumber) {
		this.rawCDS = new StringBuffer();
		this.rawChaine = new StringBuffer();
		this.needComplement = false;
		this.intCDS = new ArrayList<Integer[]>();
		this.filePath = filePath;
		this.CDSNumber = CDSNumber;
	}
	
	private static Boolean verifCodon(String str) {
		int len = str.length();
		if (len > 2) {
			String init = str.substring(0, 3).toLowerCase();
			String stop = str.substring(len - 3, len).toLowerCase();
			if (init.equals("atg") || init.equals("ctg") || init.equals("ttg") || init.equals("gtg") || init.equals("ata")
					|| init.equals("atc") || init.equals("att") || init.equals("tta")) {
				if (stop.equals("taa") || stop.equals("tag") || stop.equals("tga") || stop.equals("tta")) {
					return true;
				}
			}
		}
		return false;
	}
	private static Boolean verifCompo(String str) {
		Boolean res = true;
		int i = 0;
		int n = str.length();
		char t[] = { 'a', 't', 'c', 'g' };
		char chars[] = str.toLowerCase().toCharArray();
		while (res == true && i < n) {
			char c = chars[i];
			int j = 0;
			Boolean aux = false;
			while (j < 4 && aux == false) {
				aux = aux || c == t[j];
				j++;
			}

			res = res && aux;
			i++;
		}
		return res;
	}
	private static Boolean verifTaille(String str) {
		if (str.length() % 3 == 0) {
			return true;
		} else
			return false;
	}

	public void addRawCDS(String line) {
		rawCDS.append(line);
	}

	public void verifierEtAjouterNucleotide(char nucleotide, Integer numero_de_nucleotide) {
		for (Integer[] interval : this.intCDS) {
			if (numero_de_nucleotide >= interval[0] && numero_de_nucleotide <= interval[1]) {
				rawChaine.append(nucleotide);
				return;
			}
		}
	}
	
	public void ajouterNucleotide(char nucleotide){
		rawChaine.append(nucleotide);
	}

	//Renvoie true si le rawCDS était valide, false sinon.
	//En cas de false, intCDS reste vide.
	public Boolean calculCDS(){
		int len = rawCDS.length();
		String string = rawCDS.toString(); 
		int i = 0; // pointeur string
		int openParenthesis = 0; // nombre de parentheses ouvertes
		List<Integer[]> tempIntCDS = new ArrayList<Integer[]>();
		String tempIntString = new String("");
		Integer[] tempTab = new Integer[2];
//		Boolean res = true;
		if (! string.startsWith("     CDS             ")){
			return false;
		}
		i+=21;
		// regarde si commence par complement et/ou join
		if (string.startsWith("complement(",i)){
			openParenthesis ++;
			i+=11;
			this.needComplement = true;
		}
		if (string.startsWith("join(",i)){
			openParenthesis ++;
			i+=5;
		}
		// boucle tant que pas d'erreur ou pas fin de chaine
		while (i < len) {
			// chiffre
			if (Character.isDigit(string.charAt(i))){
				tempIntString += string.charAt(i);
				i++;
				continue;
			}
			if (Character.isSpaceChar(string.charAt(i))){
				i++;
				continue;
			}
			// non chiffre
			// pas de chiffres lus avant
			if (tempIntString.isEmpty()){
				return false;
			}
			// .. => stockage de la borne de debut
			if (string.startsWith("..",i)){
				i+=2;
				tempTab[0] = Integer.parseInt(tempIntString);
				tempIntString = new String("");
				continue;
			}
			// , => fin d'intervalle, ajout dans tempTab des bornes
			if (string.startsWith(",",i)){
				i++;
				tempTab[1] = Integer.parseInt(tempIntString);
				// intervalle de taille 1
				if (tempTab[0] == null) {
					tempTab[0] = new Integer(tempTab[1]);
				}
				tempIntCDS.add(tempTab);
				tempIntString = new String("0");
				tempTab = new Integer[2];
				continue;
			}
			// fermeture des parentheses
			if ((string.startsWith(")", i) && openParenthesis == 1) || (string.startsWith("))", i) && openParenthesis == 2)){
				i+=openParenthesis;
				openParenthesis = 0;
				// erreur si les ) ne sont pas a la fin de la chaine
				if (i != len){
					return false;
				}
				break;
			}
			// erreur pour tout autre cas
			return false;
		}
		// cas avec erreur : pas fin de chaine, parenthese non fermee, pas de chiffres lus
		if (i!=len || openParenthesis!=0 || tempIntString.isEmpty()){
			return false;
		}
		// pas d'erreur => ajout du dernier intervalle puis mise à jour de intCDS
		tempTab[1] = Integer.parseInt(tempIntString);
		if (tempTab[0] == null) {
			tempTab[0] = new Integer(tempTab[1]);
		}
		tempIntCDS.add(tempTab);
		this.intCDS = new ArrayList<Integer[]>(tempIntCDS);
		return true;
	}

	private String complement(String str) {
		String res = "";
		int len = str.length();
		char chars[] = str.toLowerCase().toCharArray();
		int i = 0;
		while (i < len) {
			res += complementSingle(chars[len - i - 1]);
			i++;
		}
		return res;
	}

	private char complementSingle(char c) {
		if (c == 't')
			return 'a';
		else if (c == 'a')
			return 't';
		else if (c == 'c')
			return 'g';
		else if (c == 'g')
			return 'c';
		else
			return 'c';
	}

	private int[][] comptagePhase(CDS g, int n) {
		String s = g.chaine;
		int t[][] = new int[n][(int) Math.pow(4, n)];
		List<String> l = Writer.listeNucleotide(n);
		int tailleChaine = s.length();
		for (int i = 0; i <= tailleChaine - 6; i = i + n) {
			if (n == 2) {
				String nucl0 = s.substring(i, i + n).toUpperCase();
				String nucl1 = s.substring(i + 1, i + n + 1).toUpperCase();
				for (int j = 0; j < 16; j++) {
					String str = l.get(j);
					if (nucl0.equals(str)) {
						t[0][j] = t[0][j] + 1;
					}
					if (nucl1.equals(str)) {
						t[1][j] = t[1][j] + 1;
					}
				}
			} else {
				String nucl0 = s.substring(i, i + n).toUpperCase();
				String nucl1 = s.substring(i + 1, i + n + 1).toUpperCase();
				String nucl2 = s.substring(i + 2, i + n + 2).toUpperCase();
				for (int j = 0; j < 64; j++) {
					String str = l.get(j);
					if (nucl0.equals(str)) {
						t[0][j] = t[0][j] + 1;
					}
					if (nucl1.equals(str)) {
						t[1][j] = t[1][j] + 1;
					}
					if (nucl2.equals(str)) {
						t[2][j] = t[2][j] + 1;
					}
				}
			}

		}
		return t;
	}

	public void finParsing(Boolean saveGene) {
		this.chaine = rawChaine.toString();
		this.fichier = new File("genes" + File.separator + this.filePath + File.separator + "gene_" + this.CDSNumber + ".txt");
		if (saveGene == true){
			try {
				FileUtils.writeStringToFile(this.fichier, this.chaine, "UTF-8", false);
			} catch (IOException e) {
				System.out.println("Failed to write gene " + e.getMessage());
			}
		}
	}

	private float[][] frequencePhase(CDS g, int n, int[][] comptagePhaseN) {
		String s = g.chaine;
		int entier = (int) Math.pow(4, n);
		float freqt[][] = new float[n][entier];
		int taille = s.length() / n;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < entier; j++)
				freqt[i][j] = ((float) comptagePhaseN[i][j]) / taille;
		}
		return freqt;
	}

	public void geneStatistique() {
		this.countPhase2 = comptagePhase(this, 2);
		this.frequencePhase2 = frequencePhase(this, 2, countPhase2);
		this.countPhase3 = comptagePhase(this, 3);
		this.frequencePhase3 = frequencePhase(this, 3, countPhase3);
	}
	
	public boolean verification() {
		if (verifCompo(chaine) && verifTaille(chaine)) {
			if (this.needComplement) {
				chaine = complement(chaine);
			}
			return verifCodon(chaine);
		}
		return false;
	}

	public int[][] getCountPhase2() {
		return countPhase2;
	}

	public int[][] getCountPhase3() {
		return countPhase3;
	}

	public float[][] getFrequencePhase2() {
		return frequencePhase2;
	}

	public float[][] getFrequencePhase3() {
		return frequencePhase3;
	}

	public int getLength2() {
		return (chaine.length() / 2);
	}

	public int getLength3() {
		return (chaine.length() / 3);
	}
	public List<Integer[]> getIntCDS() {
		return intCDS;
	}
	public String getChaine() {
		return chaine;
	}
}
