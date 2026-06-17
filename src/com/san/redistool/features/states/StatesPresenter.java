package com.san.redistool.features.states;

import java.util.List;

import com.san.redistool.features.data.RedisNodeDTO;

public class StatesPresenter implements IStatesPresenterToView,IStatesPresenterToModel{
	
	private IStatesModel iStatesModel;
	private IStatesView iStatesView;
	
	public StatesPresenter(IStatesView iStatesView) {
		this.iStatesModel = new StatesModel(this);
		this.iStatesView=iStatesView;
	}

	@Override
	public void init() {
		iStatesModel.init();
	}

	@Override
	public void parseAndPrintStatusResult(List<RedisNodeDTO> nodeList) {
		iStatesView.parseAndPrintStatusResult(nodeList);
	}

	@Override
	public void showMessage(String message) {
		iStatesView.message(message);
		
	}
	
	@Override
	public void showError(String error) {
		iStatesView.error(error);
	}

	@Override
	public List<RedisNodeDTO> getReplicaData() {
		return iStatesModel.getReplicaData();
	}


}
