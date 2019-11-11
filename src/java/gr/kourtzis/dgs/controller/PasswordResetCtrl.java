/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.ejb.UserAdministrationBeanRemote;
import gr.kourtzis.dgs.entity.User;
import gr.kourtzis.dgs.util.Util;
import java.io.Serializable;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
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
public class PasswordResetCtrl implements Serializable {
    private User user;
    private String password;
    private String confirmPassword;
    private String passwordResetToken;
    
    public PasswordResetCtrl() {
    }
    
    @PostConstruct
    public void init() {
        passwordResetToken = getToken();
        findUser();
        System.out.println(user);
    }
    
    public String reset() {
        if(user!= null) {
            System.out.println("Reset called");
            user.setPassword(Util.createPassword(password));
//            user.setPasswordResetToken(null);
            lookupAdministrationBeanRemote().update(user);
        
            System.out.println("Password changed");
            System.out.println("New password: " + Util.createPassword(password));
        }
                
        return "goToLogInPage";
    }
    
    public boolean isUserInDatabase() {
        return user != null;
    }
    
    public boolean isTokenValid() {
        if(passwordResetToken == null)
            return false;
        else
            return !passwordResetToken.isEmpty() && passwordResetToken != null;
    }
    
    public boolean isUserWithResetToken() {
        return user != null;// && 
//               user.getPasswordResetToken() != null &&
//               !user.getPasswordResetToken().isEmpty();
    }
    
    private void findUser() {
        if(passwordResetToken != null) {
            int id = 0;
            user = lookupAdministrationBeanRemote().readUser(id);
        }
    }
    
    private String getToken() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        
        return request.getParameter("token");
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
