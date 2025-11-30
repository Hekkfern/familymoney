package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Username;
import java.time.Instant;

public record UserData(Username username, Email email, Instant createdAt) {

}
