package com.san.redistool.features.clusterhealth;

public class ClusterHealthPresenter implements IClusterHealthPresenterToView,IClusterHealthPresenterToModel{
	
	private IClusterHealthModel iClusterHealthModel;
	private IClusterHealthView iClusterHealthView;
	
	public ClusterHealthPresenter(IClusterHealthView iClusterHealthView) {
		
		this.iClusterHealthView=iClusterHealthView;
		this.iClusterHealthModel = new ClusterHealthModel(this);
	}

	@Override
	public boolean init() {
		return iClusterHealthModel.init();
	}

	@Override
	public boolean clusterHealthCheck(String playbookPath) {
	return iClusterHealthView.clusterHealthCheck(playbookPath);
	}

	@Override
	public boolean verifyHealthSilent() {
		// TODO Auto-generated method stub
		return iClusterHealthModel.verifyHealthSilent();
	}

	@Override
	public boolean verifyHealthSilentCallView(String playbookPath) {
		return iClusterHealthView.verifyStep4Silent(playbookPath);
	}
}
