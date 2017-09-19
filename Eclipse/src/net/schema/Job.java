package net.schema;


public class Job {
	private int page;
	private String kingdom; 
	
	public Job(String kingdom) {
		this.page = 0;
		this.kingdom = kingdom;
	}
	
	public String page() {
		return String.valueOf(++this.page);
	}
	
	public String kingdom() {
		return this.kingdom;
	}
}
