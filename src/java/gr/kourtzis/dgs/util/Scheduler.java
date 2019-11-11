/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.kourtzis.dgs.util;

import gr.kourtzis.dgs.controller.GamesCatalogCtrl;
import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 *
 * @author Athanasios Kourtzis
 */
@Singleton
public class Scheduler {
    @Inject
    private GamesCatalogCtrl gamesCatalogCtrl;
    
    @Schedule(hour = "*", minute = "*", second = "*/10")
    @Asynchronous
    private void updateCatalog() {
        gamesCatalogCtrl.updateCatalog();
    }
}
