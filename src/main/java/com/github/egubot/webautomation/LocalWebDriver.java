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

	private static String userDataDirectory = ConfigManager.getProperty("User_Data_Directory");
	private static String userProfile = ConfigManager.getProperty("User_Profile_Name");

	public LocalWebDriver() {
		setUp(false, true, false);
	}

	public LocalWebDriver(boolean withImage) {
		setUp(withImage, true, false);
	}

	public LocalWebDriver(boolean withImage, boolean isHeadless) {
		setUp(withImage, isHeadless, false);
	}

	public LocalWebDriver(boolean withImage, boolean isHeadless, boolean isEager) {
		setUp(withImage, isHeadless, isEager);
	}

	public void setUp(boolean withImage, boolean isHeadless, boolean isEager) {
		ChromeOptions options = new ChromeOptions();
		// Disable automatic proxy detection
		if (isEager)
			options.setPageLoadStrategy(PageLoadStrategy.EAGER);

		options.addArguments("--window-size=1920x1080");
		options.addArguments("--enable-javascript");
		options.addArguments("--no-proxy-server");
		options.addArguments("--disable-notifications");
		options.addArguments("--disable-infobars");
		options.addArguments("--disable-popup-blocking");
		System.setProperty("webdriver.chrome.silentOutput", "true"); // Suppress WebDriver logs

		options.addArguments("--no-sandbox");
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("useAutomationExtension", false);

		options.addArguments(
				"--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");
		if (!withImage) {
			options.addArguments("--disable-features=HardwareMediaKeyHandling");
			options.addArguments("--disable-images");
			options.addArguments("--blink-settings=imagesEnabled=false");
			options.addArguments("--disable-gpu");
			options.addArguments("--disable-software-rasterizer");
		}

		if (isHeadless) {
			options.addArguments("--headless");
			if (userDataDirectory != null && userProfile != null) {
				options.addArguments("--user-data-dir=" + userDataDirectory);
				options.addArguments("--profile-directory=" + userProfile);
			}
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
