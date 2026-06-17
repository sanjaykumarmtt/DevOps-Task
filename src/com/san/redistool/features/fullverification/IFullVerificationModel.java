package com.san.redistool.features.fullverification;

public interface IFullVerificationModel {

	void init();

	boolean dataVerify();

	boolean allNodesReportTheSameRedisVersion();
	
	boolean slotsCoveredAndmasterHasAtLeastOneReplica();
	
	boolean clusterState();
	
	boolean allReplicasHaveMasterLinkStatus();

}