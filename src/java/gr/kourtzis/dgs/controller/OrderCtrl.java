/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import gr.kourtzis.dgs.entity.Game;
import gr.kourtzis.dgs.entity.Inventory;
import gr.kourtzis.dgs.entity.LibraryGame;
import gr.kourtzis.dgs.entity.LibraryGameId;
import gr.kourtzis.dgs.entity.MyOrder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
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
public class OrderCtrl implements Serializable {
//    @Inject
//    private UserCtrl userCtrl;
    @Inject
    private AccountCtrl accountCtrl;
//    @Inject
//    private GamesCatalogCtrl gamesCatalogCtrl;
    private MyOrder myOrder;
    private Inventory inventory;
    
    private byte[] invoicePdf;
        
    public OrderCtrl() {
    }
    
    /**
     * The method adds a game to the order.
     * @param game The game added to the order.
     */
    public void addGameToOrder(final Game game) {
        if(isGameWithInventory(game) && !isInventoryEmpty() && !isGameInOrder(game))
            myOrder.addGame(game);
        
        inventory = null;
    }
    
    /**
     * The method checks if there is an existing currentInventory for a specified game.
     * @param game The game object that was passed as a parameter.
     * @return True if an currentInventory exists or false if the returned currentInventory 
         object is null.
     */
    public boolean isGameWithInventory(final Game game) {
//        currentInventory = gamesCatalogCtrl.findInventory(game.getGameId());
        inventory = accountCtrl.getGamesCatalogCtrl().findInventory(game.getGameId());
        return inventory != null;
    }
    
    /**
     * The method checks if the user has already made orders.
     * @return True if the orders list is not empty and not null, 
     *         otherwise false.
     */
    public boolean isUserWithOrders() {
        return (!accountCtrl.getUserCtrl().getUser().getOrders().isEmpty() && 
                accountCtrl.getUserCtrl().getUser().getOrders() != null);
    }
    
    /**
     * The method checks if there are available keys for the currentInventory.
     * @return True if there are keys available otherwise false.
     */
    public boolean isInventoryEmpty() {
        return accountCtrl.getGamesCatalogCtrl().isInventoryEmpty(inventory);
    }
    
    /**
     * The method removes the game from the order
     * @param game The game we want to remove from the order
     */
    public void removeGameFromOrder(final Game game) {
        if(isGameInOrder(game))
            myOrder.removeGame(game);
    }
    
    /**
     * The method checks if a game is added to the order
     * @param game The game object we want to search for
     * @return True if the game is added or false if
     *         the order has it not.
     */
    public boolean isGameInOrder(final Game game) {
//        System.out.println(myOrder.getGames().contains(game));
        return myOrder.getGames().contains(game);
    }
    
     /**
     * The method navigates to the checkout page.
     * @param game The game object we add to the order.
     * @return A String variable of the destination.
     */
    public String checkOut(final Game game) {
        addGameToOrder(game);
        return "goToCheckOut";
    }
    
    /**
     * Them method navigates to the order summary page.
     * @return  A String variable of the destination.
     */
    public String orderSummary() {
        return "goToOrderSummary";
    }
    
    /**
     * The method navigates to the customer details page.
     * @return A String variable of the destination.
     */
    public String customerDetails() {
        return "goToCustomerDetails";
    }
    
    /**
     * The method navigates to the payment options page
     * @return A String variable of the destination
     */
    public String paymentOptions() {
        accountCtrl.getAddressCtrl().save();
        accountCtrl.getCustomerCtrl().save();
        
        return "goToPaymentOptions";
    }
    
    /**
     * The method downloads a pdf file of the invoice
     * from the order the user has made.
     * @param orderId 
     */
    public void download(int orderId) {
        MyOrder selectedOrder = null;
        
        for(MyOrder currentOrder : accountCtrl.getUserCtrl().getUser().getOrders()) {
            if(currentOrder.getOrderId() == orderId) {
                selectedOrder = currentOrder;
                break;
            }
        }
        
        if(selectedOrder != null) {
            String fileName = "invoice" + selectedOrder.getOrderDate() + "-" + selectedOrder.getOrderId() + ".pdf";
            String contentType = "application/pdf";
            
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            ec.responseReset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
            ec.setResponseContentType(contentType); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
            ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\""); // The Save As popup magic is done here. You can give it any file name you want, this only won't work in MSIE, it will use current request URL as file name instead.

            try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
                OutputStream output = ec.getResponseOutputStream()) {
                
                bos.write(selectedOrder.getInvoice());
                bos.writeTo(output);

                output.flush();
            }
            catch(IOException ex) {
                System.out.println("An exception occured in AccountCtrl --> download(int gameId)");
                System.out.println("Error message: " + ex.getMessage());
            }
            
            fc.responseComplete();
        }
        
        invoicePdf = null;
    }
    
    /**
     * The method checks out the order
     * and saves the data to the database.
     * @return 
     */
    public String save() {
        prepareOrder();
        LibraryGame libraryGame = null;
//        userCtrl.getUser().addOrder(myOrder);
        accountCtrl.getUserCtrl().getUser().addOrder(myOrder);
        for(Game game: myOrder.getGames()) {
//            int userId = userCtrl.getUser().getUserId();
            int userId = accountCtrl.getUserCtrl().getUser().getUserId();
            int gameId = game.getGameId();
            
            libraryGame = new LibraryGame();
            libraryGame.setLibraryGameId(new LibraryGameId(userId, gameId));
            libraryGame.setAddedOn(new Date());
            libraryGame.setLicenseKey(UUID.randomUUID().toString());
            
//            userCtrl.getUser().addLibraryGame(libraryGame);
            accountCtrl.getUserCtrl().getUser().addLibraryGame(libraryGame);
            game.addLibraryGame(libraryGame);
            
        }
        
//        userCtrl.getUser().getLibraryGames().add(libraryGame);
//        userCtrl.save();
        accountCtrl.getUserCtrl().getUser().getLibraryGames().add(libraryGame);
        accountCtrl.getUserCtrl().save();
        
        myOrder.getGames().forEach(it -> {
//            gamesCatalogCtrl.updateInventory(it);
            accountCtrl.getGamesCatalogCtrl().updateInventory(it);
        });
        
        myOrder = new MyOrder();
//        gamesCatalogCtrl.populateInventoryList();
        accountCtrl.getGamesCatalogCtrl().populateInventoryList();
        
        return "goUserIndex";
    }
    
    /**
     * The method checks if the order is empty or has games added to it.
     * @return True if order is empty, otherwise false.
     */
    public boolean isOrderEmpty() {
        return myOrder.getGames().isEmpty();
    }
    
    @PostConstruct
    public void init() {
        myOrder = new MyOrder();
    }
     
    /**
     * The method calculates the price of all the 
     * games added to the order
     * @return The price of all games
     */
    public double calculateTotalPrice() {
        double totalPrice = 0.0;
        
        for(Game game : myOrder.getGames()) {
            int id = game.getGameId();
            totalPrice += getPrice(id);
            
//            for(Inventory currentInventory : gamesCatalogCtrl.getInventories()) {
//                if(currentInventory.getInventoryId() == id) {
//                    totalPrice += currentInventory.getPrice();
//                    break;
//                }
//            }
        }
        
        return totalPrice;
    }
    
    // The method sets the date of the order and 
    // the price of all the games.
    private void prepareOrder() {
        double price = calculateTotalPrice();
        myOrder.setOrderDate(new Date());
        myOrder.setTotalPrice(price);
        // create pdf here and save it
        createPdf();
        myOrder.setInvoice(invoicePdf);
        invoicePdf = null;
    }
    
    private void createPdf() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        String uuid = UUID.randomUUID().toString();

        Document document = new Document();
        ByteArrayOutputStream outputPdf = new ByteArrayOutputStream();
        
        try {
            if(session != null) {
                PdfWriter.getInstance(document, outputPdf);
                document.open();

                document.add(new Paragraph("Invoice: inv:" + uuid));
                PdfPTable customerTable = new PdfPTable(1);
                customerTable.addCell(accountCtrl.getFullName());
                customerTable.addCell(accountCtrl.getAddressCtrl().getAddress().getStreetName()
                        + "\n" + accountCtrl.getAddressCtrl().getAddress().getPostalCode()
                        + " " + accountCtrl.getAddressCtrl().getAddress().getCity()
                        + "\n" + accountCtrl.getAddressCtrl().getAddress().getCountry());
                document.add(customerTable);

                document.add(new Paragraph(""));

                PdfPTable gamesTable = new PdfPTable(3);
                gamesTable.addCell("Nr.");
                gamesTable.addCell("Title");
                gamesTable.addCell("Price");

                myOrder.getGames().forEach(it -> {
                    gamesTable.addCell(String.valueOf(it.getGameId()));
                    gamesTable.addCell(it.getTitle());
                    gamesTable.addCell(String.valueOf(getPrice(it.getGameId())) + "€");
                });

                document.add(gamesTable);
                document.add(new Paragraph());

                document.add(new Paragraph("Total price: " + myOrder.getTotalPrice() + "€"));
                document.newPage();
            }
            else {
                PdfWriter.getInstance(document, outputPdf);
                document.open();
                document.add(new Paragraph("Session expired....."));
            }
            
            document.close();
            invoicePdf = outputPdf.toByteArray();
            
        }
        catch(DocumentException ex) {
            System.out.println("Exception occured in OrderCtrl --> createPdf()");
            System.out.println("Message: " + ex.getMessage());
        }
                
    }
    
    private double getPrice(int gameId) {
//        for (Inventory currentInventory : gamesCatalogCtrl.getInventories()) {
        for (Inventory currentInventory : accountCtrl.getGamesCatalogCtrl().getInventories()) {
            if (currentInventory.getInventoryId() == gameId) 
                return currentInventory.getPrice();
        }

        return 0.0;
    }
}
