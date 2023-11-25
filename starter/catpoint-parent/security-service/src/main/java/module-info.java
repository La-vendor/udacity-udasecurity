module security.service {
    requires java.desktop;
    requires image.service;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    requires com.miglayout.swing;
    opens com.udacity.securityservice.data to com.google.gson;
    requires org.slf4j;


}