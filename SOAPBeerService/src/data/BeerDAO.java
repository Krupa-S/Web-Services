package data;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

// Make sure mysql-connector-java-5.1.27-bin.jar is in the classpath
// For glassfish, needs to be in the glassfish/lib folder
// Need to add user and grant access to beerprices

public class BeerDAO
{
	DatabaseAccess db ; 
	String dbName = "beerprices";
	String user = "test";
	String pswd = "testing";
	String host = "localhost";
	String port = "3306";


	//Constructor to create dataaccess object and connect to database
	public BeerDAO(){

		try
		{

			// Create an object of the utility  class that you will use to do your queries
			db = new DatabaseAccess( dbName,user, pswd, host, port );

		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Method to validate user using username and password
	 * @param username 
	 * @param password
	 * @return int value of count of rows returned
	 */
	public int validateUser(String username, String password){

		String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
		ArrayList<ArrayList<String>> res;
		int count = 0;
		try {

			ArrayList<String> params = new ArrayList<String>();
			params.add( username );   
			params.add( password );
			res = db.getDataPS( sql, params );
			//res = db.getData( sql );


			//did we get a result
			if( res != null)
			{
				count = Integer.parseInt(res.get( 0 ) .get( 0 ) );
			}
			

		} catch(SQLException e)
		{
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * Method to insert token created into databases
	 * @param token
	 * @param username
	 * @param tmp timestamp
	 * @return count of rows inserted
	 */
	public int insertToken(String token, String username, Timestamp tmp){
		
		int count = 0;
		String sql = "INSERT into token (tokenID, username, expirationDate) VALUES (?, ?, ?) ";
		
		ArrayList v = new ArrayList();
		v.add(token);
		v.add(username);
		v.add(tmp);

		try{
			count = db.insertToken(sql, v);

		}catch(Exception e){
			e.printStackTrace();
		}

		return count; 
	}

	/**
	 * Method to fetch price of beer 
	 * @param beer
	 * @return price of the beer
	 */
	public Double getPrice(String beer){

		String sql = "SELECT beerprice FROM beers WHERE beername = ?";
		
		ArrayList<String> params = new ArrayList<String>();
		ArrayList<ArrayList<String>> res;
		Double price = null;
		
		params.add( beer ); 
		
		try{
			
			res = db.getDataPS( sql, params );

			if( res != null)
			{
				for ( ArrayList<String> row: res){
					price =  Double.parseDouble(row.get ( 0 ));
				}
			}
			

		}catch (SQLException e) {

			e.printStackTrace();
		}
		return price;

	}
	
	
	/**
	 * Method to update prices of beer
	 * @param beer 
	 * @param price
	 * @return int count of updated rows
	 */
	public int setPrice(String beer, Double price){

		String sql = "UPDATE beers SET beerprice=? "
				+ "WHERE beername= ?" ;
		//ArrayList<ArrayList<String>> res;
		Boolean res = false;
		ArrayList<String> params = new ArrayList<String>();  // wipe out previous params
		String priceupdate = price.toString();
		params.add( priceupdate );   
		params.add( beer );  
		int updated = 0;
		try{
			//res = db.setData( sql );

			updated = db.nonSelect( sql, params);

		}catch (SQLException e) {

			e.printStackTrace();
		}
		return updated;
	}


	/**
	 * @return String name of the cheapest beer
	 */
	public String getCheapestBeer(){

		String sql = "SELECT beername "
				+ "FROM beers "
				+ "WHERE beerprice=(SELECT MIN(beerprice) FROM beers )";

		String cheapestBeer = "";
		try {
			System.out.println(" Sql getCheapestBeer" + sql);
			ArrayList<ArrayList<String>> res = db.getData( sql );


			//did we get a result
			if( res != null)
			{
				for ( ArrayList<String> row: res){
					cheapestBeer = row.get ( 0 );
				}	
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cheapestBeer;
	}



	/** 
	 * @return String name of the costliest beer
	 */
	public String getCostliestBeer(){

		String sql = "SELECT beername, beerprice "
				+ "FROM beers "
				+ "WHERE beerprice=(SELECT MAX(beerprice) FROM beers )";

		String costliestBeer = "";
		ArrayList<ArrayList<String>> res;
		try {
			System.out.println(" Sql getCostliestBeer" + sql);

			res = db.getData( sql );



			//did we get a result
			if( res != null)
			{
				for ( ArrayList<String> row: res){
					costliestBeer = row.get ( 0 );
				}	

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return costliestBeer;
	}

	/**
	 * @param token 
	 * @return String expiration time of toekn in String format
	 */
	public String getTokenExpirationTime(String token){

		String sql = "SELECT expirationDate "
				+ "FROM token "
				+ "WHERE tokenID= ?";

		String tokenTimestamp = null;
		ArrayList<String> params = new ArrayList<String>();
		ArrayList<ArrayList<String>> res;
		params.add( token ); 
		try{

			res = db.getDataPS( sql, params );

			if( res != null)
			{
				for ( ArrayList<String> row: res){
					tokenTimestamp =  row.get ( 0 );
				}
			}
		

		}catch (SQLException e) {

			e.printStackTrace();
		}

		return tokenTimestamp;
	}


	/**
	 * @param token
	 * @return delete entry for token
	 */
	public int deleteToken(String token){

		String sql = "DELETE FROM token WHERE tokenID = ?";
		ArrayList<String> params = new ArrayList<String>();
		int res= 0;

		params.add( token ); 
		
		try{
			
			res = db.nonSelect( sql, params );

		}catch (SQLException e) {

			e.printStackTrace();
		}
		return res;

	}


	/**
	 * @param token username/tokenid
	 * @param istoken Username/Token ID passed in query, True is token id is sent
	 * @return int value user's age
	 */
	public int getUserAge(String token, Boolean istoken){
		String sql;
		if (istoken){

			sql = "SELECT age "
					+ "FROM users "
					+ "WHERE username=(SELECT username FROM token WHERE tokenID =?)";
		}else{

			sql = "SELECT age "
					+ "FROM users "
					+ "WHERE username=?";

		}
		
		int userAge = 0;
		ArrayList<String> params = new ArrayList<String>();
		ArrayList<ArrayList<String>> res;
		params.add( token ); 
		try{

			res = db.getDataPS( sql, params );


			if( res != null)
			{
				for ( ArrayList<String> row: res){
					userAge =  Integer.parseInt(row.get ( 0 ));
				}
			}
			
		}catch (SQLException e) {

			e.printStackTrace();
		}
		return userAge;

	}

	/**
	 * @param token
	 * @return  string access level  of user
	 */
	public String getUserLevel(String token){

		String sql = "SELECT accessLevel "
				+ "FROM users "
				+ "WHERE username=(SELECT username FROM token WHERE tokenID =?)";
		String userAccessLevel = "";
		ArrayList<String> params = new ArrayList<String>();
		ArrayList<ArrayList<String>> res;
		params.add( token ); 
		try{

			res = db.getDataPS( sql, params );


			if( res != null)
			{
				for ( ArrayList<String> row: res){
					userAccessLevel =  row.get ( 0 );
				}
			}
			
		}catch (SQLException e) {

			e.printStackTrace();
		}

		return userAccessLevel;
	}

	/**
	 * Returns vector of beer names
	 * @return Vector of beers name
	 */
	public Vector getBeers(){

		Vector v = new Vector();
		String sql = "SELECT beername FROM beers";
		try{
			ArrayList<ArrayList<String>> res = db.getData( sql ) ;

			if( res != null)
			{
				for ( ArrayList<String> row: res){
					v.add(row.get ( 0 ));
				}
			}
			
		}catch (SQLException e) {

			e.printStackTrace();
		}


		return v;
	}


	/**
	 * @param username
	 * @return token string using username
	 */
	public String getToken(String username){
		String existingToken = null;
		String sql = "SELECT tokenID FROM token "
				+ "WHERE username = ?";

		ArrayList<String> params = new ArrayList<String>();
		ArrayList<ArrayList<String>> res;
		params.add( username ); 
		try{

			res = db.getDataPS( sql, params );

			if( res != null)
			{
				for ( ArrayList<String> row: res){
					existingToken =  row.get ( 0 );
				}
			}

		}catch (SQLException e) {

			e.printStackTrace();
		}

		return existingToken;

	}


}