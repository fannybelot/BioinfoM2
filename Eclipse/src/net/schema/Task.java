package net.schema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import bioinfo.Organism;

public final class Task {
	private String kingdom;
	private String page;
	private volatile Vector<String[]> ncsList;

	public Task(String kingdom, String page) {
		this.kingdom = kingdom;
		this.page = page;
	}
	
	public Task(String kingdom, int page) {
		this(kingdom, String.valueOf(page));
	}
	
	public ArrayList<Organism> go() throws JobDoneException {
		String html = new String();
		StringBuilder sb = new StringBuilder();
		try {
			URI uri = new URIBuilder()
					.setScheme("https")
					.setHost("ftp.ncbi.nlm.nih.gov")
					.setPath("/genomes/GENOME_REPORTS/IDS/" + this.kingdom + ".ids")
					.build();
			
			HttpGet httpget = new HttpGet(uri);
			HttpClient httpclient = HttpClientBuilder.create().build();
			HttpResponse response = null;

			response = httpclient.execute(httpget);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream data = response.getEntity().getContent();
				BufferedReader in = new BufferedReader(new InputStreamReader(data));
				String line;
				while ((line = in.readLine()) != null) {
					this.ncsList.add(line.split("\\t"));
				}
			} else {
				System.out.println("Get " + kingdom + " (page " + page + ") failed : "
						+ response.getStatusLine().getReasonPhrase());
			}
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		html = sb.toString();
		if (html.length() < 25) {
			//System.out.println(html);
			throw new JobDoneException();
		}
			
		ArrayList<Organism> organisms = new ArrayList<Organism>();
		Document doc = Jsoup.parse("<table>" + html + "</table>");
		Elements trs = doc.select("tr");
		for (Element tr : trs) {
			Elements tds = tr.select("td");
			if (tds.size() > 0) {
				Organism organism = new Organism();
				organism.setName(tds.first().text());
				organism.setKingdom(kingdom);
				Vector<String> ncs = new Vector<String>();
				switch (kingdom) {
				case "Eukaryota": 
					organism.setGroup(tds.select("td:eq(4)").text());
					organism.setSubGroup(tds.select("td:eq(5)").text());
					break;
				case "Bacteria":
					organism.setGroup(tds.select("td:eq(5)").text());
					organism.setSubGroup(tds.select("td:eq(6)").text());
					break;
				case "Viruses":
					organism.setGroup(tds.select("td:eq(2)").text());
					organism.setSubGroup(tds.select("td:eq(3)").text());
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
							organism.setNCs_IDs(ncs);
							organisms.add(organism);
						}
					}
				}
			}
		}
		return organisms;
	}
}
