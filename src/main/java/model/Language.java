package model;

import java.util.Date;

public class Language {
    private Integer id;

    private String code;

    private String displayName;

    private Integer platform;

    private String language;

    private String langDisplay;

    private String country;

    private String countryDisplay;

    private Date updateTime;

    public Language(Integer id, String code, String displayName, Integer platform, String language, String langDisplay, String country, String countryDisplay, Date updateTime) {
        this.id = id;
        this.code = code;
        this.displayName = displayName;
        this.platform = platform;
        this.language = language;
        this.langDisplay = langDisplay;
        this.country = country;
        this.countryDisplay = countryDisplay;
        this.updateTime = updateTime;
    }

    public Language() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? null : displayName.trim();
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language == null ? null : language.trim();
    }

    public String getLangDisplay() {
        return langDisplay;
    }

    public void setLangDisplay(String langDisplay) {
        this.langDisplay = langDisplay == null ? null : langDisplay.trim();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

    public String getCountryDisplay() {
        return countryDisplay;
    }

    public void setCountryDisplay(String countryDisplay) {
        this.countryDisplay = countryDisplay == null ? null : countryDisplay.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}