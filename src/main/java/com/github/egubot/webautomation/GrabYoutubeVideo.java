package com.github.egubot.webautomation;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GrabYoutubeVideo extends LocalWebDriver {
	public GrabYoutubeVideo() {
		super(false, true, true);
	}

	public String getVideo(String link) {
		driver.get("https://www.y2mate.com/");

		if (!link.contains("youtu"))
			return null;

		sendURL(link);

		waitForPageChange();

		clickOnVideoDownload();

		return getVideoDownloadLink();

	}

	public String getAudio(String link) {
		driver.get("https://www.y2mate.com/");

		if (!link.contains("youtu"))
			return null;

		sendURL(link);

		waitForPageChange();

		driver.findElement(By.linkText("Audio")).click();

		clickOnAudioDownload();

		return getAudioDownloadLink();
	}

	private void clickOnAudioDownload() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#audio .btn"))).click();
	}

	private void sendURL(String link) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		WebElement a = wait.until(ExpectedConditions.elementToBeClickable(By.id("txt-url")));
		a.sendKeys(link);
		driver.findElement(By.id("btn-submit")).click();
	}
	
	private String getAudioDownloadLink() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		WebElement downloadLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Download .mp3")));
		return downloadLink.getAttribute("href");
	}

	private String getVideoDownloadLink() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		WebElement downloadLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Download .mp4")));
		return downloadLink.getAttribute("href");
	}

	private void clickOnVideoDownload() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#mp4 tr:nth-child(1) .btn"))).click();
	}

	private void waitForPageChange() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Video")));
	}

	public static void main(String[] args) {
		try (GrabYoutubeVideo a = new GrabYoutubeVideo()) {
			System.out.println(a.getAudio("https://youtu.be/JUl2fg7AOVQ?feature=shared"));

		}

	}
}
