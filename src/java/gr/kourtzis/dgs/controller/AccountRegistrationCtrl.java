/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.entity.Activation;
import gr.kourtzis.dgs.entity.User;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
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
public class AccountRegistrationCtrl {
    @Inject
    private ActivationCtrl activationCtrl;
    
    private Activation activation;
    private User user;
    private String activationToken;
    private boolean accountActivated;

    public AccountRegistrationCtrl() {
    }
    
    @PostConstruct
    public void init() {
        activation = new Activation();
        user = new User();
        accountActivated = false;
        
        activationToken = getToken();
    }
    
    public void sendNewToken() {
        System.out.println("To be implemented");
    }
    
    /**
     * The method checks if the account is verified.
     * @return True if the account is verified, otherwise false.
     */
    public boolean isAccountVerified() {
        if(isAccountRegistered()) {
            if(accountActivated) {
                accountActivated = false;
                return true;
            }
            
            System.out.println("User id: " + user.getUserId());
            System.out.println("Activation id: " + activation.getActivationId());
            
            Date now = new Date();
            Date expirationDate = activation.getExpirationDate();
            
            System.out.println("Date now: " + now);
            System.out.println("Expiration date: " + expirationDate);
            
            if(!isTokenExpired(now, expirationDate)) {
                activation.setUserActivated(true);
                activationCtrl.setActivation(activation);
                activationCtrl.save();
                
                System.out.println("User activated");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * The method checks if the account is registered.
     * @return True if the account is registered, otherwise false.
     */
    public boolean isAccountRegistered() {
        if(activationCtrl.findActivation(activationToken)) {
            activation = activationCtrl.getActivation();
            
            if(!activation.isUserActivated()) {
                user = activationCtrl.getUser(activation);
                return user != null;
            }
            else {
                accountActivated = true;
                return accountActivated;
            }
        }
        
        return false;
    }
    
    /**
     * The method checks if the user token
     * is still valid.
     * @return True if the token is valid, otherwise false.
     */
    public boolean isTokenValid() {
        activationToken = getToken();
        if(activationToken == null)
            return false;
        else 
            return activationCtrl.findActivation(activationToken);
    }
    
    /**
     * 
     * @return token or null
     */
    private String getToken() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        
        return request.getParameter("token");
    }
    
    /**
     * The method checks if a token has expired.
     * @param now Is the date when the comparison with 
     *            the token is made.
     * @param expirationDate Is the expiration date of the token.
     * @return True if the token has expired, otherwise false.
     */
    private boolean isTokenExpired(final Date now, final Date expirationDate) {
        if(now == null || expirationDate == null)
            return true;
        
        return now.after(expirationDate);
    }
}
