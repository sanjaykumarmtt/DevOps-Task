package com.san.redistool.features.upgrade;

public interface IUpdateView {
	
	void init(String version);
	
	void error(String message);
	
	void messages(String message);

}
