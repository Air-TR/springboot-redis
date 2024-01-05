package com.tr.springboot.redis.jpa;

import com.tr.springboot.redis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserJpa extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
}
