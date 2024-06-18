package kz.wonder.wonderuserrepository.config.internalization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
public class LocaleHolder {
    private Locale currentLocale;
}