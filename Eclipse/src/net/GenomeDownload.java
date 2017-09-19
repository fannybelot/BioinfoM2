/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author jihad
 */
public class GenomeDownload {
	private String name;
	private String kingdom;
	private String group;
	private String subGroup;
    private URL url;
    private String res;
    private String sum;
    public GenomeDownload(String name, String kingdom, String group,
    		String subGroup, String url) {
    	
    	this.name = name;
    	this.kingdom = kingdom;
    	this.group = group;
    	this.subGroup = subGroup;
    	try {
			this.url = new URL(url);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
        URLConnection conn = null;
		try {
			conn = this.url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        BufferedReader reader = null;
		try {
			reader = new BufferedReader(
			        new InputStreamReader(
			                conn.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        String line;
        StringBuilder builder = new StringBuilder();
        try {
			while ((line = reader.readLine()) != null)
			    builder.append(line);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.res = builder.toString();
    }
    public String getName() {
		return name;
	}
    public String getKingdom() {
		return kingdom;
	}
    public String getGroup() {
		return group;
	}
    public String getSubGroup() {
		return subGroup;
	}
    public String getRes() {
		return res;
	}
    public String getSum() {
		return sum;
	}
}
