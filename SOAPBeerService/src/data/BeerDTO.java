package data;

import java.io.*;

class BeerDTO{
	String beername;
	Double beerprices;
	
	public BeerDTO(String beername, Double beerprices) {
		this.beername = beername;
		this.beerprices = beerprices;
	}

	@Override
	public String toString() {
		return "BeerDTO [beername=" + beername + ", beerprices=" + beerprices + "]";
	}
		
}