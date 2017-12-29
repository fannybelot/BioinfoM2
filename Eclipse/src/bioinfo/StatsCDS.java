package bioinfo;

import java.util.List;

import excel.Writer;

public class StatsCDS {
	
	private int [] trinucleoPhase0;
	private int [] trinucleoPhase1;
	private int [] trinucleoPhase2;
	private int [] dinucleoPhase0;
	private int [] dinucleoPhase1;

	private int[] freqDiPhase0;
	private int[] freqDiPhase1;
	private int[] freqTriPhase0;
	private int[] freqTriPhase1;
	private int[] freqTriPhase2;
	
	private int totalDi;
	private int totalTri;
	
	public StatsCDS(){
		trinucleoPhase0 = new int[64];
		trinucleoPhase1 = new int[64];
		trinucleoPhase2 = new int[64];
		dinucleoPhase0 = new int[16];
		dinucleoPhase1 = new int[16];

		freqDiPhase0 = new int[16];
		freqDiPhase1 = new int[16];
		freqTriPhase0 = new int[64];
		freqTriPhase1 = new int[64];
		freqTriPhase2 = new int[64];
		
		totalDi = 0;
		totalTri = 0;
	}
	
	private void comptageDi(CDS g) {
		String s = g.getChaine();
		int l = s.length();
		List<String> list = Writer.listeNucleotide(2);
		if (l>2) {
			for (int i = 0; i <= l - 3; i = i + 2) {
				String nucl0 = s.substring(i, i + 2).toUpperCase();
				String nucl1 = s.substring(i + 1, i + 3).toUpperCase();
				for (int j = 0; j < 16; j++) {
					if (nucl0.equals(list.get(j))) {
						dinucleoPhase0[j] = dinucleoPhase0[j] + 1;
					}
					if (nucl1.equals(list.get(j))) {
						dinucleoPhase1[j] = dinucleoPhase1[j] + 1;
					}
				}
				totalDi++;
			} 
		}
		return;
	}

	private void comptageTri(CDS g) {
		String s = g.getChaine();
		int l = s.length();
		List<String> list = Writer.listeNucleotide(3);
		if (l>4) {
			for (int i = 0; i < s.length() - 4; i = i + 3) {
				String nucl0 = s.substring(i, i + 3).toUpperCase();
				String nucl1 = s.substring(i + 1, i + 4).toUpperCase();
				String nucl2 = s.substring(i + 2, i + 5).toUpperCase();
				for (int j = 0; j < 64; j++) {
					if (nucl0.equals(list.get(j))) {
						trinucleoPhase0[j] = trinucleoPhase0[j] + 1;
					}
					if (nucl1.equals(list.get(j))) {
						trinucleoPhase1[j] = trinucleoPhase1[j] + 1;
					}
					if (nucl2.equals(list.get(j))) {
						trinucleoPhase2[j] = trinucleoPhase2[j] + 1;
					}
				}
				totalTri++;
			}
		}
		return;
	}

	private void frequenceDi() {
		for (int i = 0; i < 16; i++) {
			freqDiPhase0[i] = dinucleoPhase0[i]/totalDi;
			freqDiPhase1[i] = dinucleoPhase1[i]/totalDi;
		}
		return;
	}
	private void frequenceTri() {
		for (int i = 0; i < 64; i++) {
			freqTriPhase0[i] = trinucleoPhase0[i]/totalTri;
			freqTriPhase1[i] = trinucleoPhase1[i]/totalTri;
			freqTriPhase2[i] = trinucleoPhase2[i]/totalTri;
		}
		return;
	}


	public void geneStatistique(CDS g) {
		comptageDi(g);
		frequenceDi();
		comptageTri(g);
		frequenceTri();
	}

	public int[] getTrinucleoPhase0() {
		return trinucleoPhase0;
	}

	public int[] getTrinucleoPhase1() {
		return trinucleoPhase1;
	}

	public int[] getTrinucleoPhase2() {
		return trinucleoPhase2;
	}

	public int[] getDinucleoPhase0() {
		return dinucleoPhase0;
	}

	public int[] getDinucleoPhase1() {
		return dinucleoPhase1;
	}

	public int[] getFreqDiPhase0() {
		return freqDiPhase0;
	}

	public int[] getFreqDiPhase1() {
		return freqDiPhase1;
	}

	public int[] getFreqTriPhase0() {
		return freqTriPhase0;
	}

	public int[] getFreqTriPhase1() {
		return freqTriPhase1;
	}

	public int[] getFreqTriPhase2() {
		return freqTriPhase2;
	}

	public int getTotalDi() {
		return totalDi;
	}

	public int getTotalTri() {
		return totalTri;
	}


}
