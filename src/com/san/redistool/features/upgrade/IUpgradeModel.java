package com.san.redistool.features.upgrade;

import java.util.List;

import com.san.redistool.features.data.RedisNodeDTO;

public interface IUpgradeModel {
	
	void init(String version);
	
	void upgradeReplica(String version);
	
	void upgradeAllMastersWithZeroDowntime(List<RedisNodeDTO> redisNodeList);

}
