package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import bioinfo.Organism;

public class PageParser extends Thread {
	private Thread t;
	private String htmlTable;
	private volatile Vector<Organism> organisms;

	public PageParser(String htmlTable) {
		this.htmlTable = htmlTable;
		this.organisms = new Vector<Organism>();
	}

	public void run() {
		//Hashtable<DownloadNCs, Organism> threads = new Hashtable<DownloadNCs, Organism>();
		Document doc = Jsoup.parse("<table>" + this.htmlTable + "</table>");
		Elements trs = doc.select("tr");
		for (Element tr : trs) {
			Elements tds = tr.select("td");
			if (tds.size() > 0) {
				String name = tds.select(":eq(0) a:eq(0)").text();
				String id = tds.select(":eq(0) a:eq(0)").attr("href").substring(8);
				String kingdom = tds.select(":eq(1)").text();
				String group = tds.select(":eq(2)").text();
				String subgroup = tds.select(":eq(3)").text();
				/* Organism organism = new Organism(name, kingdom, group, subgroup, new Vector<String>());
				DownloadNCs t = new DownloadNCs(id);
				threads.put(t, organism);
				t.start(); */
				Organism organism = new Organism(name, kingdom, group, subgroup, downloadNCs(id));
				if (organism.getNCs_IDs().size() > 0) {
					this.organisms.add(organism); // Add to ArrayList
				}
			}
		}

		// Foreach thread and associated organism :
		/*for (Map.Entry<DownloadNCs, Organism> entry : threads.entrySet())
		{
			Vector<String> ncs = entry.getKey().getNcs(); // Join thread and get NCs list from it
			// If organism has NCs
			if (ncs.size() > 0) {
				Organism organism = entry.getValue(); // Set organism
				organism.setNCs_IDs(ncs); // Set NCs
				this.organisms.add(organism); // Add to ArrayList
			}
		}*/
	}

	public void start () {
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}

	public Vector<Organism> getOrganisms() {
		try {
			t.join();
			// System.out.println("Thread joined");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(ncs.size());
		if (this.organisms == null) {
			return new Vector<Organism>();
		}
		return this.organisms;
	}

	public void setOrganisms(Vector<Organism> organisms) {
		this.organisms = organisms;
	}
	
	public Vector<String> downloadNCs(String id) {
		Vector<String> ncs = new Vector<String>();
		URI uri = null;
		try {
			uri = new URIBuilder()
					.setScheme("https")
					.setHost("www.ncbi.nlm.nih.gov")
					.setPath("/genome/" + id)
					.build();

			HttpGet httpget = new HttpGet(uri);
			CloseableHttpClient httpclient = HttpClientBuilder.create().build();
			HttpResponse response = null;
			String html = new String("");
			try {
				response = httpclient.execute(httpget);
				if (response.getStatusLine().getStatusCode() == 200) {
					InputStream data = response.getEntity().getContent();
					BufferedReader in = new BufferedReader(new InputStreamReader(data));
					String line;
					while((line = in.readLine()) != null) {
						html = html.concat(line);
					}
					Document doc = Jsoup.parse(html);
					Element table = doc.select("table.GenomeList2").first();
					if (table != null) {
						Elements trs = table.select("tr[align]");
						int refseqIndex = table.select("th:contains(RefSeq)").first().siblingIndex();
						for (Element tr : trs) {
							String chromosome = tr.select("td:eq(" + refseqIndex + ") a:eq(0)").text();
							if (chromosome.startsWith("NC_")) {
								ncs.add(chromosome);
							}
						}
						// System.out.println(ncs.size());
					}
				} else {
					System.out.println(response.getStatusLine().getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				System.out.println("Request failed");
				//e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		return ncs;
		// System.out.println("Thread " +  this.id + " exiting.");
	}
}
