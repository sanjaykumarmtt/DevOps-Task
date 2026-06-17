package com.san.redistool.features.cliapplication;

public interface ICLIApplicationView {

	void init();

	void start();

	void Message(String message);

	void Error(String error);

	void stopProjectCLI();

}
