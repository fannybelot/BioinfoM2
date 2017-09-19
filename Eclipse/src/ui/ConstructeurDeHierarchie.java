package ui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import bioinfo.Organism;

public class ConstructeurDeHierarchie {
	public static class ExceptionFichierListeGenomeAbsent extends Exception {
		private static final long serialVersionUID = 4336480942231719698L;
		public ExceptionFichierListeGenomeAbsent() { super(); }
		  public ExceptionFichierListeGenomeAbsent(String message) { super(message); }
		  public ExceptionFichierListeGenomeAbsent(String message, Throwable cause) { super(message, cause); }
		  public ExceptionFichierListeGenomeAbsent(Throwable cause) { super(cause); }
	}
	
	public static Vector<Organism> readOrganismListFromJSONFile() throws ExceptionFichierListeGenomeAbsent, IOException{
		//ouverture du fichier
    	InputStream ips;
    	try {
			ips = new FileInputStream("liste_genomes.json");
		} catch (FileNotFoundException e) {
			throw new ExceptionFichierListeGenomeAbsent();
		}
    	InputStreamReader ipsr = new InputStreamReader(ips);
    	BufferedReader br = new BufferedReader(ipsr);
    	
    	//recupération et conversion des JSON
    	Vector<Organism> listeOrgas = new Vector<Organism>();
    	String ligne;
    	JSONObject obj = new JSONObject();
    	if ((ligne = br.readLine()) != null){
			try {
				obj = new JSONObject("{\"list\": " + ligne + "}");
			} catch (org.json.JSONException e) {
				InterfaceUtilisateur.journalise("DEBUG", "Erreur lors de la désérialisation du JSON : ");
				InterfaceUtilisateur.journalise("DEBUG", e.getMessage());
				InterfaceUtilisateur.journalise("INFO", "Erreur lors de la lecture du fichier local. Abandon.");
				br.close();
				return new Vector<Organism>();
			}
		}
    	
    	JSONArray listeOrganismes = new JSONArray();
    	try {
    		listeOrganismes = obj.getJSONArray("list");
		} catch (org.json.JSONException e) {
			InterfaceUtilisateur.journalise("DEBUG", "Erreur lors de la désérialisation de la liste des organismes : ");
			InterfaceUtilisateur.journalise("DEBUG", e.getMessage());
			InterfaceUtilisateur.journalise("INFO", "Erreur lors de la lecture du fichier local. Abandon.");
			br.close();
			return new Vector<Organism>();
		}
    	
    	JSONObject organismeJsonAct;
		for (int i = 0; i < listeOrganismes.length(); i++){
			organismeJsonAct = listeOrganismes.getJSONObject(i);
			listeOrgas.add(jsonObjectToOrganism(organismeJsonAct));
		}
    	
		br.close();
		return listeOrgas;
	}
	
	public static Organism jsonObjectToOrganism(JSONObject json){
		Vector<String> liste_NCsIDs = new Vector<String>();
		JSONArray liste_IDs_json = json.getJSONArray("NCs_IDs");
		for (int i = 0; i < liste_IDs_json.length(); i++){
			liste_NCsIDs.add(liste_IDs_json.getString(i));
		}
		
		return new Organism(json.getString("name"),
					 json.getString("kingdom"),
					 json.getString("group"),
					 json.getString("subGroup"),
					 liste_NCsIDs);
	}
}
