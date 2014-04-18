package ProcessData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;




public class ToTSData {
	
	
	public static ArrayList<Integer> JsonToTSData(String jsonFilePath, int sdow, int shour, int edow, int ehour) throws IOException
	{
		ArrayList<Integer> tsArrayList = new ArrayList<Integer>();
		
		ObjectMapper mapper = new ObjectMapper();
		
		BufferedReader breader = new BufferedReader(new FileReader(jsonFilePath));
		
		JsonNode rootNode = mapper.readTree(breader);
		
		for(int d = sdow; d<=edow; d++)
		{
			int sh, eh;
			sh = 0;
			eh = 23;
			
			if(d == sdow)
				sh = shour;
			if(d == edow)
				eh = ehour;
			
			for(int h = sh; h<= eh; h++)
			{
				int number = rootNode.path(Integer.toString(d)).path(Integer.toString(h)).asInt();
				tsArrayList.add(number);
			}
				
		}
		return tsArrayList;
	}
	
	
	public static void main(String[] args) throws IOException{
		ArrayList<Integer> tsdata = JsonToTSData("tsdata/jsonData3", 1, 0, 5, 23);
		System.out.println("length:" + tsdata.size());
		System.out.println(tsdata);
	}
}
