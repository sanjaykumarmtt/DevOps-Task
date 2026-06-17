package com.san.redistool.features.dataverify;

public interface IDataVerifyPresenter {
	
	boolean inti();
    
	boolean onVerifyCompleted(String rawOutput, int exitCode);
    
    void onVerifyFailed(String errorMsg);
    
    public boolean verifyStep1Silent();
    
  
}
