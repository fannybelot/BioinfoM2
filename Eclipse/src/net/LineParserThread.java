package net;

import java.util.Vector;

import bioinfo.Organism;

public class LineParserThread extends Thread {
	private Thread t;
	private String line;
	private volatile Organism organism;

	public LineParserThread(String line) {
		this.line = line;
		
	}

	public String getLine() {
		return this.line;
	}
	
	public Organism getOrganism() {
		return this.organism;
	}

	@Override
	public void run() {
		String[] lineData = line.split("\\t");
		if (lineData.length > 8) {
			if (lineData[5] == "-" && lineData[6] == "-" && lineData[7] == "-") {
				this.organism = null;
			} else {
				this.organism = new Organism(lineData[0]);
				this.organism.setKingdom(lineData[1]);
				this.organism.setGroup(lineData[2]);
				this.organism.setSubGroup(lineData[3]);
			}
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
}
