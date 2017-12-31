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
		this.name = rectifyName(name);
	}
	
	private String rectifyName(String name) {
		for (int i=0; i<name.length(); i++) {
			if(name.charAt(i)=='/') {
				char[] tab = name.toCharArray();
				tab[i] = '*';
				name = String.valueOf(tab);
			}
		}
		return name;
	}
	
	public int countNCType(String type) {
		int res = 0;
		for (NC nc: getNCs()){
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
	
	public void downloadNCs(Boolean saveGene) throws Exception {
		this.NCs = new ArrayList<NC>();
		NC nc;
		for (String nc_id: NCs_IDs){
			String filePath = this.kingdom + File.separator + this.group + File.separator + this.subGroup + File.separator + this.name;
			nc = new NC(nc_id, filePath);
			nc.setSauv(saveGene);
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
			nc.ncStatistique();
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
		this.name = rectifyName(name);
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
		if(!this.NCs_IDs.contains(NC)) {
			this.NCs_IDs.add(NC);
		}
	}

	public String toString(){
		//return "name : " + name + " / kingdom : " + kingdom + " / group : " + group + " / subgroup : " + subGroup;
		return name;
	}
}
