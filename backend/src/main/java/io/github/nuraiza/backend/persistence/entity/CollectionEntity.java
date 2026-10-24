package io.github.nuraiza.backend.persistence.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "collection")
@NoArgsConstructor
@Getter
@Setter
public class CollectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToMany
    @JoinTable(name = "capsule",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "clothing_item_id"))
    private Set<ClothingItemEntity> clothes = new HashSet<>();

    public CollectionEntity(String name, String description, UserEntity user){
        this.name = name;
        this.description = description;
        this.user = user;
    }

    public void addClothingItem(ClothingItemEntity item) {
        this.clothes.add(item);
        item.getCollections().add(this);
    }

    public void removeClothingItem(ClothingItemEntity item) {
        this.clothes.remove(item);
        item.getCollections().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectionEntity)) return false;
        CollectionEntity col = (CollectionEntity) o;
        return id != null && id.equals(col.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
