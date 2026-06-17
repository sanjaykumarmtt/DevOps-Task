package com.san.redistool.features.states;

import java.util.List;

import com.san.redistool.features.data.RedisNodeDTO;

public interface IStatesView {

	void init();
	
	void error(String error);
	
	void message(String message);

	void parseAndPrintStatusResult(List<RedisNodeDTO> nodeList);
	
	
}
