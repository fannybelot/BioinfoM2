package net.schema;


public class Test {

	@org.junit.Test
	public void test() {
		long startTime = System.currentTimeMillis();
		Primary primary = new Primary();
		primary.start();
		try {
			primary.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Primary.organisms().size());
	    long stopTime = System.currentTimeMillis();
	    long elapsedTime = (stopTime - startTime);
	    long s = elapsedTime / 1000;
	    long h = s / 3600;
	    s %= 3600;
	    long m =  s / 60;
	    s %= 60;
	    System.out.println("duration(" + elapsedTime + "ms): " + h + "h" + m + "m" + s + "s.");
	}

}
