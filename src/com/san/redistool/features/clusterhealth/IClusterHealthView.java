package com.san.redistool.features.clusterhealth;

public interface IClusterHealthView {
	
	boolean init();
	
	boolean clusterHealthCheck(String playbookPath);
	
	public boolean verifyStep4Silent(String playbookPath);

}
