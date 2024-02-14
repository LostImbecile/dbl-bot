package com.github.egubot.objects;

import com.google.gson.annotations.SerializedName;

public class YoutubeInfo {
	private String kind;
	private String etag;
	private Item[] items;
	private PageInfo pageInfo;

	public String getKind() {
		return kind;
	}

	public String getEtag() {
		return etag;
	}

	public Item[] getItems() {
		return items;
	}

	public PageInfo getPageInfo() {
		return pageInfo;
	}

	public static class Item {
		private String kind;
		private String etag;
		private String id;
		private Snippet snippet;

		public String getKind() {
			return kind;
		}

		public String getEtag() {
			return etag;
		}

		public String getId() {
			return id;
		}

		public Snippet getSnippet() {
			return snippet;
		}
	}

	public static class Snippet {
		private String title;
		private Thumbnails thumbnails;

		public String getTitle() {
			return title;
		}

		public Thumbnails getThumbnails() {
			return thumbnails;
		}
	}

	public static class Thumbnails {
		@SerializedName("default")
		private Thumbnail defaultThumbnail;
		@SerializedName("medium")
		private Thumbnail mediumThumbnail;
		@SerializedName("high")
		private Thumbnail highThumbnail;
		@SerializedName("standard")
		private Thumbnail standardThumbnail;
		@SerializedName("maxres")
		private Thumbnail maxThumbnail;

		public Thumbnail getDefault() {
			return defaultThumbnail;
		}

		public Thumbnail getDefaultThumbnail() {
			return defaultThumbnail;
		}

		public void setDefaultThumbnail(Thumbnail defaultThumbnail) {
			this.defaultThumbnail = defaultThumbnail;
		}

		public Thumbnail getMediumThumbnail() {
			return mediumThumbnail;
		}

		public void setMediumThumbnail(Thumbnail mediumThumbnail) {
			this.mediumThumbnail = mediumThumbnail;
		}

		public Thumbnail getHighThumbnail() {
			return highThumbnail;
		}

		public void setHighThumbnail(Thumbnail highThumbnail) {
			this.highThumbnail = highThumbnail;
		}

		public Thumbnail getStandardThumbnail() {
			return standardThumbnail;
		}

		public void setStandardThumbnail(Thumbnail standardThumbnail) {
			this.standardThumbnail = standardThumbnail;
		}

		public Thumbnail getMaxThumbnail() {
			return maxThumbnail;
		}

		public void setMaxThumbnail(Thumbnail maxThumbnail) {
			this.maxThumbnail = maxThumbnail;
		}
	}

	public static class Thumbnail {
		private String url;

		public String getUrl() {
			return url;
		}
	}

	public static class PageInfo {
		private int totalResults;
		private int resultsPerPage;

		public int getTotalResults() {
			return totalResults;
		}

		public int getResultsPerPage() {
			return resultsPerPage;
		}
	}
}
