package com.streebo.bsli.productXpress.embeddedCalculator;

import java.io.File;
import java.util.HashMap;

import org.json.XML;

import android.util.Log;

import com.streebo.bsli.productXpress.PXEmbeddedCalculator;
import com.solcorp.productxpress.calculator.PxCalculatorHomeJNI;
import com.solcorp.productxpress.calculator.spec.PxCalculatorHome;
import com.solcorp.productxpress.calculator.spec.PxPushCalculator;

public class Calculation {
	
	private static boolean productXpressInitialized = false;
	
	public void performCalculation(String productDirName, String inputFileName, HashMap<String, String> inputs, HashMap<String, String> outputs) throws Exception{
		String productPath = FileUtil.getProductXpressProductDataPath() + "/" + productDirName;
		
		PxCalculatorHome calculatorHome = getCalculatorHome();
		try{
			//Log.d("Performing Calulation", "Start");

			if(productDirName.equals("planselector"))
				this.performDeployment(productDirName);
			
			//Log.d("productDirName in performCalculation", productDirName);
			//Log.d("productPath in performCalculation", productPath);
			// Perform calculations
			PxPushCalculator calculator = calculatorHome.getPushCalculator();
			
			File clcinFile = new File(productPath, inputFileName + ".clcin");
			
			//Log.d("my", "################### file name: "+clcinFile.getName());
			
			String xmlInput = FileUtil.fileToString(clcinFile);
			
			xmlInput  = FileUtil.prepareCalcInputXML( productDirName, inputFileName, xmlInput, inputs );
			
			/*String xmlInputFileName = clcinFile.getAbsolutePath().replace(".clcin", "_xml.txt");
			FileUtil.stringToFile(xmlInput, xmlInputFileName); */
			
			/*Log.w("xmlInput", xmlInput);*/
			
			//String outputFileName = clcinFile.getAbsolutePath().replace(".clcin", "_json.txt");
			String output = calculator.calculate( xmlInput );
			
			/*Log.d("output", "###############: "+"##_"+output+"_##");
			Log.d("output file name", "###############: "+"##_"+outputFileName+"_##");*/
			
            String jsonPrettyPrintString = XML.toJSONObject(output).toString(4);
            
            /*Log.w("Output JSON : ", jsonPrettyPrintString);*/
            
			for(String key : outputs.keySet()){
				 //Log.d("key : ", key);
				outputs.put(key, jsonPrettyPrintString);
			}
			
			/*FileUtil.stringToFile(jsonPrettyPrintString, outputFileName); */
			//Log.d("Performing Calulation", "End Successfully");
		} 
		catch(UnsatisfiedLinkError ule) {
			ule.printStackTrace();
		}
		finally {
			//calculatorHome.unloadDeploymentPackages();
			PXEmbeddedCalculator.isSuccessful = true;
		}
	}
	
	public void performDeployment(String productDirName){
		//Log.d("Performing Deployment", "Start");
		String productPath = FileUtil.getProductXpressProductDataPath() + "/" + productDirName;
		//Log.d("productPath in performDeployment", productPath);
		
		PxCalculatorHome calculatorHome = getCalculatorHome();
		
		try{
			calculatorHome.unloadDeploymentPackages();
			//Log.d("Deployment Package", "###### Undeploy Successfully ##########");
			
			String deploymentPackageFile = FileUtil.getDeploymentPackageFileName(productPath);
			//Log.d("Deployment Package", "####### Deployment Package: ######### "+deploymentPackageFile);
			if(deploymentPackageFile.endsWith(".pxdp")){
				calculatorHome.loadDeploymentPackage(deploymentPackageFile, null, null, null);
			}	
			else if(deploymentPackageFile.endsWith(".pxdo")) {
			    calculatorHome.loadDeploymentObject(deploymentPackageFile, null, null, null);
			}
			
			/*File files[] = FileUtil.getExternalTableFiles(productPath+"/ExternalTables");
			//Log.d("External Table", "######## ExternalTables Files: ########"+ files.length);
			for (File file : files) {
				try{
					calculatorHome.deployExternalTableData(file.getAbsolutePath(), false);
				}catch(Exception e){
					//Log.d("External Table", "###### External Table already exists #######");
					Log.e("External Table", e.getMessage());
					break;
				}
			}*/
			
			//Log.d("Performing Deployment", "End Successfully");
		}
		catch(UnsatisfiedLinkError ule) {
			ule.printStackTrace();
		}
		finally {
			//calculatorHome.unloadDeploymentPackages();
			PXEmbeddedCalculator.isSuccessful = true;
		}	
	}
	private static PxCalculatorHome getCalculatorHome() {
		PxCalculatorHome home = PxCalculatorHomeJNI.instance();
		if(!productXpressInitialized) {
			home.initialize(FileUtil.getProductXpressInstallPath());
			productXpressInitialized = true;
		}
		return home;
	}
}
