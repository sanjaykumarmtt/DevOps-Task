package com.san.redistool.features.states;

import java.util.ArrayList;
import java.util.List;

import com.san.redistool.features.BaseRedisTool;
import com.san.redistool.features.data.RedisNodeDTO;

public class StatusView extends BaseRedisTool implements IStatesView,ISattesViewGetReplicaData{
	
	private IStatesPresenterToView iStatesPresenterToView;
	
	public StatusView() {
		this.iStatesPresenterToView = new StatesPresenter(this);
	}

	@Override
	public void init() {
		iStatesPresenterToView.init();
	}
	
	public void parseAndPrintStatusResult(List<RedisNodeDTO> nodeList) {
		
	
		if(nodeList.size()<1) return;
		
		message("\n\u001B[32m=========================================================================");
		message("                      REDIS CLUSTER STATUS DASHBOARD                     ");
		message("=========================================================================\u001B[0m");
	    
	    // 1. மாஸ்டர் டேபிள் சஞ்சாய்
		message("\n\u001B[36m[ MASTERS ]\u001B[0m");
		message("+-----------------+--------+----------+----------------+-------+---------+");
	    System.out.printf("| %-15s | %-6s | %-8s | %-14s | %-5s | %-7s |\n", "IP Address", "Port", "Version", "Slots", "Keys", "Memory");
	    message("+-----------------+--------+----------+----------------+-------+---------+");
	    for (RedisNodeDTO node : nodeList) {
	    	
	        if ("MASTER".equals(node.getRole())) {
	            System.out.printf("| %-15s | %-6s | %-8s | %-14s | %-5d | %-7s |\n",
	                    node.getIpAddress(), node.getPort(), node.getVersion(), node.getSlots(), node.getKeysCount(), node.getMemory());
	        }
	       
	    }
	    message("+-----------------+--------+----------+----------------+-------+---------+");

	    // 2. ரெப்ளிகா டேபிள் சஞ்சாய்
	    message("\n\u001B[35m[ REPLICAS ]\u001B[0m");
	    message("+-----------------+--------+----------+-----------------------+---------+");
	    System.out.printf("| %-15s | %-6s | %-8s | %-21s | %-7s |\n", "IP Address", "Port", "Version", "Replicating Master", "Memory");
	    message("+-----------------+--------+----------+-----------------------+---------+");
	    for (RedisNodeDTO node : nodeList) {
	        if ("REPLICA".equals(node.getRole())) {
	            System.out.printf("| %-15s | %-6s | %-8s | %-21s | %-7s |\n",
	                    node.getIpAddress(), node.getPort(), node.getVersion(), node.getReplicatingMaster(), node.getMemory());
	        }
	       
	    }
	    message("+-----------------+--------+----------+-----------------------+---------+");
	}

	@Override
	public void error(String error) {
		showError(error);
		
	}

	@Override
	public void message(String message) {
		showMessage(message);
		
	}

	@Override
	public List<RedisNodeDTO> getReplicaData() {
		return iStatesPresenterToView.getReplicaData();
	}
}
