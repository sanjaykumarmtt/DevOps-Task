package com.san.redistool.features.states;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.san.redistool.features.ansibleconfig.AnsibleConfig;
import com.san.redistool.features.ansibleconfig.IAnsibleConfig;
import com.san.redistool.features.data.RedisNodeDTO;

public class StatesModel implements IStatesModel {

	private IStatesPresenterToModel iStatesPresenterToModel;
	private IAnsibleConfig iAnsibleConfig;

	public StatesModel(IStatesPresenterToModel iStatesPresenterToModel) {
		this.iStatesPresenterToModel = iStatesPresenterToModel;
		this.iAnsibleConfig = AnsibleConfig.getInstance();
	}

	public void init() {
		String rawOutput = executeStates(iAnsibleConfig.getStatus());

		if (rawOutput == null) {
			iStatesPresenterToModel.showError("Error: Ansible output is null!");
			return;
		}

		try {

			int jsonStartIdx = rawOutput.indexOf("{");
			if (jsonStartIdx == -1) {
				iStatesPresenterToModel.showError("Error: Invalid JSON format from Ansible!");
				return;
			}

			JSONObject rootJson = new JSONObject(rawOutput.substring(jsonStartIdx).trim());
			String rawMsg = rootJson.optString("msg", "");

			if (rawMsg.isEmpty() || rawMsg.contains("version:v|mem:|keys:|nodes_raw:")) {
				iStatesPresenterToModel.showError("⚠️ [INFO] Redis Cluster There is no data. Parsing is skipped.");

				iStatesPresenterToModel.showError("Error: Redis cluster data is empty or not stored yet.");
				return;
			}

			//System.out.println(rawOutput);
			List<RedisNodeDTO> status = parseFullAnsibleStatus(rawOutput);
			iStatesPresenterToModel.parseAndPrintStatusResult(status);

		} catch (Exception e) {
			iStatesPresenterToModel.showError("Error inside init status: " + e.getMessage());
		}
	}

	public String executeStates(String playbookPath) {
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
			StringBuilder outputBuffer = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				outputBuffer.append(line).append("\n");
			}
			return outputBuffer.toString();

		} catch (Exception e) {
			iStatesPresenterToModel.showError("Error while executing verify playbook: " + e.getMessage());
			return null;
		}

	}

	public List<RedisNodeDTO> parseFullAnsibleStatus(String rawOutput) {

//		System.out.println(rawOutput);
//		String rawOutput = executeStates(playbookPath);

		if (rawOutput == null)
			return null;
		List<RedisNodeDTO> nodeList = new ArrayList<>();

		Map<String, String> masterIdToIpMap = new HashMap<>();

		try {
			int jsonStartIdx = rawOutput.indexOf("{");
			if (jsonStartIdx == -1)
				return nodeList;

			JSONObject rootJson = new JSONObject(rawOutput.substring(jsonStartIdx).trim());
			String rawMsg = rootJson.getString("msg");

			String[] nodeBlocks = rawMsg.split("##BREAK##");

			for (String block : nodeBlocks) {
				if (!block.contains("##NODE##"))
					continue;

				String cleanBlock = block.replace("##NODE##", "").trim();
				String[] metrics = cleanBlock.split("\\|");

				String liveVersion = "N/A";
				String liveMemory = "N/A";
				int liveKeys = 0;
				String nodesRaw = "";

				for (String metric : metrics) {
					if (metric.startsWith("version:"))
						liveVersion = metric.split(":")[1];
					if (metric.startsWith("mem:"))
						liveMemory = metric.split(":")[1];
					if (metric.startsWith("keys:"))
						liveKeys = Integer.parseInt(metric.split(":")[1]);
					if (metric.startsWith("nodes_raw:"))
						nodesRaw = metric.replace("nodes_raw:", "");
				}

				String[] clusterLines = nodesRaw.split("~");
				for (String cLine : clusterLines) {
					String[] tokens = cLine.trim().split("\\s+");
					if (tokens.length < 3)
						continue;


					if (!tokens[2].contains("myself")) {
						continue; 
					}

					RedisNodeDTO dto = new RedisNodeDTO();
					String nodeId = tokens[0];

					String ipAndPort = tokens[1];
					String extractedIp = "UNKNOWN";
					String extractedPort = "UNKNOWN";

					if (ipAndPort != null && ipAndPort.contains(":")) {
						String[] ipParts = ipAndPort.split(":");
						extractedIp = ipParts[0];
						if (ipParts.length > 1) {
							if (ipParts[1].contains("@")) {
								extractedPort = ipParts[1].split("@")[0];
							} else {
								extractedPort = ipParts[1];
							}
						}
					} else if (ipAndPort != null && !ipAndPort.isEmpty()) {
						extractedIp = ipAndPort;
					}

					dto.setIpAddress(extractedIp);
					dto.setPort(extractedPort);
					dto.setVersion(liveVersion);
					dto.setMemory(liveMemory);

					if (tokens[2].contains("master")) {
						dto.setRole("MASTER");
						dto.setSlots(tokens[tokens.length - 1]);
						dto.setKeysCount(liveKeys);
						dto.setReplicatingMaster("-");

						masterIdToIpMap.put(nodeId, extractedIp + ":" + extractedPort);
					} else {
						dto.setRole("REPLICA");
						dto.setSlots("-");
						dto.setKeysCount(0);

						dto.setReplicatingMaster(tokens.length > 3 ? tokens[3] : "-");
					}
					nodeList.add(dto);

				}
			}

			for (RedisNodeDTO node : nodeList) {
				if (node.getRole().equals("REPLICA")) {
					String targetMasterId = node.getReplicatingMaster();

					if (masterIdToIpMap.containsKey(targetMasterId)) {
						node.setReplicatingMaster(masterIdToIpMap.get(targetMasterId));
					} else {
						node.setReplicatingMaster("-");
					}
				}
			}

		} catch (

		Exception e) {
			iStatesPresenterToModel.showError("Error " + e.getMessage());
			e.printStackTrace();
		}

		return nodeList;
	}

	@Override
	public List<RedisNodeDTO> getReplicaData() {

		String rawOutput = executeStates(iAnsibleConfig.getStatus());
		if (rawOutput == null) {

			return null;
		}
		try {
			int jsonStartIdx = rawOutput.indexOf("{");
			if (jsonStartIdx == -1) {
				return null;
			}
			JSONObject rootJson = new JSONObject(rawOutput.substring(jsonStartIdx).trim());
			String rawMsg = rootJson.optString("msg", "");

			if (rawMsg.isEmpty() || rawMsg.contains("version:v|mem:|keys:|nodes_raw:")) {
				return null;
			}
			return parseFullAnsibleStatus(rawOutput);
		} catch (Exception e) {
			iStatesPresenterToModel.showError("Error while executing verify playbook: " + e.getMessage());
			return null;
		}
	}
}