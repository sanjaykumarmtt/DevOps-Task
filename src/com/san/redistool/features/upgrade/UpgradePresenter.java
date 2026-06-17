package com.san.redistool.features.upgrade;

public class UpgradePresenter implements IUpgradePresenterToView,IUpgradePresenterToModel {
	
	private  IUpgradeView iUpgradeViewl;
	
	private  IUpgradeModel IUpgradeModel;

	public UpgradePresenter(IUpgradeView iUpgradeViewl) {
		
		this.iUpgradeViewl = iUpgradeViewl;
		this.IUpgradeModel =new UpgradeModel(this);
	}

	@Override
	public void init(String version) {
		IUpgradeModel.init(version);
	}

	@Override
	public void error(String message) {
		iUpgradeViewl.error(message);	
	}

	@Override
	public void messages(String message) {
		iUpgradeViewl.messages(message);		
	}
	
	

}
