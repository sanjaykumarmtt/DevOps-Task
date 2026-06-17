package com.san.redistool.features.cliapplication;

public class CLIApplicationPresenter implements ICLIApplicationPresenterToView, ICLIApplicationPresenterToModel {

	private ICLIApplicationView iCLIApplicationView;
	private ICLIApplicationModel iCLIApplicationModel;

	public CLIApplicationPresenter(ICLIApplicationView iCLIApplicationView) {
		this.iCLIApplicationView = iCLIApplicationView;
		this.iCLIApplicationModel = new CLIApplicationModel(this);
	}

	@Override
	public void init() {
		iCLIApplicationModel.init();
	}

	@Override
	public void start() {
		iCLIApplicationView.start();
	}

	@Override
	public void Message(String message) {
		iCLIApplicationView.Message(message);
	}

	@Override
	public void Error(String error) {
		iCLIApplicationView.Error(error);
	}

	@Override
	public void stopProjectCLI() {
		iCLIApplicationView.stopProjectCLI();
	}

	@Override
	public void runProvision(String version) {
		iCLIApplicationModel.runProvision(version);
	}

	@Override
	public void runDataSeed(String dataSeedNumber) {
		iCLIApplicationModel.runDataSeed(dataSeedNumber);
	}

	@Override
	public void runVerification() {
		iCLIApplicationModel.runVerification();
		
	}

	@Override
	public void runStatus() {
		iCLIApplicationModel.runStatus();
		
	}

	@Override
	public void runClusterHealthCheck() {
		iCLIApplicationModel.runClusterHealthCheck();
	}

	@Override
	public void runUpdateContainer(String version) {
		iCLIApplicationModel.runUpdateContainer(version);
		
	}

	@Override
	public void runFullVerification() {
		iCLIApplicationModel.runFullVerification();
		
	}
}
