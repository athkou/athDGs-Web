/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import gr.kourtzis.dgs.entity.Account;
import gr.kourtzis.dgs.util.Util;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
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
@RequestScoped
public class SignupCtrl implements Serializable {
    private static final String DGS_REGISTER_ACCOUNT_CONNECTION_FACTORY = "jms/athDgsAccountConnectionFactory";
    private static final String DGS_REGISTER_ACCOUNT_QUEUE = "jms/athDgsAccountQueue";
    
    private Account account;
    private String passwordConfirm;
    
    @Resource(mappedName = DGS_REGISTER_ACCOUNT_CONNECTION_FACTORY)
    private ConnectionFactory connectionFactory;
    
    @Resource(mappedName = DGS_REGISTER_ACCOUNT_QUEUE)
    private Queue queue;
    
    public SignupCtrl() {
    }
    
    /**
     * The method saves and persist the data that was given from 
     * the user during the registration phase. 
     * @return A String variable of the destination.
     */
    public String save() {
        prepareAccount();
        registerUser();
        
        return "goSignupPageTwo";
    }
    
    @PostConstruct
    public void init() {
        if(account == null) 
            account = new Account();
    }
    
    private void prepareAccount() {
        account.getAddress().setCustomer(account.getCustomer());
        account.getCustomer().setUser(account.getUser());
        
        UUID uuid = UUID.randomUUID();
        account.getActivation().setActivationToken(uuid.toString());System.out.println(account.getActivation().getActivationToken());
        account.getActivation().setUserActivated(false);
        account.getActivation().setCreatedOn(new Date());
        account.getActivation().setExpirationDate(Account.calculateExpireDate(Account.EXPIRATION));
        account.getActivation().setUser(account.getUser());
        
        String encryptedPassword = Util.createPassword(account.getUser().getPassword());
        account.getUser().setPassword(encryptedPassword);
        account.getUser().setAppRole("user");
        
        account.setSendEmail(false);
    }
    
    private void registerUser() {
        MessageProducer messageProducer = null;
        Session session = null;
        Connection connection = null;
        Context context = null;
        
        try {
            Properties props = new Properties();
            props.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
            props.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
            props.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
            props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

            context = new InitialContext(props);
            
            connectionFactory = (ConnectionFactory) context.lookup(DGS_REGISTER_ACCOUNT_CONNECTION_FACTORY);
            queue = (Queue) context.lookup(DGS_REGISTER_ACCOUNT_QUEUE);
            
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            messageProducer = session.createProducer((Destination) queue);
            ObjectMessage objectMessage = session.createObjectMessage(account);
            messageProducer.send(objectMessage);
            
            context.close();
            messageProducer.close();
            session.close();
            connection.close();
            
        }
        catch (JMSException | NamingException ex) {
            Logger.getLogger(SignupCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
