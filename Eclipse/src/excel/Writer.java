package excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import bioinfo.NC;
import bioinfo.Organism;

public class Writer {

	private static FilenameFilter folderFilter = new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
	};
	private static FilenameFilter xlsxFilter = new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).getName().endsWith(".xlsx");
		  }
	};
	
	private static FilenameFilter totalFilter = new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).getName().startsWith("Total_");
		  }
	};
	//Création des styles
	private static Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        
        CellStyle style = wb.createCellStyle();
        Font fonte = wb.createFont();
        XSSFDataFormat dataFormat = (XSSFDataFormat) wb.createDataFormat();
        
	    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    style.setFillPattern(FillPatternType.valueOf("SOLID_FOREGROUND"));
	    style.setAlignment(HorizontalAlignment.valueOf("CENTER"));
        fonte.setBold(true);
        fonte.setFontHeightInPoints((short) 14);
        style.setFont(fonte);
	    styles.put("titre", style);
	    
	    style = wb.createCellStyle();
	    fonte = wb.createFont();
	    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    style.setFillPattern(FillPatternType.valueOf("SOLID_FOREGROUND"));
	    style.setAlignment(HorizontalAlignment.valueOf("RIGHT"));
	    fonte.setFontHeightInPoints((short) 12);
        style.setFont(fonte);
	    styles.put("clair", style);
	    
	    style = wb.createCellStyle();
	    fonte = wb.createFont();
	    style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
	    style.setFillPattern(FillPatternType.valueOf("SOLID_FOREGROUND"));
	    fonte.setBold(true);
	    fonte.setColor(IndexedColors.WHITE.getIndex());
	    fonte.setFontHeightInPoints((short) 12);
        style.setFont(fonte);
	    styles.put("fonce", style);
	    
	    style = wb.createCellStyle();
	    fonte = wb.createFont();
	    fonte.setFontHeightInPoints((short) 12);
        style.setFont(fonte);
	    styles.put("int", style);
	    
	    style = wb.createCellStyle();
	    fonte = wb.createFont();
	    fonte.setFontHeightInPoints((short) 12);
	    style.setDataFormat(dataFormat.getFormat("0.00"));
        style.setFont(fonte);
	    styles.put("float", style);
        return styles;
	}
	public static FilenameFilter getFolderFilter() {
		return folderFilter;
	}

		
	public static List<String> listeNucleotide(int n){ //2 ou 3 !
		String[] lettres = {"A", "T", "C", "G"};
		List<String> res = new ArrayList<String>();
		for(int i=0;i<4;i++){
			for(int j=0;j<4;j++){
				if(n==3){
					for(int k=0;k<4;k++){
						res.add(lettres[i]+lettres[j]+lettres[k]);
					}
				}
				else{
					res.add(lettres[i]+lettres[j]);
				}
			}
		}
		return res;	
	}
	
	private static List<String> listeTitreColonne(int n){ //2 ou 3 !
		List<String> res = new ArrayList<String>();
		String[] titres = {"Phase ", "Freq Phase ", "Pref Phase "};
		for(int i=0;i<n;i++){
			res.add(titres[0]+Integer.toString(i));
			res.add(titres[1]+Integer.toString(i));
		}
		for(int i=0;i<n;i++){
			res.add(titres[2]+Integer.toString(i));
		}
		return res; 	
	}

	private XSSFWorkbook wb;
	private Map<String, CellStyle> styles;

	private String[] sheets = {"Sum_Chromosome","Sum_Mitochondrion","Sum_Plast","Sum_Plasmid","Sum_DNA","Sum_Linkage","Sum_Others"};
	
	private int diff_types = sheets.length;
		
	public Writer() {
		wb = new XSSFWorkbook();
		styles = createStyles(wb);
	}
	
	private void ajoutInfoTotal(Reader r, int[] info, int[] genome) {
		XSSFSheet s = r.getSheet("General_Information");
		info[0] += s.getRow(6).getCell(1).getNumericCellValue();
		info[1] += s.getRow(8).getCell(1).getNumericCellValue();
		info[2] += s.getRow(10).getCell(1).getNumericCellValue();

		for (int i=0; i<diff_types; i++) {
			genome[i] += s.getRow(2+i).getCell(5).getNumericCellValue();
		}
	}
	
	private void ajoutNucleoTotal(Reader r, int[][][][] valuesTrinucleo, int[][][][] valuesDinucleo, int[][] valuesInfos) {
		int[][] trinucleoPhase;
		int[][] trinucleoPrefPhase;
		int[][] dinucleoPhase;
		int[][] dinucleoPrefPhase;
		int[] colonneActuelle;
		String sheet;
		for (int sh=0;sh<diff_types;sh++){
			sheet = sheets[sh];
			XSSFSheet s = r.getSheet(sheet);
			trinucleoPhase = valuesTrinucleo[sh][0];
			trinucleoPrefPhase = valuesTrinucleo[sh][1];
			dinucleoPhase = valuesDinucleo[sh][0];
			dinucleoPrefPhase = valuesDinucleo[sh][1];

			if (s!=null){
				for(int j=0;j<3;j++){//boucle sur les colonnes de Phase
					colonneActuelle = Reader.getColonneTrinucleoInt(s, 2*j+1);
					for(int i=0;i<64;i++){
						trinucleoPhase[j][i] += colonneActuelle[i];
					}
				}
				for(int j=0;j<3;j++){//boucle sur les colonnes de PrefPhase
					colonneActuelle = Reader.getColonneTrinucleoInt(s, j+7);
					for(int i=0;i<64;i++){
						trinucleoPrefPhase[j][i] += colonneActuelle[i];
					}
				}
				
				// Passage aux dinucléotides
				for(int j=0;j<2;j++){//boucle sur les colonnes de Phase
					colonneActuelle = Reader.getColonneDinucleoInt(s, 2*j+13);
					for(int i=0;i<16;i++){
						dinucleoPhase[j][i] += colonneActuelle[i];
					}
				}
				for(int j=0;j<2;j++){//boucle sur les colonnes de PrefPhase
					colonneActuelle = Reader.getColonneDinucleoInt(s, j+17);
					for(int i=0;i<16;i++){
						dinucleoPrefPhase[j][i] += colonneActuelle[i];
					}
				}
				
				colonneActuelle = Reader.getColonneInfos(s, 12);
				valuesInfos[sh][0] += colonneActuelle[0];
				valuesInfos[sh][1] += colonneActuelle[1];
			}
			valuesTrinucleo[sh][0] = trinucleoPhase;
			valuesTrinucleo[sh][1] = trinucleoPrefPhase;
			valuesDinucleo[sh][0] = dinucleoPhase;
			valuesDinucleo[sh][1] = dinucleoPrefPhase;

		}		
	}
	
	public void close(){
		try{
			wb.close();
		}
	    catch (IOException e) {
	      e.printStackTrace();
	    } 
	}
	
	public void createTotalGroupOrKingdom(File folder){
		Reader r;
		String fileName = "Total_"+folder.getName()+".xlsx";
		String sheet;
		int[][][][] valuesTrinucleo = new int[diff_types][2][3][64];
		int[][][][] valuesDinucleo = new int[diff_types][2][2][16];
		int[][] valuesInfos = new int[diff_types][2];
		int[] info = new int[3];
		int[] genome = new int[diff_types];
	    for (File subfolder:folder.listFiles(folderFilter)) {
	    	for (File file:subfolder.listFiles(totalFilter)){
		    	r = new Reader(file.getPath());
		    	ajoutInfoTotal(r,info,genome);
		   		ajoutNucleoTotal(r,valuesTrinucleo,valuesDinucleo,valuesInfos);	    		
	    	}
	   	}
	    feuilleInfo(folder.getName(), (new Date()).toString(), info, genome);
	    for(int sh=0;sh<diff_types;sh++){
	    	if (genome[sh]!=0){
	    		sheet = sheets[sh];
		    	feuilleNucleotide(sheet, valuesTrinucleo[sh][0], valuesTrinucleo[sh][1], valuesDinucleo[sh][0], valuesDinucleo[sh][1], valuesInfos[sh][0], valuesInfos[sh][1] );	
	    	}
	    }
	    print(new File(folder.getPath(),fileName));
	}

	public void createTotalSubGroup(File folder){
		Reader r;
		String fileName = "Total_"+folder.getName()+".xlsx";
		String sheet;
		int[][][][] valuesTrinucleo = new int[diff_types][2][3][64];
		int[][][][] valuesDinucleo = new int[diff_types][2][2][16];
		int[][] valuesInfos = new int[diff_types][2];
		int[] info = new int[3];
		int[] genome = new int[diff_types];
	    for (File f:folder.listFiles(xlsxFilter)) {
	    	if (!f.getName().startsWith("Total_")){
	    		r = new Reader(f.getPath());
	    		ajoutInfoTotal(r,info,genome);
	    		ajoutNucleoTotal(r,valuesTrinucleo,valuesDinucleo,valuesInfos);
	    	}
	    }
	    feuilleInfo(folder.getName(), (new Date()).toString(), info, genome);
	    for(int sh=0;sh<diff_types;sh++){
	    	if (genome[sh]!=0){
	    		sheet = sheets[sh];
		    	feuilleNucleotide(sheet, valuesTrinucleo[sh][0], valuesTrinucleo[sh][1], valuesDinucleo[sh][0], valuesDinucleo[sh][1], valuesInfos[sh][0], valuesInfos[sh][1] );	
	    	}
	    }
	    print(new File(folder.getPath(),fileName));
	}
	
	private void faireSomme(String sheetName, List<String> l){
		int[][] trinucleoPhase = new int[3][64],
				trinucleoPrefPhase = new int[3][64],
				dinucleoPhase = new int[2][16],
				dinucleoPrefPhase = new int[2][16];
		int[] colonneActuelle;
		int nbCDS=0, nbInvalidCDS=0;
		for(String elt:l){ //boucle sur les feuilles
			XSSFSheet s = wb.getSheet(elt);
			for(int j=0;j<3;j++){//boucle sur les colonnes de Phase
				colonneActuelle = Reader.getColonneTrinucleoInt(s, 2*j+1);
				for(int i=0;i<64;i++){
					trinucleoPhase[j][i] += colonneActuelle[i];
				}
			}
			for(int j=0;j<3;j++){//boucle sur les colonnes de PrefPhase
				colonneActuelle = Reader.getColonneTrinucleoInt(s, j+7);
				for(int i=0;i<64;i++){
					trinucleoPrefPhase[j][i] += colonneActuelle[i];
				}
			}
			colonneActuelle = Reader.getColonneInfos(s, 12);
			nbCDS += colonneActuelle[0];
			nbInvalidCDS += colonneActuelle[1];
			// Passage aux dinucléotides
			for(int j=0;j<2;j++){//boucle sur les colonnes de Phase
				colonneActuelle = Reader.getColonneDinucleoInt(s, 2*j+13);
				for(int i=0;i<16;i++){
					dinucleoPhase[j][i] += colonneActuelle[i];
				}
			}
			for(int j=0;j<2;j++){//boucle sur les colonnes de PrefPhase
				colonneActuelle = Reader.getColonneDinucleoInt(s, j+17);
				for(int i=0;i<16;i++){
					dinucleoPrefPhase[j][i] += colonneActuelle[i];
				}
			}
		}
		//création de la feuille sum
		this.feuilleNucleotide(sheetName, 
			trinucleoPhase, trinucleoPrefPhase,
			dinucleoPhase, dinucleoPrefPhase, nbCDS, nbInvalidCDS);
	}

	public void feuilleInfo(String name, String date, int[] infoVals, int[] genome){
		XSSFSheet s = wb.createSheet("General_Information");
		String[] info_tab = 
			{"Name", name, 
			"Modification Date", date, 
			"Number of CDS sequences",  
			"Number of invalids sequencies", 
			"Number of Organisms"};
		String[] genome_tab =
			{"Chromosome", "Mitochondrion", "Plast", "Plasmid", "DNA", "Linkage", "Others"};
		//Titres
		Cell infoCell = s.createRow(0).createCell(0);
		infoCell.setCellStyle(styles.get("titre"));
		infoCell.setCellValue("Information");
		Cell genomeCell = s.createRow(1).createCell(4);
		genomeCell.setCellStyle(styles.get("titre"));
		genomeCell.setCellValue("Genome");
		//Boucle pour la partie information de la page
		for(int i=2 ; i<5 ; i+=2){
			XSSFRow r = s.createRow(i);
			XSSFCell c1 = r.createCell(0);
			XSSFCell c2 = r.createCell(1);
			c1.setCellValue(info_tab[i-2]);
			c1.setCellStyle(styles.get("fonce"));
			c2.setCellValue(info_tab[i-1]);
			c2.setCellStyle(styles.get("clair"));
			s.createRow(i+1);
		}
		for(int i=6 ; i<11 ; i+=2){
			XSSFRow r = s.createRow(i);
			XSSFCell c1 = r.createCell(0);
			XSSFCell c2 = r.createCell(1);
			c1.setCellValue(info_tab[i/2+1]);
			c1.setCellStyle(styles.get("fonce"));
			c2.setCellValue(infoVals[i/2-3]);
			c2.setCellStyle(styles.get("clair"));
			s.createRow(i+1);
		}
		
		//Boucle pour la partie genome
		for(int i=0 ; i<genome_tab.length; i++){
			XSSFRow r = s.getRow(2+i);
			XSSFCell c1 = r.createCell(4);
			XSSFCell c2 = r.createCell(5);
			c1.setCellValue(genome_tab[i]);
			c1.setCellStyle(styles.get("fonce"));
			c2.setCellValue(genome[i]);
			c2.setCellStyle(styles.get("clair"));
		}
		s.autoSizeColumn(0); s.autoSizeColumn(1);
		s.autoSizeColumn(4); s.autoSizeColumn(5);
	}
	
	public void feuilleNucleotide(NC nc){
		String titre = nc.getName();
		int [][] trinucleoPhase = nc.getTrinucleoPhase();
		int [][] dinucleoPhase = nc.getDinucleoPhase();
		int[][] trinucleoPrefPhase = nc.getFrequencePreferentielle3();
		int[][] dinucleoPrefPhase = nc.getFrequencePreferentielle2();
		int nbCDS = nc.getNumberCDS();
		int nbInvalidCDS = nc.getNumberInvalidCDS();
		feuilleNucleotide(titre, trinucleoPhase, trinucleoPrefPhase, dinucleoPhase, dinucleoPrefPhase, nbCDS, nbInvalidCDS);
	}

	private void feuilleNucleotide(String sheetName,
					int[][] trinucleoPhase, int[][] trinucleoPrefPhase, 
					int[][] dinucleoPhase, int[][] dinucleoPrefPhase, 
					int nbCDS, int nbInvalidCDS) {
		System.out.println(sheetName);
		XSSFSheet s = wb.createSheet(sheetName);
		XSSFRow r = s.createRow(0);
		XSSFCell cell;
		List<String> titres = listeTitreColonne(3);
		//Partie trinucleo
		//Création 1ere ligne
		int i=1;
		for(String str:titres){
			cell = r.createCell(i);
			cell.setCellStyle(styles.get("titre"));
			cell.setCellValue(str);
			i++;
		}
		//Création 1ere colonne
		List<String> nucleo = listeNucleotide(3);
		i=1;
		for(String str:nucleo){
			r = s.createRow(i);
			cell = r.createCell(0);
			cell.setCellStyle(styles.get("titre"));
			cell.setCellValue(str);
			i++;
		}
		//Impressions valeurs
		for(i=0;i<3;i++){
			setColonneInt(s, trinucleoPhase[i], 2*i+1, "int");
			for(int j=0; j<64; j++){
				r = s.getRow(j+1);
				cell = r.createCell(2*i+2);
				char col = (char) ('A'+2*i+1);
				cell.setCellFormula(col+Integer.toString(j+2)+"*100/SUM("+col+"2:"+col+"65)");
				cell.setCellStyle(styles.get("float"));
			}
			//setColonneFloat(s, trinucleoFreqPhase[i], 2*i+2, "float");
		}
		//if (n==3) temp=7 else temp=5;
		for(i=0;i<3;i++){
			setColonneInt(s, trinucleoPrefPhase[i], 7+i, "int");
		}
		//Calcul totaux
		r = s.createRow(65);
		cell = r.createCell(0);
		cell.setCellValue("Total");
		cell.setCellStyle(styles.get("titre"));
		for(i=1;i<=6;i++){
			cell = r.createCell(i);
			char col = (char) ('A'+i);
			cell.setCellFormula("SUM("+col+"2:"+col+"65)");
			cell.setCellStyle(styles.get("titre"));
		}
		//Partie dinucleo
		//Création 1ere ligne
		titres = listeTitreColonne(2);
		i=13;
		r=s.getRow(0);
		for(String str:titres){
			cell = r.createCell(i);
			cell.setCellStyle(styles.get("titre"));
			cell.setCellValue(str);
			i++;
		}
		//Création 1ere colonne
		nucleo = listeNucleotide(2);
		i=1;
		for(String str:nucleo){
			r = s.getRow(i);
			cell = r.createCell(12);
			cell.setCellStyle(styles.get("titre"));
			cell.setCellValue(str);
			i++;
		}	
		//Impressions valeurs
		for(i=0;i<2;i++){
			setColonneInt(s, dinucleoPhase[i], 13+2*i, "int");
			for(int j=0; j<16; j++){
				r = s.getRow(j+1);
				cell = r.createCell(2*i+14);
				char col = (char) ('A'+2*i+13);
				cell.setCellFormula(col+Integer.toString(j+2)+"/SUM("+col+"2:"+col+"17)");
				cell.setCellStyle(styles.get("float"));
			}
			//setColonneFloat(s, dinucleoFreqPhase[i], 13+2*i, "float");
		}
		for(i=0;i<2;i++){
			setColonneInt(s, dinucleoPrefPhase[i], 17+i, "int");
		}
		//Calcul totaux
		r = s.getRow(17);
		cell = r.createCell(12);
		cell.setCellValue("Total");
		cell.setCellStyle(styles.get("titre"));
		for(i=1;i<=4;i++){
			cell = r.createCell(12+i);
			char col = (char) ('M'+i);
			cell.setCellFormula("SUM("+col+"2:"+col+"17)");
			cell.setCellStyle(styles.get("titre"));
		}
		
		//Informations
		r = s.getRow(20);
		cell = r.createCell(11);
		cell.setCellValue("Informations");
		cell.setCellStyle(styles.get("titre"));
		
		r = s.getRow(21);
		cell = r.createCell(11);
		cell.setCellValue("Number of CDS sequences");
		cell.setCellStyle(styles.get("fonce"));
		cell = r.createCell(12);
		cell.setCellValue(nbCDS);
		cell.setCellStyle(styles.get("int"));
		
		r = s.getRow(22);
		cell = r.createCell(11);
		cell.setCellValue("Number of invalid CDS");
		cell.setCellStyle(styles.get("fonce"));
		cell = r.createCell(12);
		cell.setCellValue(nbInvalidCDS);
		cell.setCellStyle(styles.get("int"));
		
		for(i=0;i<19;i++){
			s.autoSizeColumn(i);
		}
	}
	
	public void feuillesSomme(){
		List<String> feuillesChromosome = new ArrayList<String>(),
					feuillesAdn = new ArrayList<String>(),
					feuillesPlaste = new ArrayList<String>(),
					feuillesMitochondrie = new ArrayList<String>(),
					feuillesLinkage = new ArrayList<String>(),
					feuillesOthers = new ArrayList<String>(),
					feuillesPlasmid = new ArrayList<String>();
		int n = wb.getNumberOfSheets();
		for(int i=0;i<n;i++){
			String name = wb.getSheetName(i);
			if (name.startsWith("chromosome")) feuillesChromosome.add(name);
			else if (name.startsWith("DNA")) feuillesAdn.add(name);
			else if (name.startsWith("plast")) feuillesPlaste.add(name);
			else if (name.startsWith("mitochondrion")) feuillesMitochondrie.add(name);
			else if (name.startsWith("plasmid")) feuillesPlasmid.add(name);
			else if (name.startsWith("linkage")) feuillesLinkage.add(name);
			else if (name.startsWith("others")) feuillesOthers.add(name);
		}
		if (!feuillesChromosome.isEmpty()) faireSomme("Sum_Chromosome", feuillesChromosome);
		if (!feuillesAdn.isEmpty()) faireSomme("Sum_DNA", feuillesAdn);
		if (!feuillesPlaste.isEmpty()) faireSomme("Sum_Plast", feuillesPlaste);
		if (!feuillesMitochondrie.isEmpty()) faireSomme("Sum_Mitochondrion", feuillesMitochondrie);
		if (!feuillesPlasmid.isEmpty()) faireSomme("Sum_Plasmid", feuillesPlasmid);
		if (!feuillesLinkage.isEmpty()) faireSomme("Sum_Linkage", feuillesLinkage);
		if (!feuillesOthers.isEmpty()) faireSomme("Sum_Others", feuillesOthers);
	}
	
	public void print(File file){//path est de la forme "Results/fold1/fold2/fold3"
		try {
			Files.createDirectories(file.getParentFile().toPath());
	    	FileOutputStream fileOut = new FileOutputStream(file);
	    	wb.write(fileOut);
	    	fileOut.close();
	    }
	    catch (IOException e) {
		      e.printStackTrace();
	    }
	    wb = new XSSFWorkbook();
	}
	
	private void setColonneInt(XSSFSheet s, int[] valeurs, int colonne, String style){
		XSSFRow r;
		XSSFCell cell;
		int i = 1;
		for(int val:valeurs){
			r = s.getRow(i);
			cell = r.createCell(colonne);
			cell.setCellValue(val);
			cell.setCellStyle(styles.get(style));
			i++;
		}
	}
	
	public void write(Organism g) {
		File file = new File("Results"+File.separator+g.getKingdom()+File.separator+g.getGroup()+File.separator+g.getSubGroup()+File.separator+g.getName()+".xlsx");
		int[] info = {g.getNumberCDS(), g.getNumberInvalidCDS(), 1};
		int[] genome = {g.countNCType("chromosome"),
				g.countNCType("mitochondrion"),
				g.countNCType("plast"),
				g.countNCType("plasmid"),
				g.countNCType("DNA"),
				g.countNCType("linkage"),
				g.countNCType("others")};
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		this.feuilleInfo(g.getName(), today, info, genome);
		for (NC nc : g.getNCs()) {
			System.out.println("avant feuilleNucléotide");
			this.feuilleNucleotide(nc);
			System.out.println("après feuilleNucléotide");
		}
		this.feuillesSomme();
		this.print(file);
	}
}
