package encoding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

//import static org.junit.Assert.*;

import org.junit.Test;

public class CSVReaderTest {
	@Test
	public void readAllTest() {
		String str = "a\tb\tc\td\n1\t2\t3\t4\n5\t6\t7\t8";
		InputStream is = new ByteArrayInputStream(str.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		CSVReader csvr = new CSVReader(br);
		Vector<String[]> r = csvr.readAll();
		assertEquals("a", r.elementAt(0)[0]);
		assertEquals("b", r.elementAt(0)[1]);
		assertEquals("c", r.elementAt(0)[2]);
		assertEquals("d", r.elementAt(0)[3]);
		assertEquals("1", r.elementAt(1)[0]);
		assertEquals("2", r.elementAt(1)[1]);
		assertEquals("3", r.elementAt(1)[2]);
		assertEquals("4", r.elementAt(1)[3]);
		assertEquals("5", r.elementAt(2)[0]);
		assertEquals("6", r.elementAt(2)[1]);
		assertEquals("7", r.elementAt(2)[2]);
		assertEquals("8", r.elementAt(2)[3]);
	}
	@Test
	public void readTest() {
		String str = "a\tb\tc\td\n1\t2\t3\t4\n5\t6\t7\t8";
		InputStream is = new ByteArrayInputStream(str.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		CSVReader csvr = new CSVReader(br);
		String[] r;
		r = csvr.read();
		assertEquals("a", r[0]);
		assertEquals("b", r[1]);
		assertEquals("c", r[2]);
		assertEquals("d", r[3]);
		r = csvr.read();
		assertEquals("1", r[0]);
		assertEquals("2", r[1]);
		assertEquals("3", r[2]);
		assertEquals("4", r[3]);
		r = csvr.read();
		assertEquals("5", r[0]);
		assertEquals("6", r[1]);
		assertEquals("7", r[2]);
		assertEquals("8", r[3]);
		r = csvr.read();
		assertNull(r);
	}
}
