package es.usal.bicoverlapper.data;

/*
 * TestNetAffx.java
 *
 * Created on May 28, 2003, 9:25 AM
 */

import java.util.ArrayList;

import affymetrix.GDACNetAffx.*;

/**
 *	Affymetrix Annotation Reader from ljevon example
 * @author  Rodrigo Santamaría
 */
public class AffyReader{
    
   // public GDACNetAffxAnnotationListing m_List;
   // public  java.awt.List anList;
    
    
    public static void query(String query)
    	{
    	System.out.println("java.library.path:"+System.getProperty("java.library.path"));

    	//System.loadLibrary("GDACNetAffx");
    	 System.load("C:/Archivos de programa/Affymetrix/GDAC NetAffx/GDACNetAffx.dll");
    	 // System.load("C:/Archivos de programa/Java/jre1.6.0_03/bin/GDACNetAffx.dll");
    	// System.load("C:/GDAC/GDACNetAffx.dll");
     	// System.load("C:/GDACNetAffx.dll");
      	  try
        {
        	GDACNetAffxAnnotationListing m_List;
        	//ArrayList<GDACNetAffxAnnotation> anList=new ArrayList<GDACNetAffxAnnotation>();
        	ArrayList<String> anList=new ArrayList<String>();
            	
        	m_List = new GDACNetAffxAnnotationListing();
            m_List.DataDirectory = "C:\\tempAffy\\";
            m_List.ConnectionIdentifier = query;
            m_List.UserName = "USALAMANCA0408";
            m_List.Password = "";
           //m_List.RetrieveAnnotationFile(200);
            m_List.RetrieveAnnotationList();
            long n = m_List.NumAnnotations;
            
            System.out.println("Number of annotations "+n);
            
            GDACNetAffxAnnotation an = new GDACNetAffxAnnotation();
            for (int i=0; i<n; i++)
            {
                an = m_List.GetAnnotation(i);
                anList.add(an.Description);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }        
    	
    	}
    
    /**
     * @param args the command line arguments
     */
    //public static void main(String args[]) {
   //     System.loadLibrary("GDACNetAffxJava");
   // }
    
    
}
