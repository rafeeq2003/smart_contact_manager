package com.smart.controller;

import java.util.Objects;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.services.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgetPasswordController {

	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@GetMapping("/fogot-password")
	public String openEmailForm(Model model) {

		model.addAttribute("title", "Forgot Password");
		
		return "fogot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email, Model model, HttpSession session) {
		
		model.addAttribute("title", "Forgot Password");
		
		User user = this.userRepository.getUserByUsername(email);
		
		if(Objects.isNull(user)) {
			session.setAttribute("message", "User is not resgistered with this email!!");	
			
			return "fogot_email_form";
		} else {

			int otp = random.nextInt(999999);

			String subject = "OTP form Smart Contact Manager";

			String message = "<div style='border:1px solid #e2e2e2; padding:20px;'>"
					+ "<h1>OTP is <b>"
					+ otp + "</b></h1></div>";

			String to = email;

			boolean isMailSent = this.emailService.sendMail(to, subject, message);

			if(isMailSent) {
				session.setAttribute("user-otp", otp);
				session.setAttribute("email", email);

				return "verify_otp";
			} else {
				session.setAttribute("message", "Check your email inbox..");

				return "fogot_email_form";
			}
		}
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, Model model, HttpSession session) {
		
		model.addAttribute("title", "Verify OTP");
		
		int userOtp = (int) session.getAttribute("user-otp");
		
		String email = (String) session.getAttribute("email");
		
		if(userOtp == otp) {
			
			User user = this.userRepository.getUserByUsername(email);
			
			if(Objects.isNull(user)) {
				session.setAttribute("message", "User is not resgistered with this email!!");	
				return "fogot_email_form";
			}
				
			return "password_change_form";
		} else {
			session.setAttribute("message", "You have entered wrong otp !!");
			
			return "verify_otp";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassord(@RequestParam("newpassword") String newPassword, Model model, HttpSession session) {
		model.addAttribute("title", "Change Password");
		
		String email = (String) session.getAttribute("email");
		
		User user = this.userRepository.getUserByUsername(email);
		
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		
		this.userRepository.save(user);
		
		return "redirect:/login?change=Password changes sucessfully";
	}
}
