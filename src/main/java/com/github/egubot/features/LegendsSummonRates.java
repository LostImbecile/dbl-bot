package com.github.egubot.features;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.objects.Characters;
import com.github.egubot.objects.SummonBanner;
import com.github.egubot.objects.SummonCharacter;
import com.github.egubot.objects.SummonStep;

public class LegendsSummonRates {

	public static String getRates(String summonURL) throws IOException {
		Document document = Jsoup.connect(summonURL).get();

		Element titleElement = document.selectFirst("h2.text-center");

		String title = titleElement.text();
		SummonBanner banner = new SummonBanner();

		banner.setTitle(title);

		Elements gashaDetails = document.select(".gasha-details");
		for (Element gashaDetail : gashaDetails) {
			SummonStep step = new SummonStep();

			Elements imageElements = gashaDetail.select("img");

			for (int i = 0; i < imageElements.size(); i++) {
				Element imageElement = imageElements.get(i);
				String imgTitle = imageElement.attr("title");
				String imageUrl = imageElement.absUrl("src");

				if (i == 0) {
					step.setCurrencyType(imgTitle);
				} else if (imgTitle.isBlank()) {
					step.setSpecialAttribute(imageUrl);
				} else {
					step.setRewardTitle(imgTitle);
					step.setRewardURL(imageUrl);
				}
			}

			Element crystalNumElement = gashaDetail.selectFirst(".crystal-num");
			String currencyNum;
			if (crystalNumElement != null)
				currencyNum = crystalNumElement.text();
			else {
				currencyNum = gashaDetail.selectFirst(".tix-num").text();
			}

			step.setCurrencyNeeded(currencyNum);

			try {
				Element rewardNumElement = gashaDetail.selectFirst(".reward-num");
				String rewardNum = rewardNumElement.text();
				step.setRewardNum(rewardNum);
			} catch (Exception e) {
				step.setRewardNum(0);
			}

			String text = gashaDetail.selectFirst("div[style]").text();
			step.setNumberOfPulls(text);

			if (step.getCurrencyNeeded() == 300) {
				banner.getOnceOnlySteps().add(step);
			} else {
				banner.getNormalSteps().add(step);
			}
		}

		Element characterContainer = document.selectFirst(".character-container");

		Elements elements = characterContainer.select("a.chara-list");

		for (Element element : elements) {
			SummonCharacter character = new SummonCharacter();
			String href = element.attr("href");

			character.setCharacter(getCharacer(href));

			Element leftElement = element.selectFirst("div.mx-2 > div:first-child");
			String leftValue = leftElement.text();
			character.setzPowerAmount(leftValue);

			Element rightElement = element.selectFirst("div.mx-2 > div:last-child");
			String rightValue = rightElement.text();
			character.setSummonRate(rightValue);

			Element isNewElement = element.selectFirst("div.isNew");
			character.setNew(isNewElement != null);
			banner.getFeaturedUnits().add(character);
		}

		System.out.println(banner);

		return null;
	}

	private static Characters getCharacer(String href) {
		int id = LegendsDatabase.getSiteID(href);
		if (id == -1)
			return null;

		return LegendsDatabase.getCharacterHash().get(id);

	}

	public static void main(String[] args) {
		try {
			new LegendsDatabase();
			getRates("https://dblegends.net/banner/2026300");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
