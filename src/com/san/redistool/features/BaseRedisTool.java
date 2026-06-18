package com.san.redistool.features;

public abstract class BaseRedisTool {

	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_RESET = "\u001B[0m";

	protected void showMessage(String message) {
		System.out.println(message);
	}

	public void showError(String errorMsg) {
		System.out.println(ANSI_RED + "❌ " + errorMsg + ANSI_RESET);
	}

	protected void showMessageSamLine(String message) {
		System.out.print(message);
	}

	protected void stopProject() {
		System.exit(0);
	}

	
	
}
