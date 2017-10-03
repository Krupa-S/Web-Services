/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import business.AppointmentBusiness;
import components.data.Appointment;
import data.DBAccess;
import data.DBSingleton;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

/**
 *
 * REST Web Service
 *
 * @author krupaShah
 */
@Path("Services")
public class AppointmentService {

    @Context
    private UriInfo context;

    @GET
    @Produces("application/xml")
    public String getInfo() {
        AppointmentBusiness businessObj = new AppointmentBusiness();
        //String serviceWadl = this.context.getBaseUri().toString() + "application.wadl";
        String serviceWadl = "http://localhost:8080/LAMSAppointment/webresources/application.wadl";
        String wadlURI = businessObj.getInfo(serviceWadl);
        return wadlURI;
    }

    @Path("/Appointments")
    @GET
    @Produces("application/xml")
    @Consumes("text/plain")
    public String getAtllAppointments() {

        AppointmentBusiness businessObj = new AppointmentBusiness();
        String uri = this.context.getBaseUri().toString() + "/Services/Appointments/";
        String allAppointments = businessObj.getAtllAppointments(uri);
        return allAppointments;
    }

    @Path("/Appointments/{ID}")
    @GET
    @Produces("application/xml")
    @Consumes("application/xml,text/plain")
    public String getAppointment(@PathParam("ID") String ID) {

        AppointmentBusiness businessObj = new AppointmentBusiness();
        String uri = this.context.getBaseUri().toString() + "/Services/Appointments/" + ID;
        String appointment = businessObj.getAppointment(ID, uri);
        return appointment;

    }

    @Path("Appointments/")
    @POST
    @Produces("application/xml")
    @Consumes({"text/plain", "application/xml"})
    public String createAppointment(String createXml) throws JAXBException {

        AppointmentBusiness businessObj = new AppointmentBusiness();
        System.out.println("createXML " + createXml);
        String uri = this.context.getBaseUri().toString() + "/Services/Appointments/";
        String createString = businessObj.createAppointment(createXml, uri, "create");

        //String  uri = businessObj.updateAppointment(createXml);
        return createString;

    }

    @Path("Appointments/{ID}")
    @PUT
    @Produces("application/xml")
    @Consumes({"text/xml", "application/xml"})
    public String updateAppointment(String xmlString,@PathParam("ID") String ID) throws JAXBException {

        AppointmentBusiness businessObj = new AppointmentBusiness();
        //String uri = this.context.getBaseUri().toString() + "/Services/Appointments/";
        String uri = "http://localhost:8080/LAMSAppointment/webresources/Services/Appointments/" + ID ;
        String updateString = businessObj.createAppointment(xmlString, uri, "update");
        return updateString;

    }

    @Path("/DeleteAppointment/{ID}")
    @DELETE
    @Consumes("text/plain")
    @Produces("application/xml")
    public String deleteAppointment(@PathParam("ID") String ID) {
        
        AppointmentBusiness businessObj = new AppointmentBusiness();
        String uri = this.context.getBaseUri().toString() + "/Services/Appointments/";
        //String deleteString = businessObj.deleteAppointment(ID);
        return "";
    }

}
