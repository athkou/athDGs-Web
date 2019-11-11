/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.ejb.ReviewAdministrationBeanRemote;
import gr.kourtzis.dgs.entity.Game;
import gr.kourtzis.dgs.entity.LibraryGame;
import gr.kourtzis.dgs.entity.Review;
import gr.kourtzis.dgs.entity.User;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
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
@ViewScoped
public class UserReviewCtrl implements Serializable {
    @Inject
    private AccountCtrl accountCtrl;
    
    private String reviewTitle;
    private String reviewBody;
    private Review review;
    
    private User user;
    private Game game;
    
    private boolean reviewExist;
    
    public UserReviewCtrl() {
    }
    
    /**
     * The method navigates to the library.
     * @return A String variable of the destination.
     */
    public String library() {
        save();
        return "goToLibrary";
    }
    
    /**
     * The method navigates to the library.
     * @return A String variable of the destination.
     */
    public String cancel() {
        return "goToLibrary";
    }
    
    /**
     * The method initializes the member variables with initial values.
     */
    @PostConstruct
    public void init() {
        reviewExist = false;
        
        review = new Review();
        game = new Game();
        
        user = accountCtrl.getUserCtrl().getUser();
        game = getGameFromUser();
        
        if (game != null) {
            for (Review rev : game.getReviews()) {
                if (rev.getUser().getUserId() == user.getUserId()) {
                    review = rev;
                    reviewTitle = review.getTitle();
                    reviewBody = review.getUserReview();
                    reviewExist = true;

                    break;
                }
            }
        }
        
    }
    
    /**
     * The method checks if the review list of the game object is empty.
     * @return True if the list is empty, otherwise false.
     */
    public boolean isEmptyReview() {
        return game.getReviews().isEmpty();
    }
    
    /**
     *  The method checks if the game member variable is null.
     * @return True if game is null otherwise false.
     */
    public boolean isGameNull() {
        game = getGameFromUser();
        return game == null;
    }
    
    /**
     * The method reports a review with the specified if in the parameter.
     * @param reviewId The id of the review to be reported.
     * @return A String variable of the destination.
     */
    public String report(int reviewId) {
        System.out.println("Review id: " + reviewId);
        Review reviewToFlag = lookupReviewBeanRemote().readEntry(reviewId);
        reviewToFlag.setReviewFlagged(true);
        System.out.println("Review: " + reviewToFlag.getUserReview());
        lookupReviewBeanRemote().update(reviewToFlag);
        
        return "";
    }
    
    /*
    Saves the review and saves the User object in the database.
    */
    private void save() {
        prepareReview();
        
        review.addGame(game);
        user.addReview(review);
        
        accountCtrl.getUserCtrl().setUser(user);
        accountCtrl.getUserCtrl().save();

        accountCtrl.getGamesCatalogCtrl().populateCategoryList();
    }
    
    /*
    Assigns values to the review object before saving it.
    */
    private void prepareReview() {
        Date now = new Date();
        
        review.setTitle(reviewTitle);
        review.setUserReview(reviewBody);
        if(!reviewExist)
            review.setCreatedOn(now);
        review.setReviewFlagged(false);
        review.setLastEdited(now);
        review.setLastEditedFrom(user.getUserId());
    }
    
    /*
    The method returns a game object which has the id equal
    to the id from request parameter.
    */
    private Game getGameFromUser() {
//        int gameId = accountCtrl.getIdFromParam("libGame");
        int gameId = accountCtrl.getGameId();
        
        for(LibraryGame libraryGame : user.getLibraryGames()) {
            if(libraryGame.getGame().getGameId() == gameId)
                return libraryGame.getGame();
        }
        
        return null;
    }
    
    private ReviewAdministrationBeanRemote lookupReviewBeanRemote() {
        ReviewAdministrationBeanRemote reviewRemote = null;
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
        props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
        props.setProperty(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
        
        try {
            Context context = new InitialContext(props);
            reviewRemote = (ReviewAdministrationBeanRemote) context.lookup("ejb/reviewAdministration");
        } catch (NamingException ex) {
            Logger.getLogger(SupportReviewCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return reviewRemote;
    }
}
