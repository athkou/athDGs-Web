/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Athanasios Kourtzis
 */

@Getter
@Setter

@Named
@SessionScoped
public class LanguageCtrl implements Serializable {
    private String language;
    private Locale locale;
    private Map<String, String> locales;
    
    public LanguageCtrl() {
    }
    
    @PostConstruct
    public void init() {
        populateLocalesMap();
    }
    
    /**
     * The method changes the locale of the web application.
     */
    public void switchLanguage() {
        locale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        System.out.println(locale);
        System.out.println(language);
        System.out.println(Locale.getDefault());
    }
    
    private void populateLocalesMap() {
        locales = new LinkedHashMap<>();
        locale = new Locale(Locale.getDefault().getDisplayLanguage());
        
        locales.put("English", "en");
        locales.put("Deutsch", "de");
    }
}
