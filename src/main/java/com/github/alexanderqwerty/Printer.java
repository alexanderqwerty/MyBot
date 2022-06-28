package com.github.alexanderqwerty;

import com.vk.api.sdk.objects.messages.MessageAttachment;

import javax.print.*;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


public class Printer extends Saver {
    private int printerId = 2;
    private String director = getDirector();
    public Printer(){
    }

    public Printer(int printerId, String director) {
        this.printerId = printerId;
        this.director = director;
    }

    public Printer(String director) {
        this.director = director;
    }

    public Printer(int printerId) {
        this.printerId = printerId;
    }
    public static void getPrinterNames() {
        List<PrintService> services = List.of(PrintServiceLookup.lookupPrintServices(null, null));
        services.forEach(service ->{
            System.out.print(service.getName());
        });
    }

    public void printPicture(String file) throws FileNotFoundException, PrintException {
        File f = new File(file);
        FileInputStream inputStream = new FileInputStream(f);
        DocFlavor flavor = DocFlavor.INPUT_STREAM.JPEG;
        PrintService[] printService = PrintServiceLookup.lookupPrintServices(flavor, null);
        DocPrintJob job = printService[printerId].createPrintJob();
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        DocAttributeSet das = new HashDocAttributeSet();
        Doc doc = new SimpleDoc(inputStream, flavor, das);
        job.print(doc, pras);
    }

    public void print(MessageAttachment attachment)  {
        switch (attachment.getType()) {
            case PHOTO -> {
                try {
                    getPhoto(attachment);
                    printPicture(director + attachment.getPhoto().getId() + ".jpeg");
                } catch (PrintException | IOException e){
                    throw new RuntimeException(e);
                }
            }
            default -> {
            }
        }
    }
}
