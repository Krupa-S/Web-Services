package data;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TokenDTO  implements Serializable{
	LocalDateTime expirationTime;

	public LocalDateTime getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(LocalDateTime expirationTime) {
		this.expirationTime = expirationTime;
	}
	

}
