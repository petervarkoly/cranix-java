package de.cranix.dao;

public class ProxyRule {


	private String   name;
	private boolean  enabled;
	private String   description;
	private String   longDescription;

	public ProxyRule() {
	}

	public ProxyRule(String name, boolean enabled) {
		this.name    = name;
		this.enabled = enabled;
	}

	public ProxyRule(String name, boolean enabled, String description, String longDescription) {
		this.name    = name;
		this.enabled = enabled;
		this.setDescription(description);
		this.setLongDescription(longDescription);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}
}
