/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.ejb.ActivationAdministrationBeanRemote;
import gr.kourtzis.dgs.entity.Activation;
import gr.kourtzis.dgs.entity.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
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
@SessionScoped
public class ActivationCtrl implements Serializable {
    private List<Activation> activations;
    private Activation activation;
    
    public ActivationCtrl() {
    }
    
    @PostConstruct() 
    public void init() {
        activations = new ArrayList<>();
        activation = new Activation();
        
        populateList();
        
    }
    
    /**
     * The method searches in the database to find an activation entry 
     * which has the same id as the userId.
     * @param user A user object.
     * @return Returns true if an object was found or false if null was returned.
     */
    public boolean findActivation(final User user) {
        activation = lookupActivationAdministrationBeanRemote().readEntryByUser(user);
        return activation != null;
    }
    
    /**
     * The method searches in the database to find  an activation entry
     * which has the same token as the String given in the parameter.
     * @param token A String object containing the token.
     * @return True if the activation object is found with the specified token, 
     *         false if the activation object returned is null.
     */
    public boolean findActivation(final String token) {
        activation = lookupActivationAdministrationBeanRemote().readEntry(token);
        return activation != null;
    }
    
    /**
     * The method searches the database for a user whose id matches with 
     * the id of the activation object passed as parameter.
     * @param activation An activation object.
     * @return A user object or null.
     */
    public User getUser(final Activation activation) {
        return lookupActivationAdministrationBeanRemote().readUser(activation);
    }
    
    /**
     * Updates and saves the activation object in the database.
     */
    public void save() {
        lookupActivationAdministrationBeanRemote().update(activation);
    }
    
    private ActivationAdministrationBeanRemote lookupActivationAdministrationBeanRemote() {
        ActivationAdministrationBeanRemote actRemote = null;
        
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
        props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
        props.setProperty(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
        
        try {
            Context context = new InitialContext(props);
            actRemote = (ActivationAdministrationBeanRemote) context.lookup("ejb/activationAdministration");
        } catch (NamingException ex) {
            Logger.getLogger(UserCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return actRemote;
    }
    
    private void populateList() {
        activations = lookupActivationAdministrationBeanRemote().readEntries();
    }
}
