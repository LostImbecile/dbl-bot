package com.github.egubot.interfaces;

import java.util.List;

/**
 * An interface for news scrapers.
 * @param <T> The type of news article this scraper handles.
 */
public interface NewsScraper<T> {

    /**
     * Scrapes news articles from a source.
     *
     * @param pagesToScrape The number of pages to scrape for news articles.
     * @return A list of scraped news articles.
     */
    List<T> scrapeNews(int pagesToScrape);

    /**
     * Retrieves the latest news article without scraping the entire feed.
     * This method can be used for quick checks to see if there are any updates.
     *
     * @return The latest news article, or null if unable to retrieve.
     */
    T getLatestArticle();

    /**
     * Checks if the scraper is currently able to access the news source.
     *
     * @return true if the news source is accessible, false otherwise.
     */
    boolean isSourceAccessible();

    /**
     * Gets the name or identifier of the news source.
     *
     * @return A string representing the name or identifier of the news source.
     */
    String getSourceName();
}