package kz.wonder.wonderuserrepository.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LanguageConfig {
    public static final List<Locale> LOCALES = Arrays.asList(
            new Locale("en"),
            new Locale("ru"),
            new Locale("kz")
    );
}
