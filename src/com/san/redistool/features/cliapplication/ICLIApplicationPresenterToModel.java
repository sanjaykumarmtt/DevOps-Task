package com.san.redistool.features.cliapplication;

public interface ICLIApplicationPresenterToModel {
	
	void start();
	
	void Message(String message);

	void Error(String error);
	
	void stopProjectCLI();

}
