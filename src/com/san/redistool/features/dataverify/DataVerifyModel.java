package com.san.redistool.features.dataverify;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DataVerifyModel implements IDataVerifyModel {
	private IDataVerifyPresenter presenter;
	private final String verifyPlaybook;

	public DataVerifyModel(IDataVerifyPresenter presenter) {
		this.presenter = presenter;
		String baseDir = System.getProperty("user.dir");
		this.verifyPlaybook = baseDir + "/ansible/playbooks/verify.yml";
	}

	public boolean executeVerify(String totalKeys) {
	    try {
	        String inventoryPath = verifyPlaybook.substring(0, verifyPlaybook.lastIndexOf("/"))
	                + "/../inventory/hosts.ini";

	        ProcessBuilder pb = new ProcessBuilder();
	        String os = System.getProperty("os.name").toLowerCase();
	        pb.environment().put("ANSIBLE_HOST_KEY_CHECKING", "False");

	        if (os.contains("win")) {
	            String winCommand = "ansible-playbook -i " + inventoryPath + " " + verifyPlaybook + " --extra-vars \"target_keys=" + totalKeys + "\"";
	            pb.command("cmd.exe", "/c", winCommand);
	        } else {
	            pb.command("ansible-playbook", "-i", inventoryPath, verifyPlaybook, "--extra-vars", "target_keys=" + totalKeys);
	        }

	        pb.redirectErrorStream(true);
	        Process process = pb.start();

	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line;
	        StringBuilder outputBuffer = new StringBuilder();

	        while ((line = reader.readLine()) != null) {
	            outputBuffer.append(line).append("\n");
//	            System.out.println(line); 
	        }

	        int exitCode = process.waitFor();
	        return presenter.onVerifyCompleted(outputBuffer.toString(), exitCode);

	    } catch (Exception e) {
	        presenter.onVerifyFailed("Error while executing verify playbook: " + e.getMessage());
	        return false;
	    }
	}

	public boolean verifyStep1Silent(String totalKeys) {
		try {
			String inventoryPath = verifyPlaybook.substring(0, verifyPlaybook.lastIndexOf("/"))
	                + "/../inventory/hosts.ini";

	        ProcessBuilder pb = new ProcessBuilder();
	        String os = System.getProperty("os.name").toLowerCase();
	        pb.environment().put("ANSIBLE_HOST_KEY_CHECKING", "False");

	        if (os.contains("win")) {
	            String winCommand = "ansible-playbook -i " + inventoryPath + " " + verifyPlaybook + " --extra-vars \"target_keys=" + totalKeys + "\"";
	            pb.command("cmd.exe", "/c", winCommand);
	        } else {
	            pb.command("ansible-playbook", "-i", inventoryPath, verifyPlaybook, "--extra-vars", "target_keys=" + totalKeys);
	        }

	        pb.redirectErrorStream(true);
	        Process process = pb.start();

	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line;
	        StringBuilder outputBuffer = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				outputBuffer.append(line).append("\n");
			}

			int exitCode = process.waitFor();

            String fullOutput = outputBuffer.toString();
            if (exitCode != 0 || fullOutput.contains("\"status\": \"FAIL\"") || fullOutput.contains("FAIL — 1000 keys missing")) {
                return false;
            }


            if (exitCode == 0) {          
                return true;
            }

            return false;

		} catch (Exception e) {
			return false;
		}
	}

}
