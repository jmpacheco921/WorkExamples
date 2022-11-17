package edu.cs300;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.Vector;
import java.util.ArrayList;

public class ReportGenerator extends Thread{

	private String name, searchString, outputName;
	private int id, reportCounter;
	private ArrayList<String> records = new ArrayList<String>();
	private ArrayList<String> outputFormats = new ArrayList<String>();

	public ReportGenerator(String reportFile,int reportCounter,int id) {
		try{
			this.id = id;
			this.reportCounter = reportCounter;
			File file = new File (reportFile); //For reading in info about report file
			Scanner scanner = new Scanner(file);
			name = scanner.nextLine();//Title
			searchString = scanner.nextLine();
			outputName = scanner.nextLine();//File name to be created
			while(scanner.hasNext()){
				outputFormats.add(scanner.nextLine()); //Adding row and column formatting
			}
			scanner.close();
		} catch (FileNotFoundException e) {
        	DebugLog.log("Error Attempting to open " + reportFile + ". Exiting Gracefully");
			System.exit(0);
        }
	}

	public void run(){
		MessageJNI.writeReportRequest(id,reportCounter,searchString); // Send request to C
		String newRecord = MessageJNI.readReportRecord(id); //Retrieve records add them to Arraylist
		while(newRecord.length() != 0){
			records.add(newRecord);
			newRecord = MessageJNI.readReportRecord(id); // Read Back reports
		}
		printReport();
	}


	public void printReport(){
		try{
			File newFile = new File(outputName);
			FileWriter writer = new FileWriter(outputName); // Creating outputfile and writer
			int begin, end;
			writer.write(name + "\n");
			for(int i = 0; i < outputFormats.size(); i++){ // Adding Headers to output file
				writer.write(outputFormats.get(i).substring(outputFormats.get(i).indexOf(",")+1));
				if(i != outputFormats.size()-1)
					writer.write("\t");
				else
					writer.write("\n");
			}
			for(int i = 0; i < records.size(); i++){
				for(int j = 0; j < outputFormats.size();j++){ //Formatting Output to file
					begin = Integer.parseInt(outputFormats.get(j).substring(0,outputFormats.get(j).indexOf("-")));
					end = Integer.parseInt(outputFormats.get(j).substring(outputFormats.get(j).indexOf("-")+1,outputFormats.get(j).indexOf(",")));
					writer.write(records.get(i).substring(begin-1,end+1));
					if(j == outputFormats.size()-1 && i == records.size()-1)
						writer.write("\n");
					else if(j == outputFormats.size()-1)
						writer.write("\n");
					else
						writer.write("\t");
				}
			}
			writer.close();
		} catch (Exception e) {
          System.out.println(e.getClass());
        }
	}


}