package kz.wonder.wonderuserrepository.dto.xml;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.List;

@XmlRootElement(name = "kaspi_catalog")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class KaspiCatalog {

    @XmlAttribute
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private final LocalDate date = LocalDate.now();
    @XmlElement(name = "company")
    private String company;
    @XmlElement(name = "merchant_id")
    private String merchantId;
    @XmlElementWrapper
    @XmlElement(name = "offer")
    private List<Offer> offers;


    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Offer {
        @XmlAttribute(name = "sku")
        private String sku;

        @XmlElement(name = "model")
        private String model;

        @XmlElementWrapper
        @XmlElement(name = "availability")
        private List<Availability> availabilities;

        @XmlElementWrapper
        @XmlElement(name = "cityprice")
        private List<CityPrice> cityprices;


        @Getter
        @Setter
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Availability {

            @XmlAttribute(name = "available")
            private String available;

            @XmlAttribute(name = "storeid")
            private String storeId;

        }

        @Getter
        @Setter
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class CityPrice {
            @XmlAttribute(name = "cityid")
            private String cityId;

            @XmlAttribute(name = "price")
            private String price;


        }
    }
}