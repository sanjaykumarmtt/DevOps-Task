package com.san.redistool.util;

import java.util.Scanner;

public class ConsoleInput {

	private final static Scanner scanner=new Scanner(System.in);

	private ConsoleInput() {}
	
	public static Scanner getScanner() {
		return scanner;
	}
	
}
