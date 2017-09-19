package encoding;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

public class CSVReader {
	public static String DEFAULT_SEPARATOR = "\t";
	//public static String DEFAULT_QUOTE = "";
	private BufferedReader reader;
	public CSVReader(BufferedReader reader) {
		if (reader == null) {
			System.err.println("[ERROR] CSVReader.BufferedReader = null");
		}
		this.reader = reader;
	}
	
	public Vector<String[]> readAll() {
		Vector<String[]> records = new Vector<String[]>();
		String line = null;
		try {
			while((line = reader.readLine()) != null) {
				String[] record = line.split("\\"+DEFAULT_SEPARATOR);
				records.add(record);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return records;
	}
	
	public String[] read() {
		String line = null;
		String[] record = null;
		try {
			if((line = reader.readLine()) != null) {
				record = line.split("\\"+DEFAULT_SEPARATOR);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return record;
	}
}
