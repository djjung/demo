package com.example.demo.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Service
public class MyService {
	
	/**
	 * Neflix 회로차단기 샘플
	 * 1. 정상응답시 원래 메소드 실행
	 * 2. 3초 이상 응답 지연시 Fallback 메소드 실행 
	 */
	
	@HystrixCommand(
		commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")
		},
		fallbackMethod = "getFallbackText"
	)
	
	public String getText() {
		runInRandomTime();
		
		return "I am service text";
	}
	
	public String getFallbackText() {
		return "I am fallback text";
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
