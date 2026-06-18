package com.san.redistool.features.upgrade;

import com.san.redistool.features.BaseRedisTool;

public class UpgradeView extends BaseRedisTool implements IUpdateView{
	
	private IUpgradePresenterToView IUpgradePresenterToView;

	public UpgradeView() {
		IUpgradePresenterToView = new UpgradePresenter(this);
	}
	
	@Override
	public void init(String version) {
		IUpgradePresenterToView.init(version);
	}

	@Override
	public void error(String message) {
		showError(message);
	}

	@Override
	public void messages(String message) {
		showMessage(message);
	}

	

	
	

}
