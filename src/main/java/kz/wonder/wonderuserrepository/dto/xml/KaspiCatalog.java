package kz.wonder.wonderuserrepository.dto.xml;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.List;

@XmlRootElement(name = "kaspi_catalog")
@XmlAccessorType(XmlAccessType.FIELD)
public class KaspiCatalog {
    @XmlElement(name = "company")
    private String company;

    @XmlElement(name = "merchant_id")
    private String merchantid;

    @XmlAttribute
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private final LocalDate date = LocalDate.now();

    @XmlElementWrapper
    @XmlElement(name = "offer")
    private List<Offer> offers;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getMerchantid() {
        return merchantid;
    }

    public void setMerchantid(String merchantid) {
        this.merchantid = merchantid;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

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

        // Геттеры и сеттеры
        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<Availability> getAvailabilities() {
            return availabilities;
        }

        public void setAvailabilities(List<Availability> availabilities) {
            this.availabilities = availabilities;
        }

        public List<CityPrice> getCityprices() {
            return cityprices;
        }

        public void setCityprices(List<CityPrice> cityprices) {
            this.cityprices = cityprices;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Availability {
            @XmlAttribute(name = "available")
            private String available;

            @XmlAttribute(name = "storeid")
            private String storeId;

            // Геттеры и сеттеры
            public String getAvailable() {
                return available;
            }

            public void setAvailable(String available) {
                this.available = available;
            }

            public String getStoreId() {
                return storeId;
            }

            public void setStoreId(String storeId) {
                this.storeId = storeId;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class CityPrice {
            @XmlAttribute(name = "cityid")
            private String cityId;

            @XmlAttribute(name = "price")
            private String price;

            // Геттеры и сеттеры
            public String getCityId() {
                return cityId;
            }

            public void setCityId(String cityId) {
                this.cityId = cityId;
            }

            public String getPrice() {
                return price;
            }

            public void setPrice(String price) {
                this.price = price;
            }
        }
    }
}
