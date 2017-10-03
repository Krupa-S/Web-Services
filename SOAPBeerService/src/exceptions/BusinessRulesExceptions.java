package exceptions;

import javax.xml.ws.WebFault;


@WebFault(name="BusinessRulesExceptions")
public class BusinessRulesExceptions extends Exception {
	/*
	 * Exception class to throw business class exceptions
	 */
	public BusinessRulesExceptions(String errormessage){

		super(errormessage);
	}
}
