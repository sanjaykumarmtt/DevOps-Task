package com.san.redistool.features;

public abstract class BaseRedisTool {
	
	
	protected void showMessage(String message) {
		System.out.println(message);
	}

	protected void showError(String error) {
		System.out.println(error);
	}
	
	protected void showMessageSamLine(String message) {
		
		System.out.print(message);
	}

	protected void stopProject() {
		System.exit(0);
	}

}
