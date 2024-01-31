package com.github.egubot.webautomation;

import java.util.Collections;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.github.egubot.storage.ConfigManager;

public class LocalWebDriver implements AutoCloseable{
	// Include selenium in dependencies
	WebDriver driver;
	JavascriptExecutor js;

	private static String userDataDirectory = ConfigManager.getProperty("User_Data_Directory");
	private static String userProfile = ConfigManager.getProperty("User_Profile_Name");

	public LocalWebDriver() {
		setUp();
	}

	public void setUp() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--blink-settings=imagesEnabled=false");
		options.addArguments("--window-size=1920x1080");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-images");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-software-rasterizer");
		options.addArguments("--disk-cache=true");
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("useAutomationExtension", false);
	
		if (userDataDirectory != null && userProfile != null) {
			options.addArguments("--user-data-dir=" + userDataDirectory);
			options.addArguments("--profile-directory=" + userProfile);
		}
		driver = new ChromeDriver(options);
		js = (JavascriptExecutor) driver;
	}

	public void tearDown() {
		driver.quit();
	}

	@Override
	public void close() {
		tearDown();
	}
}
