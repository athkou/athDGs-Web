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

/**
 *
 * @author akourtzis
 */

@FacesValidator(value = "myPasswordValidator")
public class PasswordValidator implements Validator {
    private static final int MINIMUM_PASSWORD_LENGTH = 4;
    private String msg;
    
    boolean isUpperCase;
    boolean isLowerCase;
    boolean isDigit;
    boolean isSpecialChar;
    
    public PasswordValidator() {
        isDigit = isLowerCase = isSpecialChar = isUpperCase = false;
    }

    /**
     * The method checks whether the passeword has a specified length and
     * contains special characters, digits upper- and lowercase letters.
     * @param facesContext FacesContext for the request we are processing.
     * @param components UIComponent we are checking for correctness.
     * @param obj The Object variable containing the password.
     * @throws ValidatorException if validation fails.
     */
    @Override
    public void validate(FacesContext facesContext, UIComponent components, Object obj) throws ValidatorException {
        if(obj == null) 
            errorMessage(facesContext, "errorNullObject");
        if(obj.toString().length() < MINIMUM_PASSWORD_LENGTH) 
            errorMessage(facesContext, "errorPasswordMinimumLength");
            
        isUpperCase = hasUpperCase(obj.toString());
        isLowerCase = hasLowerCase(obj.toString());
        isDigit = hasDigit(obj.toString());
        isSpecialChar = hasSpecialCharacter(obj.toString());
        
        if(isDigit == false || 
           isLowerCase == false || 
           isSpecialChar == false || 
           isUpperCase == false) {
            errorMessage(facesContext, "errorPasswordRequiredCharacters");
        }
    }
    
    private void errorMessage(final FacesContext facesContext, final String errorMsg) throws ValidatorException {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", facesContext.getViewRoot().getLocale());
        
        msg = bundle.getString(errorMsg);
        FacesMessage message = new FacesMessage(msg);
        
        throw new ValidatorException(message);
    }
    
    private boolean hasSpecialCharacter(final String password) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(password);
        
        return m.find();
    }
    
    private boolean hasDigit(final String password) {
        for(Character ch : password.toCharArray()) {
            if(Character.isDigit(ch))
                return true;
        }
        
        return false;
    }
    
    private boolean hasLowerCase(final String password) {
        for(Character ch : password.toCharArray()) {
           if(Character.isAlphabetic(ch)) {
               if(Character.isLowerCase(ch))
                   return true;
           }
        }
        
        return false;
    }
    
    private boolean hasUpperCase(final String password) {
        for(Character ch : password.toCharArray()) {
           if(Character.isAlphabetic(ch)) {
               if(Character.isUpperCase(ch))
                   return true;
           }
        }
        
        return false;
    }
}
