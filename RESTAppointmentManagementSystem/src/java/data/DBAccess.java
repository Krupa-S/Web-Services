/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import components.data.Appointment;
import components.data.PSC;
import components.data.Patient;
import components.data.Phlebotomist;
import java.util.List;


/**
 *
 * @author krupaShah
 */
public class DBAccess {

    DBSingleton dbSingleton;
    public DBAccess() {

        dbSingleton = DBSingleton.getInstance();

        // Populate database 
        // (need to do initial load before doing anything else; 
        //     if you don't you'll get an error message stating as much)
        //dbSingleton.db.initialLoad("LAMS");

    }

    public List appointmentList(){
        List<Object> objs;
        
        // Print out all appointments
       // System.out.println("All appointments\n");
        objs = dbSingleton.db.getData("Appointment", "");
//        for (Object obj : objs){
//            System.out.println(obj + "\n");
//        }
        
        return objs;
    }
    
    public List<Object> getAppointment(String appointmentId){
      Appointment appt = new Appointment();
            
           List<Object> objs = dbSingleton.db.getData("Appointment", "id='"+appointmentId+"'");
            
       
        return objs;
    }
    
    public Boolean updateAppointment(Appointment appt){
        
       
        Boolean updateFlag = dbSingleton.db.updateData(appt);
        String appointmentId = appt.getId();
        List<Object> objs = dbSingleton.db.getData("Appointment", "id='"+appointmentId+"'");
        for (Object obj : objs){
            System.out.println(obj + "\n");
        }
        return updateFlag;
    }
    
    public Boolean insertAppointment(Appointment appt){
        Boolean insertFlag =  dbSingleton.db.addData(appt);
        List<Object> objs = dbSingleton.db.getData("Appointment", "");
        for (Object obj : objs){
            System.out.println(obj + "\n");
        }
        return insertFlag;
    }
    
    public List getTableID(String tablename,String id){
        List<Object> objs;
                
        if(tablename.equals("Diagnosis")){
            objs = dbSingleton.db.getData(tablename, "code='"+id+"'");
            
        }else{
            
         objs = dbSingleton.db.getData(tablename, "id='"+id+"'");
        }
         return objs;
    }
    
    public List getDataUsingParameters(String tablename,String parameters){
        List<Object> objs;
         objs = dbSingleton.db.getData(tablename,parameters );
         return objs;
    }
}
