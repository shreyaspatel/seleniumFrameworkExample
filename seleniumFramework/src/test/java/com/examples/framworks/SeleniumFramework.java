package com.examples.framworks;

import org.testng.annotations.AfterMethod;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

public abstract class SeleniumFramework {
	
	//--------------Login Related Parameters---------------
	public static String url;
    public static WebDriver driver;
    //--------------Login Related Parameters---------------
    
    //--------------Folder Related Parameters--------------
    public static String userDirPath;
    //--------------Folder Related Parameters--------------
    
    //--------------Prop File Related Parameters-----------
    public static FileInputStream parentPropFileInp = null;
    public static Properties parentPropFile = null;
    //--------------Prop File Related Parameters-----------
    
    
    //--------------Excel File Related Parameters----------
    CellStyle curStyle = null;
	Font font = null;
	Row compareResultRow = null;
	Row compareResultRow2 = null;
	Cell compareResultExpected = null;
	Cell compareResultActual = null;
	//--------------Excel File Related Parameters----------
	
/*
 *	A method that runs before the test suit to call openPropertiesFile function using
 * 	predefined environment variable and configure browser for Selenium webdriver based on the parameter passed throw testng.xml
 *	as well as the property defined in property file
 */
	
	@Parameters({"browser"})
	@BeforeSuite
	public static void setup(String browser) throws MalformedURLException {		
		//Opening and Loading the Properties File
		System.out.println(System.getenv("PARENT_PROPERTIES_FILE_PATH"));
		parentPropFile = openPropertiesFile(System.getenv("PARENT_PROPERTIES_FILE_PATH"));
    	
		//Abort testing if properties file not found
		if(parentPropFile == null){
			System.out.println("Parent properties file not found. Application testing aborted.");
			assert false;
		}
		
		//Read value for root
		url = parentPropFile.getProperty("ApplicationURL");
		
		//Initiate Remote WebDriver or Local WebDriver and navigate to root URL
		if(parentPropFile.getProperty("RemoteOrLocal").contentEquals("Remote")){
	        driver = getRemoteWebDriver(browser);
	        
	        getDriver(url);
	        
			driver = new Augmenter().augment(driver);
		} else{
			driver = getLocalWebDriver(browser);
	        
	        getDriver(url);
		}
    	
        //Create Master Directory - Get the parent path using the environment variable - PARENT_RESULT_PATH
        userDirPath = createDirectory(System.getenv("PARENT_RESULT_PATH"), "Regression_yyyyMMddHHmmss");
	}
	
/*
 * The following method gets executed after each methods defined in test suits    
 */
	
	@AfterMethod
	public static void afterMethod() {
    	try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
/*
 * this method will perform closing of the file streams opened during the 
 * setup phase of the framework. 
 */
	
	@AfterMethod
	@AfterSuite
	public static void tearDown() {
		//Closing properties file
		closePropFile(parentPropFileInp);	
	}
	
/*
 * This method will configure web browser based on configuration defined 
 * in setup face through property file. 
 */
	
	public static WebDriver getRemoteWebDriver(String browser){
		driver = null;
		
		//Set the desired capabilities
		DesiredCapabilities caps = new DesiredCapabilities();
		//Setting Desired Capabilities
		if(browser.equalsIgnoreCase("Internet Explorer")){
			caps = DesiredCapabilities.internetExplorer();
		}
       	//Setting Browser
		caps.setBrowserName(browser);
		
		try {
			driver = new RemoteWebDriver(new URL(parentPropFile.getProperty("ServerHubUrl")), caps);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			driver = null;
		}
		
		return driver;
	}
	
	public static WebDriver getLocalWebDriver(String browser){
		WebDriver driver = null;
		
		//File file = new File(System.getenv("MERCURY_IE_DRIVER_PATH"));
		File file;
		
		
		//Set the desired capabilities
		DesiredCapabilities caps = new DesiredCapabilities();
		//Setting Platform
		caps.setPlatform(org.openqa.selenium.Platform.LINUX);
		if(browser.equalsIgnoreCase("Internet Explorer")){
			//file = new File("IEDriverServer.exe");
	    	//System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
			//caps = DesiredCapabilities.internetExplorer();
		}
		if(browser.equalsIgnoreCase("Firefox")){
			file = new File(System.getenv("PARENT_FIREFOX_DRIVER_PATH"));
			System.setProperty("webdriver.gecko.driver", file.getAbsolutePath());
			caps = DesiredCapabilities.firefox();
		}
		if(browser.equalsIgnoreCase("Chrome")){
			file = new File(System.getenv("PARENT_CHROME_DRIVER_PATH"));
	    	System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
			caps = DesiredCapabilities.chrome();
		}
		
		//Create a new instance of the IE/Firefox Driver
		if(browser.equalsIgnoreCase("Internet Explorer")){
			driver = new InternetExplorerDriver(caps);
		}
		if(browser.equalsIgnoreCase("Firefox")){
			driver = new FirefoxDriver(caps);
		}
		if(browser.equalsIgnoreCase("Chrome")){
			driver = new ChromeDriver(caps);
		}
		//Setting Browser
		caps.setBrowserName(browser);
		//Setting Version
		//caps.setVersion(version);
		
		return driver;
	}
	
/*
 * Opens Property file and return Properties object to perform framework configuration  
 */
	
	public static Properties openPropertiesFile(String pathPropFile) {
		//Properties file variables
		parentPropFileInp = null;
	    Properties propFile = new Properties();
	    
	    //Accesses properties file
	    System.out.println("Opening properties file - " + pathPropFile);
    	try {
    		parentPropFileInp = new FileInputStream(pathPropFile);
    		propFile.load(parentPropFileInp);
    		System.out.println("Properties file - " + pathPropFile + " Loaded.");
		} catch (FileNotFoundException e) {
			//Set Null if not found
			propFile = null;
			System.out.println("Properties file not found - " + pathPropFile);
			e.printStackTrace();
		} catch (IOException e) {
			//Set Null if IO Exception
			propFile = null;
			System.out.println("IO Exception while accessing properties file - "  + pathPropFile);
			e.printStackTrace();
		}
    	
    	//Return properties file object
    	return propFile;
	}

/*
 * Method to make get request on specified URL
 */

	public static void getDriver(String url) {
		driver.get(url);
	}
	
/*
 * Close a web browsers opened through out the framework
 */
	
	public static void closeDriver(WebDriver driver){
		driver.close();
	}
	
/*
 * Close all the web browsers opened through out the framework
 */
	
	public static void quitDriver(WebDriver driver){
		driver.quit();
	}
	
/*
 * Method that creates directory based on give path location and folder name
 */
	
	public static String createDirectory(String path, String folder) {
		String folderName = path + folder;
		String directoryPath = "Directory Not Created";
		File file = new File(folderName);
		
		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println("Directory created -" + folderName);
				directoryPath = folderName + "/";
			} else {
				System.out.println("Failed to create directory!");
			}
		}
		return directoryPath;
	}
	
/*
 * This method waits till the given web element on a given web page is display
 * and returns the web element if located otherwise returns null.
 */
	
	public WebElement findAndWait(final By by, int waitInSec){
		try{
			WebElement webElement = (new WebDriverWait(driver, waitInSec))
	  			  .until(new ExpectedCondition<WebElement>(){
	  				@Override
	  				public WebElement apply(WebDriver d) {
	  					try{
	  						if (d.findElement(by).isDisplayed()){
	  							return d.findElement(by);
	  						}else{
	  							return null;
	  						}
	  					}catch(Exception e){
	  						WebElement webElement = null;
							return webElement;
	  					}
	  				}
	  				}
	  			  );
			return webElement;
		} catch(Exception e) {
			WebElement webElement = null;
			return webElement;
		}
	}
	
/*
 * Locates multiple web elements of given type. 
 */

	public List<WebElement> findAndWaitWebElements(final By by, int waitInSec) {
		List<WebElement> webElement = null;
		try{
			webElement = (new WebDriverWait(driver, waitInSec))
	  			  .until(new ExpectedCondition<List<WebElement>>(){
	  				@Override
	  				public List<WebElement> apply(WebDriver d) {
	  					List<WebElement> elementList = null;
	  					try{
	  						elementList = d.findElements(by);
	  						if (elementList.size()>0){
	  							return d.findElements(by);
	  						}else{
	  							return null;
	  						}
	  					}catch(Exception e){
							return null;
	  					}
	  				}
	  				}
	  			  );
			return webElement;
		} catch(Exception e) {
			return null;
		}
	}
	
/*
 * Calls TakesScreenshot interface to capture a screenshot and return the path
 * where the screenshot is being stored.
 */
	
	public String captureScrnShot(String Path, String fileName){
		try{
			File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(scrFile, new File(Path + fileName + ".png"));
		}catch(IOException IE){
			System.out.println("Failed To Capture Image: " + IE.getMessage());
		}
		return Path + fileName + ".png";
	}
	
	public void createFormatting(Workbook formattingWB){
		curStyle = formattingWB.createCellStyle();
    	font = formattingWB.createFont();
	}	
	
/*
 * Method to open a given excel file in a given path using Apache POI and 
 * return the opened excel file. 
 */
	
	public Workbook fileOpen(String Path, String FileName){
		Workbook resultFileWorkBook = null;
		InputStream inp = null;
		
		try { 
    		inp = new FileInputStream(Path + FileName);
    		resultFileWorkBook = WorkbookFactory.create(inp);
    	} catch(FileNotFoundException e) {
    		System.out.println("File not found");
    	      e.printStackTrace();
        } catch (IOException e) {
        	System.out.println("IO Exception");
          e.printStackTrace();
        } catch (InvalidFormatException e) {
        	System.out.println("InvalidFormatException");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resultFileWorkBook;
	}
	
	public void fileSaveClose(Workbook WB, String Path, String FinalResultFileName) {
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(Path + FinalResultFileName);
			WB.write(fileOut);
			fileOut.close(); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
/*
 * Write in a particular cell of given sheet, row, and column.
 */
	
	public void writeData(Sheet SH, int RowNum, int CellNum, String Data) {
		Row row = null;     
		Cell cell = null;
		row = SH.getRow(RowNum);
		if(row == null){
			row = SH.createRow(RowNum);
		}
		cell = row.getCell(CellNum);
		if(cell == null){
			cell = row.createCell(CellNum);
		}
		cell.setCellValue(Data);
	}
	
/*
 * close the given property file.
 */
	
	public static void closePropFile(FileInputStream propFileInp){
		try{
			//Close Properties File
			propFileInp.close();
			System.out.println("Properties File - '" + propFileInp + "' Closed successfully");			
		}catch(IOException e){
			System.out.println("IO Exception while closing properties file - '" + propFileInp + "'");
		}
	}
	
/*
 * Compare the expected and actual results in a cell of given 
 * sheet, row, and column.   
 */
	
	public void compareResults(Sheet sheet, String path, int resRow, int expCell, int actCell, int resCell, int fileNameCell, String scrnShot, boolean takeScrnShot) {
		compareResultRow = sheet.getRow(resRow);
		compareResultExpected = compareResultRow.getCell(expCell);
		compareResultActual = compareResultRow.getCell(actCell);
		if(compareResultExpected.getStringCellValue().contentEquals(compareResultActual.getStringCellValue())){
			writeData(sheet, resRow, resCell, "Pass");
		}else{
			writeData(sheet, resRow, resCell, "Fail");
			//Take Screen shot if required 
			if(takeScrnShot){
				scrnShot = captureScrnShot(path, scrnShot);
			}
			//Write Screen shot file name if required
			if(scrnShot.length() > 2){
				writeData(sheet, resRow, fileNameCell, scrnShot);
			}
		}
	}
	
	public void waitForSeconds(int noOfMilliSeconds){
		try {
			Thread.sleep(noOfMilliSeconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
/*
 *  Click the given element using javascript. 
 */

	public void clickElementThroughJavaScript(WebDriver driver, WebElement element){
		//To Click on elements which may not be visible on screen
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
	}

/*
 *  Click the given element using Action API. 
 */
	
	public void clickElementThroughActions(WebDriver driver, WebElement element){
		//To Click on elements which may not be visible or are from 'LI' tag
		Actions action = new Actions(driver);
		action.moveToElement(element).click().perform();
	}
	
/*
 *  Hover over the given element using Action API. 
 */
	
	public void hoverOverThroughActions(WebDriver driver, WebElement element){
		//To hover over an WebElement
		Actions action = new Actions(driver);
		action.moveToElement(element).perform();
	}
}