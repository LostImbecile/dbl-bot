package com.github.egubot.webautomation;

import org.openqa.selenium.By;

public class AIResponseGenerator extends LocalWebDriver {
	// Include selenium in dependencies

	public String getResponse(String targetPerson, String reason) {
		driver.get("https://aiinsults.com/shakespeare");

		driver.findElement(By.id("target")).sendKeys(targetPerson);

		driver.findElement(By.id("reason")).sendKeys(reason);

		driver.findElement(By.cssSelector(".btn:nth-child(6)")).click();

		// Get text
		String st = driver.findElement(By.cssSelector(".fw-bold")).getText().replace("\"", "")
				.replaceAll("(?i)" + targetPerson, targetPerson.toLowerCase());
		
		if (!st.contains(","))
			return st.replaceAll("([a-z])\s([A-Z])", "$1, $2");
		return st;
	}

	public static void main(String[] args) {
		try (AIResponseGenerator a = new AIResponseGenerator()) {
			System.out.println(a.getResponse("person", "test"));
		}
	}

}