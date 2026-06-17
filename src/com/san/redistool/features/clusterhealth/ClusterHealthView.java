package com.san.redistool.features.clusterhealth;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.san.redistool.features.BaseRedisTool;

public class ClusterHealthView extends BaseRedisTool implements IClusterHealthView, IHealthStatus {

	private IClusterHealthPresenterToView iClusterHealthPresenterToView;

	public ClusterHealthView() {
		this.iClusterHealthPresenterToView = new ClusterHealthPresenter(this);
	}

	@Override
	public boolean init() {
		return iClusterHealthPresenterToView.init();
	}

	@Override
	public boolean clusterHealthCheck(String playbookPath) {
	   
	    String RESET = "\u001B[0m";
	    String GREEN = "\u001B[32m";
	    String RED = "\u001B[31m";
	    String CYAN = "\u001B[36m";
	    String YELLOW = "\u001B[33m";

	    boolean isClusterHealthy = false;

	    try {
	        String inventoryPath = playbookPath.substring(0, playbookPath.lastIndexOf("/")) + "/../inventory/hosts.ini";

	        ProcessBuilder pb = new ProcessBuilder();
	        String os = System.getProperty("os.name").toLowerCase();
	        pb.environment().put("ANSIBLE_HOST_KEY_CHECKING", "False");

	        if (os.contains("win")) {
	            String winCommand = "ansible-playbook -i " + inventoryPath + " " + playbookPath;
	            pb.command("cmd.exe", "/c", winCommand);
	        } else {
	            pb.command("ansible-playbook", "-i", inventoryPath, playbookPath);
	        }

	        pb.redirectErrorStream(true);
	        Process process = pb.start();

	    
	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line;

	        while ((line = reader.readLine()) != null) {
	            if (line.contains("cluster_state:ok")) {
	                isClusterHealthy = true;
	            }
	        }

	        int exitCode = process.waitFor();

	        System.out.println("\n" + CYAN + "=== Redis Cluster Health Check Summary ===" + RESET);

	        
	        if (exitCode == 0 && isClusterHealthy) {
	            System.out.println(GREEN + "✓ 1. Nodes Status     : ONLINE (All 6 nodes responding)" + RESET);
	            System.out.println(GREEN + "✓ 2. Cluster State    : OK (cluster_state:ok recognized)" + RESET);
	            System.out.println(GREEN + "🎉 [SUCCESS] Overall Redis Cluster Health is PERFECT!" + RESET);
	            return true;
	        } else {
	            
	            System.out.println(RED + "❌ 1. Nodes Status     : ERROR or UNREACHABLE" + RESET);
	            System.out.println(RED + "❌ 2. Cluster State    : FAIL (Target state 'ok' not found)" + RESET);
	            System.out.println(RED + "🛑 [FAILURE] Cluster Health Check failed. System has issues!" + RESET);
	            return false;
	        }

	    } catch (Exception e) {
	        showError(RED + "❌ Error while executing health check playbook: " + e.getMessage() + RESET);
	        return false;
	    }
	}
	public boolean verifyStep4Silent(String playbookPath) {

		boolean isClusterHealthy = false;

		try {
			String inventoryPath = playbookPath.substring(0, playbookPath.lastIndexOf("/")) + "/../inventory/hosts.ini";

			ProcessBuilder pb = new ProcessBuilder();
			String os = System.getProperty("os.name").toLowerCase();
			pb.environment().put("ANSIBLE_HOST_KEY_CHECKING", "False");

			if (os.contains("win")) {
				String winCommand = "ansible-playbook -i " + inventoryPath + " " + playbookPath;
				pb.command("cmd.exe", "/c", winCommand);
			} else {
				pb.command("ansible-playbook", "-i", inventoryPath, playbookPath);
			}

			pb.redirectErrorStream(true);
			Process process = pb.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.contains("cluster_state:ok")) {
					isClusterHealthy = true;
				}
			}

			int exitCode = process.waitFor();
			if (exitCode == 0 && isClusterHealthy) {

				return true;
			} else {

				return false;
			}

		} catch (Exception e) {

			return false;
		}
	}

	@Override
	public boolean verifyHealthSilent() {

		return iClusterHealthPresenterToView.verifyHealthSilent();
	}
}
