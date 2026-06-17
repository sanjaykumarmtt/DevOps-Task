package com.san.redistool.features.states;

import java.util.List;

import com.san.redistool.features.data.RedisNodeDTO;

public interface IStatesPresenterToView {
	
	void init();

	List<RedisNodeDTO> getReplicaData();
}
