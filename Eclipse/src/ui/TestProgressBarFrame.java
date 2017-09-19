package ui;
import javax.swing.*;

public class TestProgressBarFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	private JProgressBar jpb;

	public TestProgressBarFrame(){
		setLayout(null);
		setSize(400,70);
		setTitle("Loading...");
		setLocation(150,100);
		jpb = new JProgressBar(0,100);
		jpb.setBounds(10,10,375,20);
		jpb.setStringPainted(true);
		add(jpb);

	}

	public void updateProgressBarValue(int value){
		jpb.setValue(value);
	}

	public static void progress_tool(TestProgressBarFrame tpbf) {
		//TestProgressBarFrame tpbf = new TestProgressBarFrame();
		tpbf.setVisible(true);
		//for(int i=0;i<=100;i++){
		//	try{
		//		Thread.sleep(20);
		//		tpbf.updateProgressBarValue(i);
		//	}
		//	catch(InterruptedException e1) {}
		//}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tpbf.updateProgressBarValue(100);
	}

	public static void f(){
		//public static void main(String[] arg){
		TestProgressBarFrame tpbf = new TestProgressBarFrame();
		progress_tool(tpbf);

		// create our jbutton for Import
		//JButton Import = new JButton("Import");
		//Import.addActionListener(new ActionListener() {
		//    public void actionPerformed(ActionEvent e) {
		//        //progress_tool(tpbf);
		//    }
		//});
		// add the listener to the jbutton to handle the "pressed" event
		//toolBar.add("North",Import);
	}
}