package net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.Vector;

import com.google.gson.Gson;

import bioinfo.Organism;
import ui.InterfaceUtilisateur;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class Download {
	private static ArrayList<DownloadThread> threads = new ArrayList<DownloadThread>();
	private Vector<Organism> archaea = new Vector<Organism>();
	private Vector<Organism> bacteria = new Vector<Organism>();
	private Vector<Organism> eukaryota = new Vector<Organism>();
	private Vector<Organism> viruses = new Vector<Organism>();
	private static Download instance = null;
	
   protected Download() {
      // Exists only to defeat instantiation.
   }
   public static Download getInstance() {
      if (instance == null) {
         instance = new Download();
      }
      return instance;
   }
	
	public static void getNC(String id, File local_file) throws Exception {
		Timer timer = new Timer(true);
		InterruptTimerTask interruptTimerTask = 
		    new InterruptTimerTask(Thread.currentThread());
		timer.schedule(interruptTimerTask, 12000000);
		try {
			URL url = null;
			url = new URL("https://www.ncbi.nlm.nih.gov/sviewer/viewer.cgi?tool=portal&save=file&log$=seqview&db=nuccore&report=gbwithparts&sort=&from=begin&to=end&maxplex=3&id=" + id);
			//Ancienne méthode
			try {
				FileUtils.copyURLToFile(url, local_file);
			} catch (IOException e) {
				System.out.println("Erreur dans download : "+e.getMessage()); // erreur souvent : Premature EOF
				System.out.println(id);
				e.printStackTrace();
			}
		} catch (Exception e) {
			System.out.println("Download NC timeout : " + e.getMessage());
		} finally {
		    timer.cancel();
		}
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
	
	public Vector<Organism> getOrganisms() {
		this.getOverview();
		System.out.println("Archaea " + this.archaea.size());
		System.out.println("Bacteria " + this.bacteria.size());
		System.out.println("Eukaryota " + this.eukaryota.size());
		System.out.println("Viruses " + this.viruses.size());
		Vector<Organism> organisms = new Vector<Organism>();
		threads.add(new DownloadThread(this.archaea, "Archaea"));
		threads.add(new DownloadThread(this.bacteria, "Bacteria"));
		threads.add(new DownloadThread(this.eukaryota, "Eukaryota"));
		threads.add(new DownloadThread(this.viruses, "Viruses"));
		for (DownloadThread dt : threads) {
			dt.start();
		}
		for (DownloadThread dt : threads)  {
			dt.joinThread();
			organisms.addAll(dt.getOrganisms());
		}
		System.out.println(organisms.size());
		this.filterOrganisms(organisms);
		System.out.println(organisms.size());
		Download.organismsToJson(organisms);
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
	
	public void filterOrganisms(Vector<Organism> organisms) {
		Iterator<Organism> it = organisms.iterator();
		while (it.hasNext()) {
			Organism o = it.next();
			if (o.getNCs_IDs().isEmpty()) {
				it.remove();
			}
		}
	}
	
//	public static void getOverview() {
//		DownloadThread t = new DownloadThread();
//		t.start();
//		t.joinThread();
//		System.out.println("Done");
//	}
	
	
	public void getOverview() {
		URI uri = null;
		// InterfaceUtilisateur.journalise("INFO", "Téléchargement de la hiérarchie." );
		try {
			uri = new URIBuilder()
					.setScheme("https")
					.setHost("ftp.ncbi.nlm.nih.gov")
					.setPath("/genomes/GENOME_REPORTS/overview.txt")
					.build();

			HttpGet httpget = new HttpGet(uri);
			CloseableHttpClient httpclient = HttpClientBuilder.create().build();
			HttpResponse response = null;

			response = httpclient.execute(httpget);
			// System.out.println(response.getStatusLine().getStatusCode());
			// If HTTP request is a success
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream data = response.getEntity().getContent();
				BufferedReader in = new BufferedReader(new InputStreamReader(data));
				String line;
				while ((line = in.readLine()) != null) {
					Organism o = this.parseLine(line);
					if (o != null) {
						this.addToKingdom(o);
					}
				}
			} else {
				System.out.println("Get overview failed : " + response.getStatusLine().getReasonPhrase());
			}
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Organism parseLine(String line) {
		String[] lineData = line.split("\\t");
		Organism organism = null;
		if (lineData.length > 8) {
//			if (!(lineData[5] == "-" && lineData[6] == "-" && lineData[7] == "-")) {
				String[] nameData = lineData[0].split("\\s");
				String name = nameData.length > 1 ? nameData[0] + " " + nameData[1] : lineData[0];
				organism = new Organism(name);
				organism.setKingdom(lineData[1]);
				organism.setGroup(lineData[2]);
				organism.setSubGroup(lineData[3]);
//			}
		}
//		if (organism.getName().contains("*")) {
//			System.out.println(organism.getName());
//		}
		return organism;
	}
	
	private void addToKingdom(Organism o) {
		String kingdom = o.getKingdom();
		switch (kingdom) {
		case "Archaea":
			if (this.archaea.isEmpty() || !this.archaea.lastElement().getName().toLowerCase().equals(o.getName().toLowerCase())) this.archaea.add(o);
			break;
		case "Bacteria":
			if (this.bacteria.isEmpty() || !this.bacteria.lastElement().getName().toLowerCase().equals(o.getName().toLowerCase())) this.bacteria.add(o);
			break;
		case "Eukaryota":
			if (this.eukaryota.isEmpty() || !this.eukaryota.lastElement().getName().toLowerCase().equals(o.getName().toLowerCase())) this.eukaryota.add(o);
			break;
		case "Viruses":
			if (this.viruses.isEmpty() || !this.viruses.lastElement().getName().toLowerCase().equals(o.getName().toLowerCase())) this.viruses.add(o);
			break;
		}
	}
	
	public static void killDownloadThreads(){
		for(Thread curThread : threads){
			((DownloadThread) curThread).suicide();
		}
	}
}
