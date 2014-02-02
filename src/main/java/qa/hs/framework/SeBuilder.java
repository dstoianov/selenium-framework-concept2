package qa.hs.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.Reporter;

public final class SeBuilder
{

	private SeUtil util;
	private WebDriver driver;
	
	private final File CHROMEDRIVER = new File("chromedriver.exe");
	private final File CHROMEDRIVERZIP = new File("chromedriver_win32.zip");
	private String browser;
	private URL appUrl;
	private URL hubUrl;
	private String sauceUsername;
	private String sauceKey;

	public SeBuilder() {
		System.out.println("Created new SeBuilder object.");
	}

	public URL getAppUrl() {
		if ( appUrl == null || appUrl.toExternalForm().isEmpty() ) {
			throw new NullPointerException("The app url was not yet set in the SeBuilder object.");
		} else {
		    return appUrl;
		}
	}

	public WebDriver getDriver() {
		if ( driver == null ) {
			loadNewBrowser();
			return driver;
		} else {
		    return driver;
		}
	}

	public URL getHubUrl() {
		return hubUrl;
	}
	
	public SeUtil getUtil() {
		if ( this.util == null ) {
			return new SeUtil( driver );
		} else {
		   return this.util;
		}
	}

	/*
	 * Download latest Chrome WebDriver binary if necessary.
	 */
	private void getLatestWindowsChromeDriver() {
		if ( !CHROMEDRIVER.exists() ) {	
			FileOutputStream fos = null;
			InputStream in = null;
			try {
				URL downloadUrl = new URL("http://chromedriver.storage.googleapis.com/index.html?path=2.8/chromedriver_win32.zip");
				URLConnection conn = downloadUrl.openConnection();
				in = conn.getInputStream();
				fos = new FileOutputStream( CHROMEDRIVERZIP.getAbsoluteFile() );
				byte[] b = new byte[1024];
				int count;
				while ( ( count = in.read(b) ) != -1 ) {
					fos.write(b, 0, count);
				}				
			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
			} catch ( IOException e ) {
				e.printStackTrace();
			} finally {
				try {
					fos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if ( CHROMEDRIVERZIP.exists() ) {
					Reporter.log( "Finished downloading Chrome driver zip archive: " + CHROMEDRIVERZIP.getAbsolutePath(), true );
				} else {
					Reporter.log( "Failure to download the Chrome driver zip archive.", true );
				}				
			}
			if ( CHROMEDRIVERZIP.exists() ) {
				unzip( CHROMEDRIVERZIP.getAbsolutePath(), CHROMEDRIVER.getAbsolutePath(), "" );
				//CHROMEDRIVERZIP.delete();
			} else {
				throw new IllegalStateException( "Could not unzip Chrome driver.");
			}
		} else {
			Reporter.log("Chrome driver was found located at: " + CHROMEDRIVER.getAbsolutePath(), true );
		}
	}

	public String getSauceKey() {
		return sauceKey;
	}

	public String getSauceUsername() {
		return sauceUsername;
	}

	public SeBuilder loadNewBrowser() {
		System.out.println("Loading WebDriver instance...");
		DesiredCapabilities abilities = null;
		switch ( browser ) {
		case "chrome":
			getLatestWindowsChromeDriver();
			System.setProperty( "webdriver.chrome.driver", CHROMEDRIVER.getAbsolutePath() );
			abilities = DesiredCapabilities.chrome();
			driver = new ChromeDriver( abilities );
			break;
		case "firefox":
			abilities = DesiredCapabilities.firefox();
			driver = new FirefoxDriver( abilities );
			break;
		case "ie":
			System.setProperty("webdriver.ie.driver","IEDriverServer.exe");
			abilities = DesiredCapabilities.internetExplorer();
			driver = new InternetExplorerDriver( abilities );
			break;
		case "safari":
			abilities = DesiredCapabilities.safari();
			driver = new SafariDriver( abilities );
			break;
		case "phantomjs":
			// not yet
			break;
		case "gridchrome32":
			if ( hubUrl.toExternalForm().isEmpty() ) {
				throw new IllegalStateException( "Please set the Selenium Grid hub URL before calling loadNewBrowser()");
			}
			abilities = DesiredCapabilities.chrome();
			abilities.setCapability( "platform", "Windows 8" );
			abilities.setCapability( "version", "32" );
			abilities.setCapability( "screen-resolution", "1280x1024" );
			driver = new RemoteWebDriver( hubUrl, abilities );
			break;
		case "gridfirefox26":
			// not yet
			break;
		case "gridie10":
			// not yet
			break;
		case "gridsafari7":
			// not yet
			break;
		default:
			throw new IllegalStateException( "Unknown browser string '" + browser + "'." );
		}
        initializeUtil();
        navigateTo( appUrl.toExternalForm() );
		return this;
	}
	
	public void initializeUtil() {
		if ( !(driver == null) ) {
			if ( util == null ) {
			    setUtil( new SeUtil( driver ) );
			} else {
				throw new IllegalStateException("Utility object already was initialized when you tried to initialize it.");
			}
		} else {
			throw new NullPointerException("Driver is not yet loaded or is null.");
		}
	}
	
	public void navigateTo( String url ) {
		if ( !(driver == null) ) {
			driver.navigate().to( url );
		} else {
			throw new NullPointerException("Driver is not yet loaded or is null.");
		}		
	}

	public void setAppUrl( String appUrl ) {
		try {
			this.appUrl = new URL( appUrl );
		} catch ( MalformedURLException e ) {
			e.printStackTrace();
		}
	}

	public void setBrowser( String bro ) {
		browser = bro;		
	}

	public void setDriver( WebDriver driver ) {
		this.driver = driver;
	}

	public void setHubUrl( String hubUrl ) {
		if ( !hubUrl.isEmpty() ) {
			try {
				this.hubUrl = new URL( hubUrl );
			} catch ( MalformedURLException e ) {
				e.printStackTrace();
			}
		}
	}

	public void setSauceKey(String sauceKey) {
		this.sauceKey = sauceKey;
	}

	public void setSauceUsername(String sauceUsername) {
		this.sauceUsername = sauceUsername;
	}
	
	public void setUtil( SeUtil util ) {
		this.util = util;
	}
	
	public String getBrowser() {
		return browser;
	}

	/**
	 * Unzip a zip file.
	 * @param source
	 * @param destination
	 * @param password
	 */
	private static void unzip( String source, String destination, String password ) {
		try {
			ZipFile zipFile = new ZipFile( source );
			if ( zipFile.isEncrypted() ) {
				zipFile.setPassword( password );
			}
			zipFile.extractAll( destination );
		} catch ( ZipException e ) {
			e.printStackTrace();
		}
	}	
	
}
