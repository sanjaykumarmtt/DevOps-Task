package com.san.redistool.features.clusterhealth;

public interface IClusterHealthPresenterToModel {
	
	boolean clusterHealthCheck(String playbookPath);
	
	public boolean verifyHealthSilentCallView(String playbookPath);
	
}
