package com.san.redistool.features.upgrade;

public interface IUpgradeView {
	
	void init(String version);
	
	void error(String message);
	
	void messages(String message);

}
