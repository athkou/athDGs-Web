/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.ejb.DigitalGameStoreBeanRemote;
import gr.kourtzis.dgs.ejb.InventoryAdministrationBeanRemote;
import gr.kourtzis.dgs.entity.Category;
import gr.kourtzis.dgs.entity.Game;
import gr.kourtzis.dgs.entity.Inventory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Athanasios Kourtzis
 */

@Getter
@Setter

@Named
@ApplicationScoped
public class GamesCatalogCtrl {
    private final static int INVENTORY_IS_EMPTY = 0;
    
    private List<Category> categories;
    private List<Inventory> inventories;
    
    private Context context;
    
    public GamesCatalogCtrl() {
    }
    
    @PostConstruct
    public void init() {
        initContext();
        
        categories = new ArrayList<>();
        populateCategoryList();
        
        inventories = new ArrayList<>();
        populateInventoryList();
    }
    
    /**
     * The method navigates to the user index.
     * @return A String variable of the destination
     */
    public String back() {
        return "goUserIndex";
    }
    
    /**
     * The method navigates to the games catalog.
     * @return A String variable of the destination.
     */
    public String goBrowseGames() {
        return "goGamesCatalog";
    }
    
    /**
     * The method navigates to the page of the selected
     * game in the web store.
     * @return A String variable of the destination.
     */
    public String goToSelectedGame() {
        return "goToSelectedGame";
    }
    
    /**
     * The Method searches the category list to find
     * a category with a specific id.
     * @param categoryId The id of the category we are searching for
     * @return The Category objct or null if nothing was found.
     */
    public Category findCategory(int categoryId) {
        for(Category currentCategory : categories) {
            if(currentCategory.getCategoryId() == categoryId)
                return currentCategory;
        }

        return null;
    }
    
    /**
     * The method update the category and inventory lists.
     * 
     */
    public void updateCatalog() {
        populateCategoryList();
        populateInventoryList();
        
        System.out.println("Catalog updated.");
    }
    
    /**
     * The method searches the inventory list of the controller
     * and looks to find an inventory for a specified gameId.
     * @param gameId An integer, the gameId which is passed 
     *               as a parameter.
     * @return An inventory object if it was found, otherwise null.
     */
    public Inventory findInventory(int gameId) {
        for(Inventory inventory : inventories) {
            if(inventory.getInventoryId() == gameId)
                return inventory;
        }
        
        return null;
    }
    
    /**
     * The method searches the database for a game with a specified id. 
     * @param gameId An integer value. The gameId of the game we are looking for.
     * @return A game object if it was found otherwise null.
     */
    public Game findGame(int gameId) {
        List<Game> games = lookupDigitalGameStoreBeanRemote().getAllGames();
        for(Game game : games) {
            if(game.getGameId() == gameId) {
                return game;
            }
        }
        
        return null;
    }
    
    /**
     * The method checks if there are available keys for the inventory
     * passed as a parameter.
     * @param inventory The inventory we check for key availability.
     * @return True if there zero keys available, otherwise false.
     */
    public boolean isInventoryEmpty(final Inventory inventory) {
        if(inventory == null)
            return true;
        
        return inventory.getQuantity() == INVENTORY_IS_EMPTY;
    }
    
    /**
     * The method updates the inventory for a purchased game.
     * @param game the game Object.
     */
    public void updateInventory(final Game game) {
        Inventory inventory = findInventory(game.getGameId());
        if(inventory != null) {
            int amount = inventory.getQuantity();
            inventory.setQuantity(amount - 1);
            lookupInventoryAdministrationBeanRemote().update(inventory);
        }
    }
    
    /**
     * The method populates the category list with all the entries from 
     * the database.
     */
    public void populateCategoryList() {
        categories = lookupDigitalGameStoreBeanRemote().getCategories();
    }
    
    /**
     * The method populates the inventory list with all the entries from the 
     * database.
     */
    public void populateInventoryList() {
        inventories = lookupInventoryAdministrationBeanRemote().readEntries();
    }
    
    private void initContext() {
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
        props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
        props.setProperty(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
        
        try {
            context = new InitialContext(props);
            
        } catch (NamingException ex) {
            Logger.getLogger(GamesCatalogCtrl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    private Object lookupObjectBeanRemote(final String lookupName) {
        Object object = null;
        try {
            object = context.lookup(lookupName);
        }
        catch(NamingException ex) {
            Logger.getLogger(GamesCatalogCtrl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        
        return object;
    }
    
    private DigitalGameStoreBeanRemote lookupDigitalGameStoreBeanRemote() {
        return (DigitalGameStoreBeanRemote) lookupObjectBeanRemote("ejb/athDigitalGameStore");
    }
    
    private InventoryAdministrationBeanRemote lookupInventoryAdministrationBeanRemote() {
        return (InventoryAdministrationBeanRemote) lookupObjectBeanRemote("ejb/inventoryAdministration");
    }
}
