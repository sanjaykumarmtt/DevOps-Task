package com.san.redistool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.san.redistool.features.cliapplication.CLIApplicationView;

public class RedisToolMain {
	public static final int VERSION = 1;
	public static final String VERSION_NAME = "1.0.0";

	public static void main(String[] args) {
		System.out.println("==================================================");
		System.out.println("    🚀 REDIS CLUSTER MANAGEMENT TOOL (redis-tool)   ");
		System.out.println("    ««----------- Version " + VERSION_NAME + " -----------»»");
		System.out.println("==================================================");

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		System.out.println("🕒 Started At : " + dtf.format(LocalDateTime.now()));
		System.out.println("📁 Working Dir : " + System.getProperty("user.dir"));
		System.out.println("--------------------------------------------------");
		
		System.out.println("Entering Interactive Mode. Type 'exit' to quit.\n"
				+ "--------------------------------------------------\n"
				+ "Available Commands:\n"
				+ " 💡 provision            : provision --version 7.0.15 --masters 3 --replicas-per-master 1\n"
				+ " 🐳 data seed            : data seed --keys 1000\n"
				+ " 🔍 upgrade              : upgrade --target-version 7.2.6 \n"
				+ " 📊 status               : redis-tool status.\n"
				+ " 🩺 cluster Health Check : redis-tool cluster-health-check\n"
				+ " 🔍 data verify          : redis-tool data verify\n"
				+ " 🚀 verify --full        : redis-tool verify --full\n"
				+ "--------------------------------------------------");
		
		new CLIApplicationView().init();

		//System.out.println("Proceeding to command parsing...");
	}
}