package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import bioinfo.Organism;

public class DownloadThread extends Thread {
	private Thread t;
	private String kingdom;
	private volatile Vector<Organism> organisms;
	private volatile Vector<String[]> ncsList;

	public DownloadThread(Vector<Organism> organisms, String kingdom) {
		this.organisms = organisms;
		this.kingdom = kingdom; // Character.toUpperCase(kingdom.charAt(0)) + kingdom.substring(1);
		this.ncsList = new Vector<String[]>();
	}

	public Vector<Organism> getOrganisms() {
		return this.organisms;
	}
	
	/*public Vector<String[]> getNcsList() {
		return this.ncsList;
	}

	public String getKingdom() {
		return this.kingdom;
	}*/

	@Override
	public void run() {
		this.downloadNcsList(this.kingdom);
		if (this.kingdom == "Viruses") {
			this.downloadNcsList("Phages");
		}
		if (!this.ncsList.isEmpty()) {
			this.ncsList.sort(new Comparator<String[]>() {
				@Override
				public int compare(String[] nc1, String[] nc2) {
					if (nc1.length > 5 && nc2.length > 5) {
						return nc1[5].toLowerCase().compareTo(nc2[5].toLowerCase());
					}
					return 0;
				}
			});
			listNCs();
//			filterOrganisms();
		}
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

	protected void downloadNcsList(String filename) {
		URI uri = null;
		try {
			uri = new URIBuilder()
					.setScheme("https")
					.setHost("ftp.ncbi.nlm.nih.gov")
					.setPath("/genomes/GENOME_REPORTS/IDS/" + filename + ".ids")
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
					this.ncsList.add(line.split("\\t"));
				}
			} else {
				System.out.println("Get " + this.kingdom + " ids failed : " + response.getStatusLine().getReasonPhrase());
			}
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void listNCs() {
		ListIterator<Organism> itOrganism = this.organisms.listIterator();
		ListIterator<String[]> itNcs = this.ncsList.listIterator();
//		if (this.kingdom == "Viruses") {
//			System.out.println(this.kingdom + " NCs " + this.ncsList.size());
//			System.out.println(this.kingdom + " organisms " + this.organisms.size());
//		}
		int id = itOrganism.nextIndex();
		Organism o = itOrganism.next();
		String[] nc = itNcs.next();
		String ncName = this.truncateName(nc[5]);
		while (itNcs.hasNext()) {
			int namesComparison = o.getName().toLowerCase().compareTo(ncName.toLowerCase());
//			if (this.kingdom == "Viruses") {
//				System.out.println(namesComparison + " " + o.getName() + " /// " + ncName);
//			}
			if (namesComparison == 0) {
				if (nc[1].startsWith("NC_")) {
					o.addNC_ID(nc[1]);
				} 
//				else {
//					System.out.println("NOT NC " + nc[1]);
//				}
				this.organisms.set(id, o);
				if (!itNcs.hasNext()) break;
				nc = itNcs.next();
				ncName = this.truncateName(nc[5]);
//				System.out.println(nc[5]);
			} else if (namesComparison < 0) {
				if (!itOrganism.hasNext()) break;
				id = itOrganism.nextIndex();
				o = itOrganism.next();				
			} else {
//				System.out.println(o.getName());
				if (!itNcs.hasNext()) break;
				nc = itNcs.next();
				ncName = this.truncateName(nc[5]);
			}
		}
	}
	
	protected String truncateName(String s) {
		String[] nameData = s.split("\\s");
		String name = nameData.length > 1 ? nameData[0] + " " + nameData[1] : s;
		return name;
	}
	
	/*public void filterOrganisms() {
		Iterator<Organism> it = this.organisms.iterator();
		int i = 0;
		int c = 0;
		while (it.hasNext()) {
			Organism o = it.next();
			if (!o.getNCs_IDs().isEmpty()) {
				i++;
				c += o.getNCs_IDs().size();
			}
		}
		System.out.println(this.kingdom + " Has NCs : " + i + "(" + c + ")");
	}*/

//	private void parsePage(String html) {
//		Document doc = Jsoup.parse("<table>" + html + "</table>");
//		Elements trs = doc.select("tr");
//		for (Element tr : trs) {
//			Elements tds = tr.select("td");
//			if (tds.size() > 0) {
//				String name = tds.first().text();
//				String kingdom = tds.select("td:eq(1)").text();
//				String group = tds.select("td:eq(2)").text();
//				String subgroup = tds.select("td:eq(3)").text();
//				Vector<String> ncs = new Vector<String>();
//				switch (kingdom) {
//				case "Eukaryota": 
//					group = tds.select("td:eq(4)").text();
//					subgroup = tds.select("td:eq(5)").text();
//					break;
//				case "Bacteria": 
//					group = tds.select("td:eq(5)").text();
//					subgroup = tds.select("td:eq(6)").text();
//					break;
//				case "Viruses": 
//					group = tds.select("td:eq(2)").text();
//					subgroup = tds.select("td:eq(3)").text();
//					break;
//				}
//
//				Element table = tds.select("table.projects_replicons").first();
//				if (table != null) {
//					Elements links = table.select("a");
//					if (links.size() > 0) {
//						for (Element link : links) {
//							String chr = link.text();
//							if (chr.startsWith("NC_")) {
//								ncs.add(chr);
//							}
//						}
//						if (ncs.size() > 0) {
//							Organism organism = new Organism(name, kingdom, group, subgroup, ncs);
//							// System.out.println("Adding organism : " + name + " | " + this.kingdom + " | " + group + " | " + subgroup);
//							if (!this.organisms.add(organism)) {
//								System.out.println("Adding organism " + name + "failed");
//							}
//						}
//					}
//				}
//			}
//		}
//	}
}
