package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class MainController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping(value = "/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, 
			@RequestParam(value = "aggrement", defaultValue = "false") boolean aggrement, 
			Model model, HttpSession session) {
		try {

			System.out.println("Agg" + aggrement);
			
			if(!aggrement) {
				throw new Exception("You have not aggreed on terms and conditions");
			}
			
			if(bindingResult.hasErrors()) {
				System.out.println("Errors"+bindingResult.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImgUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			User res = userRepository.save(user);
			
			session.setAttribute("message", new Message("New User Created	", "alert-success"));

			model.addAttribute("user", res);

		} catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went worng :: "+e.getLocalizedMessage(), "alert-error"));
		}

		return "signup";
	}

	@GetMapping("/login")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login");
		
		return "login";
	}
}
