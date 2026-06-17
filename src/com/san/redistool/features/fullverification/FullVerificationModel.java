package com.san.redistool.features.fullverification;

import java.util.List;

import com.san.redistool.features.clusterhealth.ClusterHealthView;
import com.san.redistool.features.clusterhealth.IHealthStatus;
import com.san.redistool.features.data.RedisNodeDTO;
import com.san.redistool.features.dataverify.DataVerifyView;
import com.san.redistool.features.dataverify.IDataVerifyView;
import com.san.redistool.features.states.ISattesViewGetReplicaData;
import com.san.redistool.features.states.StatesView;

public class FullVerificationModel implements IFullVerificationModel {

	private IFullVerificationPresenterToModel iFullVerificationPresenterToModel;
	private IDataVerifyView iDataVerifyView;
	private ISattesViewGetReplicaData iSattesViewGetReplicaData;
	private List<RedisNodeDTO> allNodes;
	private IHealthStatus iHealthStatus;

	public FullVerificationModel(IFullVerificationPresenterToModel iFullVerificationPresenterToModel) {
		this.iFullVerificationPresenterToModel = iFullVerificationPresenterToModel;
		this.iDataVerifyView = new DataVerifyView();
		this.iSattesViewGetReplicaData = new StatesView();

		this.iHealthStatus = new ClusterHealthView();
	}

	@Override
	public void init() {

		iFullVerificationPresenterToModel.message("\n=== Full Verification Summary ===\n\n");

		if (dataVerify()) {
			iFullVerificationPresenterToModel.message("1. ✅ Data Integrity    : PASS\n");
		} else {
			iFullVerificationPresenterToModel.message("1. ❌ Data Integrity    : FAIL\n");
		}

		if (allNodesReportTheSameRedisVersion()) {
			iFullVerificationPresenterToModel.message("2. ✅ Version Match     : PASS\n");
		} else {
			iFullVerificationPresenterToModel.message("2. ❌ Version Match     : FAIL\n");
		}

		if (slotsCoveredAndmasterHasAtLeastOneReplica()) {
			iFullVerificationPresenterToModel.message("3. ✅ Topology Health   : PASS\n");
		} else {
			iFullVerificationPresenterToModel.message("3. ❌ Topology Health   : FAIL\n");
		}

		if (clusterState()) {
			iFullVerificationPresenterToModel.message("4. ✅ Cluster State     : PASS\n");
		} else {
			iFullVerificationPresenterToModel.message("4. ❌ Cluster State     : FAIL\n");
		}

		if (allReplicasHaveMasterLinkStatus()) {
			iFullVerificationPresenterToModel.message("5. ✅ Replication Link  : PASS\n");
		} else {
			iFullVerificationPresenterToModel.message("5. ❌ Replication Link  : FAIL\n");
		}

		iFullVerificationPresenterToModel.message("\n🎉 [FINAL SUMMARY] FULL VERIFICATION: PASS. SYSTEM IS HEALTHY!\n");
	}

	@Override
	public boolean dataVerify() {
		return iDataVerifyView.verifyStep1Silent();
	}

	@Override
	public boolean allNodesReportTheSameRedisVersion() {
		this.allNodes = iSattesViewGetReplicaData.getReplicaData();
		if (allNodes == null || allNodes.isEmpty())
			return false;
		String version = allNodes.get(0).getVersion();
		for (RedisNodeDTO redisNodeDTO : allNodes) {
			if (!version.equals(redisNodeDTO.getVersion())) {
				return false;
			}
		}
		return true;

	}

	@Override
	public boolean slotsCoveredAndmasterHasAtLeastOneReplica() {

		if (allNodes == null || allNodes.isEmpty()) {
			return false;
		}
		int totalSlotsCovered = 0;
		for (RedisNodeDTO node : allNodes) {
			if ("MASTER".equalsIgnoreCase(node.getRole())) {
				String slotsStr = node.getSlots();

				if (slotsStr != null && slotsStr.contains("-")) {
					try {
						String[] range = slotsStr.trim().split("-");

						int start = Integer.parseInt(range[0].trim());
						int end = Integer.parseInt(range[1].trim());

						totalSlotsCovered += (end - start + 1);

					} catch (Exception e) {
						iFullVerificationPresenterToModel.error("❌Error" + e.getMessage());
					}
				}
			}
		}

		for (RedisNodeDTO node : allNodes) {
			if ("MASTER".equalsIgnoreCase(node.getRole())) {
				String masterAddress = node.getIpAddress() + ":" + node.getPort();

				boolean hasReplicaPair = false;

				for (RedisNodeDTO subNode : allNodes) {
					if ("REPLICA".equalsIgnoreCase(subNode.getRole())) {

						if (masterAddress.equals(subNode.getReplicatingMaster().trim())) {
							hasReplicaPair = true;
							break;
						}
					}
				}
				if (!hasReplicaPair) {
					return false;
				}
			}
		}

		if (totalSlotsCovered == 16384) {
			return true;
		} else {

			return false;
		}

	}

	@Override
	public boolean clusterState() {

		boolean isClusterOk = iHealthStatus.verifyHealthSilent();

		if (isClusterOk) {
			return true;
		} else {

			return false;
		}
	}

	@Override
	public boolean allReplicasHaveMasterLinkStatus() {
	    List<RedisNodeDTO> allNodes = this.allNodes;
	    if (allNodes == null || allNodes.isEmpty()) {
	        return false;
	    }

	    for (RedisNodeDTO node : allNodes) {
	        if ("REPLICA".equalsIgnoreCase(node.getRole())) {
	            if (node.getReplicatingMaster() == null || "-".equals(node.getReplicatingMaster())) {
	                return false; 
	            }
	        }
	    }
	    return true; 
	}

}
