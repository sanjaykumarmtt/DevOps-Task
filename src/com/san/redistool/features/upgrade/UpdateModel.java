package com.san.redistool.features.upgrade;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.san.redistool.features.ansibleconfig.AnsibleConfig;
import com.san.redistool.features.ansibleconfig.IAnsibleConfig;
import com.san.redistool.features.clusterhealth.ClusterHealthView;
import com.san.redistool.features.clusterhealth.IClusterHealthView;
import com.san.redistool.features.data.RedisNodeDTO;

import com.san.redistool.features.dataverify.DataVerifyView;

import com.san.redistool.features.dataverify.IDataVerifyView;
import com.san.redistool.features.states.ISattesViewGetReplicaData;
import com.san.redistool.features.states.StatusView;

public class UpdateModel implements IUpgradeModel {

	private IUpgradePresenterToModel iUpgradePresenterToModel;
	private IDataVerifyView iDataVerifyView;
	private IClusterHealthView iClusterHealthView;
	private ISattesViewGetReplicaData ISattesViewGetReplicaData;
	private IAnsibleConfig iAnsibleConfig;

	public UpdateModel(IUpgradePresenterToModel iUpgradePresenterToModel) {
		this.iUpgradePresenterToModel = iUpgradePresenterToModel;
		this.ISattesViewGetReplicaData = new StatusView();

		this.iDataVerifyView = new DataVerifyView();
		this.iClusterHealthView = new ClusterHealthView();

		this.iAnsibleConfig = AnsibleConfig.getInstance();
	}

	@Override
	public void init(String version) {
		iUpgradePresenterToModel.messages("🚀 [INIT] Starting Zero-Downtime Rolling Upgrade to v" + version);

		List<RedisNodeDTO> preFlightTopology = ISattesViewGetReplicaData.getReplicaData();
		boolean allMastersUpgraded = true;
		List<RedisNodeDTO> finalTopology = ISattesViewGetReplicaData.getReplicaData();

		if (finalTopology == null) {
			allMastersUpgraded = false;
			iUpgradePresenterToModel
					.error("🛑 [CRITICAL FAILURE] Cannot fetch final topology! Cluster connection lost.");
		} else {
			for (RedisNodeDTO node : finalTopology) {
				if (!node.getVersion().replace("v", "").trim().equals(version.replace("v", "").trim())) {
					allMastersUpgraded = false;
					break;
				}
			}
		}
		if (allMastersUpgraded) {
			iUpgradePresenterToModel
					.messages("🛑 [SKIP] All master nodes are already running the target version: v" + version);
			return;
		}

		iUpgradePresenterToModel.messages("\n--- [STEP 1] Upgrading Initial Replicas ---");
		upgradeReplica(version);

		iUpgradePresenterToModel.messages("\n--- [STEP 2] Failing Over Masters and Upgrading them One by One ---");
		List<RedisNodeDTO> initialTopology = ISattesViewGetReplicaData.getReplicaData();
		if (initialTopology == null) {
			iUpgradePresenterToModel.error("🛑 [CRITICAL FAILURE] Cannot fetch initial topology! Cluster connection lost.");
			return;
		}
		upgradeAllMastersWithZeroDowntime(initialTopology, version);

	
		iUpgradePresenterToModel.messages("\n--- [STEP 3] Post-Upgrade Verification ---");
		boolean isDataValid = iDataVerifyView.init();

		boolean allNodesUpgraded = true;
		List<RedisNodeDTO> finalTopolog = ISattesViewGetReplicaData.getReplicaData();
		
		if (finalTopolog == null) {
			allNodesUpgraded = false;
			iUpgradePresenterToModel.error("🛑 [CRITICAL FAILURE] Cannot fetch final topology! Cluster connection lost.");
		} else {
			for (RedisNodeDTO node : finalTopolog) {
				if (!node.getVersion().replace("v", "").trim().equals(version.replace("v", "").trim())) {
					allNodesUpgraded = false;
					break;
				}
			}
		}
		
		
		

		if (isDataValid && allNodesUpgraded) {
			iUpgradePresenterToModel
					.messages("\n🎉 UPGRADE COMPLETE — all nodes on v" + version + ", data integrity verified");
		} else {
			iUpgradePresenterToModel.error("\n🛑 [CRITICAL FAILURE] Verification failed! Data Valid: " + isDataValid
					+ ", All Nodes Upgraded: " + allNodesUpgraded);
		}
	}

	@Override
	public void upgradeReplica(String version) {

		if (iDataVerifyView.init() && iClusterHealthView.init()) {
			List<RedisNodeDTO> replica = ISattesViewGetReplicaData.getReplicaData();

			if (replica == null) {
				iUpgradePresenterToModel.error("🛑 [CRITICAL FAILURE] Cannot fetch replica topology! Cluster connection lost.");
				return;
			}

			String currentProjectDir = System.getProperty("user.dir");
			String replicasPlaybook = iAnsibleConfig.getUpgradeReplicasPlaybook();
			int progressCount = 1;

			for (RedisNodeDTO node : replica) {

				if ("replica".equalsIgnoreCase(node.getRole())) {

					String targetIp = node.getIpAddress().trim();

					String currentRedisVersion = node.getVersion().trim();

					if (currentRedisVersion.replace("v", "").trim().equals(version.replace("v", "").trim())) {
						iUpgradePresenterToModel
								.messages("🛑 [SKIP] Node " + targetIp + " is ALREADY on target version: v" + version);
						continue;

					}

					String targetContainerName = selectContainerName(targetIp);

					iUpgradePresenterToModel.messages("🔄 [Phase 4] Starting Rolling Upgrade for Replica Node: "
							+ targetContainerName + " (" + targetIp + ")...");

					String extraVars = String.format(
							"{\"target_host_ip\":\"%s\", \"target_node_name\":\"%s\", \"target_redis_version\":\"%s\", \"base_path\":\"%s\"}",
							targetIp, targetContainerName, version, currentProjectDir.replace("\\", "/"));

					try {
						String rawOutput = runAnsiblePlaybookLive(replicasPlaybook, extraVars, currentProjectDir);
						if (rawOutput != null
								&& rawOutput.contains("NODE_UPGRADED_SUCCESSFULLY_AND_CLUSTER_STATE_IS_OK")) {

							iUpgradePresenterToModel.messages(
									"✅ [" + progressCount + "/6] Upgraded replica " + targetIp + " — cluster: ok");
							progressCount++;

						} else {
							iUpgradePresenterToModel
									.error("❌ [CRITICAL FAILURE] Upgrade execution failed or sync timed out on node: "
											+ targetIp);
							iUpgradePresenterToModel.error(
									"⚠️ [TERMINATING] Stopping the upgrade process immediately. Remaining nodes untouched.");
							return;
						}

					} catch (Exception e) {
						iUpgradePresenterToModel.error("❌ [EXCEPTION] Runtime error occurred during upgrade on node "
								+ targetIp + ": " + e.getMessage());
						iUpgradePresenterToModel.error("⚠️ [TERMINATING] Emergency halt triggered.");
						return;
					}
				}
			}

			iUpgradePresenterToModel
					.messages("\n🎉 [SUCCESS] All Replica nodes upgraded to v" + version + " successfully!");

		} else {
			iUpgradePresenterToModel.error("\n🛑 [CRITICAL FAILURE] Phase 4 Pre-flight checks failed!");
			iUpgradePresenterToModel
					.error("❌ Cluster health state is NOT 'ok' or 1000 keys baseline integrity is compromised.");
			iUpgradePresenterToModel
					.error("⚠️ [TERMINATING] Stopping the upgrade process immediately before touching any node.");
			return;
		}

	}

	private String selectContainerName(String targetIp) {
		String targetContainerName = "";
		if (targetIp.contains("10.10.0.14")) {
			targetContainerName = "redis-node-4";
		} else if (targetIp.contains("10.10.0.15")) {
			targetContainerName = "redis-node-5";
		} else if (targetIp.contains("10.10.0.16")) {
			targetContainerName = "redis-node-6";
		} else if (targetIp.contains("10.10.0.11")) {
			targetContainerName = "redis-node-1";
		} else if (targetIp.contains("10.10.0.12")) {
			targetContainerName = "redis-node-2";
		} else if (targetIp.contains("10.10.0.13")) {
			targetContainerName = "redis-node-3";
		}

		iUpgradePresenterToModel.messages("🔄 [Phase 4] Starting Rolling Upgrade for Replica Node: "
				+ targetContainerName + " (" + targetIp + ")...");
		return targetContainerName;
	}

	private String runAnsiblePlaybookLive(String playbookPath, String extraVars, String currentProjectDir) {
		StringBuilder outputBuffer = new StringBuilder();

		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.environment().put("ANSIBLE_HOST_KEY_CHECKING", "False");

			String inventoryPath = currentProjectDir + "/ansible/inventory/hosts.ini";
			String os = System.getProperty("os.name").toLowerCase();

			if (os.contains("win")) {
				pb.command("cmd.exe", "/c", "ansible-playbook", "-i", inventoryPath, playbookPath, "--extra-vars",
						extraVars);
			} else {
				pb.command("ansible-playbook", "-i", inventoryPath, playbookPath, "--extra-vars", extraVars);
			}

			pb.redirectErrorStream(true);
			Process process = pb.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				outputBuffer.append(line).append("\n");
				iUpgradePresenterToModel.messages(line);
			}

			int exitCode = process.waitFor();

			if (exitCode != 0) {
				iUpgradePresenterToModel.messages("❌ Ansible playbook exited with error code: " + exitCode);
			}

		} catch (Exception e) {
			iUpgradePresenterToModel.error("❌ Error while executing Ansible playbook: " + e.getMessage());
			return null;
		}
		return outputBuffer.toString();
	}

	public void upgradeAllMastersWithZeroDowntime(List<RedisNodeDTO> redisNodeList, String version) {

		if (redisNodeList == null) {
			iUpgradePresenterToModel.error("🛑 [CRITICAL FAILURE] Topology list is null!");
			return;
		}

		List<RedisNodeDTO> replicaNodes = new ArrayList<>();
		for (RedisNodeDTO node : redisNodeList) {
			if ("REPLICA".equalsIgnoreCase(node.getRole())) {
				replicaNodes.add(node);
			}
		}

		for (int i = 0; i < replicaNodes.size(); i++) {
			RedisNodeDTO replicaNode = replicaNodes.get(i);

			String replicaIp = replicaNode.getIpAddress();
			String replicaPort = replicaNode.getPort();

			String oldMasterIpWithPort = replicaNode.getReplicatingMaster();
			String oldMasterIp = oldMasterIpWithPort.contains(":") ? oldMasterIpWithPort.split(":")[0]
					: oldMasterIpWithPort;

			System.out.println("\n🔄 [" + (i + 1) + "/" + replicaNodes.size()
					+ "] Starting Rolling Upgrade for Master: " + oldMasterIp);

			try {
				iUpgradePresenterToModel.messages("🚀 Triggering CLUSTER FAILOVER on Replica: " + replicaIp);

				String statusPath = iAnsibleConfig.getStatus();
				String inventoryPath = statusPath.substring(0, statusPath.lastIndexOf("/")) + "/../inventory/hosts.ini";
				String playbookPath = statusPath.substring(0, statusPath.lastIndexOf("/")) + "/failover_node.yml";
				String os = System.getProperty("os.name").toLowerCase();

				ProcessBuilder playbookPb = new ProcessBuilder();
				String extraVarsFailover = String.format("target_replica_ip=%s target_replica_port=%s", replicaIp, replicaPort);
				if (os.contains("win")) {
					playbookPb.command("cmd.exe", "/c", "ansible-playbook", "-i", inventoryPath, playbookPath, "--extra-vars", extraVarsFailover);
				} else {
					playbookPb.command("ansible-playbook", "-i", inventoryPath, playbookPath, "--extra-vars", extraVarsFailover);
				}
				playbookPb.environment().put("ANSIBLE_HOST_KEY_CHECKING", "False");
				playbookPb.inheritIO();
				Process playbookProcess = playbookPb.start();
				int exitCode = playbookProcess.waitFor();

				if (exitCode != 0) {
					iUpgradePresenterToModel.error("❌ Failover playbook failed for replica: " + replicaIp);
					iUpgradePresenterToModel.error("⚠️ [TERMINATING] Stopping the upgrade process immediately.");
					return;
				}

				// ❌ இந்த வரியை தேடிப்பிடிங்க சஞ்சாய்:
				iUpgradePresenterToModel.messages("⏳ Waiting for success response from " + replicaIp + "...");

				// ✂️ அங்கிருந்து கீழே இருக்கிற பழைய 'for' லூப் மொத்தத்தையும் தூக்கிட்டு...
				// ✅ இந்த கோடை மட்டும் அப்படியே அந்த இடத்துல ஒட்டிடுங்க தல:

				iUpgradePresenterToModel.messages("⏳ Waiting for success response inside Ansible check playbook...");

				ProcessBuilder checkPb = new ProcessBuilder();
				String checkPlaybookPath = iAnsibleConfig.getPlaybook() + "/check_failover_status.yml";
				if (os.contains("win")) {
					checkPb.command("cmd.exe", "/c", "ansible-playbook", "-i", inventoryPath, checkPlaybookPath, "--extra-vars", extraVarsFailover);
				} else {
					checkPb.command("ansible-playbook", "-i", inventoryPath, checkPlaybookPath, "--extra-vars", extraVarsFailover);
				}

				checkPb.environment().put("ANSIBLE_HOST_KEY_CHECKING", "False");
				checkPb.redirectErrorStream(true);

				Process checkProcess = checkPb.start();

				StringBuilder ansibleOutput = new StringBuilder();
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						ansibleOutput.append(line).append("\n");
						iUpgradePresenterToModel.messages(line);
					}
				}
				int exCode = checkProcess.waitFor();

				if (exCode == 0
						&& ansibleOutput.toString().contains("FAILOVER_COMPLETED_SUCCESSFULLY_AND_NODE_IS_MASTER")) {
					iUpgradePresenterToModel.messages(
							"\n✅ Success Response Received! " + replicaIp + " It has now become a new master copy..");
				} else {
					iUpgradePresenterToModel.error("\n❌ Timeout or Failure! Master validation failed inside Ansible.");
					iUpgradePresenterToModel.error("⚠️ [TERMINATING] Stopping the upgrade process immediately.");
					return;
				}

				iUpgradePresenterToModel.messages("🛠️ Old Master (" + oldMasterIp
						+ ") has now transformed into a replica. The upgrade is starting...");

				String currentProjectDir = System.getProperty("user.dir");
				String replicasPlaybook = iAnsibleConfig.getUpgradeReplicasPlaybook();
				String targetContainerName = selectContainerName(oldMasterIp);

				String extraVars = String.format(
						"{\"target_host_ip\":\"%s\", \"target_node_name\":\"%s\", \"target_redis_version\":\"%s\", \"base_path\":\"%s\"}",
						oldMasterIp, targetContainerName, version, currentProjectDir.replace("\\", "/"));

				String rawOutput = runAnsiblePlaybookLive(replicasPlaybook, extraVars, currentProjectDir);
				if (rawOutput != null && rawOutput.contains("NODE_UPGRADED_SUCCESSFULLY_AND_CLUSTER_STATE_IS_OK")) {
					iUpgradePresenterToModel.messages("✅ [" + (i + 1) + "/" + replicaNodes.size()
							+ "] Upgraded old master " + oldMasterIp + " — cluster: ok");
				} else {
					iUpgradePresenterToModel
							.error("[CRITICAL FAILURE] Upgrade execution failed on demoted master: " + oldMasterIp);
					return;
				}

				iUpgradePresenterToModel
						.messages("👍 [" + (i + 1) + "/" + replicaNodes.size() + "] Successfully completed iteration!");

				iUpgradePresenterToModel.messages("⏳ Waiting 5 seconds for cluster topology to stabilize...");
				Thread.sleep(5000);

			} catch (Exception e) {
				iUpgradePresenterToModel.error("Error in iteration " + i + ": " + e.getMessage());
			}
		}
	}

	@Override
	public void upgradeAllMastersWithZeroDowntime(List<RedisNodeDTO> redisNodeList) {
		upgradeAllMastersWithZeroDowntime(redisNodeList, "7.2.6");
	}
}
