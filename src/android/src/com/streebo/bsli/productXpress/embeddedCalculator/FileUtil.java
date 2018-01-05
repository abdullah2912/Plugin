package com.streebo.bsli.productXpress.embeddedCalculator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Vector;

import android.os.Environment;

public class FileUtil {

	private static String externalStoragePath = null;
	
	public static String getExternalStoragePath() {
		if (externalStoragePath == null) {
			externalStoragePath ="/data/data/com.bsli.eapp/files";// Environment.getExternalStorageDirectory().getAbsolutePath();
		}

		return externalStoragePath;
	}

	public static String getProductXpressProductDataPath() {
		return getProductXpressPath() + "/products";
	}

	public static String getProductXpressInstallPath() {
		return getProductXpressPath() + "/install";
	}

	private static String getProductXpressPath() {
		return getExternalStoragePath() + "/BSLI/px";
	}
	
	public static String getFundExcelPath() {
		return getExternalStoragePath() + "/BSLI/fund_performance.json";
	}
	
	public static void stringToFile(String string, String filename)
			throws Exception {
		FileWriter writer = new FileWriter(filename);
		writer.write(string);
		writer.close();
	}

	public static String fileToString(File file) throws Exception {
	    String result = null;
	    BufferedReader br = null;

	    try {
	      StringBuffer sb = new StringBuffer();
	      br = new BufferedReader(new FileReader(file));

	      char[] buffer = new char[1024];

	      int bytesRead = 0;

	      do {
	        bytesRead = br.read(buffer, 0, 1024);
	        if (bytesRead > 0) {
	          sb.append(buffer, 0, bytesRead);
	        }
	      } while (bytesRead > 0);

	      result = sb.toString();
	    } finally {
	      if (br != null) {
	          br.close();
	      }
	    }
	    
	    return result;
	}

	public static String getDeploymentPackageFileName(String fromPath) {
		File searchDir = new File(fromPath);
		
		if(searchDir.isDirectory()) {
			File files[] = searchDir.listFiles();
			
			for(File file: files) {
				if(file.isFile()) {
					String absolutePath = file.getAbsolutePath();
					if(absolutePath.endsWith(".pxdp") || absolutePath.endsWith(".pxdpz") || absolutePath.endsWith(".pxdo")) {
						return absolutePath;
					}
				}
			}
		}
		
		return null;
	}

	public static File[] getExternalTableFiles(String fromPath) {
		/*Log.d("getExternalTableFiles", "Path: "+fromPath);*/
		File searchDir = new File(fromPath);
		File files[] = new File[0];
		FilenameFilter filter = new FilenameFilter() {
	         public boolean accept(File dir, String name){
	            return name.endsWith(".pxtbl");
	        }
	    };
	    /*Log.d("getExternalTableFiles", "isDirectory: "+searchDir.isDirectory());*/
		if(searchDir.isDirectory()) {
			files = searchDir.listFiles(filter);
		}
		/*Log.d("getExternalTableFiles", "Total Files: "+files.length);*/
		return files;
	}

	public static Vector<File> getClcinFiles(String fromPath) {
		Vector<File> result = new Vector<File>();
		
		File searchDir = new File(fromPath);
		
		if(searchDir.isDirectory()) {
			File files[] = searchDir.listFiles();
			
			for(File file: files) {
				if(file.isFile()) {
					if(file.getName().endsWith(".clcin")) {
						result.add(file);
					}
				} else if(file.isDirectory()) {
					result.addAll(getClcinFiles(file.getAbsolutePath()));
				}
			}
		}
		
		return result;
	}
	
	public static String prepareCalcInputXMLWealthMax(  String inputFileName, String xmlInput, HashMap<String, String> inputs ) {
		
		if(inputFileName.equalsIgnoreCase("PremiumCalculation")) {
			
			StringBuffer preparedWithdrawalTemplate = new StringBuffer();
			
			if( !inputs.get( "WITHDRAWAL_AMT" ).trim().isEmpty() ) {
				
				String[] withDrawalAmt = inputs.get( "WITHDRAWAL_AMT" ).split(",");
				String[] withDrawalYears = inputs.get( "WITHDRAWAL_YEAR" ).split(","); 
				
				//Log.d("withDrawalAmt length", "" + withDrawalAmt.length);
				
				String withdrawlTemplate = 
						"<Partial_withdrawal_Wealth_Max id='ID30##_ID_##'>" + 
		                    "<Features>" + 
			                    "<Partial_Withdrawal_Amount>##_WITHDRAW_AMT_##</Partial_Withdrawal_Amount>" + 
			                    "<Partial_Withdrawal_Year>##_WITHDRAW_YEAR_##</Partial_Withdrawal_Year>" + 
		                    "</Features>" + 
	                    "</Partial_withdrawal_Wealth_Max>";
				
				for(int index=0; index<withDrawalAmt.length; index++) {
					
					//Log.d("withDrawalAmt ----------- ",withDrawalAmt[index]);
					
					preparedWithdrawalTemplate.append( withdrawlTemplate.replace("##_ID_##", String.valueOf((index+1)) )
											 .replace("##_WITHDRAW_AMT_##", withDrawalAmt[index])
											 .replace("##_WITHDRAW_YEAR_##", withDrawalYears[index]) );
				}
			}
			
			StringBuffer preparedTopUpTemplate = new StringBuffer();
			
			if( !inputs.get( "TOPUP_AMT" ).trim().isEmpty() ) {
				
				String[] topUpAmt = inputs.get( "TOPUP_AMT" ).split(",");
				String[] topUpYears = inputs.get( "TOPUP_YEAR" ).split(",");
				
				//Log.d("topUpAmt length","" + topUpAmt.length);
				
				String topUpTemplate = 
						"<Top_up_Wealth_Max id='ID50##_ID_##'>" + 
				            "<Features>" + 
				              	"<Top_Up_Amount>##_TOPUP_AMT_##</Top_Up_Amount>" + 
				              	"<Top_Up_Year>##_TOPUP_YEAR_##</Top_Up_Year>" + 
				            "</Features>" + 
				        "</Top_up_Wealth_Max>";
				
				for(int index=0; index<topUpAmt.length; index++) {
					preparedTopUpTemplate.append( topUpTemplate.replace("##_ID_##", String.valueOf((index+1)) )
											.replace("##_TOPUP_AMT_##", topUpAmt[index])
											.replace("##_TOPUP_YEAR_##", topUpYears[index]) );
				}
			}
			
			inputs.put( "WITHDRAWAL_DETAILS" , preparedWithdrawalTemplate.toString());
			inputs.put( "TOPUP_DETAILS" , preparedTopUpTemplate.toString());
			
			String proposerDetail = "";
			
			boolean propInsured = Boolean.parseBoolean(inputs.get( "PROP_INSURED" ).trim());
			if(!propInsured){
				proposerDetail = "<Policy-Proposer_WM><Proposer_Wealth_Max id='ID19'><Features><Age>"+inputs.get( "PROP_AGE" )+"</Age><Gender>"+inputs.get( "PROP_GENDER" )+"</Gender><Tax_Rate xsi:nil='true'/></Features></Proposer_Wealth_Max></Policy-Proposer_WM>";
			}
			inputs.put("PROPOSER_DETAILS", proposerDetail);
			
			for(String key : inputs.keySet()){
				//Log.d(key, "###############: "+"##_"+inputs.get(key)+"_##");
				xmlInput = xmlInput.replace("##_"+key+"_##", inputs.get(key));
			}
			
		}
		else {
			for(String key : inputs.keySet()){
				//Log.d(key, "###############: "+"##_"+inputs.get(key)+"_##");
				xmlInput = xmlInput.replace("##_"+key+"_##", inputs.get(key));
			}
		}
		
		return xmlInput;
	}
	
	public static String prepareCalcInputXMLDefault(  String inputFileName, String xmlInput, HashMap<String, String> inputs ) {
		
		for(String key : inputs.keySet()){
			//Log.d(key, "###############: "+"##_"+inputs.get(key)+"_##");
			xmlInput = xmlInput.replace("##_"+key+"_##", inputs.get(key));
		}
		
		return xmlInput;
	}

	public static String prepareCalcInputXMLWealthSecure(  String inputFileName, String xmlInput, HashMap<String, String> inputs ) {
		if(inputFileName.equalsIgnoreCase("wealth_secure_illustration") || inputFileName.equalsIgnoreCase("wealth_secure_premiumCalculation")){
			StringBuffer preparedWithdrawalTemplate = new StringBuffer();
			if( !inputs.get( "WITHDRAWAL_AMT" ).trim().isEmpty() ) {

				String[] withDrawalAmt = inputs.get( "WITHDRAWAL_AMT" ).split(",");
				String[] withDrawalYears = inputs.get( "WITHDRAWAL_YEAR" ).split(","); 

				//Log.d("withDrawalAmt length", "" + withDrawalAmt.length);

				String withdrawlTemplate = "<Partial_Withdrawal_Wealth_Secure id='ID21##_ID_##'>" + 
		                    "<Features>" + 
			                    "<Partial_Withdrawal_Amount>##_WITHDRAW_AMT_##</Partial_Withdrawal_Amount>" + 
			                    "<Partial_Withdrawal_Year>##_WITHDRAW_YEAR_##</Partial_Withdrawal_Year>" + 
		                    "</Features>" + 
	                    "</Partial_Withdrawal_Wealth_Secure>";

				for (int index = 0; index < withDrawalAmt.length; index++) {
					//Log.d("withDrawalAmt ----------- ", withDrawalAmt[index]);
					preparedWithdrawalTemplate.append(withdrawlTemplate.replace("##_ID_##", String.valueOf((index + 1)))
									.replace("##_WITHDRAW_AMT_##", withDrawalAmt[index])
									.replace("##_WITHDRAW_YEAR_##", withDrawalYears[index]));
				}
			}

			StringBuffer preparedTopUpTemplate = new StringBuffer();
			if( !inputs.get( "TOPUP_AMT" ).trim().isEmpty() ) {
				String[] topUpAmt = inputs.get( "TOPUP_AMT" ).split(",");
				String[] topUpYears = inputs.get( "TOPUP_YEAR" ).split(",");

				//Log.d("topUpAmt length","" + topUpAmt.length);
				String topUpTemplate = "<Top_Up_Wealth_Secure id='ID18##_ID_##'>" + 
				            "<Features>" + 
				              	"<Top_Up_Amount>##_TOPUP_AMT_##</Top_Up_Amount>" + 
				              	"<Top_Up_Year>##_TOPUP_YEAR_##</Top_Up_Year>" + 
				            "</Features>" + 
				        "</Top_Up_Wealth_Secure>";

				for(int index=0; index<topUpAmt.length; index++) {
					preparedTopUpTemplate.append( topUpTemplate.replace("##_ID_##", String.valueOf((index+1)) )
									.replace("##_TOPUP_AMT_##", topUpAmt[index])
									.replace("##_TOPUP_YEAR_##", topUpYears[index]) );
				}
			}
			inputs.put( "WITHDRAWAL_DETAILS" , preparedWithdrawalTemplate.toString());
			inputs.put( "TOPUP_DETAILS" , preparedTopUpTemplate.toString());

			for(String key : inputs.keySet()){
				//Log.d(key, "###############: "+"##_"+inputs.get(key)+"_##");
				xmlInput = xmlInput.replace("##_"+key+"_##", inputs.get(key));
			}
			return xmlInput;
		}else{
			return prepareCalcInputXMLDefault(inputFileName, xmlInput, inputs);
		}
	}

	public static String prepareCalcInputXML( String product, String inputFileName, String xmlInput, HashMap<String, String> inputs ) {
		
		if( product.equalsIgnoreCase("WealthMax") ) {
			return prepareCalcInputXMLWealthMax( inputFileName, xmlInput, inputs );
		}
		else if( product.equalsIgnoreCase("WealthSecure") ) {
			return prepareCalcInputXMLWealthSecure( inputFileName, xmlInput, inputs );
		}
		else{
			return prepareCalcInputXMLDefault( inputFileName, xmlInput, inputs );
		}
	}	
}