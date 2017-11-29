package net.schema;

import java.util.ArrayList;
import java.util.Vector;

import bioinfo.Organism;

public final class Primary extends Thread {
	private static int JOB = 0;
	private static Job[] JOBLIST = new Job[5];
	private static Vector<Organism> ORGANISMS = new Vector<Organism>();
	private static Secondary[] SECONDARIES = new Secondary[32];
	static {
		JOBLIST[0] = new Job("Archaea");
		JOBLIST[1] = new Job("Bacteria");
		JOBLIST[2] = new Job("Eukaryota");
		JOBLIST[3] = new Job("Viroids");
		JOBLIST[4] = new Job("Viruses");
	}
	
	public static synchronized String page() {
		return JOBLIST[JOB].page();
	}
	
	public static synchronized String kingdom() {
		return JOBLIST[JOB].kingdom();
	}
	public synchronized static void add(ArrayList<Organism> organisms) {
		ORGANISMS.addAll(organisms);
	}
	
	public static synchronized Vector<Organism> organisms() {
		return ORGANISMS;
	}
	
	private void go() {
		for (int i = 0; i < SECONDARIES.length; i++) {
			SECONDARIES[i] = new Secondary();
			SECONDARIES[i].start();
		}
		for (int i = 0; i < SECONDARIES.length; i++) {
			try {
				SECONDARIES[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		while (JOB < JOBLIST.length) {
			this.go();
			JOB++;
		}
	}
}
