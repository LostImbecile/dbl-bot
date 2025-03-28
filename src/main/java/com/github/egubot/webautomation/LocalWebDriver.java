package com.github.egubot.webautomation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.github.egubot.storage.ConfigManager;

public class LocalWebDriver implements AutoCloseable {
	// Include selenium in dependencies
	WebDriver driver;
	JavascriptExecutor js;
	Map<String, Object> vars;

	// Set these up in your config.properties file (make sure to double up on
	// slashes)
	// If you don't it's not an issue, this also doesn't affect non-headless mode
	private static String userDataDirectory = ConfigManager.getProperty("User_Data_Directory");
	private static String userProfile = ConfigManager.getProperty("User_Profile_Name");

	public LocalWebDriver() {
		// Not eager, headless, without image
		setUp(false, true, false);
	}

	public LocalWebDriver(boolean withImage) {
		// Not eager, headless
		setUp(withImage, true, false);
	}

	public LocalWebDriver(boolean withImage, boolean isHeadless) {
		// Not eager
		setUp(withImage, isHeadless, false);
	}

	// Preferred for testing
	public LocalWebDriver(boolean withImage, boolean isHeadless, boolean isEager) {
		setUp(withImage, isHeadless, isEager);
	}

	public void setUp(boolean withImage, boolean isHeadless, boolean isEager) {
		ChromeOptions options = new ChromeOptions();

		if (isEager)
			options.setPageLoadStrategy(PageLoadStrategy.EAGER);

		options.addArguments("--window-size=1920,1080");
		options.addArguments("--enable-javascript");
		options.addArguments("--no-proxy-server");
		options.addArguments("--disable-notifications");
		options.addArguments("--disable-infobars");
		options.addArguments("--disable-popup-blocking");
		
		options.addArguments("--dns-prefetch-disable");
		options.addArguments("--enable-cdp-events");
		options.addArguments("--disable-web-security");
		options.addArguments("--disable-features=NetworkService");
		options.addArguments("--disable-features=SidePanelPinning");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-hang-monitor");
		
		options.addArguments("--disable-blink-features=AutomationControlled");
		options.addArguments("--disable-features=TranslateUI,BlinkGenPropertyTrees");
		options.addArguments("--disable-ipc-flooding-protection");
		
		System.setProperty("webdriver.chrome.silentOutput", "true"); // Suppress WebDriver logs

		options.addArguments("--no-sandbox");
		// To avoid being detected as an automated process to an extent
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("useAutomationExtension", false);

		if (!withImage) {
			options.addArguments("--disable-features=HardwareMediaKeyHandling");
			options.addArguments("--disable-images");
			options.addArguments("--blink-settings=imagesEnabled=false");
			options.addArguments("--disable-gpu");
			options.addArguments("--disable-software-rasterizer");
		}

		options.addArguments("--disable-search-engine-choice-screen");

		if (isHeadless) {
			// Some pages will be blank in headless mode, this is to circumvent it
			options.addArguments(
					"--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");

			options.addArguments("--headless=new");
			if (userDataDirectory != null && userProfile != null) {
				options.addArguments("--user-data-dir=" + userDataDirectory);
				options.addArguments("--profile-directory=" + userProfile);
			}
			// Disable this if you don't need it
			options.addArguments("--disk-cache=true");
		}
		driver = new ChromeDriver(options);
		js = (JavascriptExecutor) driver;
		vars = new HashMap<>();
	}

	public void tearDown() {
		driver.quit();
	}

	@Override
	public void close() {
		tearDown();
	}
}
