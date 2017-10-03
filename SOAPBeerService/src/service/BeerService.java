package service;

import business.*;
import exceptions.BusinessRulesExceptions;

import java.util.Vector;

//Java API for XML Web Services (JAX-WS)
import javax.jws.*;  //have APIs specific to Java for WSDL 

//mappping annotations

@WebService(serviceName="BeerService")
public class BeerService{

	@WebMethod(operationName="GetToken")
	public String getToken(@WebParam(name = "username") String userame,@WebParam(name = "password") String password) throws BusinessRulesExceptions{
		
		BeerBusiness  beerBusinessObj= new BeerBusiness();
		String token = beerBusinessObj.createToken(userame,password);
		return token; 
		
	}
	
	
	@WebMethod(operationName="GetMethods")
	public Vector getMethods(@WebParam(name = "token") String token) throws BusinessRulesExceptions{
		//Call to business layer method 
		BeerBusiness  beerBusinessObj= new BeerBusiness();
		
		Vector methodsVector = beerBusinessObj.getMethods(token);
		return methodsVector;
		
	}
	
	@WebMethod(operationName="SetPrice")
	public Boolean setPrice(@WebParam(name = "beer") String beer,@WebParam(name = "price") Double price,@WebParam(name = "token") String token) throws BusinessRulesExceptions{
		BeerBusiness beerBusinessObj= new BeerBusiness();
		Boolean updated = beerBusinessObj.setPrice(beer, price, token);
		return updated;
	}

	
	@WebMethod(operationName="GetPrice")
	public Double getPrice(@WebParam(name = "beer") String beer,@WebParam(name = "token") String token) throws BusinessRulesExceptions{
		BeerBusiness  beerBusinessObj= new BeerBusiness();
		Double  price = beerBusinessObj.getPrice(beer, token);
		return price;
	}
	
	
	@WebMethod(operationName="BeersList")
	public Vector getBeers(@WebParam(name = "token") String token) throws BusinessRulesExceptions{
		BeerBusiness  beerBusinessObj= new BeerBusiness();
		Vector beerVector = beerBusinessObj.getBeers(token);
		return beerVector;
	}
	
	@WebMethod(operationName="CheapestBeer")
	public String getCheapest(@WebParam(name = "token") String token) throws BusinessRulesExceptions{
		BeerBusiness  beerBusinessObj= new BeerBusiness();
		String cheapest = beerBusinessObj.getCheapestBeer(token);
		return cheapest;
		
	}
	
	@WebMethod(operationName="CostliestBeer")
	public String getCostliest(@WebParam(name = "token") String token) throws BusinessRulesExceptions{
		BeerBusiness  beerBusinessObj= new BeerBusiness();
		String costliestBeer = beerBusinessObj.getCostliestBeer(token);
		return costliestBeer;
	}
}
