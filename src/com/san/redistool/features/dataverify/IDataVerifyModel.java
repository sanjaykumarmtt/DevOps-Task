package com.san.redistool.features.dataverify;

public interface IDataVerifyModel {
    boolean executeVerify(String totalKeys);
    
    public boolean verifyStep1Silent(String totalKeys);
}
