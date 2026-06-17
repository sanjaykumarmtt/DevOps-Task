package com.san.redistool.features.states;

import java.util.List;

import com.san.redistool.features.data.RedisNodeDTO;

public interface IStatesModel {
	
	void init();
	
	String executeStates(String playbookPath);

	List<RedisNodeDTO> getReplicaData();

}
