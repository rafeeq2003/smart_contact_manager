package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRespository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRespository contactRespository;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@ModelAttribute
	public void addCommonDetails(Model model, Principal principal) {
		String userName = principal.getName();

		User user =  this.userRepository.getUserByUsername(userName);

		System.out.println(user);

		model.addAttribute("user", user);
	}

	@GetMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "User Dashboard");

		return "user/user_dashboard";
	}

	@GetMapping("/add_contact")
	public String showContact(Model model) {
		model.addAttribute("title", "Add contact");
		model.addAttribute("contact", new Contact());

		return "user/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String addContact(@Valid @ModelAttribute Contact contact, BindingResult bindingResult,
			@RequestParam("profileImage")MultipartFile file, 
			Model model, Principal principal, HttpSession session) {

		try {

			if(bindingResult.hasErrors()) {
				if(!bindingResult.getFieldError().getField().equalsIgnoreCase("profileImage")) {
					model.addAttribute("contact", contact);
					return "user/add_contact_form";
				}
			}

			if(file.isEmpty()) {
				System.out.println("File is empty");
				contact.setProfileImage("user.png");
			} else  {
				contact.setProfileImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("/static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("File is uploaded");
			}

			String userName = principal.getName();

			User user = userRepository.getUserByUsername(userName);

			contact.setUser(user);

			user.getContacts().add(contact);

			this.userRepository.save(user);

			session.setAttribute("message", new Message("New Contact Created..", "alert-success"));

			model.addAttribute("contact", contact);

		} catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("contact", contact);
			session.setAttribute("message", new Message("Something went worng :: "+e.getLocalizedMessage(), "alert-error"));
		} 

		return "redirect:/user/add_contact";
	}

	@GetMapping("/show_contacts/{page}")
	public String showAllContacts(@PathVariable("page")Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show Contacts");
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUsername(userName);
		
		Pageable pageable = PageRequest.of(page, 4);
		
		Page<Contact> contactsList = this.contactRespository.findContactByUser(user.getId(), pageable);
		
		if(!contactsList.isEmpty()) {
			model.addAttribute("contactsList", contactsList);
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", contactsList.getTotalPages());
		}
		
		return "/user/show_contacts";
	}

	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer id, Model model, Principal principal) {
		model.addAttribute("title", "User Details"); 
		
		Contact contact = this.contactRespository.findById(id).get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUsername(userName);
		
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);			
		}
	
		
		return "/user/contact_details";
	}
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer id, Model model, Principal principal, HttpSession session) {
		model.addAttribute("title", "Delete Contact"); 

		Contact contact = this.contactRespository.findById(id).get();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUsername(userName);

		if(user.getId() == contact.getUser().getId()) {

			this.contactRespository.deleteContactById(contact.getcId());

			session.setAttribute("message", new Message("Contact Delete Successfully...", "alert-success"));
		}


		return "redirect:/user/show_contacts/0";
	}

	@PostMapping("/update-contact/{cId}")
	public String updateContact(@PathVariable("cId") Integer id, Model model) {
		model.addAttribute("title", "Update Contact");

		Contact contact = this.contactRespository.findById(id).get();

		model.addAttribute("contact", contact);

		return "/user/update_contact";
	}

	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute Contact contact, BindingResult bindingResult,
			@RequestParam("profileImage")MultipartFile file, 
			Model model, Principal principal, HttpSession session) {

		try {
			
			if(bindingResult.hasErrors()) {
				int c = bindingResult.getFieldErrorCount();
				if(!bindingResult.getFieldError().getField().equalsIgnoreCase("profileImage")) {
					model.addAttribute("contact", contact);
					return "redirect:/user/update_contact";
				}
			}
			
			Contact oldContact = this.contactRespository.findById(contact.getcId()).get();
			
			if(file.isEmpty()) {
				contact.setProfileImage(oldContact.getProfileImage());
			} else  {
				
				// Delete existing file
				File delFile = new ClassPathResource("/static/img").getFile();
				File delFile2 = new File(delFile, oldContact.getProfileImage());
				delFile2.delete();
				
				// Save new file
				File saveFile = new ClassPathResource("/static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setProfileImage(file.getOriginalFilename());
				System.out.println("File is uploaded");
			}
			
			User user = this.userRepository.getUserByUsername(principal.getName());
			
			contact.setUser(user);
			
			this.contactRespository.save(contact);
			
			session.setAttribute("message", new Message("Contact Updated Successfully!!!", "alert-success"));
			
		} catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("contact", contact);
			session.setAttribute("message", new Message("Something went worng :: "+e.getLocalizedMessage(), "alert-error"));
		} 

		return "redirect:/user/"+contact.getcId()+"/contact";
		
	}

	@GetMapping("/user-profile")
	public String userProfile(Model model) {
		
		model.addAttribute("title", "Your Profile");
		
		return "user/user_profile";
	}
	
	@GetMapping("/user-settings")
	public String userSetting(Model model) {
		
		model.addAttribute("title", "Settings");
		
		return "user/user_settings";
	}
	
	@PostMapping("/change-password")
	public String userChangePassword(@RequestParam("oldPassword") String oldPassword, 
			@RequestParam("newPassword") String newPassword, Model model, 
			Principal principal, HttpSession session) {
		
		model.addAttribute("title", "Change Password");
		
		User user = this.userRepository.getUserByUsername(principal.getName());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Password Updated Successfully!!!", "alert-success"));
		} else {
			session.setAttribute("message", new Message("Please enter correct current password...", "alert-danger"));
			return "redirect:/user/user-settings";
		}
		
		return "redirect:/user/index";
	}
	

}