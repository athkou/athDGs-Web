/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.util;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Athanasios Kourtzis
 */

@FacesValidator(value = "myPasswordConfirmValidator")
public class PasswordConfirmValidator implements Validator {
    private String msg;
    
    
    public PasswordConfirmValidator() {
    }
   
    /**
     * The method checks wether two input fields contain the same password.
     * @param facesContext FacesContext for the request we are processing
     * @param components UIComponent we are checking for correctness
     * @param obj The Object variable containing the password 
     *            we want to confirm.
     * @throws ValidatorException if validation fails
     */
    @Override
    public void validate(FacesContext facesContext, UIComponent components, Object obj) throws ValidatorException {
        if(obj == null) 
            errorMessage(facesContext, "errorNullObject");
        
        String passwordConfirm = (String) obj;
        
        UIInput passwordInput = (UIInput) components.findComponent("password");
        String password = (String) passwordInput.getLocalValue();
        
        if(password == null || !password.equals(passwordConfirm))
            errorMessage(facesContext, "errorPasswordConfirm");
    }
    
    private void errorMessage(final FacesContext facesContext, final String errorMsg) throws ValidatorException {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", facesContext.getViewRoot().getLocale());
        
        msg = bundle.getString(errorMsg);
        FacesMessage message = new FacesMessage(msg);
        
        throw new ValidatorException(message);
    }
}
