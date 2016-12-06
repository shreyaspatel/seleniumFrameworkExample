package com.examples.samples;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.examples.framworks.SeleniumFramework;
import com.examples.samples.LoginTestLocators;

public class LoginTest extends SeleniumFramework{
	
	Sheet loginSheet = null;
	Workbook loginResultWorkbook = null;		
	String path = null;
	
	@Test
	public void createLoginDirectory(){
		File loginSrc = null;
		File loginDest = null;
		
		//Create Directory
		path = createDirectory(userDirPath, "Login");
		loginSrc = new File(parentPropFile.getProperty("LoginInputFile"));
		loginDest = new File(path);
		
		//Copy File
		try {
			FileUtils.copyFileToDirectory(loginSrc, loginDest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Open File
		loginResultWorkbook = fileOpen(path, parentPropFile.getProperty("LoginResultFile"));
		
		assert(null != loginResultWorkbook);
	}
	
	@Test(dependsOnMethods = {"createLoginDirectory"})
	public void loginToFacebook(){
		//Create Formatting
		createFormatting(loginResultWorkbook);
		
		String scrnShot = "";
		WebElement userId = null;
		WebElement password = null;
		WebElement loginButton = null;
		
		//Move to sheet at index 0
		loginSheet = loginResultWorkbook.getSheetAt(0);
		
		
		userId = findAndWait(LoginTestLocators.USERID, 15);
		password = findAndWait(LoginTestLocators.PSWD, 15);
		loginButton = findAndWait(LoginTestLocators.LOGINBTN, 15);
		
		scrnShot = captureScrnShot(path, "FaceBookLoginPage");
		if(userId != null && password != null && loginButton != null){
			//Enter Input and Login
			userId.sendKeys(parentPropFile.getProperty("UserId"));
			password.sendKeys(parentPropFile.getProperty("Password"));
			loginButton.click();
			//Enter Results
			writeData(loginSheet, 1, 2, "Successs");
			compareResults(loginSheet, path, 1, 1, 2, 3, 4, scrnShot, false);
		}else{
			System.out.println("Failed to locate objects");
			//Enter Results
			writeData(loginSheet, 1, 2, "Failed to locate objects");
			compareResults(loginSheet, path, 1, 1, 2, 3, 4, scrnShot, false);
		}
	}
	
	@Test(dependsOnMethods = {"loginToFacebook"})
	public void saveCloseLoginResultFile(){
		//Save and close the result file
		fileSaveClose(loginResultWorkbook, path, parentPropFile.getProperty("LoginResultFile"));
	}
}
