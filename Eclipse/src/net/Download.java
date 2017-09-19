package net;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import com.google.gson.Gson;

import bioinfo.Organism;

import org.apache.commons.io.FileUtils;

public class Download {
	private static ArrayList<DownloadThread> threads;
	public static void getNC(String id, File local_file) throws Exception {
		URL url = null;
		url = new URL("https://www.ncbi.nlm.nih.gov/sviewer/viewer.cgi?tool=portal&save=file&log$=seqview&db=nuccore&report=gbwithparts&sort=&from=begin&to=end&maxplex=3&id=" + id);
		FileUtils.copyURLToFile(url, local_file);
	}
	
//	public static void listOrganisms() throws IOException {
//		// TODO Set page to 1 for final testing and remove page < 239 condition 5 lines below
//		int page = 1;
//		Gson gson = new Gson();
//		File file = new File(System.getProperty("user.dir") + "/liste_genomes.json");
//		FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
//		// FileWriter file = new FileWriter();
//		String html;
//		ArrayList<PageParser> threads = new ArrayList<PageParser>();
//		ArrayList<Organism> organisms = new ArrayList<Organism>();
//		while ((html = getPageOrganisms(page)) != null) {
//			System.out.println("Page " + page);
//			ArrayList<Organism> pageOrganisms = parseHtmlList(html);
//			if (pageOrganisms.size() > 0) {
//				organisms.addAll(pageOrganisms);
//			}
//			System.out.println("Page " + page + " parsed");
//			page++;
//		}
//		/*	PageParser t = new PageParser(html);
//			threads.add(t);
//			t.start();
//			System.out.println(page);
//			page++;
//		}
//		
//		for (PageParser t : threads)
//		{
//			Vector<Organism> pageOrganisms = t.getOrganisms();
//			if (pageOrganisms.size() > 0) {
//				organisms.addAll(pageOrganisms);
//			}
//		}*/
//		
//		System.out.println(organisms.size());
//		gson.toJson(organisms, fileWriter);
//		fileWriter.close();
//	}
	
	public static Vector<Organism> getOrganisms() {
		Vector<Organism> organisms = new Vector<Organism>();
		ArrayList<String> kingdoms = new ArrayList<String>();
		threads = new ArrayList<DownloadThread>();
		kingdoms.add("Eukaryota");
		kingdoms.add("Bacteria");
		kingdoms.add("Viruses");
		for (String kingdom : kingdoms) {
			DownloadThread t = new DownloadThread(kingdom);
			t.start();
			threads.add(t);
		}
		for (DownloadThread dt : threads)  {
			dt.joinThread();
			organisms.addAll(dt.getOrganisms());
		}
		System.out.println(organisms.size());
		return organisms;
	}
	
	public static void organismsToJson(Vector<Organism> organisms) {
		Gson gson = new Gson();
		File file = new File(System.getProperty("user.dir") + "/liste_genomes.json");
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(file.getAbsolutePath());
			gson.toJson(organisms, fileWriter);
			fileWriter.close();	
		} catch (IOException e) {
			System.out.println("Failed to write organisms in file");
		}
	
	}
	
	public static void killDownloadThreads(){
		for(Thread curThread : threads){
			((DownloadThread) curThread).suicide();
		}
	}
}
