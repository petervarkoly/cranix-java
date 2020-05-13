package de.cranix.dao;

public class CrxBaseObject {

	Long id;
	String name;
	
	public CrxBaseObject() {
		// TODO Auto-generated constructor stub
	}

	public CrxBaseObject(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
