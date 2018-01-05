/*package com.streebo.bsli.productXpress;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.streebo.bsli.productXpress.embeddedCalculator.Calculation;

public class PXEmbeddedCalculator extends Activity {
	
	public static Boolean isSuccessful = false;
	private Calculation calculation = new Calculation();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("This is a test message")
	           .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   // FIRE ZE MISSILES!
	               }
	           })
	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   // User cancelled the dialog
	               }
	           });
		builder.show();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				String productName = getIntent().getStringExtra("productName");
				String inputFileName = getIntent().getStringExtra("inputFileName");
				//Log.d("~~~~JAVA :: start time : " +productName,  String.valueOf(new Date().getTime()));
				
				Bundle bundle = getIntent().getExtras();
				HashMap<String,String> inputs = new HashMap<String,String>();
				HashMap<String,String> outputs = new HashMap<String,String>();
				
				for (String key : bundle.keySet()) {
				    String value = bundle.get(key).toString();
				    if(key.startsWith("ip_")){
				    	inputs.put(key.split("ip_")[1], value);
				    }
				    else if(key.startsWith("op_")){
				    	outputs.put(key.split("op_")[1], value);
				    }
				}
				//Log.d("my", "************ Product: "+productName);
				//Log.d("my", "************ InputFileName: "+inputFileName);
				
				try {
					if(inputFileName.equals("deployPackageAndCalc")){
						calculation.performDeployment(productName);
					}else{
						//Log.d("my", "#####performCalculation######");
						calculation.performCalculation(productName, inputFileName, inputs, outputs);
					}
				} catch (Exception e) {
					e.printStackTrace();
					//Log.d("my", e.toString());
				} finally {
					Intent returnObj = new Intent();
					returnObj.putExtra("isSuccessful", PXEmbeddedCalculator.isSuccessful);
					returnObj.putExtra("productName", productName);
					
					for(String key : inputs.keySet()) {
						returnObj.putExtra("ip_"+key, inputs.get(key));
					}
					
					//Log.d("my", "###################### Outputs after calculations");
					
					for(String key : outputs.keySet()) {
						returnObj.putExtra("op_"+key, outputs.get(key));
						//Log.d("my", "************* outputs--"+ key +":"+ outputs.get(key));
					}
					
					setResult(RESULT_OK, returnObj);
					//Log.d("~~~~JAVA :: End time : " +productName,  String.valueOf(new Date().getTime()));
					finish();
				}
				
			}
		};
		Thread t= new Thread(runnable);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
*/


package com.streebo.bsli.productXpress;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.streebo.bsli.productXpress.embeddedCalculator.Calculation;

public class PXEmbeddedCalculator extends CordovaPlugin {
	public static Boolean isSuccessful = false;
	private Calculation calculation = new Calculation();
	@Override 
	public boolean execute (String action, JSONArray pxInputs, CallbackContext callbackContext)
	throws JSONException {
		Log.d("PXEmbeddedCalculator", "************ action: "+action);
		try {
			if(action.equals("pxCall")){
				JSONObject inputParams = pxInputs.getJSONObject(0);
				Log.d("PXEmbeddedCalculator", "************ inputParams: "+inputParams);
			
				String productName = inputParams.getString("productName");
				String inputFileName = inputParams.getString("inputFileName");
				//Log.d("PXEmbeddedCalculator", "************ productName: "+productName);
				//Log.d("PXEmbeddedCalculator", "************ inputFileName: "+inputFileName);
				Iterator<String> keys = inputParams.keys();
				HashMap<String,String> inputs = new HashMap<String,String>();
				HashMap<String,String> outputs = new HashMap<String,String>();
				
				 while(keys.hasNext()){
					
					 String key = keys.next(); 
				    String value = inputParams.get(key).toString();
				    if(key.startsWith("ip_")){
				    	inputs.put(key.split("ip_")[1], value);
				    }
				    else if(key.startsWith("op_")){
				    	outputs.put(key.split("op_")[1], value);
				    }
				}
				Log.d("my", "************ Product: "+productName);
				Log.d("my", "************ InputFileName: "+inputFileName);
				
				try {
					if(inputFileName.equals("deployPackageAndCalc")){
						calculation.performDeployment(productName);
					}else{
						Log.d("my", "#####performCalculation######");
						calculation.performCalculation(productName, inputFileName, inputs, outputs);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.d("my", e.toString());
				} finally {
					JSONObject returnObj = new JSONObject();
					returnObj.put("isSuccessful", PXEmbeddedCalculator.isSuccessful);
					returnObj.put("productName", productName);
					
					for(String key : inputs.keySet()) {
						returnObj.put("ip_"+key, inputs.get(key));
					}
					
					Log.d("my", "###################### Outputs after calculations");
					
					for(String key : outputs.keySet()) {
						returnObj.put("op_"+key, outputs.get(key));
						Log.d("my", "************* outputs--"+ key +":"+ outputs.get(key));
					}
					
					Log.d("~~~~JAVA :: End time : " +productName,  String.valueOf(new Date().getTime()));
					
					callbackContext.success(returnObj);
				}
			}
				return true;
		} catch (Exception e) {
			return false;
		}
		
	}
}