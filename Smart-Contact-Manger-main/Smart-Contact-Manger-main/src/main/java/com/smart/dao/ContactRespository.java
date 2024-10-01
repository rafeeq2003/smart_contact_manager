package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

import jakarta.transaction.Transactional;

public interface ContactRespository extends JpaRepository<Contact, Integer>{
	
	@Query("select c from Contact c where c.user.id = :userId")
	public Page<Contact> findContactByUser(@Param("userId")int userId, Pageable pageable);
	
	@Modifying
	@Transactional
	@Query("delete from Contact c where c.Id =:id")
	public void deleteContactById(@Param("id") Integer id);
	
	public List<Contact> findByFirstNameContainingAndUser(String name, User user);

}
