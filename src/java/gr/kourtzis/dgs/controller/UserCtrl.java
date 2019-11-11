/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.ejb.UserAdministrationBeanRemote;
import gr.kourtzis.dgs.entity.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
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
public class UserCtrl implements Serializable {
    private List<User> users;
    private User user;
        
    public UserCtrl() {
    }
    
    /**
     * The method initializes the member variables and looks in the database
     * for a user with the email that was given as a username during the log in.
     */
    @PostConstruct
    public void init() {
        users = new ArrayList<>();
        user = new User();
        
        populateList();

        
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        String email = (String) session.getAttribute("email");
        
        findUser(email);
    }
    
    /**
     * The method saves the user to the database.
     */
    public void save() {
        lookupAdministrationBeanRemote().update(user);
    }
    
    /**
     * The method searches for a user in the database with an email that
     * is equal with the String variable passed as a parameter.
     * @param email A String variable containing the email searching for.
     * @return True if a user was found or false if null was returned.
     */
    public boolean findUser(final String email) {
        user = lookupAdministrationBeanRemote().readUser(email);
        return user != null;
    }
    
    /**
     * The method returns true if the user has games saved in the library
     * @return True if the collection libraryGames contains games 
     *         otherwise false.
     */
    public boolean isUserWithGames() {
        return !user.getLibraryGames().isEmpty();
    }
    
    private void populateList() {
        users = lookupAdministrationBeanRemote().readAllUsers();
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
