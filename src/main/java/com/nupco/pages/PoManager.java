package com.nupco.pages;

import com.nupco.sqlqueries.PurchaseOrderManagerSqlQueries;
import com.nupco.utils.BaseFunctions;
import com.nupco.utils.helper;
import com.shaft.driver.SHAFT;

public class PoManager {

    private final AsnNewRequestPage asnNewRequestPage;
    private final BlanketAsnRequestPage blanketAsnRequestPage;
    private final BlanketPOListPage blanketPOListPage;
    private final E_ServicesPage eServicesPage;
    private final LaunchPadPage launchPadPage;
    private final LoginPage loginPage;
    private final MyRequestListPage myRequestListPage;
    private final MyTasksPage myTasksPage;
    private final  PoDetailsPage poDetailsPage;
    private final POsListPage pOsListPage;
    private final RequestDetailsPage requestDetailsPage;
    private final BaseFunctions baseFunctions;
    private final PurchaseOrderManagerSqlQueries PurchaseOrderManagerSql;

    public PoManager (SHAFT.GUI.WebDriver driver){
        this.asnNewRequestPage = new AsnNewRequestPage(driver);
        this.blanketAsnRequestPage = new BlanketAsnRequestPage(driver);
        this.blanketPOListPage = new BlanketPOListPage(driver);
        this.eServicesPage = new E_ServicesPage(driver);
        this.launchPadPage = new LaunchPadPage(driver);
        this.loginPage = new LoginPage(driver);
        this.myRequestListPage = new MyRequestListPage(driver);
        this.myTasksPage = new MyTasksPage(driver);
        this.poDetailsPage = new PoDetailsPage(driver);
        this.pOsListPage = new POsListPage(driver);
        this.requestDetailsPage = new RequestDetailsPage(driver);
        this.baseFunctions = new BaseFunctions(driver);
        this.PurchaseOrderManagerSql = new PurchaseOrderManagerSqlQueries();
    }

    public AsnNewRequestPage getAsnNewRequestPage(){
        return  asnNewRequestPage ;
    }
    public BlanketAsnRequestPage getBlanketAsnRequestPage(){
        return blanketAsnRequestPage;
    }
    public BlanketPOListPage getBlanketPOListPage(){
        return blanketPOListPage;
    }
    public E_ServicesPage getEServicesPage(){
        return eServicesPage;
    }
    public LaunchPadPage getLaunchPadPage(){
        return launchPadPage;
    }
    public LoginPage getLoginPage(){
        return loginPage;
    }
    public MyRequestListPage getMyRequestListPage(){
        return myRequestListPage ;
    }
    public MyTasksPage getMyTasksPage(){
        return myTasksPage;
    }
    public PoDetailsPage getPoDetailsPage(){
        return poDetailsPage;
    }
    public POsListPage getpOsListPage(){
        return pOsListPage;
    }
    public RequestDetailsPage getRequestDetailsPage(){
        return requestDetailsPage;
    }
    public BaseFunctions getBaseFunctions(){
        return baseFunctions;
    }
    public PurchaseOrderManagerSqlQueries getPurchaseOrderManagerSqlQueries(){
        return PurchaseOrderManagerSql;
    }
}
