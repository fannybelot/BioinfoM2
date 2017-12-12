package bioinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Organism {
	private List<NC> NCs;			//list of NC objects
	private Vector<String> NCs_IDs = new Vector<String>();	//list of NC's IDs, needed to download them.
	private String name;
	private String kingdom;
	private String group;
	private String subGroup;
	private int numberCDS;
	private int numberInvalidCDS;
	
	public Organism() {
		
	}
	
	public Organism(String name){
		this.NCs = new ArrayList<NC>();
		this.name = name;
	}
	
	/**
	 * @param name must be a 2-word string.
	 */
	public Organism(String name, String kingdom, String group, String subGroup, Vector<String> NCs_IDs){
		this.name = name;
		this.kingdom = kingdom;
		this.group = group;
		this.subGroup = subGroup;
		this.setNCs_IDs(NCs_IDs);
	}

	public void ajouteFichier(File file) {
		NC nc = new NC();
		nc.setFichier(file);
		this.NCs.add(nc);
	}
	
	public int countNCType(String type) {
		System.out.println("je suis dans count nc type");
		int res = 0;
		for (NC nc: getNCs()){
			System.out.println("je suis dans count nc type for");
			if (nc.getType().equals(type)) {
				res +=1;
			}
		}
		return res;
	}

	public void deleteDechet(){
		for (NC nc : getNCs()){
			nc.deleteDechet();
		}
		NCs.clear();
	}
	
	public void downloadNCs() throws Exception {
		this.NCs = new ArrayList<NC>();
		NC nc;
		for (String nc_id: NCs_IDs){
			nc = new NC(nc_id);
			nc.download();
			NCs.add(nc);
		}
	}
	
	public void parse() throws Exception {
		for ( NC nc : getNCs()) {
			
			nc.parse();
		}
	}
	
	public void statistique() {
		for ( NC nc : getNCs()) {
			System.out.println("avant ncStatistique");//TODO
			nc.ncStatistique();
			System.out.println("après ncStatistique");//TODO
			numberCDS += nc.getNumberCDS();
			numberInvalidCDS += nc.getNumberInvalidCDS();
		}
	}
	
	public String getGroup() {
		return this.group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getKingdom() {
		return this.kingdom;
	}
	
	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Iterable<NC> getNCs() {
		return this.NCs;
	}
	
	public Vector<String> getNCs_IDs() {
		return NCs_IDs;
	}
	
	public int getNumberCDS() {
		return numberCDS;
	}

	public int getNumberInvalidCDS() {
		return numberInvalidCDS;
	}

	public String getSubGroup() {
		return this.subGroup;
	}
	
	public void setSubGroup(String subGroup) {
		this.subGroup = subGroup;
	}
	
	public void setNCs_IDs(Vector<String> nCs_IDs) {
		NCs_IDs = nCs_IDs;
	}
	
	public void addNC_ID (String NC) {
		this.NCs_IDs.add(NC);
	}

	public String toString(){
		//return "name : " + name + " / kingdom : " + kingdom + " / group : " + group + " / subgroup : " + subGroup;
		return name;
	}
}
