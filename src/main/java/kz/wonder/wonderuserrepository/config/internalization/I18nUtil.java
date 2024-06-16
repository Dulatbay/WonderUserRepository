package kz.wonder.wonderuserrepository.config.internalization;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class I18nUtil {

    private final MessageSource messageSource;

    @Resource(name = "localHolder")
    private final LocaleHolder localHolder;

    public String getMessage(String code, String... args){
        return messageSource.getMessage(code, args, localHolder.getCurrentLocale());
    }

}
