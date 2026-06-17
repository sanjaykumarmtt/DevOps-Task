package com.san.redistool.features.data;

public class RedisNodeDTO {
	private String ipAddress;
	private String port;
	private String role;
	private String version;
	private String memory;

	private String slots;
	private int keysCount;

	private String replicatingMaster;


	public RedisNodeDTO() {
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getSlots() {
		return slots;
	}

	public void setSlots(String slots) {
		this.slots = slots;
	}

	public int getKeysCount() {
		return keysCount;
	}

	public void setKeysCount(int keysCount) {
		this.keysCount = keysCount;
	}

	public String getReplicatingMaster() {
		return replicatingMaster;
	}

	public void setReplicatingMaster(String replicatingMaster) {
		this.replicatingMaster = replicatingMaster;
	}
}