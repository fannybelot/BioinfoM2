package excel;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Reader {
	
	private XSSFWorkbook wb;
	
	public Reader(String nom){
		try{
			OPCPackage pkg = OPCPackage.open(new File(nom));
			wb = new XSSFWorkbook(pkg);
			pkg.close();
		}
		catch(IOException | InvalidFormatException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try{
			wb.close();
		}
	    catch (IOException e) {
	      e.printStackTrace();
	    } 
	}
	
	public static int[] getColonneTrinucleoInt(XSSFSheet s, int colonne){//colonne commence à 1
		int[] res = new int[64];
		XSSFRow r;
		XSSFCell cell;
		for(int i=0;i<64;i++){
			r=s.getRow(i+1);
			cell = r.getCell(colonne);
			res[i]=(int) cell.getNumericCellValue();
		}
		return res;
	}
	
	public static int[] getColonneDinucleoInt(XSSFSheet s, int colonne){
		int[] res = new int[16];
		XSSFRow r;
		XSSFCell cell;
		for(int i=0;i<16;i++){
			r=s.getRow(i+1);
			cell = r.getCell(colonne);
			res[i]=(int) cell.getNumericCellValue();
		}
		return res;
	}

	public XSSFSheet getSheet(String sheet) {
		XSSFSheet s = wb.getSheet(sheet);
		return s;
	}
	
	public static int[] getColonneInfos(XSSFSheet s, int colonne){
		int[] res = new int[2];
		XSSFRow r;
		XSSFCell cell;
		for(int i=0;i<2;i++){
			r=s.getRow(i+21);
			cell = r.getCell(colonne);
			res[i]=(int) cell.getNumericCellValue();
		}
		return res;
	}
}
