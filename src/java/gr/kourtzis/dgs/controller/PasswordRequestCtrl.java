/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.ejb.UserAdministrationBeanRemote;
import gr.kourtzis.dgs.entity.User;
import java.io.Serializable;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
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
public class PasswordRequestCtrl implements Serializable {
    private User user;
    private String email;
        
    public PasswordRequestCtrl() {
    }
    
    @PostConstruct
    public void init() {
    }
    
    public String requestResetPassword() {
        findUser(email);
        if(isUserInDatabase()) {
//            user.setPasswordResetToken(UUID.randomUUID().toString());
            lookupAdministrationBeanRemote().update(user);
            System.out.println("Updated user table in database.\nPassword reset token was set");
        }
        
        return "";
    }
    
    public boolean isUserInDatabase() {
        return user != null;
    }
    
    private void findUser(final String email) {
        user = lookupAdministrationBeanRemote().readUser(email);
    }
    
    private UserAdministrationBeanRemote lookupAdministrationBeanRemote() {
        UserAdministrationBeanRemote userRemote = null;
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
        props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
        props.setProperty(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
        
        try {
            Context context = new InitialContext(props);
            userRemote = (UserAdministrationBeanRemote) context.lookup("ejb/userAdministration");
        } catch (NamingException ex) {
            Logger.getLogger(UserCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return userRemote;
    }
}
