package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "box_type_images", schema = schemaName)
public class BoxTypeImages extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_type_id", nullable = false)
    public BoxType boxType;

    @Column(name = "image_url", nullable = false, unique = true)
    public String imageUrl;
}