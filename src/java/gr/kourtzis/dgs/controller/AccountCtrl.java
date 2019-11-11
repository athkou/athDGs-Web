/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.entity.Category;
import gr.kourtzis.dgs.entity.Game;
import gr.kourtzis.dgs.entity.Inventory;
import gr.kourtzis.dgs.entity.LibraryGame;
import gr.kourtzis.dgs.entity.User;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
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
public class AccountCtrl implements Serializable {
    public final static int GAME_NOT_FOUND = 0;
    
    @Inject
    private UserCtrl userCtrl;
    @Inject
    private CustomerCtrl customerCtrl;
    @Inject
    private AddressCtrl addressCtrl;
    @Inject
    private ActivationCtrl activationCtrl;
    @Inject 
    private GamesCatalogCtrl gamesCatalogCtrl;
    @Inject
    private LanguageCtrl languageCtrl;
        
//    private String language;
    private Category category;
    
    private Game currentGame;
    private Inventory currentInventory;
  
    private Integer categoryId;
    private Map<String, Integer> gamesMap;
    
//    private Locale locale;
//    private Map<String, String> locales;
    
    private int gameId;
    
    public AccountCtrl() {
    }
    
    /**
     * The method initializes all injected private member variables. 
     */
    @PostConstruct
    public void init() {
        if(userCtrl.getUser() != null) {
            System.out.println("AccountCtrl --> @PostConstruct init(): called");
            System.out.println("User: " + userCtrl.getUser());

            getAccountInformation(userCtrl.getUser());
        }
        else {
            User user = new User();
            userCtrl.setUser(user);
        }

//        populateLocalesMap();

        gamesMap = new LinkedHashMap<>();
        gamesMap = populateCategoryComboBox();
    }
    
    public void getAccountInformation(final User user) {
        if(user != null) {
            if(customerCtrl.findCustomer(user))
                System.out.println("Customer found");
            if(addressCtrl.findAddress(user))
                System.out.println("Address found");
            if(activationCtrl.findActivation(user))
                System.out.println("Activation found");
        }
    }
    
    /**
     * The method saves the data in the entity classes and then 
     * the object are being saved in the database.
     */
    public void save() {
        userCtrl.save();
        activationCtrl.save();
        customerCtrl.save();
        addressCtrl.save();
        
        languageCtrl.switchLanguage();
        
        System.out.println("Updates saved");
    }
    
    /**
     * The method navigates back to user index.
     * @return A String variable of the destination.
     */
    public String back() {
        return "goUserIndex";
    } 
    
    /**
     * The method navigates to the user profile
     * @return A String variable of the destination.
     */
    public String goProfile() {
        return "goUserProfile";
    }
    
    /**
     * The method navigates to the library of games.
     * @return A String variable of the destination.
     */
    public String goLibrary() {
        return "goToLibrary";
    }
    
    /**
     * The method returns true if a game is selected or false
     * if a game is not selected.
     * @return true of false.
     */
    public boolean isGameSelected() {
        if(gameId != GAME_NOT_FOUND) {
            currentGame = gamesCatalogCtrl.findGame(gameId);
        } 

        return (currentGame != null && currentGame.getGameId() != 0);
    }
    
    /**
     * The method checks if a game from the library was selected.
     * @return True if a game from the library is selected otherwise false.
     */
    public boolean isLibraryGameSelected() {
        if(gameId != GAME_NOT_FOUND) {
           for(LibraryGame libraryGame : userCtrl.getUser().getLibraryGames()) {
               if(libraryGame.getGame().getGameId() == gameId)
                   return true;
           }
        }
            
        return false;
    }
    
    /**
     * The method picks a game from the list and moves to
     * the page from the game.
     * @param gameId An integer, is the id from the game.
     * @param categoryId An integer, is the id from the 
     *                   category of the game.
     * @return A String variable of the destination.
     */
    public String selectedGame(int gameId, int categoryId) {
        this.gameId = gameId;
        this.categoryId = categoryId;
        
        return "goToSelectedGame";
    }
    
    /**
     * The method navigates to the review page for the
     * game that was clicked in the library.
     * @param gameId An integer, the id of the game.
     * @return A String variable of the destination.
     */
    public String writeReview(int gameId) {
        this.gameId = gameId;
        
        return "goToWriteReview";
    }
    
    /**
     * The method checks if a user already owns the game in his library
     * @return True if the user has the game otherwise false.
     */
    public boolean isGameInLibrary() {
        Set<LibraryGame> gamesInLibrary = userCtrl.getUser().getLibraryGames();
        for(LibraryGame libraryGame : gamesInLibrary) {
            if(currentGame.getGameId() == libraryGame.getGame().getGameId())
                return true;
        }
        
        return false;
    }
    
    /**
     * The method checks if the user has any games 
     * in his library.
     * @return True if the library is empty otherwise false.
     */
    public boolean isLibraryEmpty() {
        return userCtrl.getUser().getLibraryGames().isEmpty();
    }
    
    /**
     * The method searches if an inventory exist for a game.
     * @return true if an inventory exists otherwise false.
     */
    public boolean isGameWithInventory() {
        currentInventory = gamesCatalogCtrl.findInventory(currentGame.getGameId());
        if(currentInventory != null)
            return currentGame.getGameId() == currentInventory.getInventoryId();
        else
            return false;
    }
    
    /**
     * The method returns the full name of a customer.
     * @return A String variable containing the name.
     */
    public String getFullName() {
        return customerCtrl.getCustomer().getFirstName() + " " + customerCtrl.getCustomer().getLastName();
    }
    
//    /**
//     * The method changes the locale of the web application.
//     */
//    public void switchLanguage() {
//        locale = new Locale(language);
//        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
//        System.out.println(locale);
//        System.out.println(language);
//        System.out.println(Locale.getDefault());
//    }
    
    /**
     * The method searches and assigns a inventory with a specified game id.
     */
    public void findInventory() {
        currentInventory = gamesCatalogCtrl.findInventory(currentGame.getGameId());
    }
    
    /**
     * The method searches and assigns a category with a specified category id
     */
    public void findCategory() {
        category = gamesCatalogCtrl.findCategory(categoryId);
    }
    
    /*
    The method populates the combobox with categories
    */
    private Map<String, Integer> populateCategoryComboBox() {
        Map<String, Integer> temp = new LinkedHashMap<>();
        
        temp.put("-- select one --", 0);
        gamesCatalogCtrl.getCategories().forEach(it -> {
            temp.put(it.getGenre(), it.getCategoryId());
        });
        
        return temp;
    }
    
//    private void populateLocalesMap() {
//        locales = new LinkedHashMap<>();
//        locale = new Locale(Locale.getDefault().getDisplayLanguage());
//        
//        locales.put("English", "en");
//        locales.put("Deutsch", "de");
//    }
}
