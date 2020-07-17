package de.cranix.dao;

import java.io.Serializable;
import java.lang.Long;
import java.lang.String;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: Translation
 *
 */
@Entity
@Table(name="Translations")
@NamedQueries({
	@NamedQuery(name="Translation.findAll",     query="SELECT t FROM Translation t"),
	@NamedQuery(name="Translation.findByLang",  query="SELECT t FROM Translation t WHERE t.lang = :lang"),
	@NamedQuery(name="Translation.find",	    query="SELECT t FROM Translation t WHERE t.lang = :lang AND t.string = :string"),
	@NamedQuery(name="Translation.untranslated",query="SELECT t FROM Translation t WHERE t.lang = :lang AND t.value = ''"),
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Translation implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	Long id;
	private String lang;
	private String string;
	private String value;

	public Translation() {
		super();
	}

	public Translation(String lang, String string) {
		this.lang   = lang;
		this.string = string;
		this.value  = "";
	}

	public Translation(String lang, String string, String value) {
		this.lang   = lang;
		this.string = string;
		this.value  = value;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLang() {
		return this.lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getString() {
		return this.string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
