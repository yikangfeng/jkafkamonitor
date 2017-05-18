package com.elong.hotel.service.daemon.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.symantec.cpe.analytics.core.kafka.KafkaOffsetMonitor;

/**
* @author YiKangFeng.
*/
public class Test {
	static private final Logger LOGGER = LoggerFactory.getLogger(Test.class);
    private int v;
    private String name;
    public Test(int v,String name){
    	this.v=v;
    	this.name=name;
    }
    
	public int getV() {
		return v;
	}

	public void setV(int v) {
		this.v = v;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	Map<Integer,List<Integer>> map=new HashMap<>();
	map.put(1, Arrays.asList(1,2));
	map.put(2, Arrays.asList(3,4));
	System.out.println(JSONObject.toJSONString(map));
	
	System.out.println(Arrays.asList(5));
		
       List<Test> l=new ArrayList<>();
       l.add(new Test(1,"康峰"));
       l.add(new Test(2,"测试"));
       l.add(new Test(2,"3"));
       l.add(new Test(2,"4"));
       long sum= l.stream().filter(test->test.getName().equalsIgnoreCase("kf")).mapToLong(Test::getV).reduce(0,
				(x, y) -> x + y);
      
      
       System.out.println(sum);
       
       
       
       Stream<List<Integer>> inputStream = Stream.of(
    		   Arrays.asList(1),
    		   Arrays.asList(2, 3),
    		   Arrays.asList(4, 5, 6)
    		   );
    		 
       inputStream.flatMap((childList) -> childList.stream()).forEach(System.out::println);
       l.stream().
    		   map(Test::getName).limit(3).skip(1).forEach(System.out::println);
       
       System.out.println("test sorted.");
       l.stream().sorted((p1, p2) -> 
       p1.getName().compareTo(p2.getName())).limit(2).map(p->p.getName()).forEach(System.out::println);
    		  
	}

}

