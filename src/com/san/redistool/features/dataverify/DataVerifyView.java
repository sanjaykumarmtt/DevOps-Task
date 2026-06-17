package com.san.redistool.features.dataverify;

import org.json.JSONException;
import org.json.JSONObject;


public class DataVerifyView implements IDataVerifyView {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BOLD = "\u001B[1m";
    
    private IDataVerifyPresenter iDataVerifyPresenter;
    
    
    
    public DataVerifyView() {
		
		this.iDataVerifyPresenter =new  DataVerifyPresenter(this);
	}

	@Override
	public boolean init() {
		return iDataVerifyPresenter.inti();
	}

    public boolean verifyDataIntegrityResult(String rawOutput, int exitCode) {
        System.out.println(ANSI_CYAN + ANSI_BOLD + "\n=== Post-Upgrade Verification ===" + ANSI_RESET);

        String jsonPart = extractJson(rawOutput);
        if (jsonPart == null) {
            System.err.println(ANSI_RED + "❌ Could not locate JSON output in Ansible response." + ANSI_RESET);
            if (exitCode != 0) System.err.println("Ansible Exit Code: " + exitCode);
            System.out.println("Raw Output:\n" + rawOutput);
           
        }

        try {
            JSONObject root = new JSONObject(jsonPart);
            JSONObject msg = root.optJSONObject("msg");
            if (msg == null) msg = root;

            String status = msg.optString("status", "UNKNOWN");
            String pdfFormattedOutput = msg.optString("pdf_formatted_output", "N/A");
            int verified = msg.optInt("keys_successfully_verified", 0);
            int mismatched = msg.optInt("mismatched_values", 0);
            int missing = msg.optInt("missing_keys", 0);

            if ("PASS".equalsIgnoreCase(status) && exitCode == 0 && missing == 0 && mismatched == 0) {
                System.out.println(ANSI_GREEN + "✓ " + pdfFormattedOutput + ANSI_RESET);
                System.out.println("  ↳ Data Integrity Verified: 100% Intact.");
                return true;
            } else {
                System.out.println(ANSI_RED + ANSI_BOLD + "✗ [FAIL] Data Integrity Check Failed!" + ANSI_RESET);
                System.out.println(ANSI_RED + "  ↳ Critical Metrics Summary:" + ANSI_RESET);
                System.out.println(ANSI_RED + "      - Missing Keys: " + missing + ANSI_RESET);
                System.out.println(ANSI_RED + "      - Mismatched Values (Corruption): " + mismatched + ANSI_RESET);
                System.out.println(ANSI_RED + "      - Successfully Verified: " + verified + ANSI_RESET);
                return false; 
            }

        } catch (JSONException e) {
            System.err.println(ANSI_RED + "❌ Error parsing Ansible JSON: " + e.getMessage() + ANSI_RESET);
            return false;
        } catch (Exception e) {
            System.err.println(ANSI_RED + "❌ Unexpected CLI error: " + e.getMessage() + ANSI_RESET);
            return false;
        }
    }
     

    @Override
    public void displayError(String errorMsg) {
        System.err.println(ANSI_RED + "❌ " + errorMsg + ANSI_RESET);
    }

    private String extractJson(String rawOutput) {
        int startIndex = rawOutput.indexOf('{');
        int endIndex = rawOutput.lastIndexOf('}');
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return rawOutput.substring(startIndex, endIndex + 1);
        }
        return null;
    }


	@Override
	public boolean verifBaselineData() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean verifyStep1Silent() {
		return iDataVerifyPresenter.verifyStep1Silent();
	
	}


	
}
