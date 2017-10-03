/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import com.jamesmurty.utils.XMLBuilder2;
import components.data.*;
import data.DBAccess;
import data.DBSingleton;
import java.io.StringReader;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import static javax.ws.rs.client.Entity.xml;
import javax.ws.rs.core.*;
//import javax.xml.bind.*;
import javax.xml.parsers.*;
import javax.xml.transform.stream.StreamSource;
//import jdk.internal.org.xml.sax.InputSource;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.swing.*;
import org.xml.sax.InputSource;

/**
 *
 * @author krupaShah
 */
public class AppointmentBusiness {

    @Context
    private UriInfo context;
    public DBAccess dataAccessObj;
    ArrayList<String> errorsList = new ArrayList<String>();

    //Constructor to initialize database objects
    public AppointmentBusiness() {
        dataAccessObj = new DBAccess();
    }

    //Display Wadl
    public String getInfo(String wadlURI) {
        IComponentsData db = new DB();
        db.initialLoad("LAMS");

        //Form XML String Response
        String uriXML = objectToXML("getinfo", null, wadlURI);

        return uriXML;
    }

    //Get List of Appointments
    public String getAtllAppointments(String uri) {

        List objs = dataAccessObj.appointmentList();
        String allAppointments = "";
        // Print out all appointments
        for (Object obj : objs) {
            allAppointments += "<appointment>" + ((Appointment) obj).getPatientid() + "<appointment>";
        }
        String xmlAppointment = objectToXML("list", objs, uri);
        return xmlAppointment;
    }

    //get an appointment with appointment ID
    public String getAppointment(String ID, String uri) {

        List objs = dataAccessObj.getAppointment(ID);
        String xmlAppointment = objectToXML("list", objs, uri);
        return xmlAppointment;
    }

    //Create Appointment
    public String createAppointment(String createXML, String uri, String operation) {

        String newAppointmentID = "";
        String uri1 = "";

        if (operation.equals("create")) {
            //generate new appointment ID
            List<Object> objList = dataAccessObj.getDataUsingParameters("Appointment", "");
            int appointmentListSize = objList.size();
            int newID = Integer.parseInt((((Appointment) objList.get(appointmentListSize - 1))).getId());
            newAppointmentID = Integer.toString(newID + 10);
        } else {
            //get appointment ID for update
            uri1 = "http://localhost:8080/LAMSAppointment/webresources/Services/Appointments/";
            newAppointmentID = uri.substring(uri1.length(), (uri.length()));
            System.out.println("newAppointmentID " + newAppointmentID);
        }

        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        InputSource is;
        org.w3c.dom.Document doc;
        ArrayList<String> line = new ArrayList<String>();
        String createResponse = "";

        try {
            //parsing XML request
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            is = new InputSource(new StringReader(createXML));
            doc = (org.w3c.dom.Document) builder.parse(is);

            // NodeList date = doc.getElementsByTagName("appointment");
            String dateAppt = doc.getElementsByTagName("date").item(0).getTextContent();
            //createAppt.setApptdate(dateAppt);
            System.out.println("dateAppt" + dateAppt);
            String time = doc.getElementsByTagName("time").item(0).getTextContent();
            String timetemp = time + ":00";
            System.out.println("tie " + timetemp);

            //Appointment Object Created
            Appointment createAppt = new Appointment(newAppointmentID, java.sql.Date.valueOf(dateAppt), java.sql.Time.valueOf(timetemp));

            HashMap<String, String> createData = new HashMap<String, String>();

            //Parse XML to get data 
            String patientId = doc.getElementsByTagName("patientId").item(0).getTextContent();
            createData.put("Patient", patientId);
            String physicianId = doc.getElementsByTagName("physicianId").item(0).getTextContent();
            createData.put("Physician", physicianId);
            String pscId = doc.getElementsByTagName("pscId").item(0).getTextContent();
            createData.put("PSC", pscId);
            String phlebotomistId = doc.getElementsByTagName("phlebotomistId").item(0).getTextContent();
            createData.put("Phlebotomist", phlebotomistId);

            NodeList labTests = doc.getElementsByTagName("labTests");
            NodeList tests = labTests.item(0).getChildNodes();
            ArrayList<AppointmentLabTest> testList = new ArrayList<AppointmentLabTest>();
            for (int i = 0; i < tests.getLength(); i++) {
                if (tests.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) tests.item(i);
                    String dxcode = e.getAttribute("dxcode");
                    String id = e.getAttribute("id");
                    createData.put("Diagnosis", dxcode);
                    createData.put("LabTest", id);
                    AppointmentLabTest tempTest = new AppointmentLabTest(newAppointmentID, id, dxcode);
                    tempTest.setDiagnosis((Diagnosis) dataAccessObj.getTableID("Diagnosis", dxcode).get(0));
                    tempTest.setLabTest((LabTest) dataAccessObj.getTableID("LabTest", id).get(0));
                    //tempTest.setAppointment(createAppt);

                    testList.add(tempTest);
                }

            }

            //method call to validate data
            Boolean validitityCheck = isDataValid(createData);
            System.out.println("validitityCheck" + validitityCheck);

            //validate the data provided
            if (!validitityCheck) {
                //check time if input data is valid
                createAppt.setAppointmentLabTestCollection(testList);
                createAppt.setPatientid((Patient) dataAccessObj.getTableID("Patient", patientId).get(0));
                createAppt.setPhlebid((Phlebotomist) dataAccessObj.getTableID("Phlebotomist", phlebotomistId).get(0));
                createAppt.setPscid((PSC) dataAccessObj.getTableID("PSC", pscId).get(0));
                //Availability of appointment
                boolean dateTimeCheck = validateDateTime(dateAppt, timetemp, pscId, phlebotomistId, patientId);

                //Create appointment is valid
                if (dateTimeCheck) {
                    //Create
                    if (operation.equals("create")) {
                        Boolean result = dataAccessObj.insertAppointment(createAppt);
                        if (result) {
                            String createURI = uri + newAppointmentID;
                            createResponse = objectToXML("create", null, createURI);
                        } else {
                            errorsList.add("ERROR: Appointment Not Available");
                            return errorXML(errorsList);
                        }
                    } else {
                        //update
                        Boolean result = dataAccessObj.updateAppointment(createAppt);
                        if (result) {
                            String createURI = uri;
                            createResponse = objectToXML("update", null, createURI);
                        } else {
                            errorsList.add("ERROR: Appointment Not Available");
                            return errorXML(errorsList);

                        }
                    }

                }

            } else {
                return errorXML(errorsList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return createResponse;
    }

    //Validate ID of input data
    public boolean isValidID(String tablename, String id) {
        List<Object> objs = dataAccessObj.getTableID(tablename, id);

        if (objs.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    //check time avalaibilty
    public boolean validateDateTime(String date, String time, String psc, String phlebotomist, String patient) {
        try {
            // Check if same Phlebotomist already present at other psc for the requested apponintment time
            System.out.println("patient ID" + patient + " timeAppt " + patient + "dateAppt  " + date);
            List<Object> objs = dataAccessObj.getDataUsingParameters("Appointment", "apptdate='" + date + "' and patientid='" + patient + "'");
            if (objs.size() > 0) {
                for (Object ob : objs) {

                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    Date date1 = format.parse(time);
                    System.out.println("Time: " + date1);
                    Date date2 = format.parse(((Appointment) ob).getAppttime().toString());
                    //System.out.println("date1" + date1.getHours());
                    //System.out.println("date2" + date2.getHours());

                    if (Math.abs(date2.getTime() - date1.getTime()) < 2700000) {
                        System.out.println("Here5");

                        return false;
                    }
                }
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date checkTime = dateFormat.parse(time);
            //Appointments from 8:00 AM to 4:45 PM 
            if (checkTime.after(dateFormat.parse("8:00:00")) && checkTime.before(dateFormat.parse("16:46:00"))) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                String d = dateFormat.format(c.getTime());
                //Check date of appointment; If past date return false
                if ((dateFormat.parse(date)).before(dateFormat.parse(d))) {
                    System.out.println("Here4");

                    return false;
                } else {
                    Date parseDate = dateFormat.parse(date);
                    //Check for PSC and Date
                    objs = dataAccessObj.getDataUsingParameters("Appointment", "apptdate = '" + date + "' and pscid='" + psc + "'");

                    //Check for date and phlebotomist
                    List<Object> objsPhlebotomist = dataAccessObj.getDataUsingParameters("Appointment", "apptdate = '" + date + "' and phlebid = '" + phlebotomist + "'");

                    if (objs.size() == 0) {
                        if (objsPhlebotomist.size() == 0) {
                            return true;
                        } else {
                            //Check reservations of phlebotomist

                            return checkPhlebotomistAvailability(time, psc, objsPhlebotomist);
                        }
                    } else {
                        List<Time> timeList = new ArrayList<Time>();
                        for (Object timeObj : objs) {
                            timeList.add(((Appointment) timeObj).getAppttime());
                        }

                        if (timeList.contains(Time.valueOf(time))) {
                            //System.out.println("Here3");

                            return false;
                        }
                        if (objsPhlebotomist.size() == 0) {
                            return true;
                        } else {
                            return checkPhlebotomistAvailability(time, psc, objsPhlebotomist);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    //check availability of Phlebotomist
    public boolean checkPhlebotomistAvailability(String timeAppt, String psc, List<Object> objsPhlebotomist) throws Exception {
        boolean match = false;
        try {
            HashMap<String, String> time_phle = new HashMap<String, String>();
            for (Object ob : objsPhlebotomist) {
                Time t = ((Appointment) ob).getAppttime();
                PSC p = ((Appointment) ob).getPscid();
                time_phle.put(t.toString(), p.getId());
            }

            for (Entry<String, String> entry : time_phle.entrySet()) {
                if (!entry.getValue().equals(psc)) {
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    Date date1 = format.parse(timeAppt);
                    Date date2 = format.parse(entry.getKey());
                    if (Math.abs(date2.getTime() - date1.getTime()) < 2700000) {
                        //System.out.println("Here2");

                        match = true;
                    }
                } else {
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    Date date1 = format.parse(timeAppt);
                    Date date2 = format.parse(entry.getKey());
                    if (Math.abs(date2.getTime() - date1.getTime()) < 900000) {
                        //System.out.println("Here1");
                        match = true;
                    }
                }
            }

        } catch (Exception e) {
            return false;
        }

        if (match) {
            return false;
        } else {
            return true;
        }
    }

    //Method tp validate all input data
    public Boolean isDataValid(HashMap<String, String> createData) {
        Boolean invalidFlag = false;
        for (String key : createData.keySet()) {

            if (!isValidID(key, createData.get(key))) {
                invalidFlag = true;
                errorsList.add(key + " is in valid");
            }
        }
        return invalidFlag;
    }
 
    //Build XML from Object
    public String objectToXML(String operation, List<Object> objList, String uri) {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat tf = new SimpleDateFormat("HH:mm:ss");

        XMLBuilder2 builder = XMLBuilder2.create("AppointmentList");
        if (operation.equals("getinfo")) {
            XMLBuilder2 builder1 = builder.e("intro").t("Welcome to the LAMS Appointment Service").up()
                    .e("wadl").t(uri);
        } else if (operation.equals("create") || operation.equals("update")) {

            XMLBuilder2 builder1 = builder.e("uri").t(uri);

        } else if (operation.equals("list")) {

            for (Object objs : objList) {
                Appointment obj = ((Appointment) objs);
                XMLBuilder2 builder1 = builder.e("appointment").a("date", df.format(obj.getApptdate()))
                        .a("id", obj.getId()).a("time", tf.format(obj.getAppttime()))
                        .e("uri").t(uri).up()
                        .e("patient").a("id", obj.getPatientid().getId())
                        .e("uri").up()
                        .e("name").t(obj.getPatientid().getName()).up()
                        .e("address")
                        .t(obj.getPatientid().getAddress()).up()
                        .e("insurance").t(Character.toString(obj.getPatientid().getInsurance())).up()
                        .e("dob").t(df.format(obj.getPatientid().getDateofbirth())).up()
                        .up()
                        .e("phlebotomist").a("id", obj.getPhlebid().getId())
                        .e("uri").up()
                        .e("name").t(obj.getPhlebid().getName()).up()
                        .up()
                        .e("psc").a("id", obj.getPscid().getId())
                        .e("uri").up()
                        .e("name").t(obj.getPscid().getName()).up()
                        .up()
                        .e("allLabTests")
                        .e("appointmentLabTest").a("appointmentId", obj.getId())
                        .a("dxcode", obj.getAppointmentLabTestCollection().get(0).getDiagnosis().getCode())
                        .a("labTestId", obj.getAppointmentLabTestCollection().get(0).getLabTest().getId())
                        .e("uri").up()
                        .up()
                        .up();
            }

        }

        return builder.asString();

    }

    //Error List display
    public String errorXML(ArrayList<String> errorsList) {
        XMLBuilder2 builder = XMLBuilder2.create("AppointmentList");
        for (int i = 0; i < errorsList.size(); i++) {
            XMLBuilder2 builder1 = builder.e("error").t(errorsList.get(i)).up();

        }
        System.out.println(builder.asString());
        return builder.asString();
    }
}
