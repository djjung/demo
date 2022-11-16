package com.example.demo.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Service
public class MyService {
	
	@HystrixCommand(
		commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")
		}
	)
	
	public String getText() {
		runInRandomTime();
		
		return "I am service text";
	}
	
	private void runInRandomTime() {
		Random rand = new Random();
		
		if(rand.nextInt(3) + 1 == 3) {
			try {
				Thread.sleep(10000);
			}catch(InterruptedException e) {}
		}
	}
	
}
