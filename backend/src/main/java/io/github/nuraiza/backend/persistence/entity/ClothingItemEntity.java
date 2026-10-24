package io.github.nuraiza.backend.persistence.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clothing_item")
@NoArgsConstructor
@Getter
@Setter
public class ClothingItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToMany(mappedBy = "clothes")
    private Set<CollectionEntity> collections = new HashSet<>();

    public ClothingItemEntity(String name, String description, UserEntity user) {
        this.name = name;
        this.description = description;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClothingItemEntity)) return false;
        ClothingItemEntity col = (ClothingItemEntity) o;
        return id != null && id.equals(col.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
