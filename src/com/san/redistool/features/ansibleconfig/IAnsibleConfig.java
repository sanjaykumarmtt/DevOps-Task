package com.san.redistool.features.ansibleconfig;

public interface IAnsibleConfig {



	void executeSeed(String dataSeedNumber);
	

	String getClusterHealth();

	String getStatus();
	
	String getUpgradeReplicasPlaybook();


	void executeProvision(String version);
	
	public String getPlaybook();

}
