package com.streebo.bsli.productXpress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class PXHandler extends Activity {
	
	public static Logger LOGGER = Logger.getLogger(PXHandler.class.getName());
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.activity_view_document);
		LOGGER.warning("In PXHandler");
		
		String checkPxFolderExists = getIntent().getStringExtra("checkPxFolderExists");
		String pxFolderPath = getIntent().getStringExtra("pxFolderPath");
		String extractZip = getIntent().getStringExtra("extractZip");
		String zipFilePath = getIntent().getStringExtra("zipFilePath");
		String fundExcelPath = getIntent().getStringExtra("fundExcelPath");
		
		LOGGER.warning("checkPxFolderExists : " + checkPxFolderExists);
		LOGGER.warning("pxFolderPath : " + pxFolderPath);
		LOGGER.warning("extractZip : " + extractZip);
		LOGGER.warning("zipFilePath : " + zipFilePath);
		
		Intent returnIntent = new Intent();
		
		if(checkPxFolderExists!=null && checkPxFolderExists.equalsIgnoreCase("true")) {
			returnIntent.putExtra("pxFolderExists", checkPXFolder(pxFolderPath));
			returnIntent.putExtra("fundExcelExists", checkFundExcel(fundExcelPath));	
		}
		else if(extractZip!=null && extractZip.equalsIgnoreCase("true")) {
			boolean deleteFolder = false;
			if( getIntent().getStringExtra("deleteBeforeExtract").equalsIgnoreCase("true") )
				deleteFolder = true;
			returnIntent.putExtra("zipExtractedStatus", extractZip(zipFilePath, pxFolderPath, deleteFolder)); 
		}
		
		LOGGER.warning("Returning from PXHandler");
		
		setResult(RESULT_OK, returnIntent);
		
		finish();
	}
	
	private boolean checkPXFolder(String pxFolderPath) {
		
		LOGGER.warning("In checkPXFolder");
		
		URI pxFolderUrl = URI.create(pxFolderPath);
		String pxFolderFullPath = pxFolderUrl.toString();
		if ( pxFolderFullPath.startsWith("file:") ){
			pxFolderFullPath = pxFolderFullPath.substring(7);
		}
		
		LOGGER.warning("pxFolderFullPath : " + pxFolderFullPath);
		
		File pxDir = new File(pxFolderFullPath);
		
		boolean pxFolderExists = false;
		
		if(pxDir.exists() && pxDir.isDirectory()) {
			LOGGER.warning("PX Folder Exists");
			pxFolderExists = true;
		}
		else {
			LOGGER.warning("PX Folder Doesn't Exists");
		}
		
		return pxFolderExists;
	}
	
	private boolean checkFundExcel(String fundExcelPath) {
		
		LOGGER.warning("In checkFundExcel");
		
		URI fundExcelUrl = URI.create(fundExcelPath);
		String fundExcelFullPath = fundExcelUrl.toString();
		if ( fundExcelFullPath.startsWith("file:") ){
			fundExcelFullPath = fundExcelFullPath.substring(7);
		}
		
		LOGGER.warning("fundExcelFullPath : " + fundExcelFullPath);
		
		File pxDir = new File(fundExcelFullPath);
		boolean fundExcelExists = false;
		
		if(pxDir.exists() && pxDir.isFile()) {
			LOGGER.warning("Fund Excel Exists");
			fundExcelExists = true;
		}
		else {
			LOGGER.warning("Fund Excel Doesn't Exists");
		}
		
		return fundExcelExists;
	}

	private boolean extractZip(String zipFilePath, String pxFolderPath, boolean deleteFolder) {
		
		URI zipFileURL = URI.create(zipFilePath);
		String zipFileFullPath = zipFileURL.toString();
		if ( zipFileFullPath.startsWith("file:") ){
			zipFileFullPath = zipFileFullPath.substring(7);
		}
		
		URI pxFolderUrl = URI.create(pxFolderPath);
		String pxFolderFullPath = pxFolderUrl.toString();
		if ( pxFolderFullPath.startsWith("file:") ){
			pxFolderFullPath = pxFolderFullPath.substring(7);
		}
		
		LOGGER.warning("zipFileFullPath : " + zipFileFullPath);
		LOGGER.warning("pxFolderFullPath : " + pxFolderFullPath);
		
		File zipFile = new File(zipFileFullPath);
		
		boolean isValidZip = isValidZip(zipFile);
		if(!isValidZip){
			boolean zipDeleted = zipFile.delete();
			LOGGER.warning("zipDeleted : " + zipDeleted);
			return false;
		}
		
		File parentFolder = new File(pxFolderFullPath).getParentFile();
		
		LOGGER.warning("zipFile Exists : " + zipFile.exists());
		LOGGER.warning("parentFolder Exists : " + parentFolder.exists());
		
		if(deleteFolder){ //Delete before zip extract
			LOGGER.warning("Delete PX Folder Start : " + pxFolderFullPath);
			deleteFolderContent(new File(pxFolderFullPath));
			LOGGER.warning("Delete PX Folder End : ");
		}
			
		boolean status = false;
		
		try {
			
			FileInputStream fin = new FileInputStream(zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			
			while ((ze = zin.getNextEntry()) != null) {
				
				String fileName = ze.getName().replace("\\", "/");
				
				String destFile = parentFolder.getAbsolutePath() + "/" + fileName;
				
				LOGGER.warning("Path : " + destFile);
				
				if (ze.isDirectory()) {
					
					File f = new File( destFile );

					if (!f.isDirectory()) {
						f.mkdirs();
					}

				} else {
					
					File parentDir = new File(destFile.substring(0, destFile.lastIndexOf(File.separator)));
					
					if( !parentDir.exists() ) {
						parentDir.mkdirs();
					}

					FileOutputStream fout = new FileOutputStream( destFile );

					byte[] bytes = new byte[102400];
					int noBytesRead = 0;

					while ((noBytesRead = zin.read(bytes)) != -1) {
						fout.write(bytes, 0, noBytesRead);
					}

					fout.close();
				}
				
				zin.closeEntry();
			}
			
			zin.close();
			
			status = true;
			
			System.gc();
			boolean zipDeleted = zipFile.delete();
			LOGGER.warning("zipDeleted : " + zipDeleted);
			
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} finally {
			boolean zipDeleted = zipFile.delete();
			LOGGER.warning("zipDeleted : " + zipDeleted);
		}
		return status;
	}
	private void deleteFolderContent(File  fileOrDirectory){
		 if (fileOrDirectory.isDirectory())
		        for (File child : fileOrDirectory.listFiles())
		        	deleteFolderContent(child);

	    try{fileOrDirectory.delete();}catch(Exception e){LOGGER.warning(e.getMessage());}
	    return ;
	}
	static boolean isValidZip(final File file) {
	    ZipFile zipfile = null;
	    try {
	        zipfile = new ZipFile(file);
	        return true;
	    } catch (IOException e) {
	        return false;
	    } finally {
	        try {
	            if (zipfile != null) {
	                zipfile.close();
	                zipfile = null;
	            }
	        } catch (IOException e) {
	        }
	    }
	}
}
