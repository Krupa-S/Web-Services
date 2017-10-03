package business;

import data.*;
import exceptions.BusinessRulesExceptions;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

/**
 * @author krupaShah
 *
 */
public class BeerBusiness {

	BeerDAO beerdaoObj = new BeerDAO(); // Object of data layer
	String invalidateMessage = "";

	/*
	 * Method to check if the user is valid or not
	 * 
	 * @param String username of the user
	 * 
	 * @param password password of the user
	 * 
	 * @return boolean if count is not 0
	 */
	public Boolean isValidUser(String username, String password) {

		int count = beerdaoObj.validateUser(username, password);
		if (count > 0) {

			return true;

		} else {

			return false;
		}

	}

	/**
	 * To check user's age is not under 21 
	 * @param token
	 * @return boolean based on age
	 */
	public Boolean isValidAge(String token, Boolean istoken) {

		// call data layer function to get user age
		int userAge = beerdaoObj.getUserAge(token, istoken);
		if (userAge < 21) {
			System.out.println("User's age is not valid");
			return false;
		} else {
			return true;
		}
	}

	
	/**
	 * To check operation time is not between midnight and 10am
	 * 
	 * @return Boolean value. if between midnight and 10am return false.
	 */
	public Boolean isValidOperationTime() {

		// get current time to check for operation hours
		int operationTime = LocalDateTime.now().getHour();

		if (operationTime < 8 && 0 < operationTime) {
			System.out.println("Not the Opertion Time ");
			return false;
		} else {
			return true;
		}

	}

	/**
	 * Check if the user is administrator
	 * 
	 * @param token
	 * @return
	 */
	public Boolean isAdministrativeUser(String token) {

		String accessLevel = beerdaoObj.getUserLevel(token);
		if (accessLevel.equals("administrator")) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * To check token is valid and not expired
	 * 
	 * @param token
	 *            user token to access operation
	 * @return boolean value true, if token is valid
	 */
	public Boolean isValidToken(String token) {

		// Get Expiration Time of Token
		String timeStmp = beerdaoObj.getTokenExpirationTime(token);

		if ( timeStmp != null ) {

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.n");
			LocalDateTime expirationTime = LocalDateTime.parse(timeStmp, formatter);
			LocalDateTime timeNow = LocalDateTime.now();

			// Compare expiration time and current time
			if ( timeNow.compareTo(expirationTime) > 0 ) {

				//System.out.println("Token is Expired");

				// Delete Expired Token
				beerdaoObj.deleteToken(token);

				return false;

			} else {

				//System.out.println("Not Expired");
				return true;

			}

		}
		return false;
	}

	public Boolean validateConditions(String token) throws BusinessRulesExceptions {

		Boolean validFlag = true;
		if(token == null || token == "" ){
			throw new BusinessRulesExceptions("Token is empty");

		}

		if (!isValidToken(token)) {

			validFlag = false;
			invalidateMessage += "Token is expired";

		} else {

			if ( !isValidAge(token, true) ) {

				validFlag = false;
				invalidateMessage += "Age less than 21 years is not valid.";

			}

			if ( !isValidOperationTime() ) {

				validFlag = false;
				invalidateMessage += "Requested service is down.";

			}
		}

		return validFlag;
	}

	/**
	 * Method to create token
	 * 
	 * @param userame
	 * @param password
	 * @return String
	 */
	public String createToken(String username, String password) throws BusinessRulesExceptions {

		SecureRandom random = new SecureRandom(); // for random token generation

		String token = "";
		if( !isValidOperationTime() ){

			throw new BusinessRulesExceptions("Requested service is down.");

		} else if( !isValidAge(username,false) ){

			throw new BusinessRulesExceptions(" User's age is less than 21");

		}else{

			// Call valid user method
			Boolean flag = isValidUser(username, password);

			if ( flag ) {

				// Check if user has existing active token
				String existingToken = beerdaoObj.getToken(username);

				if ( existingToken != null && isValidToken(existingToken) ) {

					token = existingToken;

				} else {
					// Create random token
					token = new BigInteger(130, random).toString(32);

					// Set expiration time of 5 mins
					LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

					System.out.println("expirationTime " + expirationTime);

					// Convert java LocalDateTime to SQL timestamp
					Timestamp tmp = Timestamp.valueOf(expirationTime);
					int count = beerdaoObj.insertToken(token, username, tmp);
					if (count > 0) {
						System.out.println("Inserted Succesfully");
					} else {
						System.out.println("Not Inserted");
					}

				}

			} else{

				throw new BusinessRulesExceptions(" Invalid user. Check username and password ");
			}
		}

		return token;
	}


	/**
	 * @param beer beername for fetching its price
	 * @param token user token
	 * @return double price of beer
	 */
	public Double getPrice(String beer, String token) throws BusinessRulesExceptions {

		//Check for if beer is null
		if ( beer.equals(null) || beer.equals("") ){

			throw new BusinessRulesExceptions("Beer is empty");

		}
		// check operation time, valid token, valid age
		if ( validateConditions(token) ) {

			Double price = beerdaoObj.getPrice(beer);
			if( price != null){

				return price;

			}else{

				throw new BusinessRulesExceptions("Check beer name. Prices not retrieved");
			}

		}else{

			String message = invalidateMessage;
			invalidateMessage = "";
			throw new BusinessRulesExceptions(message);
		}
	}

	/**
	 * @param beer beer name
	 * @param price price for beer name to be set
	 * @return
	 * @throws BusinessRulesExceptions 
	 */
	public Boolean setPrice(String beer, Double price, String token) throws BusinessRulesExceptions {

		if( beer == null || price == null){
			throw new BusinessRulesExceptions("Beer Name/Price missing");

		}
		
		//Admin user check
		
		if(!isAdministrativeUser(token)){
			throw new BusinessRulesExceptions("Not an admin user. No access rights to set price");
		}
		// Check for operation hours, valid token,valid age, admin user
		if (validateConditions(token)){

			int updated = beerdaoObj.setPrice(beer, price);

			if (updated > 0) {

				return true;

			} else {

				return false;
			}
		}else{
			String message = invalidateMessage;
			invalidateMessage = "";
			throw new BusinessRulesExceptions(message);
		}
	}

	/**
	 * Method to get cheapest beer
	 * @param token user token
	 * @return String cheapest beer name
	 * @throws BusinessRulesExceptions 
	 */
	public String getCheapestBeer(String token) throws BusinessRulesExceptions {

		// check operation time, valid token, valid age
		if (validateConditions(token)) {

			String cheapest = beerdaoObj.getCheapestBeer(); // Call to data layer function
			return cheapest;

		} else {

			String message = invalidateMessage;
			invalidateMessage = "";
			throw new BusinessRulesExceptions(message);
		}

	}

	/**
	 * Method to fetch costliest beer 
	 * @param token
	 * @return
	 * @throws BusinessRulesExceptions 
	 */
	public String getCostliestBeer(String token) throws BusinessRulesExceptions {

		// check operation time, valid token, valid age

		if (validateConditions(token)) {
			// Call to data layer function
			String costliestBeer = beerdaoObj.getCostliestBeer();
			return costliestBeer;

		} else {

			String message = invalidateMessage;
			invalidateMessage = "";
			throw new BusinessRulesExceptions(message);
		}

	}

	/**
	 * Method to get list of beers
	 * @param token
	 * @return Vector of beers
	 * @throws BusinessRulesExceptions 
	 */
	public Vector getBeers(String token) throws BusinessRulesExceptions {

		// Check operation time, valid token, userage

		Vector beerVector = new Vector();

		// call to data layer fucntions
		if (validateConditions(token)) {

			beerVector = beerdaoObj.getBeers();

		} else {

			String message = invalidateMessage;
			//System.out.println("dsf" + message);
			invalidateMessage = "";
			throw new BusinessRulesExceptions(message);
		}
		return beerVector;
	}

	/**
	 * To fetch vector of methods in service
	 * @param token
	 * @return Vector of service methods
	 * @throws BusinessRulesExceptions 
	 */
	public Vector getMethods(String token) throws BusinessRulesExceptions {

		// valid token , operation time

		Vector<String> methodsVector = new Vector();

		if (isValidToken(token) && isValidOperationTime()) {

			// Add the methods to vector

			methodsVector.add("getToken()");
			methodsVector.add("getPrice()");
			methodsVector.add("setPrice()");
			methodsVector.add("getBeers()");
			methodsVector.add("getCheapest()");
			methodsVector.add("getCostliest()");

		}else{

			throw new BusinessRulesExceptions("Token is invalid");
		}
		return methodsVector;

	}

}