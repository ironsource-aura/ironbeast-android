package com.mobilecore.mctester.automation;

import java.io.Serializable;

public class OfferwallItem implements Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = 4002034234493155987L;
	private String name;
	private String description;
	private int displaySize;
	private String resourcesFileUrl;
	private int iconId = -1;

	public OfferwallItem(String name, String description, int displaySize, String feedJson) {
		this.setName(name);
		this.setDescription(description);
		this.setDisplaySize(displaySize);
		this.setResourcesFileUrl(feedJson);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDisplaySize() {
		return displaySize;
	}

	public void setDisplaySize(int displaySize) {
		this.displaySize = displaySize;
	}

	public String getResourcesFileUrl() {
		return resourcesFileUrl;
	}

	public void setResourcesFileUrl(String resourcesJsonUrl) {
		this.resourcesFileUrl = resourcesJsonUrl;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

}
