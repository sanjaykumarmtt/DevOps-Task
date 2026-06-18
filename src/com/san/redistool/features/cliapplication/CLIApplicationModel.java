package com.san.redistool.features.cliapplication;

import com.san.redistool.features.ansibleconfig.AnsibleConfig;
import com.san.redistool.features.ansibleconfig.IAnsibleConfig;
import com.san.redistool.features.clusterhealth.ClusterHealthView;
import com.san.redistool.features.clusterhealth.IClusterHealthView;
import com.san.redistool.features.dataverify.DataVerifyPresenter;
import com.san.redistool.features.dataverify.DataVerifyView;
import com.san.redistool.features.dataverify.IDataVerifyPresenter;
import com.san.redistool.features.dataverify.IDataVerifyView;
import com.san.redistool.features.fullverification.FullVerificationView;
import com.san.redistool.features.fullverification.IFullVerificationView;
import com.san.redistool.features.states.IStatesView;
import com.san.redistool.features.states.StatusView;
import com.san.redistool.features.upgrade.IUpdateView;
import com.san.redistool.features.upgrade.UpgradeView;

public class CLIApplicationModel implements ICLIApplicationModel {

	private ICLIApplicationPresenterToModel iCLIApplicationPresenterToModel;
	private IAnsibleConfig iAnsibleConfig;
	private IClusterHealthView iClusterHealthView;
	private IDataVerifyView iDataVerifyView;
	private IStatesView iStatesView;
	private IUpdateView iUpgradeView;
	private IFullVerificationView iFullVerificationView;

	public CLIApplicationModel(ICLIApplicationPresenterToModel iCLIApplicationPresenterToModel) {
		this.iCLIApplicationPresenterToModel = iCLIApplicationPresenterToModel;
		this.iAnsibleConfig = AnsibleConfig.getInstance();
		this.iClusterHealthView = new ClusterHealthView();

		this.iDataVerifyView = new DataVerifyView();
		this.iFullVerificationView = new FullVerificationView();

		this.iStatesView = new StatusView();
		this.iUpgradeView = new UpgradeView();
	}

	@Override
	public void init() {
		chikDorckerAndAnsible();
	}
	
	@Override
	public void chikDorckerAndAnsible() {
		String podmanVersionOut = getCommandOutput("podman --version");
		String dockerVersionOut = getCommandOutput("docker --version");
		String ansibleVersionOut = getCommandOutput("ansible-playbook --version");

		boolean hasContainerRuntime = false;
		String containerMessage = "";

		if (podmanVersionOut != null && !podmanVersionOut.isEmpty()) {
			hasContainerRuntime = true;
			containerMessage = "✓ " + podmanVersionOut.trim() + " found";
		} else if (dockerVersionOut != null && !dockerVersionOut.isEmpty()) {
			hasContainerRuntime = true;
			containerMessage = "✓ " + dockerVersionOut.trim() + " found";
		}

		boolean hasAnsible = false;
		String ansibleMessage = "";
		if (ansibleVersionOut != null && !ansibleVersionOut.isEmpty()) {
			String[] lines = ansibleVersionOut.split("\n");
			if (lines.length > 0) {
				String firstLine = lines[0];
				java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+\\.\\d+(\\.\\d+)?)").matcher(firstLine);
				if (m.find()) {
					String version = m.group(1);
					String[] parts = version.split("\\.");
					int major = Integer.parseInt(parts[0]);
					int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
					
					if (major > 2 || (major == 2 && minor >= 14)) {
						hasAnsible = true;
						ansibleMessage = "✓ Ansible " + version + " found";
					} else {
						ansibleMessage = "Ansible version " + version + " is too low. Required 2.14+.";
					}
				}
			}
		}

		if (hasContainerRuntime && hasAnsible) {
			iCLIApplicationPresenterToModel.Message(containerMessage);
			iCLIApplicationPresenterToModel.Message(ansibleMessage);
			iCLIApplicationPresenterToModel.Message("Proceeding...\n-----------------------------------------");
			iCLIApplicationPresenterToModel.start();
		} else {
			if (!hasContainerRuntime) {
				iCLIApplicationPresenterToModel.Error(
						"✗ Container runtime not found (Docker or Podman)\n" +
						"  Install Podman: https://podman.io/docs/installation\n" +
						"  Install Docker: https://docs.docker.com/engine/install/");
			}
			if (!hasAnsible) {
				if (!ansibleMessage.isEmpty()) {
					iCLIApplicationPresenterToModel.Error("✗ " + ansibleMessage);
				} else {
					iCLIApplicationPresenterToModel.Error("✗ Ansible not found");
				}
				iCLIApplicationPresenterToModel.Error("  Install: pip install ansible (or use your OS package manager)");
			}
			iCLIApplicationPresenterToModel.Error("\nPlease install the missing dependencies and retry.");
			System.exit(1);
		}
	}

	private static String getCommandOutput(String command) {
		try {
			Process process;
			String os = System.getProperty("os.name").toLowerCase();

			if (os.contains("win")) {
				process = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", command });
			} else {
				process = Runtime.getRuntime().exec(command);
			}
			
			if (process.waitFor() == 0) {
				java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
				StringBuilder output = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}
				return output.toString().trim();
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public void runProvision(String version) {
		iCLIApplicationPresenterToModel.Message("🚀 [Initiating] Deploying Redis Cluster Infrastructure...");
		iAnsibleConfig.executeProvision(version);
	}

	public void runDataSeed(String dataSeedNumber) {
		iCLIApplicationPresenterToModel.Message("🚀 [Initiating] Seeding Data into Redis Cluster...");
		iAnsibleConfig.executeSeed(dataSeedNumber);
	}

	public void runVerification() {
		iCLIApplicationPresenterToModel.Message("🚀 [Initiating] Verifying Redis Cluster Data Integrity...");
		if(!iDataVerifyView.init()) iCLIApplicationPresenterToModel.Error("Redis cluster data is empty or not stored yet.");
	}

	@Override
	public void runStatus() {
		iCLIApplicationPresenterToModel.Message("🚀 [Initiating] Check Redis Cluster Status...");
		iStatesView.init();
	}

	@Override
	public void runClusterHealthCheck() {
		iCLIApplicationPresenterToModel.Message("🚀 [Initiating] Overall Redis Cluster Health Check...");
		iClusterHealthView.init();
	}

	@Override
	public void runUpdateContainer(String version) {
		iCLIApplicationPresenterToModel.Message("🚀 [Initiating] Update Redis Container...");
		iUpgradeView.init(version);
	}

	@Override
	public void runFullVerification() {
		iCLIApplicationPresenterToModel.Message("🚀 [Initiating] Full Verification Redis Container...");
		iFullVerificationView.init();

	}

}
