/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.util;

import gr.kourtzis.dgs.ejb.UserAdministrationBeanRemote;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Athanasios Kourtzis
 */

@FacesValidator(value = "myEmailValidator")
public class EmailValidator implements Validator {
    private String msg;
    private String regex;
    private Context emailContext;
    private List<String> emails;
    
    public EmailValidator() {
        emails = new ArrayList<>();
    }

    /**
     * The method verifies that the value of the obj variable is valid according
     * to the constrain of the regular expression.
     * @param facesContext FacesContext for the request we are processing
     * @param components UIComponent we are checking for correctness
     * @param obj The Object variable containing the email address.
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
        
        emails = lookupUserAdministrationBeanRemote().readAllEmails();
        
        emails.forEach(it -> {
            if(it.equalsIgnoreCase(obj.toString())) {
                msg = bundle.getString("errorUniqueEmail");
              
                errorMessage(msg);
            }
        });
    }
    
    private void errorMessage(final String errorMsg) throws ValidatorException {
        FacesMessage message = new FacesMessage(errorMsg);
        throw new ValidatorException(message);
    }
    
    private UserAdministrationBeanRemote lookupUserAdministrationBeanRemote() {
        UserAdministrationBeanRemote userRemote = null;
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
            props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
            props.setProperty(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
            props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
            props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

            emailContext = new InitialContext(props);
            userRemote = (UserAdministrationBeanRemote) emailContext.lookup("ejb/userAdministration");
        }
        catch(NamingException ex) {
            throw new RuntimeException("Exception occured:" + ex.getMessage(), ex);
        }
        
        return userRemote;
    }
}
