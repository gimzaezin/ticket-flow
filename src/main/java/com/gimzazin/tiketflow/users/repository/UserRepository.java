package com.gimzazin.tiketflow.users.repository;

import com.gimzazin.tiketflow.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
