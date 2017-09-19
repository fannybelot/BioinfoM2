package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ui.InterfaceUtilisateur;
import ui.PanneauHierarchie;
import bioinfo.Organism;

public class DownloadThread extends Thread {
	private Thread t;
	private String kingdom;
	private volatile Vector<Organism> organisms;

	public DownloadThread(String kingdom) {
		this.kingdom = kingdom;
		this.organisms = new Vector<Organism>();
	}

	public Vector<Organism> getOrganisms() {
		return organisms;
	}

	@Override
	public void run() {
		int page = 1;
		String html = new String();
		while ((html = this.getPage(this.kingdom, page)).length() > 25) {
			this.parsePage(html);
			// System.out.println("Fetching page " + page);
			page++;
			PanneauHierarchie.incrementeNombreDePageTraites();
			InterfaceUtilisateur.journalise("INFO", "Téléchargement et traitement de la page n°" + page + " des " + this.kingdom );
		} // Empty table returns 21-length string
		System.out.println(organisms.size() + " " + this.kingdom + " on " + page + " pages");
	}

	@Override
	public void start () {
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void suicide() {
		t.interrupt();
		t.stop();
	}

	public void joinThread () {
		if (t != null) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// Get page #page for kingdom kingdom
	protected String getPage(String kingdom, int page) {
		// System.out.println("Fetching page " + page);
		String html = new String();
		URI uri = null;
		try {
			uri = new URIBuilder()
					.setScheme("https")
					.setHost("www.ncbi.nlm.nih.gov")
					.setPath("/genomes/Genome2BE/genome2srv.cgi")
					.setParameter("action", "GetGenomes4Grid")
					.setParameter("king", kingdom)
					.setParameter("mode", "2")
					.setParameter("page", Integer.toString(page))
					.setParameter("pageSize", "100")
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
					html = html.concat(line);
				}
			} else {
				System.out.println("Get " + kingdom + " (page " + page + ") failed : " + response.getStatusLine().getReasonPhrase());
			}
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return html;
	}

	private void parsePage(String html) {
		Document doc = Jsoup.parse("<table>" + html + "</table>");
		Elements trs = doc.select("tr");
		for (Element tr : trs) {
			Elements tds = tr.select("td");
			if (tds.size() > 0) {
				String name = tds.first().text();
				String group = new String();
				String subgroup = new String();
				Vector<String> ncs = new Vector<String>();
				switch (this.kingdom) {
				case "Eukaryota": 
					group = tds.select("td:eq(4)").text();
					subgroup = tds.select("td:eq(5)").text();
					break;
				case "Bacteria": 
					group = tds.select("td:eq(5)").text();
					subgroup = tds.select("td:eq(6)").text();
					break;
				case "Viruses": 
					group = tds.select("td:eq(2)").text();
					subgroup = tds.select("td:eq(3)").text();
					break;
				}

				Element table = tds.select("table.projects_replicons").first();
				if (table != null) {
					Elements links = table.select("a");
					if (links.size() > 0) {
						for (Element link : links) {
							String chr = link.text();
							if (chr.startsWith("NC_")) {
								ncs.add(chr);
							}
						}
						if (ncs.size() > 0) {
							Organism organism = new Organism(name, this.kingdom, group, subgroup, ncs);
							// System.out.println("Adding organism : " + name + " | " + this.kingdom + " | " + group + " | " + subgroup);
							if (!this.organisms.add(organism)) {
								System.out.println("Adding organism " + name + "failed");
							}
						}
					}
				}
			}
		}
	}
}
