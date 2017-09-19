package net;

import java.io.IOException;

import org.junit.Test;

public class DownloadTest extends Download {

	@Test
	public void test() throws IOException {
		organismsToJson(getOrganisms());
	}

}
