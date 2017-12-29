package bioinfo;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class StatsNC {
	private int[] freqPrefDiPhase0;
	private int[] freqPrefDiPhase1;
	private int[] freqPrefTriPhase0;
	private int[] freqPrefTriPhase1;
	private int[] freqPrefTriPhase2;

	private int [] trinucleoPhase0;
	private int [] trinucleoPhase1;
	private int [] trinucleoPhase2;
	private int [] dinucleoPhase0;
	private int [] dinucleoPhase1;
	
	private int numberInvalidCDS;
	
	public StatsNC(){
		trinucleoPhase0 = new int[64];
		trinucleoPhase1 = new int[64];
		trinucleoPhase2 = new int[64];
		dinucleoPhase0 = new int[16];
		dinucleoPhase1 = new int[16];

		freqPrefDiPhase0 = new int[16];
		freqPrefDiPhase1 = new int[16];
		freqPrefTriPhase0 = new int[64];
		freqPrefTriPhase1 = new int[64];
		freqPrefTriPhase2 = new int[64];
		
		numberInvalidCDS = 0;
	}
	
	private void frequencePrefDi(NC nc) {
		for (CDS g : nc.getCDS()) {
			int[] freq0 = g.getStatsCDS().getFreqDiPhase0();
			int[] freq1 = g.getStatsCDS().getFreqDiPhase1();
			for (int i = 0; i < 16; i++) {
				if (freq0[i] >= freq1[i]) {
					freqPrefDiPhase0[i] += 1;
				}
				if (freq1[i] >= freq0[i]) {
					freqPrefDiPhase1[i] += 1;
				}
			}
		}
		return;
	}
	
	private void frequencePrefTri(NC nc) {
		for (CDS g : nc.getCDS()) {
			int[] freq0 = g.getStatsCDS().getFreqTriPhase0();
			int[] freq1 = g.getStatsCDS().getFreqTriPhase1();
			int[] freq2 = g.getStatsCDS().getFreqTriPhase2();
			for (int i = 0; i < 64; i++) {
				if (freq0[i] >= freq1[i] && freq0[i] >= freq2[i]) {
					freqPrefTriPhase0[i] += 1;
				}
				if (freq1[i] >= freq0[i] && freq1[i] >= freq2[i]) {
					freqPrefTriPhase1[i] += 1;
				}
				if (freq2[i] >= freq0[i] && freq2[i] >= freq1[i]) {
					freqPrefTriPhase2[i] += 1;
				}
			}
		}
		return;
	}
	
	public int[] addition(int[] A, int[] B) {
		try {
			int[] add = A;
			for (int i=0; i< B.length; i++) {
					add[i] += B[i];
			}
			return add;
		}
		catch (Exception e) {
			System.out.println("add 2 tab with different lengths !");
		}
		return A;
	}
	
	public void ncStatistique(NC nc){
		Iterator<CDS> it = nc.getCDS().iterator();
		while (it.hasNext()) {
			CDS gene = it.next();
			if (!gene.verification()) {
				numberInvalidCDS += 1;
				it.remove();
			} else {
				gene.geneStatistique();
			}
		}
		
		this.frequencePrefDi(nc);
		this.frequencePrefTri(nc);

		for (CDS cds : nc.getCDS()) {
			
			trinucleoPhase0 = addition(cds.getStatsCDS().getTrinucleoPhase0(), trinucleoPhase0);
			trinucleoPhase1 = addition(cds.getStatsCDS().getTrinucleoPhase1(), trinucleoPhase1);
			trinucleoPhase2 = addition(cds.getStatsCDS().getTrinucleoPhase2(), trinucleoPhase2);
			dinucleoPhase0 = addition(cds.getStatsCDS().getDinucleoPhase0(), dinucleoPhase0);
			dinucleoPhase1 = addition(cds.getStatsCDS().getDinucleoPhase1(), dinucleoPhase1);
		}

	}

	public int[] getFreqPrefDi(int phase) {
		if (phase == 0)
			return freqPrefDiPhase0;
		else if (phase == 1)
			return freqPrefDiPhase1;
		else
			return new int[16];
	}

	public int[] getFreqPrefTri(int phase) {
		if (phase == 0)
			return freqPrefTriPhase0;
		else if (phase == 1)
			return freqPrefTriPhase1;
		else if (phase == 2)
			return freqPrefTriPhase2;
		else
			return new int[64];
	}

	public int[] getTrinucleoPhase0(int phase) {
		if (phase == 0)
			return trinucleoPhase0;
		else if (phase == 1)
			return trinucleoPhase1;
		else if (phase == 2)
			return trinucleoPhase2;
		else
			return new int[64];
	}

	public int[] getDinucleoPhase0(int phase) {
		if (phase == 0)
			return dinucleoPhase0;
		else if (phase == 1)
			return dinucleoPhase1;
		else
			return new int[16];
	}

}
