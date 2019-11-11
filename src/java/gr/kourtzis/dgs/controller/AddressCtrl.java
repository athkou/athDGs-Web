/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.ejb.AddressAdministrationBeanRemote;
import gr.kourtzis.dgs.entity.Address;
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
public class AddressCtrl implements Serializable {
    private List<Address> addresses;
    private Address address;
    
    public AddressCtrl() {
    }
    
    @PostConstruct
    public void init() {
        addresses = new ArrayList<>();
        address = new Address();
        
        populateList();
    }
    
    /**
     * The method searches in the database to find an address entry 
     * which has the same id as the userId from the user object.
     * @param user A user object.
     * @return Returns true if an object was found or false if null was returned.
     */
    public boolean findAddress(final User user) {
        address = lookupAddressAdministrationBeanRemote().readEntry(user);
        return address != null;
    }
    
    /**
     * Updates and saves the address object in the database.
     */
    public void save() {
        lookupAddressAdministrationBeanRemote().update(address);
    }
    
    private AddressAdministrationBeanRemote lookupAddressAdministrationBeanRemote() {
        AddressAdministrationBeanRemote addrRemote = null;
        
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
        props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
        props.setProperty(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
        
        try {
            Context context = new InitialContext(props);
            addrRemote = (AddressAdministrationBeanRemote) context.lookup("ejb/addressAdministration");
        } catch (NamingException ex) {
            Logger.getLogger(UserCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return addrRemote;
    }
    
    private void populateList() {
        addresses = lookupAddressAdministrationBeanRemote().readEntries();
    }
}
