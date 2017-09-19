package net;

import java.util.Vector;

import org.junit.Test;

public class SchemaDownloadTest extends SchemaDownload {

	@Test
	public void getEukaryotesTest() {
		Vector<String[]> v = getEukaryotes();
		System.out.printf("[Assembly Accession] [Group] [SubGroup]\n");
		for (String[] e: v) {
			System.out.printf("[%s] [%s] [%s]\n",
					e[SchemaDownload.ASSEMBLY_ACCESSION_INDEX],
					e[SchemaDownload.GROUP_INDEX],
					e[SchemaDownload.SUBGROUP_INDEX]);
		}
	}

}
