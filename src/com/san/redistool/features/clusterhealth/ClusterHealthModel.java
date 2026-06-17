package com.san.redistool.features.clusterhealth;

import com.san.redistool.features.ansibleconfig.AnsibleConfig;
import com.san.redistool.features.ansibleconfig.IAnsibleConfig;

public class ClusterHealthModel implements IClusterHealthModel{
	
	private IClusterHealthPresenterToModel iClusterHealthPresenterToModel;
	private IAnsibleConfig iAnsibleConfig;
	
	
	public ClusterHealthModel(IClusterHealthPresenterToModel iClusterHealthPresenterToModel) {
		
		this.iClusterHealthPresenterToModel = iClusterHealthPresenterToModel;
		this.iAnsibleConfig=AnsibleConfig.getInstance();
	}

	@Override
	public boolean init() {
		return clusterHealthCheck(iAnsibleConfig.getClusterHealth());
	}

	@Override
	public boolean clusterHealthCheck(String playbookPath) {
		return iClusterHealthPresenterToModel.clusterHealthCheck(playbookPath);
	}

	@Override
	public boolean verifyHealthSilent() {
		// TODO Auto-generated method stub
		return iClusterHealthPresenterToModel.verifyHealthSilentCallView(iAnsibleConfig.getClusterHealth());
	}

}
