package com.github.egubot.webautomation;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InsultGenerator extends LocalWebDriver {
	// Include selenium in dependencies

	public InsultGenerator() {
		super(false, true, true);
	}

	public String getResponse(String targetPerson, String reason) {
		driver.get("https://aiinsults.com/shakespeare");

		driver.findElement(By.id("target")).sendKeys(targetPerson);

		driver.findElement(By.id("reason")).sendKeys(reason);

		submit();

		// Get text
		String st = driver.findElement(By.cssSelector(".fw-bold")).getText().replace("\"", "")
				.replaceAll("(?i)" + targetPerson, targetPerson.toLowerCase());

		if (!st.contains(","))
			return st.replaceAll("([a-z])\s([A-Z])", "$1, $2");
		return st;
	}

	public void submit() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn:nth-child(6)")));
		element.click();
	}

	public static void main(String[] args) {
		try (InsultGenerator a = new InsultGenerator()) {
			System.out.println(a.getResponse("person", "test"));
		}
	}

}