package com.san.redistool.features.fullverification;

import com.san.redistool.features.BaseRedisTool;

public class FullVerificationView extends BaseRedisTool implements IFullVerificationView {
	
	private IFullVerificationPresenterToView iFullVerificationPresenterToView;
	
	public FullVerificationView() {
		this.iFullVerificationPresenterToView = new FullVerificationPresenter(this);
	}

	@Override
	public void init() {
		iFullVerificationPresenterToView.init();
	}

	@Override
	public void error(String error) {
		
		showError(error);
	}

	@Override
	public void message(String message) {
		
		showMessage(message);
		
	}
}
