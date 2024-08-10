package com.github.egubot.managers;

import com.github.egubot.features.legends.LegendsNewsScraper;
import com.github.egubot.interfaces.NewsScraper;
import com.github.egubot.objects.legends.LegendsNewsPiece;
import com.github.egubot.shared.utils.JSONUtilities;
import java.util.ArrayList;
import java.util.List;

public class NewsFeedManager<T> {
    private static final int MAX_ARTICLES = 100;
    private final String cacheFile;
    private List<T> cachedArticles;
    private final NewsScraper<T> scraper;
    private final Class<T[]> articleArrayClass;

    public NewsFeedManager(NewsScraper<T> scraper, Class<T[]> articleArrayClass, String cacheFile) {
        this.scraper = scraper;
        this.articleArrayClass = articleArrayClass;
        this.cacheFile = cacheFile;
        loadCache();
    }

    public List<T> getNewArticles(int pagesToScrape) {
        List<T> newArticles = new ArrayList<>();
        List<T> scrapedArticles = scraper.scrapeNews(pagesToScrape);

        for (T article : scrapedArticles) {
            if (article != null && !cachedArticles.contains(article)) {
                newArticles.add(article);
                cachedArticles.add(article);
            }
        }

        // Trim cache to MAX_ARTICLES
        if (cachedArticles.size() > MAX_ARTICLES) {
            cachedArticles = cachedArticles.subList(0, MAX_ARTICLES);
        }

        saveCache();
        return newArticles;
    }

    public T getlatestArticle() {
		return scraper.getLatestArticle();
    	
    }
    private void saveCache() {
        JSONUtilities.writeListToJson(cachedArticles, cacheFile);
    }

    private void loadCache() {
        cachedArticles = JSONUtilities.readListFromJson(cacheFile, articleArrayClass);
        if (cachedArticles == null) {
            cachedArticles = new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        NewsScraper<LegendsNewsPiece> scraper = new LegendsNewsScraper();
        NewsFeedManager<LegendsNewsPiece> manager = new NewsFeedManager<>(scraper, LegendsNewsPiece[].class, "legends_news_cache.json");
        List<LegendsNewsPiece> list = manager.getNewArticles(3);
        for (LegendsNewsPiece piece : list) {
           System.out.println(piece);
        }
    }
}