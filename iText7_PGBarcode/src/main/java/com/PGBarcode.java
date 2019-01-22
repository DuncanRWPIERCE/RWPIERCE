
/**
 *  iText Barcode project
 *  Using iText7 OpenSource Libraries to merge and apply bar-codes to pdf documents.
 *
 *   Copyright (C) 2019 RW Pierce
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see https://www.gnu.org/licenses/.
 *
 **/
package com;

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
        import java.util.ArrayList;
        import java.util.Date;

//@WrapToTest
public class PGBarcode {

    private static int k= 1, //initialise total page count
            l= 1, //initialise total duplex page count
            m= 1; //initialise total duplex side count
    private static String in,
            path = "C:/defaultFolder/";
    private static ArrayList<String[]> AL = new ArrayList<String[]>();
    private static BufferedWriter logWrtr;
    static final String NL = System.getProperty("line.separator");
    private static boolean pathBool = false;    //stores if previous log of same name detected


    public static void main(String[] args) throws IOException {
        in = args[0];
        ArrayList<Integer> bs = new ArrayList<Integer>(),
                cs = new ArrayList<Integer>(),
                bd = new ArrayList<Integer>(),
                cd = new ArrayList<Integer>();
        Logger.getRootLogger().setLevel(Level.OFF); //Prevents log4j (a dependency of itextpdf.kernel) from issuing irrelevant warnings
        String line, //define string to store line read from input csv
                fName; //string to store pack pdf path and name
//        String[] splitLine; // String array to store tokenised line from csv
//        String[][] csv; // 2D String array to store array of tokenised lines from csv
        int i = 0;
        Date start = new Date(),  //record start time
                end;    //will hold end time
        File input = new File(args[0]), //define and initialise first argument string as a File object
                logFile = new File(("iTextLogs/" + input.getName().substring(0, args[0].lastIndexOf(".")) + ".log")); //define and initialise log as a File object
        logFile.getParentFile().mkdir();   //makes log folder at inputFile location if it does not already exist
        //path assigned
        boolean logBool = logFile.createNewFile();  //creates new logfile if not already existing
        logWrtr = new BufferedWriter(new FileWriter(logFile, true)); //writes to logfile
        if (!logBool) { //if logfile already existed
            logWrtr.append(NL)  //newline to logfile
                    .append(NL);
            for (int o = 0; o < 50; o++) logWrtr.append("~ ");  //write visual separator to log file
            logWrtr.append(NL)
                    .append(NL)
                    .append("New instance detected").append(NL);    //write line to logfile
        }
        if (args.length == 2) {    //if path argument detected
            path = args[1] + ((args[1].substring(args[1].length() - 1).contains("/")) ? "" : "/");  //adds "/" character to the end of path argument if it does not already end with it
            new File(path + "processed").mkdirs();    //make processed folder if it does not already exist
            pathBool = true;    //records path argument detected - will be used later to decide whether to move files to processed
        }
        if (input.exists()) {    //if file specified as first argument exists
            System.out.println(start.toString() + NL + // timestamp to console
                    input.getName() + " processing...");  //filename processing
            logWrtr.append(input.getName()) //input fileName to logfile
                    .append(" detected as input at ")
                    .append(start.toString()).append(NL)//timestamp
                    .append(NL); //to log
        } else {
            System.out.println("[ERROR]: " + input.getName() + " - file not found!"); //to log
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
//        if (input.getName().contains("Combined_output_Escapee")){

        br.readLine();//skip csv header
        while (!((line = br.readLine()) == null)) { //assigns next line of csv to the "line" String variable.
            // If this is not null then perform the following {function} and loop.
//                System.out.println(line); //diagnostic
            AL.add(line.split("\",\""));    //tokenise line of csv based on the regex for delimiter (",") and store as String array
            if (AL.get(AL.size() - 1)[7].equals("1SIDE") && AL.get(AL.size() - 1)[8].equals("BW")) bs.add(i);
            else if (AL.get(AL.size() - 1)[7].equals("1SIDE") && AL.get(AL.size() - 1)[8].equals("COLOR")) cs.add(i);
            else if (AL.get(AL.size() - 1)[7].equals("2SIDE") && AL.get(AL.size() - 1)[8].equals("BW")) bd.add(i);
            else if (AL.get(AL.size() - 1)[7].equals("2SIDE") && AL.get(AL.size() - 1)[8].equals("COLOR")) cd.add(i);
            else { //neither simplex nor duplex flags detected in correct field of csv line
                fName = path + AL.get(AL.size() - 1)[0].substring(1);
                System.out.println("[ERROR]: " + fName + " not defined as simplex/duplex or Colour/BW"); //to console
                logWrtr.append("[ERROR]: ")
                        .append(fName)
                        .append(" not defined as simplex/duplex or Colour/BW")
                        .append(NL)
                        .append(new Date().toString())//timestamp
                        .append(NL)
                        .close(); //to log
                System.exit(-3);//errorCode+exit
            }
            i++;
        }
        br.close();
        if (!bs.isEmpty()) PGBarcode.process ("_black_simplex.pdf", bs);
        if (!cs.isEmpty()) PGBarcode.process ("_colour_simplex.pdf", cs);
        if (!bd.isEmpty()) PGBarcode.process ("_black_duplex.pdf", bd);
        if (!cd.isEmpty()) PGBarcode.process ("_colour_duplex.pdf", cd);
        if (AL.isEmpty()){//if no flags detected ie: incorrect csv fields
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
        end = new Date();
        long runTime = (end.getTime()-start.getTime())/1000;
        System.out.println(input.getName()+" successfully processed"+NL+"iTextBarcode runTime: "+runTime+" seconds");
        logWrtr.append(NL)
                .append(input.getName()).append(" successfully processed").append(NL)
                .append("iTextBarcode Program end: ").append(end.toString()).append(NL)
                .append("runTime: ").append(String.valueOf(runTime)).append(" seconds").append(NL)
                .close();
    }
    private static void process(String suffix, ArrayList<Integer> Int) throws IOException { // packs processed
        Boolean simp = suffix.contains("simplex");
        String outName = in.substring(0, in.lastIndexOf(".")) + suffix; //define and initialise output file name
        PdfDocument merged = new PdfDocument(new PdfWriter(outName).setSmartMode(true)),
                temp; //define+initialise Simplex merged pdf
        merged.setFlushUnusedObjects(true);  //delete unused objects in pdf
        PdfMerger merger = new PdfMerger(merged);   //define+initialise Simplex pdf merger
        String fName,
                fNameBC;
        File fFile;
        for (Integer I : Int) {
            fName = path + AL.get(I)[0].substring(1);
//            System.out.println(simp+" "+fName+" "+suffix); //diagnostic
            fFile = new File(fName);   //define file for moving later
            fNameBC = fName.substring(0, fName.lastIndexOf(".")) + "_Barcoded.pdf"; //set output pack pdf name and path to the same as the input but
            // replace extension with (_Barcoded.pdf)
            File fFileBC = new File(fNameBC);   //define processed file for moving later
            if (fFile.exists()) {
                if (simp)new PGBarcode().manipulatePDF(fName, AL.get(I)[5]); //use this.manipulatePDF method (defined after main method in this class)
                else new PGBarcode().manipulateDuplexPDF(fName, AL.get(I)[5]); //use this.manipulatePDF method (defined after main method in this class)
                // taking the input file namePath and the packSeq from csv as arguments
                temp = new PdfDocument(new PdfReader(fNameBC)); //read barcoded pack pdf to temp pdfDocument object
                merger.merge(temp, 1, temp.getNumberOfPages()); // merge all pages to merger
                temp.close(); //close temp PdfDoc object
                logWrtr.append(fNameBC).append(" successfully created")
                        .append(NL);
                if (pathBool){ //if path is specified by second argument then move input and intermediary pdf files to path/processed folder
//                    fFile.renameTo(new File(path + "processed/" + fFile.getName())); //devtime
                    fFileBC.renameTo(new File(path + "processed/" + fFileBC.getName()));
                }
            } else {//file from csv does not exist at the location
                System.out.println("[ERROR]: " + fName + " - file not found"); //to log
                logWrtr.append("[ERROR]: ")
                        .append(fName)
                        .append(" - file not found")
                        .append(NL)
                        .append(new Date().toString())//timestamp
                        .append(NL)
                        .close(); //to log
                System.exit(-2);//errorCode+exit
            }
        }
        k= 1; //reset total page count
        l= 1; //reset total duplex page count
        m= 1; //reset total duplex side count
        merger.close(); //close simplex merger
        merged.close();  //close simplex merged pdf
        logWrtr.append(NL)
                .append(outName).append(" successfully created")
                .append(NL); //log
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
            if (rotation==0)canvas.saveState().concatMatrix(0,-1.395,.78,0,2.8, 538.1);
            else canvas.saveState().concatMatrix(0,1.395,-.78,0,pageSize.getWidth()-2.8, pageSize.getHeight()-538.1);
//            if (rotation==0)canvas.saveState().concatMatrix(0,1.445,.8,0,2.5, 500.5); //MailsyncPos
//            else canvas.saveState().concatMatrix(0,-1.45,-.8,0,pageSize.getWidth()-2.5, pageSize.getHeight()-500); //MailsyncPos
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

            doc.setProperty(Property.FONT_SIZE, 7);
            if (rotation==0) doc.showTextAligned(new Paragraph(code+"- " + k + " - "+i + "/" + n +" - "+packSeq),
                    pageSize.getWidth()-7, 12, i,TextAlignment.LEFT, VerticalAlignment.BOTTOM,(float)Math.PI/2);
            else doc.showTextAligned(new Paragraph(k + "   "+packSeq+"  -  " + i + " of " + n+"\t"+code),
                    7, pageSize.getHeight()-12, i, TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float)-Math.PI/2);
//            doc.setProperty(Property.FONT_SIZE, 6);
//            if (rotation==0) doc.showTextAligned(new Paragraph(k + "   "+packSeq+"  -  " + i + " of " + n +"\t"+code),
//                    pageSize.getWidth()-10, 28, i,TextAlignment.LEFT, VerticalAlignment.BOTTOM,(float)Math.PI/2);
//            else doc.showTextAligned(new Paragraph(k + "   "+packSeq+"  -  " + i + " of " + n+"\t"+code),
//                    10, pageSize.getHeight()-28, i, TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float)-Math.PI/2);


            //2D-Code

            if (i==1) {
                canvas.saveState().concatMatrix(1, 0, 0, 1, 53.85, 671.3);
//                canvas.saveState().concatMatrix(1, 0, 0, 1, 212, 692.85); //MailsyncPos
                BarcodeDataMatrix datMat = new BarcodeDataMatrix(packSeq);
                datMat.placeBarcode(canvas, null, 1.253f);
//                datMat.placeBarcode(canvas, null, 1.65f); //MailsyncPos
                canvas.restoreState()
                        .beginText().setFontAndSize(PdfFontFactory.createFont(FontConstants.HELVETICA), 4)
                        .moveText(54, 691.38)
//                        .moveText(206, 718.2) //MailsyncPos
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
                if (rotation==0)canvas.saveState().concatMatrix(0,-1.395,.78,0,2.8, 538.1);
                else canvas.saveState().concatMatrix(0,1.395,-.78,0,pageSize.getWidth()-2.8, pageSize.getHeight()-538.1);
//                if (rotation == 0) canvas.saveState().concatMatrix(0, 1.445, .8, 0, 2.5, 500.5);
//                else canvas.saveState().concatMatrix(0, -1.45, -.8, 0, pageSize.getWidth() - 2.5, pageSize.getHeight() - 500);
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
                    canvas.saveState().concatMatrix(1, 0, 0, 1, 53.85, 671.3);
//                canvas.saveState().concatMatrix(1, 0, 0, 1, 212, 692.85); //MailsyncPos
                    BarcodeDataMatrix datMat = new BarcodeDataMatrix(packSeq);
                    datMat.placeBarcode(canvas, null, 1.253f);
//                datMat.placeBarcode(canvas, null, 1.65f); //MailsyncPos
                    canvas.restoreState()
                            .beginText().setFontAndSize(PdfFontFactory.createFont(FontConstants.HELVETICA), 5)
                            .moveText(54, 691.38)
//                        .moveText(206, 718.2) //MailsyncPos
                            .showText(packSeq)
                            .endText();
                }
                //FrontPageNo
                canvas.release();
                doc.setProperty(Property.FONT_SIZE, 6);
                if (rotation==0) doc.showTextAligned(new Paragraph(code+"- " + m + " - "+((i+1)/2) + "/" + ((n+1)/2) +" - "+packSeq),
                        pageSize.getWidth()-7, 12, i,TextAlignment.LEFT, VerticalAlignment.BOTTOM,(float)Math.PI/2);
                else doc.showTextAligned(new Paragraph(code+"- " + m + " - "+((i+1)/2) + "/" + ((n+1)/2) +" - "+packSeq),
                        7, pageSize.getHeight()-12, i, TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float)-Math.PI/2);
            }
            else{
                //BackPageNo
                doc.setProperty(Property.FONT_SIZE, 6);
                if (rotation==0) doc.showTextAligned(new Paragraph(code+"- " + m + " - "+((i+1)/2) + "/" + ((n+1)/2)),
                        pageSize.getWidth()-7, 12, i,TextAlignment.LEFT, VerticalAlignment.BOTTOM,(float)Math.PI/2);
                else doc.showTextAligned(new Paragraph(code+"- " + m + " - "+((i+1)/2) + "/" + ((n+1)/2)),
                        7, pageSize.getHeight()-12, i, TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float)-Math.PI/2);
//            doc.setProperty(Property.FONT_SIZE, 6);
//            if (rotation==0) doc.showTextAligned(new Paragraph(m + "   "+packSeq+"  -  " + i + " of " + n
//                            +"\t"+code//HR 1D-Barcode
////                    +"\t"+rotation//HR PageRotation
//                    ),
//                    pageSize.getWidth()-10, 28, i,
//                    TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float)Math.PI/2);
//            else doc.showTextAligned(new Paragraph(m + "   "+packSeq+"  -  " + i + " of " + n
//                            +"\t"+code//HR 1D-Barcode
////                    +"\t"+rotation//HR PageRotation
//                    ),
//                    10, pageSize.getHeight()-28, i,
//                    TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float)-Math.PI/2);
            }
            m++;


        }
//        merger.merge(pdf,1,n); //Cannot copy indirect object from the document that is being written.
        doc.close();
        pdf.close();
    }
}
