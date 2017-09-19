package ui;

import java.io.*;

public class Tree {
	/*
	 * Construct the String about the repository
	 */
	public static String printContent (File file, int level , String t)
	{
		int j=0;
		for (File f : file.listFiles())
		{
			//risk infinite loop in Linux according to websites
			// => limit
			if( j<10000000 ){ 
				for (int i = 0; i < level; i++){
					//System.out.print ("|\t");
					t = t + "|    ";
				}
				if (f.isDirectory())
				{
				    //System.out.println ("+ " + f.getName());
				    t = t + "+ " + f.getName() +"\n";
				    t = t + printContent (f, level + 1 ,t );
				}
				else
				{
				    //System.out.println ("| " + f.getName());
				    t = t +"| " + f.getName()+"\n" ;
				}
			}
			j=j+1;		
		}
		return t;
	}
	
	public static String give_tree() {	
        String current = System.getProperty("user.dir");
        String word_dir = "src";
        System.out.println("Current working directory in Java : " + current+"/"+ word_dir +"/");		
		File file = new File (current+"/src/");
		//System.out.println ("+ " + file.getName());
		String t = "+ " + file.getName()+"\n" ;
		t = printContent (file, 1 , t );
		//System.out.println ("<================>");
		//concat t is bullshit
		String tree = t.substring(t.lastIndexOf(" " + word_dir )+1);
		//System.out.println ( tree );
		return tree;
   	}
}