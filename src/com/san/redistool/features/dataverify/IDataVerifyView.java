package com.san.redistool.features.dataverify;

public interface IDataVerifyView {
	
	boolean init();
    
    void displayError(String errorMsg);
    
    boolean verifBaselineData();

	boolean verifyDataIntegrityResult(String rawOutput, int exitCode);
	
	public boolean verifyStep1Silent();
}
