package com.github.egubot.webautomation;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GrabYoutubeVideo extends LocalWebDriver {

	// Grabs the video or audio file of a youtube video
	public GrabYoutubeVideo() {
		super(false, true, true);
	}

	// Doesn't check for the link being valid thoroughly.
	// Link expires after some time passes.
	public String[] getVideo(String link) {
		driver.get("https://www.y2mate.com/");

		if (!link.contains("youtu"))
			return null;

		sendURL(link);

		waitForPageChange();

		String thumbnail = getThumbnail();
		String name = getName();

		clickOnVideoDownload();

		return new String[] { getVideoDownloadLink(), thumbnail, name };

	}

	public String[] getAudio(String link) {
		driver.get("https://www.y2mate.com/");

		if (!link.contains("youtu"))
			return null;

		sendURL(link);

		waitForPageChange();

		String image = getThumbnail();
		String name = getName();

		driver.findElement(By.linkText("Audio")).click();

		clickOnAudioDownload();

		return new String[] { getAudioDownloadLink(), image, name };
	}

	private String getThumbnail() {
		try {
			return driver.findElement(By.cssSelector(".thumbnail > img")).getAttribute("src");
		} catch (Exception e) {
			return null;
		}
	}

	private String getName() {
		try {
			return driver.findElement(By.cssSelector(".caption > b")).getText();
		} catch (Exception e) {
			return null;
		}
	}

	private void clickOnAudioDownload() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#audio tbody:nth-child(2) .btn"))).click();
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
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#mp4 tbody:nth-child(2) > tr:nth-child(1) .btn"))).click();
	}

	private void waitForPageChange() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Video")));
	}

	public static void main(String[] args) {
		try (GrabYoutubeVideo a = new GrabYoutubeVideo()) {
			System.out.println(a.getVideo("https://www.youtube.com/watch?v=3tE3UzwloJU")[0]);
		}

	}
}
