package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.MyService;
import com.netflix.hystrix.exception.HystrixRuntimeException;

@RestController
public class MyController {
	
	@Autowired
	private MyService myService;
	
	@GetMapping("/test")
	public String test() {
		String result;
		try {
			result = myService.getText();
		}catch(HystrixRuntimeException e) {
			e.printStackTrace();
			result = e.getMessage();
		}
		return result;
	}
}
