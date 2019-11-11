/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.entity.User;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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
public class LoginCtrl implements Serializable {
    @Inject
    private AccountCtrl accountCtrl;
   
    private FacesContext context;
    private HttpServletRequest request;
    
    private String username;
    private String password;
    
    public LoginCtrl() {
    }
    
    /**
     * The method allows a user to log in to the web shop. After checking
     * whether a account is verified the user is navigated to the index 
     * page according his role.
     * @return A String variable of the destination.
     */
    public String login() {
        context = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        try {
            request.login(username, password);
            
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            session.setAttribute("email", username);
            accountCtrl.getUserCtrl().getUser().setEmail(username);
        }
        catch(ServletException ex) {
            System.out.println("Exception occured: " + ex.getMessage());
            return "goErrorPage"; 
        }
        
        if(isAccountVerified()) {
            System.out.println("User is activated");
            if(request.isUserInRole("user")) {
                return "goUserIndex";
            } 
            else if(request.isUserInRole("support")) {
                return "goSupportIndex";
            }
            else if(request.isUserInRole("admin")) {
                return "goAdminIndex";
            }

            logout();
            return "goErrorPage";
        }
        else {
            System.out.println("User is not activated");
            System.out.println("Calling logout function...");
            logout();
            return "goErrorAccountNotVerified";
        }
    }
    
    /**
     * The method logs out from the web app.
     * @return A String variable of the destination.
     */
    public String logout() {
        context = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        try {
            request.logout();
            context.getExternalContext().invalidateSession();
        }
        catch(ServletException ex) {
            System.out.println("Logout failed");
            System.out.println(ex.getMessage());
            
            return "";
        }
        
        return "goPublicIndex";
    }
    
    /**
     * The method checks if the logged in user has the role 'user'
     * @return true or false.
     */
    public boolean isUser() {
        context = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        return request.isUserInRole("user");
    }
    
     /**
     * The method checks if the logged in user has the role 'admin'
     * @return true or false.
     */
    public boolean isAdmin() {
        context = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        return request.isUserInRole("admin");
    }
    
     /**
     * The method checks if the logged in user has the role 'support'
     * @return true or false.
     */
    public boolean isSupport() {
        context = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        return request.isUserInRole("support"); 
    }
    
    /**
     * The method checks if the logged in user has verified the account
     * @return true or false.
     */
    public boolean isAccountVerified() {
        if(accountCtrl.getUserCtrl().getUser().getUserId() == 0 && !accountCtrl.getUserCtrl().getUser().getEmail().isEmpty()) {
            for (User user : accountCtrl.getUserCtrl().getUsers()) {
                if(user.getEmail().equalsIgnoreCase(accountCtrl.getUserCtrl().getUser().getEmail())) {
                    accountCtrl.getUserCtrl().setUser(user);
                    accountCtrl.getAccountInformation(accountCtrl.getUserCtrl().getUser());
                    break;
                }
            }
        }
        
        if(!accountCtrl.getActivationCtrl().findActivation(accountCtrl.getUserCtrl().getUser()))    
            return false;
        
        return accountCtrl.getActivationCtrl().getActivation().isUserActivated();
    }
    
    /**
     * The method checks if the user has logged in. If he has, the web app
     * redirects to the users home index.
     * @return A String variable of the destination.
     */
    public String onIndex() {
        context = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        if(request.getUserPrincipal() != null) {
            return navigateToRestrictedPage(request);
        }
        
        return "";
    }
    
    /**
     * The method checks if the user has the appropriate "user" 
     * permission to access the user-restricted pages.
     * @return The page that was typed in the browser if the user
     *         has the permissions otherwise the user is redirected
     *         to the public index.
     */
    public String onUserIndex() {
        context = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        if(request.getUserPrincipal() != null) {
            if(request.isUserInRole("user") && isAccountVerified())
                return "";
            else 
                return "goPublicIndex";
        }
        
        return "goPublicIndex";
    }
    
    /**
     * The method checks if the user has the appropriate "support"
     * permission to access the support-restricted pages.
     * @return The page that was typed in the browser if the user has
     *         the permission otherwise the user is redirected to the
     *         public index.
     */
    public String onSupportIndex() {
        context = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        if(request.getUserPrincipal() != null) {
            if(request.isUserInRole("support") && isAccountVerified())
                return "";
            else 
                return "goPublicIndex";
        }
        
        return "goPublicIndex";
    }
    
    /**
     * The method checks if the user has the appropriate "admin" permission
     * to access the admin-restricted pages.
     * @return The page that was typed in the browser if the user has the
     *         permission otherwise the user is redirected to the public index.
     */
    public String onAdminIndex() {
        context = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        if(request.getUserPrincipal() != null) {
            if(request.isUserInRole("admin") && isAccountVerified())
                return "";
            else 
                return "goPublicIndex";
        }
        
        return "goPublicIndex";
    }
    
    private String navigateToRestrictedPage(HttpServletRequest request) {
        if(request.isUserInRole("user") && isAccountVerified())
            return "goUserIndex";
        else if(request.isUserInRole("support") && isAccountVerified())
            return "goSupportIndex";
        else if(request.isUserInRole("admin") && isAccountVerified())
            return "goAdminIndex";
        else 
            return "";
    }
}
