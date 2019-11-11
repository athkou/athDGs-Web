/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.ejb.ReviewAdministrationBeanRemote;
import gr.kourtzis.dgs.entity.Review;
import gr.kourtzis.dgs.entity.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
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
@ConversationScoped
public class SupportReviewCtrl implements Serializable {
    public final static int REQUEST_PARAMETER_GAME_NOT_FOUND = 0;
    public final static int REQUEST_PARAMETER_GAME_NOT_A_NUMBER = -1;
    
    @Inject
    private AccountCtrl accountCtrl;
    @Inject
    private Conversation conversation;
    
    private List<Review> reportedReviews;
    
    private Review review;
    private User user;
    
    private String reasonForEdit;
    
    public SupportReviewCtrl() {
    }
    
    /**
     * The method cancel the editing of the review and 
     * navigates back to the reported reviews.
     * @return A String variable of the destination.
     */
    public String cancel() {
        return "goToReportedReviews";
    }
    
    /**
     * The method edits and saves the review and navigates 
     * back to the reported review.
     * @return A String variable of the destination.
     */
    public String edit() {
        save();
        if(!conversation.isTransient())
            conversation.end();
        
        return cancel();
    }
    
    /**
     * The method checks if the review object is null.
     * @return True if the review object is null, otherwise false.
     */
    public boolean isReviewNull() {
        return review == null;
    }
    
    /**
     * The method checks if the reportedReview list is empty.
     * @return True if the list is empty, otherwise is false.
     */
    public boolean isReviewListEmpty() {
        if(reportedReviews == null)
            return true;
        else 
            return reportedReviews.isEmpty();
    }
    
    /**
     * The method gets the review with the specified review id and
     * navigates to the selected reported review.
     * @param reviewId An integer, the id for the review we are looking for.
     * @return A String variable of the destination.
     */
    public String retrieveReview(int reviewId) {
        for(Review currentReview : reportedReviews) {
            if(currentReview.getReviewId() == reviewId) {
                review = currentReview;
                user = review.getUser();
                
                return "goToSelectedReportedReview";
            }
        }
        
        review = null;
        return "";
    }
    
    /**
     * The method saves the updated review.
     */
    public void save() {
        updateReview();
        lookupReviewBeanRemote().update(review);
        
        review = null;
        populateList();
    }
    
    /**
     * The method initializes the member variables and loads all the
     * reported reviews in the list.
     */
    @PostConstruct
    public void init() {
        if(conversation.isTransient())
            conversation.begin();
        
        System.out.println("SupportReviewCtrl --> @PostConstruct --> init() called.");
        reportedReviews = new ArrayList<>();
        populateList();
    }
    
    public void populateList() {
        reportedReviews.clear();
        reportedReviews = lookupReviewBeanRemote().readEntries(true);
    }
    
    private void updateReview() {
        review.setLastEdited(new Date());
        review.setLastEditedFrom(accountCtrl.getUserCtrl().getUser().getUserId());
        review.setReviewFlagged(false);
        review.setUserReview(review.getUserReview());
        review.setReasonForEdit("Edited from: " + accountCtrl.getFullName()+ " Reason: " + reasonForEdit);
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
