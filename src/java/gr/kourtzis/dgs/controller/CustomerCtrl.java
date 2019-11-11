/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.ejb.CustomerAdministrationBeanRemote;
import gr.kourtzis.dgs.entity.Customer;
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
public class CustomerCtrl implements Serializable {
    private List<Customer> customers;
    private Customer customer;
    
    public CustomerCtrl() {
    }
    
    @PostConstruct
    public void init() {
        customers = new ArrayList<>();
        customer = new Customer();
        
        populateList();
    }
    
    /**
     * The method searches in the database to find an customer entry 
     * which has the same id as the userId from the user object. 
     * @param user A User object.
     * @return Returns true if an object was found or false if null was returned.
     */
    public boolean findCustomer(final User user) {
        customer = lookupAdministrationBeanRemote().readEntry(user);
        return customer != null;
    }
    
    /**
     * Updates and saves the address object in the database.
     */
    public void save() {
        lookupAdministrationBeanRemote().update(customer);
    }
    
    private CustomerAdministrationBeanRemote lookupAdministrationBeanRemote() {
        CustomerAdministrationBeanRemote custRemote = null; 
        
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
        props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
        props.setProperty(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
        
        try {
            Context context = new InitialContext(props);
            custRemote = (CustomerAdministrationBeanRemote) context.lookup("ejb/customerAdministration");
        } catch (NamingException ex) {
            Logger.getLogger(UserCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return custRemote;
    }
    
    private void populateList() {
        customers = lookupAdministrationBeanRemote().readEntries();
    }
}
