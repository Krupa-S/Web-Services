
import data.DBAccess;
import java.util.List;
import components.data.Appointment;
import java.time.Clock;
import javax.xml.bind.JAXBException;
import service.AppointmentService;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author krupaShah
 */
public class client {
    public static void main(String[] args) throws JAXBException{
        DBAccess dao = new DBAccess();
        List objs = dao.appointmentList();
        AppointmentService service = new AppointmentService();
        System.out.println(service.getInfo());
        
        // Print out all appointments
//        System.out.println("All appointments\n");
//        for (Object obj : objs){
//            System.out.println(((Appointment)obj).getPatientid() + "\n");
//        }
        
        String xmlString = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" +
"<appointment>\n" +
"    <date>2017-05-18</date>\n" +
"    <time>10:00</time>\n" +
"    <patientId>210</patientId>\n" +
"    <physicianId>10</physicianId>\n" +
"    <pscId>500</pscId>\n" +
"    <phlebotomistId>1000</phlebotomistId>\n" +
"    <labTests>\n" +
"        <test id=\"86900\" dxcode=\"292.9\" />\n" +
"    </labTests>\n" +
"</appointment>";

        //System.out.println(service.getAtllAppointments());
        //System.out.println(service.getAppointment("700"));
       System.out.println(service.updateAppointment(xmlString,"700"));
        //System.out.println(service.getAppointment("700"));
        
        
      // System.out.println(service.createAppointment(createXML));
      //System.out.println(service.getAppointment("700"));
      //System.out.println(service.getAtllAppointments());
    }
}
