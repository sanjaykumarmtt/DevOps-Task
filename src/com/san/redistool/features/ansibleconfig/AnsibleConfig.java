package com.san.redistool.features.ansibleconfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import com.san.redistool.features.BaseRedisTool;
import com.san.redistool.features.data.RedisNodeDTO;

public class AnsibleConfig extends BaseRedisTool implements IAnsibleConfig {

	private static AnsibleConfig instance;

	private final String provisionPlaybook;
	private final String seedPlaybook;
	private final String verifyPlaybook;
	private final String clusterHealth;
	private final String statusPlaybook;
	private final String upgradeReplicasPlaybook;
	private final String playbook;
	
	private String dataSeedNumber;

	// ANSI Escape Codes for CLI styling
	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_CYAN = "\u001B[36m";
	private static final String ANSI_BOLD = "\u001B[1m";

	private AnsibleConfig() {

		String baseDir = System.getProperty("user.dir");

		this.provisionPlaybook = baseDir + "/ansible/playbooks/provision.yml";
		this.seedPlaybook = baseDir + "/ansible/playbooks/seed.yml";
		this.verifyPlaybook = baseDir + "/ansible/playbooks/verify.yml";
		this.clusterHealth = baseDir + "/ansible/playbooks/cluster_health.yml";
		this.statusPlaybook = baseDir + "/ansible/playbooks/status.yml";
		this.upgradeReplicasPlaybook = baseDir + "/ansible/playbooks/upgrade.yml";
		this.playbook = baseDir + "/ansible/playbooks/";

	}

	public static synchronized AnsibleConfig getInstance() {
		if (instance == null) {
			instance = new AnsibleConfig();
		}
		return instance;
	}

	public void executeProvision(String version) {
		executePlaybook(provisionPlaybook,version);
	}

	public void executeSeed(String dataSeedNumber) {
		executeDataSeedingPlaybook(seedPlaybook,dataSeedNumber);
	}

	@Override
	public String getStatus() {
		return statusPlaybook;
	}

	@Override
	public String getClusterHealth() {
		return clusterHealth;
	}
	
	public String getPlaybook() {
		return playbook;
	}

	@Override
	public String getUpgradeReplicasPlaybook() {
		// TODO Auto-generated method stub
		return upgradeReplicasPlaybook;
	}

	private void executePlaybook(String playbookPath,String version) {
		try {
			String inventoryPath = playbookPath.substring(0, playbookPath.lastIndexOf("/")) + "/../inventory/hosts.ini";

			ProcessBuilder pb = new ProcessBuilder();
			String os = System.getProperty("os.name").toLowerCase();
			pb.environment().put("ANSIBLE_HOST_KEY_CHECKING", "False");

			if (os.contains("win")) {

				String winCommand = "ansible-playbook -i " + inventoryPath + " " + playbookPath + " --extra-vars \"redis_version=" + version + "\"";
				pb.command("cmd.exe", "/c", winCommand);
			} else {
				pb.command("ansible-playbook", "-i", inventoryPath, playbookPath, "--extra-vars", "redis_version=" + version);
			}

			pb.redirectErrorStream(true);
			Process process = pb.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			StringBuilder outputBuffer = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				outputBuffer.append(line).append("\n");

					System.out.println(line);
			}

			int exitCode = process.waitFor();
			
				if (exitCode == 0) {
					showMessage("\n✅ [SUCCESS] Execution finished without errors.");
				} else {
					showError("\n❌ [FAILURE] Playbook execution failed with exit code: " + exitCode);
				}

		} catch (Exception e) {
			showError("❌ Error while executing playbook: " + e.getMessage());
		}
	}
	
	
	private void executeDataSeedingPlaybook(String playbookPath, String dataSeedNumber) {
		try {
			String inventoryPath = playbookPath.substring(0, playbookPath.lastIndexOf("/")) + "/../inventory/hosts.ini";

			ProcessBuilder pb = new ProcessBuilder();
			String os = System.getProperty("os.name").toLowerCase();
			pb.environment().put("ANSIBLE_HOST_KEY_CHECKING", "False");

			if (os.contains("win")) {
				String winCommand = "ansible-playbook -i " + inventoryPath + " " + playbookPath + " --extra-vars \"target_keys=" + dataSeedNumber + "\"";
				pb.command("cmd.exe", "/c", winCommand);
			} else {
				pb.command("ansible-playbook", "-i", inventoryPath, playbookPath, "--extra-vars", "target_keys=" + dataSeedNumber);
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
			parseAndPrintSeedResult(outputBuffer.toString(), exitCode);
			
		} catch (Exception e) {
			showError("❌ Error while executing playbook: " + e.getMessage());
		}
	}


	private void parseAndPrintSeedResult(String rawOutput, int exitCode) {
		System.out.println(ANSI_CYAN + ANSI_BOLD + "\n=== Phase 2: Data Seeding ===" + ANSI_RESET);

		String jsonPart = extractJson(rawOutput);
		if (jsonPart == null) {
			showError("❌ Could not locate JSON output in Ansible response.");
			if (exitCode != 0)
				showError("Ansible Exit Code: " + exitCode);
			System.out.println("Raw Output:\n" + rawOutput);
			return;
		}

		try {
			JSONObject root = new JSONObject(jsonPart);
			JSONObject msg = root.optJSONObject("msg");
			if (msg == null)
				msg = root;

			String status = msg.optString("status", "UNKNOWN");
			int inserted = msg.optInt("total_keys_inserted", 0);
			int failures = msg.optInt("failures", 0);

			if ("SUCCESS".equalsIgnoreCase(status) && exitCode == 0) {
				System.out.println(ANSI_GREEN + "✓ [SUCCESS] Data Seeded into Cluster." + ANSI_RESET);
				System.out.println("  ↳ Total Keys Inserted: " + inserted);
				System.out.println("  ↳ Failures: " + failures);
			} else {
				System.out.println(ANSI_RED + ANSI_BOLD + "✗ [FAIL] Seeding encountered connection or syntax errors."
						+ ANSI_RESET);
				System.out.println(ANSI_RED + "  ↳ Successful Inserts: " + inserted + ANSI_RESET);
				System.out.println(ANSI_RED + "  ↳ Failed Inserts: " + failures + ANSI_RESET);
			}
		} catch (JSONException e) {
			showError("❌ Error parsing Ansible JSON: " + e.getMessage());
		} catch (Exception e) {
			showError("❌ Unexpected CLI error: " + e.getMessage());
		}
	}
	private String extractJson(String rawOutput) {
		int startIndex = rawOutput.indexOf('{');
		int endIndex = rawOutput.lastIndexOf('}');
		if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
			return rawOutput.substring(startIndex, endIndex + 1);
		}
		return null;
	}
}