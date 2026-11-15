package com.scentstation.mapper;

import com.scentstation.dto.response.UserResponse;
import com.scentstation.entity.User;
import com.scentstation.entity.Role;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),

                user.getTenant() != null
                        ? user.getTenant().getId()
                        : null,

                user.getTenant() != null
                        ? user.getTenant().getCode()
                        : null,

                user.getRoles()
                        .stream()
                        .map(Role::getCode)
                        .collect(Collectors.toSet()),

                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
