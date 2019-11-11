/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.util;

import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Athanasios Kourtzis
 */

@FacesValidator(value = "myRecoveryEmailValidator")
public class RecoveryEmailValidator implements Validator {
    private String msg;
    private String regex;

    public RecoveryEmailValidator() {
    }

    /**
     * The method verifies that the value of the obj variable is valid according
     * to the constrain of the regular expression. It also checks that the
     * recovery email address is different from the primary email address.
     * @param facesContext FacesContext for the request we are processing
     * @param components UIComponent we are checking for correctness
     * @param obj The Object variable containing the recovery email address.
     * @throws ValidatorException if validation fails
     */
    @Override
    public void validate(FacesContext facesContext, UIComponent components, Object obj) throws ValidatorException {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", facesContext.getViewRoot().getLocale());
        msg = bundle.getString("errorEmail");
        if(obj == null) 
            errorMessage(msg);
        
        regex = "^[\\w-]+(?:\\.[\\w-]+)*@(?:[\\w-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(obj.toString());
        
        if(!matcher.matches()) 
            errorMessage(msg);
        
        String recoveryEmail = (String) obj;
        
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        String email = (String) session.getAttribute("email");
        
        if(email == null || email.equals(recoveryEmail)) {
            msg = bundle.getString("errorRecoveryEmail");
            errorMessage(msg);
        }
    }
    
    private void errorMessage(final String errorMsg) throws ValidatorException {
        FacesMessage message = new FacesMessage(errorMsg);
        throw new ValidatorException(message);
    }
}
