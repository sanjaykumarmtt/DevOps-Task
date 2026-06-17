package com.san.redistool.features.clusterhealth;

public interface IClusterHealthModel {
	
	boolean init();

	boolean clusterHealthCheck(String playbookPath);
	
	public boolean verifyHealthSilent();
}
