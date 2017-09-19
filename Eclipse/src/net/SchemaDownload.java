package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import encoding.CSVReader;

public class SchemaDownload {
	public static final int ASSEMBLY_ACCESSION_INDEX = 0;
	public static final int GROUP_INDEX = 1;
	public static final int SUBGROUP_INDEX = 2;
	public static Vector<String[]> getEukaryotes() {
		Vector<String[]> eukaryotes =  download(
				"ftp://ftp.ncbi.nlm.nih.gov/genomes/"
				+ "GENOME_REPORTS/eukaryotes.txt");
		String[] header = eukaryotes.get(0);
		int assemblyAccessionIndex = 0;
		int groupIndex = 0;
		int subGroupIndex = 0;
		for (int i = 0; i < header.length; i++) {
			if (header[i].equals("Assembly Accession")) {
				assemblyAccessionIndex = i;
				continue;
			}
			if (header[i].equals("Group")) {
				groupIndex = i;
				continue;
			}
			if (header[i].equals("SubGroup")) {
				subGroupIndex = i;
				continue;
			}
		}
		for (int i = 1; i < eukaryotes.size(); i++) {
			String[] elt = eukaryotes.get(i);
			String[] newElt = new String[3];
			newElt[ASSEMBLY_ACCESSION_INDEX] = elt[assemblyAccessionIndex];
			newElt[GROUP_INDEX] = elt[groupIndex];
			newElt[SUBGROUP_INDEX] = elt[subGroupIndex];
			eukaryotes.set(i-1, newElt);
		}
		eukaryotes.remove(eukaryotes.size() - 1);
		return eukaryotes;
	}
	private static Vector<String[]> download(String s) {
		URL url;
		try {
			url = new URL(s);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		URLConnection conn;
		try {
			conn = url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		BufferedReader reader;
		try {
			reader = new BufferedReader(
			        new InputStreamReader(
			                conn.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		CSVReader csv = new CSVReader(reader);
		return csv.readAll();
	}
}
