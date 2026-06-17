package com.san.redistool.features.states;

import java.util.List;

import com.san.redistool.features.data.RedisNodeDTO;

public interface IStatesPresenterToModel {
	
	void parseAndPrintStatusResult(List<RedisNodeDTO> nodeList);
	
	void showError(String error);
	
	void showMessage(String message); 

}
