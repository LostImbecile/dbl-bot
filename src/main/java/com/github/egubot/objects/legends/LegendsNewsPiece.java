package com.github.egubot.objects.legends;


public class LegendsNewsPiece {
	private long id;
	private String title;
	private String url;
	private String bannerUrl;
	private String startTime;
	private String endTime;

	public LegendsNewsPiece() {
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}


	public void setId(long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUrl(String url) {
		this.url = "https://dblegends.net" + url;
	}

	public String getBannerUrl() {
		return bannerUrl;
	}

	public void setBannerUrl(String bannerUrl) {
		this.bannerUrl = bannerUrl;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LegendsNewsPiece o)
			return getId() == o.getId();
		return false;
	}
	
	@Override
	public String toString() {
		return "NewsPiece [\nid=" + id + "\ntitle=" + title + "\nurl=" + url + "\nbannerUrl=" + bannerUrl
				+ "\nstartTime=" + startTime + "\nendTime=" + endTime + "\n]";
	}

}
