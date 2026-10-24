package io.github.nuraiza.backend.web.mapper;

import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.web.dto.UserDtos;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDtos.Response toResponse(UserEntity userEntity){
        if (userEntity == null) return null;
        return new UserDtos.Response(
                userEntity.getId(),
                userEntity.getName()
        );
    }
}
