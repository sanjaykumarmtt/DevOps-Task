package com.san.redistool.features.upgrade;

public class UpgradePresenter implements IUpgradePresenterToView,IUpgradePresenterToModel {
	
	private  IUpdateView iUpgradeViewl;
	
	private  IUpgradeModel IUpgradeModel;

	public UpgradePresenter(IUpdateView iUpgradeViewl) {
		
		this.iUpgradeViewl = iUpgradeViewl;
		this.IUpgradeModel =new UpdateModel(this);
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
