package com.github.egubot.features;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LegendsSummonRates {

	public static String getRates(String summonURL) throws IOException {
		// Connect to the website and get the HTML document
		Document document = Jsoup.connect(summonURL).get();
		
		// Select the element with class "text-center"
        Element titleElement = document.selectFirst("h2.text-center");
        
        // Get the text of the element
        String title = titleElement.text();
        
        // Print the title
        System.out.println("Banner: " + title);
        System.out.println("------------------------------");
        Elements gashaDetails = document.select(".gasha-details");
        int rewardCounter = 0;
        for (Element gashaDetail : gashaDetails) {
            Elements imageElements = gashaDetail.select("img");

            for (Element imageElement : imageElements) {
                String imgTitle = imageElement.attr("title");
                String imageUrl = imageElement.absUrl("src");

                System.out.println("Title: " + imgTitle);
                System.out.println("Image URL: " + imageUrl);
            }

            Element crystalNumElement = gashaDetail.selectFirst(".crystal-num");
            String crystalNum = crystalNumElement.text();
            System.out.println("Crystal Number: " + crystalNum);

            try {
				Element rewardNumElement = gashaDetail.selectFirst(".reward-num");
				String rewardNum = rewardNumElement.text();
				System.out.println("Reward Number: " + rewardNum);
				rewardCounter++;
			} catch (Exception e) {
			}
            
            String text = gashaDetail.selectFirst("div[style]").text();
            System.out.println("Text: " + text);


            System.out.println("------------------------------");
        }
        System.out.println("Number of rewards: " +  rewardCounter);
        System.out.println("Featured:");
        
        Element characterContainer = document.selectFirst(".character-container");

        Elements elements = characterContainer.select("a.chara-list");

        for (Element element : elements) {
            String href = element.attr("href");

            Element leftElement = element.selectFirst("div.mx-2 > div:first-child");
            String leftValue = leftElement.text();

            Element rightElement = element.selectFirst("div.mx-2 > div:last-child");
            String rightValue = rightElement.text();

            Element isNewElement = element.selectFirst("div.isNew");
            boolean isNew = isNewElement != null;

            System.out.println("Href: " + href);
            System.out.println("Left value: " + leftValue);
            System.out.println("Right value: " + rightValue);
            System.out.println("Is new: " + isNew);
        }
		return null;
	}

	public static void main(String[] args) {
		try {
			getRates("https://dblegends.net/banner/2025900");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
