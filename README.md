# athDGs-Web
A module containing a web based implementation using JSF as the front end framework.

Steam is a distribution platform for software in general. Steam is also known for selling games for the PC as a target platform. Installation and maintenance are automated via the Steam client and it is also possible to download games before the official release date, especially if the amount of data includes several gigabytes. 

For the reasons mentioned above, it is common for third-party vendors to sell license keys for Steam games. From this starting position the requirement is derived to develop software to enable the client to establish itself in the market as an official retailer. 
In order to come one step closer to this goal, four software components are being developed. 

The Web-based application is developed in the Web module. It uses the MVC (Model-View-Controller) pattern which is required to use the JSF as frontend framework. The advantage of JSF is that it allows access to data of entity classes using the Expression Language and the templating of Web components. Furthermore, JSF and the use of message bundles facilitate the internationalization of the application. It should also be mentioned that by using an EJB Timer Service class the databases are updated time controlled. 
