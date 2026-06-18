package com.san.redistool.features.dataverify;

import java.util.List;

import com.san.redistool.features.data.RedisNodeDTO;
import com.san.redistool.features.states.ISattesViewGetReplicaData;
import com.san.redistool.features.states.StatusView;

public class DataVerifyPresenter implements IDataVerifyPresenter {
	private IDataVerifyView view;
	private IDataVerifyModel model;
	private List<RedisNodeDTO> allNodes;
	private ISattesViewGetReplicaData iSattesViewGetReplicaData;

	public DataVerifyPresenter(IDataVerifyView view) {
		this.view = view;
		this.iSattesViewGetReplicaData = new StatusView();
		this.model = new DataVerifyModel(this);
	}
	
	@Override
	public boolean inti() {
		this.allNodes = iSattesViewGetReplicaData.getReplicaData();

		int totalKeys = 0;
		if (allNodes != null) {
			for (RedisNodeDTO countkeys : allNodes) {
				if ("MASTER".equalsIgnoreCase(countkeys.getRole())) {
					totalKeys += countkeys.getKeysCount();
				}
			}
			
			if (totalKeys == 0)
				onVerifyFailed("[CRITICAL ERROR] You haven't inserted any keys into the DB yet! "
						+ "Please run 'redis-tool data populate' first to insert the 1000 required keys.");
			
			return totalKeys != 0 ? model.executeVerify(String.valueOf(totalKeys)) : false;
		}
		return false;
	}

	@Override
	public boolean onVerifyCompleted(String rawOutput, int exitCode) {
		return view.verifyDataIntegrityResult(rawOutput, exitCode);
	}

	@Override
	public void onVerifyFailed(String errorMsg) {
		view.displayError(errorMsg);
	}

	@Override
	public boolean verifyStep1Silent() {
		// TODO Auto-generated method stub

		this.allNodes = iSattesViewGetReplicaData.getReplicaData();

		int totalKeys = 0;
		if (allNodes != null) {
			for (RedisNodeDTO countkeys : allNodes) {
				if ("MASTER".equalsIgnoreCase(countkeys.getRole())) {
					totalKeys += countkeys.getKeysCount();
				}
			}
			return totalKeys != 0 ? model.verifyStep1Silent(String.valueOf(totalKeys)) : false;
		}
		return false;

	}

}
