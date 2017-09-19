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

public class DownloadNCs extends Thread {
	private Thread t;
	private String id;
	private volatile Vector<String> ncs;

	public DownloadNCs(String id) {
		this.id = id;
		this.ncs = new Vector<String>();
	}

	public void run() {
		// System.out.println("Running " + this.id);
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
				if (response.getStatusLine().getStatusCode() != 200) {
					System.out.println(response.getStatusLine().getReasonPhrase());
					return;
				}
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
							this.ncs.add(chromosome);
						}
					}
					// System.out.println(ncs.size());
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
		// System.out.println("Thread " +  this.id + " exiting.");
	}

	public void start () {
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}

	public Vector<String> getNcs() {
		try {
			t.join();
			// System.out.println("Thread joined");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(ncs.size());
		return this.ncs;
	}

	public void setNcs(Vector<String> ncs) {
		this.ncs = ncs;
	}
}
