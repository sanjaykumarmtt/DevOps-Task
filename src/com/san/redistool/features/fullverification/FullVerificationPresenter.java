package com.san.redistool.features.fullverification;

public class FullVerificationPresenter implements IFullVerificationPresenterToView,IFullVerificationPresenterToModel {
	
	private IFullVerificationView iFullVerificationView;
	private IFullVerificationModel iFullVerificationModel;
	

	public FullVerificationPresenter(IFullVerificationView iFullVerificationView) {
		
		this.iFullVerificationView = iFullVerificationView;
		this.iFullVerificationModel = new FullVerificationModel(this);
	}

	@Override
	public void init() {
		iFullVerificationModel.init();
	}

	@Override
	public void error(String error) {
		iFullVerificationView.error(error);
	}

	@Override
	public void message(String message) {
		iFullVerificationView.message(message);
	}



}
