package com.san.redistool.features.cliapplication;

public interface ICLIApplicationModel {

	void init();

	void chikDorckerAndAnsible();

	void runProvision(String version);

	void runDataSeed(String dataSeedNumber);

	void runVerification();

	void runStatus();
	
	void runClusterHealthCheck();

	void runUpdateContainer(String version);
	
	void runFullVerification();
}
