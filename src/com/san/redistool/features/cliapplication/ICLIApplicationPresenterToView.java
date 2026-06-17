package com.san.redistool.features.cliapplication;

public interface ICLIApplicationPresenterToView {
	
	void init();
	
	void runProvision(String version);
	
	void runDataSeed(String dataSeedNumber);
	
	void runVerification();
	
	void runStatus();
	
	void runClusterHealthCheck();

	void runUpdateContainer(String version);
	
	void runFullVerification();

}
