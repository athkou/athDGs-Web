/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.ejb.ActivationAdministrationBeanRemote;
import gr.kourtzis.dgs.ejb.AddressAdministrationBeanRemote;
import gr.kourtzis.dgs.ejb.CustomerAdministrationBeanRemote;
import gr.kourtzis.dgs.ejb.UserAdministrationBeanRemote;
import gr.kourtzis.dgs.entity.Account;
import gr.kourtzis.dgs.entity.Activation;
import gr.kourtzis.dgs.entity.Address;
import gr.kourtzis.dgs.entity.Customer;
import gr.kourtzis.dgs.entity.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.enterprise.context.RequestScoped;
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
@RequestScoped
public class OrphanAccountCtrl implements Serializable {
    private List<Account> orphanAccounts;
    private List<Integer> orphanAccountIds;
    private Date today;
    private Context context;
    
    public OrphanAccountCtrl() {
    }
    
    @PostConstruct
    public void init() {
        initContext();
        
        orphanAccounts = new ArrayList<>();
        orphanAccountIds = new ArrayList<>();
        today = new Date();
        
        populateOrphanAccounts();
    }
    
    /**
     * The method checks if the orphan accounts list is empty.
     * @return True if the list is empty, otherwise false.
     */
    public boolean isOrphanListEmpty() {
        return orphanAccounts.isEmpty();
    }
    
    /**
     * The method activates the orphan account with an activation
     * id that matches with the id passed as a parameter.
     * @param activationId An integer, the activation id of an account
     * @return An empty String, stays at the same page after activation.
     */
    public String activate(int activationId) {
        System.out.println("OrphanAccountCtrl --> activate() called");
        Account account = getAccount(activationId);
        if(account != null) {
            Activation activation = account.getActivation();
            activation.setUserActivated(true);
            lookupActivationAdministrationBeanRemote().update(activation);
        }
        
        System.out.println("User activated.");
        return "";
    }
    
    /**
     * The method deletes the orphan account with an activation id
     * that matches with the id passed as a parameter.
     * @param activationId An integer, the activation id of the account.
     * @return An empty String, stays at the same page after deletion.
     */
    public String delete(int activationId) {
        System.out.println("OrphanAccountCtrl --> delete() called");
        
        Account account = getAccount(activationId);
        if(account != null) {
            Address address = account.getAddress();
            Customer customer = account.getCustomer();
            Activation activation = account.getActivation();
            User user = account.getUser();
            
            try {
                lookupAddressAdministrationBeanRemote().delete(address);
                lookupCustomerAdministrationBeanRemote().delete(customer);
                lookupActivationAdministrationBeanRemote().delete(activation);
                lookupUserAdministrationBeanRemote().delete(user);
                
            }
            catch(EJBException ex) {
                System.out.println(ex.getMessage());
                System.out.println("Was not able to delete the user.");
            }
        }
        
        System.out.println("User successfully deleted.");
        populateOrphanAccounts();
        
        return "";
    }
    
    private Account getAccount(int activationId) {
        for(Account account : orphanAccounts) {
            if(account.getActivation().getActivationId() == activationId)
                return account;
        }
        
        return null;
    }
    
    private void findOrphanAccountIds(final List<Activation> accounts) {
        accounts.forEach(it -> {
            if(!it.isUserActivated()) {
                Date createdOn = it.getCreatedOn();
                Date orphanDate = Account.calculateExpireDate(createdOn, 10);
                System.out.println("Expiration date: " + createdOn);
                System.out.println("Orphan date: " + orphanDate);
                if(today.after(orphanDate)) {
                    orphanAccountIds.add(it.getActivationId());
                    System.out.println("Orphan account added");
                }
            }
        });
    }
    
    private void loadOrphanAccounts() {
        orphanAccountIds.forEach(it -> {
            User user = lookupUserAdministrationBeanRemote().readUser(it);
            if(user != null) {
                System.out.println("User with id: " + it + " found.");
                Customer customer = lookupCustomerAdministrationBeanRemote().readEntry(user);
                Address address = lookupAddressAdministrationBeanRemote().readEntry(user);
                Activation activation = lookupActivationAdministrationBeanRemote().readEntryByUser(user);
                
                if(customer != null && address != null && activation != null) {
                    System.out.println("Full account with user id: " + it + " found");
                    Account account = new Account(user, customer, activation, address, false);
                    account.setOrphanAccountDate(Account.calculateExpireDate(activation.getCreatedOn(), 10));
                    orphanAccounts.add(account);
                }
            }
        });
    }
    
    public void populateOrphanAccounts() {
        if(orphanAccountIds!= null)
            orphanAccountIds.clear();
        if(orphanAccounts != null)
            orphanAccounts.clear();
        
        List<Activation> allAccounts = lookupActivationAdministrationBeanRemote().readEntries();
        findOrphanAccountIds(allAccounts);
        loadOrphanAccounts();
        System.out.println("Orphan accounts list updated");
    }
    
    private AddressAdministrationBeanRemote lookupAddressAdministrationBeanRemote() {
        return (AddressAdministrationBeanRemote) lookupObjectBeanRemote("ejb/addressAdministration");
    } 
    
    private CustomerAdministrationBeanRemote lookupCustomerAdministrationBeanRemote() {
        return (CustomerAdministrationBeanRemote) lookupObjectBeanRemote("ejb/customerAdministration");
    }
    
    private ActivationAdministrationBeanRemote lookupActivationAdministrationBeanRemote() {
        return (ActivationAdministrationBeanRemote) lookupObjectBeanRemote("ejb/activationAdministration");
    }
    
    private UserAdministrationBeanRemote lookupUserAdministrationBeanRemote() {
        return (UserAdministrationBeanRemote) lookupObjectBeanRemote("ejb/userAdministration");
    }
    
    private Object lookupObjectBeanRemote(final String lookupName) {
        Object object = null;
        try {
            object = context.lookup(lookupName);
        }
        catch(NamingException ex) {
            Logger.getLogger(OrphanAccountCtrl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        
        return object;
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
            Logger.getLogger(OrphanAccountCtrl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
}
