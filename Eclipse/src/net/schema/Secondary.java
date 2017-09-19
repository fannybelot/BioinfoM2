package net.schema;


public class Secondary extends Thread {
	@Override
	public void run() {
		while (true) {
			String kingdom = Primary.kingdom();
			String page = Primary.page();
			Task task = new Task(kingdom, page);
			try {
				Primary.add(task.go());
			} catch (JobDoneException e) {
				return;
			}
		}
	}
}
