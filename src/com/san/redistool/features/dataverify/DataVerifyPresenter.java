package com.san.redistool.features.dataverify;

import java.util.List;

import com.san.redistool.features.data.RedisNodeDTO;
import com.san.redistool.features.states.ISattesViewGetReplicaData;
import com.san.redistool.features.states.StatesView;

public class DataVerifyPresenter implements IDataVerifyPresenter {
    private IDataVerifyView view;
    private IDataVerifyModel model;
    private List<RedisNodeDTO> allNodes;
    private ISattesViewGetReplicaData iSattesViewGetReplicaData;

    public DataVerifyPresenter(IDataVerifyView view) {
        this.view = view;
        this.iSattesViewGetReplicaData = new StatesView();
        this.model = new DataVerifyModel(this);
    }

    @Override
    public boolean inti() {
    	this.allNodes = iSattesViewGetReplicaData.getReplicaData();
    	
    	int totalKeys=0;
    	
    	for(RedisNodeDTO countkeys:allNodes) {
    		if ("MASTER".equalsIgnoreCase(countkeys.getRole())) {
    			totalKeys+=countkeys.getKeysCount();
    		}
    	}
//    	System.out.println(totalKeys);
      return model.executeVerify(String.valueOf(totalKeys));
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
    	
    	int totalKeys=0;
    	
    	for(RedisNodeDTO countkeys:allNodes) {
    		if ("MASTER".equalsIgnoreCase(countkeys.getRole())) {
    			totalKeys+=countkeys.getKeysCount();
    		}
    	}
		return model.verifyStep1Silent(String.valueOf(totalKeys));
	}


}
