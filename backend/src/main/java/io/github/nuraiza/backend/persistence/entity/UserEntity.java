package io.github.nuraiza.backend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CollectionEntity> collections = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClothingItemEntity> clothes = new HashSet<>();

    public UserEntity(String name){
        this.name = name;
    }

    public void addCollection(CollectionEntity collection) {
        collections.add(collection);
        collection.setUser(this);
    }

    public void removeCollection(CollectionEntity collection) {
        collections.remove(collection);
        collection.setUser(null);
    }

    public void addClothingItem(ClothingItemEntity item) {
        clothes.add(item);
        item.setUser(this);
    }

    public void removeClothingItem(ClothingItemEntity item) {
        clothes.remove(item);
        item.setUser(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity)) return false;
        UserEntity that = (UserEntity) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
