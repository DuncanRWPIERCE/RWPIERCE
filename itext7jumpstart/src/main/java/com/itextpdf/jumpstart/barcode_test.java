/**
 *
 *  iText Barcode project
 *
 *  Using: iText7 OpenSource Libraries
 *
 *  This program is designed to apply 1d and 2d bar-codes with a human readable page number and sequence number to both
 *  simplex and duplex pdf documents for printing and return tracking purposes.
 *
 *   Copyright (C) 2019 RW Pierce
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 *  https://www.gnu.org/licenses/ .
 *
 **/

package com.itextpdf.jumpstart;

import com.itextpdf.barcodes.Barcode39;
import com.itextpdf.barcodes.BarcodeDataMatrix;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.Property;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Date;

//@WrapToTest
public class barcode_test {

    private static int k= 1, //initialise total page count
            l= 1, //initialise total duplex page count
            m= 1; //initialise total duplex side count

    public static void main(String[] args) throws IOException {
        boolean simp = false,   //define + initialise boolean indicating whether a simplex pack pdf has been detected
                dup = false,    //and for duplex
                pathBool = false,    //path assigned
                logBool;    //stores if previous log of same name detected
        Logger.getRootLogger().setLevel(Level.OFF); //Prevents log4j (a dependency of itextpdf.kernel) from issuing irrelevant warnings
        String outName = args[0].substring(0, args[0].lastIndexOf("."))+"_Simplex.pdf", //define and initialise simplex output file name
                outNameB = args[0].substring(0, args[0].lastIndexOf("."))+"_Duplex.pdf", //define and initialise simplex output file name
//                logName = args[0].substring(0, args[0].lastIndexOf("."))+".log", //define and initialise log file name
                line, //define string to store line read from input csv
                path = "C:/Default/", //default path to look in
                fName, //string to store pack pdf path and name
                fNameBC,//stores output pack pdf name
                NL = System.getProperty("line.separator"); //newline
        String[] splitLine; // String array to store tokenised line from csv
        Date start=new Date(),  //record start time
                end;    //will hold end time
        File input = new File(args[0]), //define and initialise first argument string as a File object
                logFile = new File(("iTextLogs/"+input.getName().substring(0, args[0].lastIndexOf("."))+".log")); //define and initialise log as a File object
        logFile.getParentFile().mkdir();   //makes log folder at inputFile location if it does not already exist
        logBool = logFile.createNewFile();  //creates new logfile if not already existing
        FileWriter logWrtr = new FileWriter(logFile, true); //writes to logfile
        if (!logBool){ //if logfile already existed
            logWrtr.append(NL)  //newline to logfile
                    .append(NL);
            for (int o = 0; o < 50; o++) logWrtr.append("~ ");  //write visual separator to log file
            logWrtr.append(NL)
                    .append(NL)
                    .append("New instance detected").append(NL);    //write line to logfile
        }
        if (args.length==2){    //if path argument detected
            path = args[1]+((args[1].substring(args[1].length()-1).contains("/"))?"":"/");  //adds "/" character to the end of path argument if it does not already end with it
            new File(path+"processed").mkdirs();    //make processed folder if it does not already exist
            pathBool = true;    //records path argument detected - will be used later to decide whether to move files to processed
        }
        if (input.exists()){    //if file specified as first argument exists
            System.out.println(start.toString()+NL+ // timestamp to console
                    input.getName()+" processing...");  //filename processing
            logWrtr.append(input.getName()) //input fileName to logfile
                    .append(" detected as input at ")
                    .append(start.toString()).append(NL)//timestamp
                    .append(NL); //to log
        }
        else {
            System.out.println("[ERROR]: "+input.getName() + " - file not found!"); //to log
            logWrtr.append("[ERROR]: ")
                    .append(input.getName())
                    .append(" - file not found!")
                    .append(NL)
                    .append(new Date().toString())//timestamp
                    .append(NL)
                    .close();//to log
            System.exit(-1);    //errorCode+exit
        }
        BufferedReader br = new BufferedReader(new FileReader(input));  //Reader object reads from file object
        PdfDocument mergedPdf = new PdfDocument(new PdfWriter(outName ).setSmartMode(true)), //define+initialise Simplex merged pdf
                mergedPdfD = new PdfDocument(new PdfWriter(outNameB).setSmartMode(true));   //define+initialise Duplex merged pdf
        mergedPdf.setFlushUnusedObjects(true);  //delete unused objects in pdf
        mergedPdfD.setFlushUnusedObjects(true); //delete unused objects in duplex pdf
        PdfMerger merger = new PdfMerger(mergedPdf),    //define+initialise Simplex pdf merger
                mergerD = new PdfMerger(mergedPdfD);    //define+initialise Duplex pdf merger

        br.readLine();//skip csv header
        while (!((line = br.readLine()) == null)) { //assigns next line of csv to the "line" String variable.
            // If this is not null then perform the following {function} and loop.
//                System.out.println(line); //diagnostic
            splitLine = line.split("\",\"");    //tokenise line of csv based on the regex for delimiter (",") and store as String array
            fName = path + splitLine[0].substring(1);    //set input pack pdf path 
            // and set name to first String in String array (minus first character whish is ("))
            File fFile = new File(fName);   //define file for moving later
            fNameBC = fName.substring(0, fName.lastIndexOf(".")) + "_Barcoded.pdf"; //set output pack pdf name and path to the same as the input but
            // replace extension with (_Barcoded.pdf)
            File fFileBC = new File(fNameBC);   //define processed file for moving later
            if (fFile.exists()) { //if file exists
                if (splitLine[7].contains("1SIDE")) { //if the "plex" field contains the simplex flag
                    simp = true; //simplex pdf detected
                    new barcode_test().manipulatePDF(fName, splitLine[5]); //use this.manipulatePDF method (defined after main method in this class)
                    // taking the input file namePath and the packSeq from csv as arguments
                    PdfDocument temp = new PdfDocument(new PdfReader(fNameBC)); //read barcoded pack pdf to temp pdfDocument object
                    merger.merge(temp, 1, temp.getNumberOfPages()); // merge all pages to simplex merger
                    temp.close(); //close temp PdfDoc object
                    logWrtr.append(fNameBC).append(" successfully created")
                            .append(NL);
                } else if (splitLine[7].contains("2SIDE")){ // duplex: as simplex but calls this.manipulateDuplexPdf method and merges to duplex merger
                    dup = true;
                    new barcode_test().manipulateDuplexPDF(fName, splitLine[5]);
                    PdfDocument tempD = new PdfDocument(new PdfReader(fNameBC));
                    mergerD.merge(tempD, 1, tempD.getNumberOfPages());
                    tempD.close();
                    logWrtr.append(fNameBC)
                            .append(" successfully created")
                            .append(NL);
                } else { //neither simplex nor duplex flags detected in correct field of csv line
                    System.out.println("[ERROR]: "+fName + " not defined as \"1SIDE\" for simplex or \"2SIDE\" for duplex"); //to console
                    logWrtr.append("[ERROR]: ")
                            .append(fName)
                            .append(" not defined as \"1SIDE\" for simplex or \"2SIDE\" for duplex")
                            .append(NL)
                            .append(new Date().toString())//timestamp
                            .append(NL)
                            .close(); //to log
                    System.exit(-3);//errorCode+exit
                }
            } else {  //file from csv does not exist at the location
                System.out.println("[ERROR]: "+fName + " - file not found"); //to log
                logWrtr.append("[ERROR]: ")
                        .append(fName)
                        .append(" - file not found")
                        .append(NL)
                        .append(new Date().toString())//timestamp
                        .append(NL)
                        .close(); //to log
                System.exit(-2);//errorCode+exit
            }
            if (pathBool){ //if path is specified by second argument then move input and intermediary pdf files to path/processed folder
                fFile.renameTo(new File(path + "processed/" + fFile.getName()));
                fFileBC.renameTo(new File(path + "processed/" + fFileBC.getName()));
            }
        }
        if (simp) { //simplex packs processed
            merger.close(); //close simplex merger
            mergedPdf.close();  //close simplex merged pdf
            logWrtr.append(NL)
                    .append(outName).append(" successfully created")
                    .append(NL); //log
        }
        else {// no simplex flags
//close merger here? ->error
            mergedPdf.addNewPage();//add blank page (document object needs pages in order to close)
            mergedPdf.close(); //close unused simplex merged pdf
            File sFile = new File(outName );    // define and initialise file object
            if (!sFile.delete()) logWrtr.append(NL)
                    .append("Empty ")
                    .append(outName)
                    .append(" not deleted!")
                    .append(NL); //attempt to delete empty file and log if not successfully deleted
        }
        if (dup) {  //as simplex for merged duplex pdf and duplex merger
            mergerD.setCloseSourceDocuments(true).close();
            mergedPdfD.close();
            logWrtr.append(NL)
                    .append(outNameB).append(" successfully created")
                    .append(NL);
        }
        else {
            mergedPdfD.addNewPage();
            mergedPdfD.close();
            File dFile = new File(outNameB);
            if (!dFile.delete()) logWrtr
                    .append(NL)
                    .append("Empty ")
                    .append(outNameB)
                    .append(" not deleted!")
                    .append(NL);
        }

        if (!simp&&!dup){//if no simplex or duplex flags detected ie: incorrect csv fields
            System.out.println("[ERROR]: No valid input detected in "+args[0]);//log
            logWrtr.append(NL)
                    .append("[ERROR]: No valid input detected in ")
                    .append(args[0])
                    .append(NL)
                    .append(new Date().toString())//timestamp
                    .append(NL)
                    .close();//log
            System.exit(-4);//errorCode+exit
        }

        br.close();//close buffered reader of input file
        end = new Date();
        long runTime = (end.getTime()-start.getTime())/1000;
        System.out.println(input.getName()+" successfully processed"+NL+"iTextBarcode runTime: "+runTime+" seconds");
        logWrtr.append(NL)
                .append(input.getName()).append(" successfully processed").append(NL)
                .append("iTextBarcode Program end: ").append(end.toString()).append(NL)
                .append("runTime: ").append(String.valueOf(runTime)).append(" seconds").append(NL)
                .close();
    }


    private void manipulatePDF(String src, String packSeq) throws IOException { // Applies 1D and 2D barcodes and human readable pack-seq and pageNo to Simplex pack pdf



        String dest = src.substring(0,src.lastIndexOf("."))+"_Barcoded.pdf";
        //Initialize PDF and writer
        PdfDocument pdf = new PdfDocument(new PdfReader(src),new PdfWriter(dest).setSmartMode(true));
        //define and add page
        Document doc = new Document(pdf);
        Rectangle pageSize;
        PdfCanvas canvas;
        //generate code
        //note: if (duplex = true) only  advance count if (pageNo % 2 == 1)


        char[] BCode = {'A','A','A'};
        String code;

        //test 1D-code generation

	

        //detect number of pages
        int n = pdf.getNumberOfPages();
        //for each page (i)
        for (int i = 1; i <= n; i++, k++){
            PdfPage page = pdf.getPage(i);
            pageSize=page.getPageSize();
            int rotation = page.getRotation();
//            page.setRotation(0);  //resets rotation but does not invert orientation
            canvas=new PdfCanvas(page);

            //1D-Barcode
            BCode[0]=(i==n)? 'B':'A';
            BCode[1]=(char)(65+(i-1)%26);
            BCode[2]=(char)(65+(k-1)%26);
            code = ""+BCode[0]+BCode[1]+BCode[2];
            //position and orientation matrix
            if (rotation==0)canvas.saveState().concatMatrix(0,1.445,.8,0,2.5, 500.5);
            else canvas.saveState().concatMatrix(0,-1.45,-.8,0,pageSize.getWidth()-2.5, pageSize.getHeight()-500);
            //barcode object instantiated
            Barcode39 code39 = new Barcode39(pdf);
            //extend and escape string
//            String Ex = Barcode39.getCode39Ex(code); //just seems equal to ("\\J"+code+"\\J"), not desirable.
            //encode and set
            code39.setCode(code);
            //switch off visible text
            code39.setFont(null);
//            code39.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA));//visible text
            //place barcode (default black)
            code39.placeBarcode(canvas, null,null);

            canvas.restoreState();

            //PageNo

            doc.setProperty(Property.FONT_SIZE, 6);
            if (rotation==0) doc.showTextAligned(new Paragraph(k + "   "+packSeq+"  -  " + i + " of " + n
                            +"\t"+code//HR 1D-Barcode
//                    +"\t"+rotation//HR PageRotation
                    ),
                    pageSize.getWidth()-10, 28, i,
                    TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float)Math.PI/2);
            else doc.showTextAligned(new Paragraph(k + "   "+packSeq+"  -  " + i + " of " + n
                            +"\t"+code//HR 1D-Barcode
//                    +"\t"+rotation//HR PageRotation
                    ),
                    10, pageSize.getHeight()-28, i,
                    TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float)-Math.PI/2);

            //2D-Code

            if (i==1) {
                canvas.saveState().concatMatrix(1, 0, 0, 1, 212, 692.85);
                BarcodeDataMatrix datMat = new BarcodeDataMatrix(packSeq);
                datMat.placeBarcode(canvas, null, 1.65f);
                canvas.restoreState()
                        .beginText().setFontAndSize(PdfFontFactory.createFont(FontConstants.HELVETICA), 5)
                        .moveText(206, 718.2)
                        .showText(packSeq)
                        .endText();
            }

            canvas.release();
        }
//        merger.merge(pdf,1,n);
        doc.close();
        pdf.close();
    }
    private void manipulateDuplexPDF(String src, String packSeq) throws IOException {// Applies 1D and 2D barcodes and human readable pack-seq and pageNo to Duplex pack pdf
        String dest = src.substring(0,src.lastIndexOf("."))+"_Barcoded.pdf";
        //Initialize PDF and writer
        PdfDocument pdf = new PdfDocument(new PdfReader(src),new PdfWriter(dest).setSmartMode(true));
        //define and add page
        Document doc = new Document(pdf);
        Rectangle pageSize;
        PdfCanvas canvas;
        //generate code
        //note: if (duplex = true) only  advance count if (pageNo % 2 == 1)


        char[] BCode = {'A','A','A'};
        String code = "" + BCode[0] + BCode[1] + BCode[2];

        //test 1D-code generation

		

        //detect number of pages
        int n = pdf.getNumberOfPages();
        //if number of pages is not equal
        if (n%2!=0){
            //add blank page to end of pack
            pdf.addNewPage();
            n++;
        }
        //for each page (i)
        for (int i = 1, j=1; i <= n; i++){
            PdfPage page = pdf.getPage(i);
            pageSize=page.getPageSize();
            int rotation = page.getRotation();
//            page.setRotation(0);  //resets rotation but does not invert orientation
            if (i%2==1) {
                canvas = new PdfCanvas(page);

                //1D-Barcode
                BCode[0] = (i == n - 1) ? 'B' : 'A';
                BCode[1] = (char) (65 + (j - 1) % 26);
                BCode[2] = (char) (65 + (l - 1) % 26);
                code = "" + BCode[0] + BCode[1] + BCode[2];
                //position and orientation matrix
                if (rotation == 0) canvas.saveState().concatMatrix(0, 1.445, .8, 0, 2.5, 500.5);
                else
                    canvas.saveState().concatMatrix(0, -1.45, -.8, 0, pageSize.getWidth() - 2.5, pageSize.getHeight() - 500);
                //barcode object instantiated
                Barcode39 code39 = new Barcode39(pdf);
                //extend and escape string
//            String Ex = Barcode39.getCode39Ex(code); //just seems equal to ("\\J"+code+"/J"), not desirable.
                //encode and set
                code39.setCode(code);
                //switch off visible text
                code39.setFont(null);
//            code39.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA));//visible text
                //place barcode (default black)
                code39.placeBarcode(canvas, null, null);

                canvas.restoreState();

                j++;
                l++;
                //2D-Code

                if (i==1) {
                    canvas.saveState().concatMatrix(1, 0, 0, 1, 212, 692.85);
                    BarcodeDataMatrix datMat = new BarcodeDataMatrix(packSeq);
                    datMat.placeBarcode(canvas, null, 1.65f);
                    canvas.restoreState()
                            .beginText().setFontAndSize(PdfFontFactory.createFont(FontConstants.HELVETICA), 5)
                            .moveText(206, 718.2)
                            .showText(packSeq)
                            .endText();
                }

                canvas.release();
            }
            //PageNo

            doc.setProperty(Property.FONT_SIZE, 6);
            if (rotation==0) doc.showTextAligned(new Paragraph(m + "   "+packSeq+"  -  " + i + " of " + n
                            +"\t"+code//HR 1D-Barcode
//                    +"\t"+rotation//HR PageRotation
                    ),
                    pageSize.getWidth()-10, 28, i,
                    TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float)Math.PI/2);
            else doc.showTextAligned(new Paragraph(m + "   "+packSeq+"  -  " + i + " of " + n
                            +"\t"+code//HR 1D-Barcode
//                    +"\t"+rotation//HR PageRotation
                    ),
                    10, pageSize.getHeight()-28, i,
                    TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float)-Math.PI/2);
            m++;



        }
//        merger.merge(pdf,1,n); //Cannot copy indirect object from the document that is being written.
        doc.close();
        pdf.close();
    }
}
