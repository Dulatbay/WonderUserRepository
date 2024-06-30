package kz.wonder.wonderuserrepository.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class DocumentValidator implements ConstraintValidator<ValidDocument, MultipartFile> {

    @Override
    public void initialize(ValidDocument constraintAnnotation) {}

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {

        if (multipartFile == null || multipartFile.isEmpty()) {
            return false;
        }

        return isValidContentType(multipartFile.getContentType());
    }

    private boolean isValidContentType(String contentType) {
        return contentType == null || contentType.trim().isEmpty() ? false :
                contentType.contains("application/pdf");
    }
}
