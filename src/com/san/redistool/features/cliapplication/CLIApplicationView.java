package com.san.redistool.features.cliapplication;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.san.redistool.features.BaseRedisTool;
import com.san.redistool.util.ConsoleInput;

public class CLIApplicationView extends BaseRedisTool implements ICLIApplicationView {

	private ICLIApplicationPresenterToView iCLIApplicationPresenterToView;
	private Scanner scanner;

	public CLIApplicationView() {
		this.iCLIApplicationPresenterToView = new CLIApplicationPresenter(this);
		this.scanner = ConsoleInput.getScanner();
	}

	@Override
	public void init() {
		iCLIApplicationPresenterToView.init();
	}

	@Override
	public void start() {
		Message("✓ System Readiness Verified.\nEntering Interactive Mode. Type 'exit' to quit.\nAvailable Commands: provision, data seed, verify, status, cluster-health-check, upgrade, verify --full\n--------------------------------------------------");
		while (true) {
			cliInputStatement("\nredis-tool> ");
			String rawInput = scanner.nextLine().replaceAll("[\\p{Cf}\\p{Zs}]+", " ").trim();
			if (rawInput.equalsIgnoreCase("exit")) {
				Message("Exiting redis-tool. Goodbye Sanjay!");
				stopProjectCLI();
			}
			if (rawInput.isEmpty()) {
				continue;
			}
			String lowerInput = rawInput.toLowerCase();
			if (lowerInput.startsWith("upgrade")) {
				String targetVersion = extractTargetVersion(rawInput);
				if (!targetVersion.isEmpty()) {
					iCLIApplicationPresenterToView.runUpdateContainer(targetVersion);
				} else {
					Error("❌ Invalid syntax. Required: upgrade --target-version <version> --strategy rolling");
				}
				continue; 
			}
			else if (lowerInput.startsWith("data seed")) {
				int totalKeys = extractKeysCount(rawInput);
				if (totalKeys > 0) {	
					iCLIApplicationPresenterToView.runDataSeed(String.valueOf(totalKeys));
				} else {
					Error("❌ Invalid syntax. Required: data seed --keys <count>");
				}
				continue;
			}
			else if (lowerInput.startsWith("provision")) {
				String provisionVersion = validateAndExtractProvisionVersion(rawInput);
				if (!provisionVersion.isEmpty()) {
					iCLIApplicationPresenterToView.runProvision(provisionVersion); 
				} else {
					Error("❌ Invalid syntax. Required: provision --version <version> --masters 3 --replicas-per-master 1");
				}
				continue;
			}
			switch (lowerInput) {
			case "redis-tool data verify":
				iCLIApplicationPresenterToView.runVerification();
				break;
			case "redis-tool status":
				iCLIApplicationPresenterToView.runStatus();
				break;

			case "redis-tool cluster-health-check":
				iCLIApplicationPresenterToView.runClusterHealthCheck();
				break;
				
			case "redis-tool verify --full":
				iCLIApplicationPresenterToView.runFullVerification();
				break;
			default:
				Error("❌ Invalid command: '" + rawInput + "'. Try provision, data seed, or verify.");
				break;
			}
		}
	}
	private String validateAndExtractProvisionVersion(String rawInput) {
		try {
		String regexPattern = "^provision\\s+--version\\s+([0-9]+(\\.[0-9]+)*)\\s+--masters\\s+[0-9]+\\s+--replicas-per-master\\s+[0-9]+$";			
			Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(rawInput.trim());
			if (matcher.matches()) {
				return matcher.group(1); 
			}
		} catch (Exception e) {
		}
		return ""; 
	}
	private String extractTargetVersion(String rawInput) {
		if (!rawInput.contains("--target-version")) return "";
		try {
			String afterFlag = rawInput.split("--target-version")[1].trim();
			String firstToken = afterFlag.split("\\s+")[0].trim();		
			Pattern pattern = Pattern.compile("^[0-9]+(\\.[0-9]+)*$");
			Matcher matcher = pattern.matcher(firstToken);
			if (matcher.matches()) {
				return firstToken;
			}
		} catch (Exception e) {
		}
		return "";
	}
	private int extractKeysCount(String rawInput) {
		if (!rawInput.contains("--keys")) return -1;
		try {
			String keysPart = rawInput.split("--keys")[1].trim();
			String keysValueStr = keysPart.split("\\s+")[0].trim();
			
			if (keysValueStr.matches("^[0-9]+$")) {
				return Integer.parseInt(keysValueStr);
			}
		} catch (Exception e) {
			return -1;
		}
		return -1;
	}
	private void cliInputStatement(String message) {
		System.out.print(message);
	}
	@Override
	public void Message(String message) {
		showMessage(message);
	}
	@Override
	public void Error(String error) {
		showError(error);
	}

	@Override
	public void stopProjectCLI() {
		stopProject();
	}
}

