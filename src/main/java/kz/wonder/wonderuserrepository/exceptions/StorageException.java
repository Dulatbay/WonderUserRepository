package kz.wonder.wonderuserrepository.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class StorageException extends RuntimeException {
	private final String error;
	private final String message;
}
