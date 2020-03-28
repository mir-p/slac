// TODO
// FURTHER FUTURE 
// - export / import settings
// - export file list
// - lemma/word choice, collocation measure choice
// SEPARATE APP
// - wrapper for lemmatiser
// - wrapper for scrapper

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;


public class Collocator extends JFrame {

	private JPanel contentPane;
	//private JTextField txtQuery;
	JTextField txtQuery;
	private JTable table;
	private JTextField txtNode;
	private JTextField txtCollocate;
	private JButton btnOk;
	private JButton btnOk_1;
	private JTextField txtPos;
	private JTable freqs;
	private JTextField fileQuery;
	// setting fields
	ArrayList<FileType<File, Boolean>> files = new ArrayList<FileType<File, Boolean>>();
	private String sPos = new String();
	private ArrayList<Object[]> sFreqs = new ArrayList<Object[]>();
	private int sTokenNumber;
	private String sQuery = new String();
	private int sWL;
	private int sWR;
	private ArrayList<Object[]> sTable = new ArrayList<Object[]>();
	private String sNode = new String();
	private int sConcSortPos1;
	private String sConcSortSide1 = new String();
	private int sConcSortPos2;
	private String sConcSortSide2 = new String();
	private String sCollocate = new String();
	private int sConWinL;
	private int sConWinR;
	private ArrayList<Object[]> sConcordances  = new ArrayList<Object[]>();
	private String sFreqlistLemmaSetting = new String();
	private String sCollocLemmaSetting = new String();
	private String sConcLemmaSetting = new String();
	private String sFileviewLemmaSetting = new String();
	private int sCollocMeasure;
	private String sCorpusDir = new String();
	private String sDefaultDir = new String();
	private XPathExpression lemmax;
	
	
	public class FileType<File, Boolean> {
		public File FileObject;
		public Boolean Type;
		public FileType(File file, Boolean b) {
			this.FileObject = file;
			this.Type = b; 
		} 
	}

	
	public static void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
	        double... percentages) {
	    double total = 0;
	    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
	        total += percentages[i];
	    }
	 
	    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
	        TableColumn column = table.getColumnModel().getColumn(i);
	        column.setPreferredWidth((int)
	                (tablePreferredWidth * (percentages[i] / total)));
	    }
	}
	
	public static double log2(double x) {
		return (double) (Math.log(x) / Math.log(2));
	}
	
	public static double logn (double x) {
		if(x == 0) {
			return 0.0;
		} else {
			return Math.log(x);
		}
	}
	
	public Boolean customMatch(String s1, String s2) {
		if(s2.contains("*")) {
			String s = s2.replace("*", "[^\\s]*");
			if(s1.matches(s)) {
				return true;
			} else {
				return false;
			}
		} else {
			if(s1.equals(s2)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	public Set<Entry<String, Integer>> generateFrequencies(String pos) {
		HashMap<String, Integer> uniques = new HashMap();
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).Type == true) {
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setNamespaceAware(true); 
					DocumentBuilder builder;
					builder = factory.newDocumentBuilder();
					Document doc = builder.parse(files.get(i).FileObject.getPath());
					XPathFactory factoryx = XPathFactory.newInstance();
					XPath xpath = factoryx.newXPath();
					Object lemmax = new Object();
					if(sFreqlistLemmaSetting == "lemmas") {
						lemmax = xpath.compile("//@lemma");
					} else {
						lemmax = xpath.compile("//w/text()");
					}
					NodeList lemmas = (NodeList) ((XPathExpression) lemmax).evaluate(doc, XPathConstants.NODESET);
					XPathExpression posx = xpath.compile("//@pos");
					NodeList poss = (NodeList) posx.evaluate(doc, XPathConstants.NODESET);
					for (int j = 0; j < lemmas.getLength(); j++) {
						if(customMatch(poss.item(j).getNodeValue(), pos) | pos.equals("")) {
							if(uniques.get(lemmas.item(j).getNodeValue()) == null) {
								uniques.put(lemmas.item(j).getNodeValue(), 1);
							} else {
								uniques.put(lemmas.item(j).getNodeValue(), uniques.get(lemmas.item(j).getNodeValue())+1);
							}
						}
					}
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
					String str;
					try {
						str = new String(Files.readAllBytes(Paths.get(files.get(i).FileObject.getPath())));
						String[] lemmas = str.replaceAll("\\p{IsPunctuation}", "").split("\\s+");
						for (int j = 0; j < lemmas.length; j++) {
							if(uniques.get(lemmas[j]) == null) {
								uniques.put(lemmas[j], 1);
							} else {
								uniques.put(lemmas[j], uniques.get(lemmas[j])+1);
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}

		Set<Entry<String, Integer>> entries = uniques.entrySet();
		Comparator<Entry<String, Integer>> valueComparator = new Comparator<Entry<String, Integer>>() {
            @Override
            public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
                Integer v1 = e1.getValue();
                Integer v2 = e2.getValue();
                return v2.compareTo(v1);
            }
		};
            List<Entry<String, Integer>> listOfEntries = new ArrayList<Entry<String, Integer>>(entries);
            Collections.sort(listOfEntries, valueComparator);
            LinkedHashMap<String, Integer> sortedByValue = new LinkedHashMap<String, Integer>(listOfEntries.size());
            for(Entry<String, Integer> entry : listOfEntries){
                sortedByValue.put(entry.getKey(), entry.getValue());
            }
            Set<Entry<String, Integer>> entrySetSortedByValue = sortedByValue.entrySet();
		
		return entrySetSortedByValue;
	}
	
	public Set<Entry<String, Double[]>> findCollocations(String query, int swl, int swr, int sCollocMeasure) {
		HashMap<String, int[]> uniques = new HashMap();
		int corpusLength = 0;
		int queryOccurrences = 0;
		int fn = 0;
		for(int i = 0; i < files.size(); i++) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true); 
			DocumentBuilder builder;
			try {
				// XML files - generate lemma list and unique lemma list
				if(files.get(i).Type == true) {
					builder = factory.newDocumentBuilder();
					Document doc = builder.parse(files.get(i).FileObject.getPath());
					XPathFactory factoryx = XPathFactory.newInstance();
					XPath xpath = factoryx.newXPath();
					Object lemmax = new Object();
					if(sFreqlistLemmaSetting == "lemmas") {
						lemmax = xpath.compile("//@lemma");
					} else {
						lemmax = xpath.compile("//w/text()");
					}
					NodeList lemmas = (NodeList) ((XPathExpression) lemmax).evaluate(doc, XPathConstants.NODESET);
					for (int j = 0; j < lemmas.getLength(); j++) {
						if(uniques.get(lemmas.item(j).getNodeValue()) == null) {
							int[] k = new int[6];
							uniques.put(lemmas.item(j).getNodeValue(), k);
						}
					}
					corpusLength += lemmas.getLength();
				// TXT files - generate lemma list and unique lemma list
				} else {
					String str = new String(Files.readAllBytes(Paths.get(files.get(i).FileObject.getPath())));
					String[] lemmas = str.replaceAll("\\p{IsPunctuation}", "").split("\\s+");
					for (int j = 0; j < lemmas.length; j++) {
						if(uniques.get(lemmas[j]) == null) {
							int[] k = new int[6];
							uniques.put(lemmas[j], k);
						}
					}
					corpusLength += lemmas.length;
				}
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SAXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (XPathExpressionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			
		}
		for(int i = 0; i < files.size(); i++) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true); 
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				List<String> bag = new ArrayList<String>();
				if(files.get(i).Type == true) {
					// XML files - generate tokenised representations
					Document doc = builder.parse(files.get(i).FileObject.getPath());
					XPathFactory factoryx = XPathFactory.newInstance();
					XPath xpath = factoryx.newXPath();
					Object lemmax = new Object();
					if(sFreqlistLemmaSetting == "lemmas") {
						lemmax = xpath.compile("//@lemma");
					} else {
						lemmax = xpath.compile("//w/text()");
					}
					NodeList lemmas = (NodeList) ((XPathExpression) lemmax).evaluate(doc, XPathConstants.NODESET);
					for (int j = 0; j < lemmas.getLength(); j++) {
						bag.add(lemmas.item(j).getNodeValue());
					}
				} else {
					// TXT files - generate tokenised representations
					String str = new String(Files.readAllBytes(Paths.get(files.get(i).FileObject.getPath())));
					String[] lemmas = str.replaceAll("\\p{IsPunctuation}", "").split("\\s+");
					for (int j = 0; j < lemmas.length; j++) {
						bag.add(lemmas[j]);
					}
				}
					queryOccurrences += Collections.frequency(bag, sQuery);
					fn =  Collections.frequency(bag, sQuery);
					Iterator itUniques1 = uniques.entrySet().iterator();
					while(itUniques1.hasNext()) {
						Map.Entry pair = (Map.Entry)itUniques1.next();
						int[] v0 = (int[]) pair.getValue();
						int v01 =  Collections.frequency(bag, pair.getKey());
						uniques.put((String) pair.getKey(), new int[]{v0[0], v0[1], v0[2], v01, v0[4], v0[5]});
					}
					
					for (int j = 0; j < bag.size(); j+=1) {
						int wl = j;
						int wr = bag.size();
						if ((wl + sWL + sWR) < bag.size()) {
							wr = wl + sWL + sWR + 1;
						}
						int windowCentre = 0;
						if ((wl + sWL) < bag.size()) {
							windowCentre = wl + sWL;
						} else if((wl + sWL) >= bag.size()) {
							windowCentre = bag.size() - 1;
						}
						/*int wl = 0;
						int wr = bag.size();
						if((j - swl) < 0) {
							wl = 0;
						} else {
							wl = j - swl;
						}
						if((j + swr) > bag.size() ) {
							wr = bag.size();
						} else {
							wr = j + swr;
						}*/
						List<String> window = new ArrayList<String>(bag.subList(wl, wr));
						List<String> windowDc = new ArrayList<String>(window);
						List<String> windowL = new ArrayList<String>();
						List<String> windowR = new ArrayList<String>();
						if(sWL < windowDc.size()) {
							windowDc.remove(sWL);
							windowL = window.subList(0, sWL);
							windowR = window.subList(sWL, window.size()-1);
						} else {
							windowDc.remove(windowDc.size()-1);
							windowL = window.subList(0, window.size()-1);
						}
						
						Iterator itUniques = uniques.entrySet().iterator();
						while(itUniques.hasNext()) {
							Map.Entry pair = (Map.Entry)itUniques.next();
							if (customMatch(bag.get(windowCentre), sQuery) & windowDc.contains(pair.getKey())) {
								int[] val = (int[]) pair.getValue();
								int[] newVal = {val[0]+1, val[1], val[2], val[3], val[4], val[5]};
								if(windowL.contains(pair.getKey())) {
									newVal[4]+=1;	
								} else if(windowR.contains(pair.getKey())) {
									newVal[5]+=1;	
								}
								uniques.put((String) pair.getKey(), newVal);
							} else if (customMatch(bag.get(windowCentre), sQuery) & !(windowDc.contains(pair.getKey()))) {
								int[] val = (int[]) pair.getValue();
								int[] newVal = {val[0], val[1]+1, val[2], val[3], val[4], val[5]};
								uniques.put((String) pair.getKey(), newVal);
							} else if (customMatch(bag.get(windowCentre), (String) pair.getKey()) & !(windowDc.contains(sQuery))) {
								int[] val = (int[]) pair.getValue();
								int[] newVal = {val[0], val[1], val[2]+1, val[3], val[4], val[5]};
								uniques.put((String) pair.getKey(), newVal);
							}
						}
					}
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SAXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (XPathExpressionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			
		}
		HashMap<String, Double[]> lls = new HashMap<String, Double[]>(); 
		Iterator itUniques = uniques.entrySet().iterator();
		while(itUniques.hasNext()) {
			Map.Entry pair = (Map.Entry)itUniques.next();
			int[] vals = (int[]) pair.getValue();
			int a = vals[0]; // fnc
			int b = vals[1];
			int c = vals[2];
			int fc =  vals[3];
			int d = corpusLength - fn - fc;
			int al = vals[4];
			int ar = vals[5];

			double ll = 0.0;
			double mi = 0.0;
			double logRatio = 0.0;
			double ll_mi = 0.0;
			double ts = 0.0;
			
			double da = (double) a;
			double db = (double) b;
			double dc = (double) c;
			double dd = (double) d;
			double dfc = (double) fc;
			double dfn = (double) fn;
			double dcorpusLength = (double) corpusLength;
				ll = 2*( da*logn(da) + db*logn(db) + dc*logn(dc) + dd*logn(dd) - (da+db)*logn(da+db) - (da+dc)*logn(da+dc) - (db+dd)*logn(db+dd) - (dc+dd)*logn(dc+dd) + (da+db+dc+dd)*logn(da+db+dc+dd));
				mi = log2( (da*dcorpusLength) / (dfn*dfc) );
				ts = (da-((dfn*dfc)/dcorpusLength))/Math.sqrt(da);
				if(dc <= 0.0) {
					dc = 0.5;
				}
				logRatio = Math.log((da/fn) / (dc/(dcorpusLength - fn)))/Math.log(2);
				if(a > 0) {
					if(sCollocMeasure == 1) {
						lls.put((String) pair.getKey(), new Double[]{mi, (double) a, (double) al, (double) ar});
					} else if (sCollocMeasure == 2) {
						if(ll > 3.8) {
							lls.put((String) pair.getKey(), new Double[]{mi, (double) a, (double) al, (double) ar});
						}
					} else if (sCollocMeasure == 3) {
						lls.put((String) pair.getKey(), new Double[]{ts, (double) a, (double) al, (double) ar});
					} else if(sCollocMeasure == 4) {
						lls.put((String) pair.getKey(), new Double[]{logRatio, (double) a, (double) al, (double) ar});
					} else {
						lls.put((String) pair.getKey(), new Double[]{ll, (double) a, (double) al, (double) ar});
					}
				}
				 
			//}
		}
		Set<Entry<String, Double[]>> entries = lls.entrySet();
		Comparator<Entry<String, Double[]>> valueComparator = new Comparator<Entry<String, Double[]>>() {
            @Override
            public int compare(Entry<String, Double[]> e1, Entry<String, Double[]> e2) {
                Double v1 = e1.getValue()[0];
                Double v2 = e2.getValue()[0];
                return v2.compareTo(v1);
            }
		};
            List<Entry<String, Double[]>> listOfEntries = new ArrayList<Entry<String, Double[]>>(entries);
            Collections.sort(listOfEntries, valueComparator);
            LinkedHashMap<String, Double[]> sortedByValue = new LinkedHashMap<String, Double[]>(listOfEntries.size());
            for(Entry<String, Double[]> entry : listOfEntries){
                sortedByValue.put(entry.getKey(), entry.getValue());
            }
            return sortedByValue.entrySet();
	//return Set<Entry<String, Double>> collocations;
	}
	
	public List<Object[]> findConcordances(String node, String collocate, int swl, int swr) {
		List<Object[]> els = new ArrayList<Object[]>();
		for(int i = 0; i < files.size(); i++) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true); 
			DocumentBuilder builder;
			try {
				List<String> bag = new ArrayList<String>();
				List<String> bow = new ArrayList<String>();
				if(files.get(i).Type == true) {
					builder = factory.newDocumentBuilder();
					Document doc = builder.parse(files.get(i).FileObject.getPath());
					XPathFactory factoryx = XPathFactory.newInstance();
					XPath xpath = factoryx.newXPath();
					Object lemmax = new Object();
					if(sFreqlistLemmaSetting == "lemmas") {
						lemmax = xpath.compile("//@lemma");
					} else {
						lemmax = xpath.compile("//w/text()");
					}
					NodeList lemmas = (NodeList) ((XPathExpression) lemmax).evaluate(doc, XPathConstants.NODESET);
					XPathExpression wordx = xpath.compile("//w/text()");
					NodeList words = (NodeList) wordx.evaluate(doc, XPathConstants.NODESET);
					for (int j = 0; j < words.getLength(); j++) {
						bag.add(words.item(j).getNodeValue());
					}
					for (int j = 0; j < lemmas.getLength(); j++) {
						bow.add(lemmas.item(j).getNodeValue());
					}
				} else {
					String str = new String(Files.readAllBytes(Paths.get(files.get(i).FileObject.getPath())));
					String[] lemmas = str.replaceAll("\\p{IsPunctuation}", "").split("\\s+");
					for (int j = 0; j < lemmas.length; j++) {
						bag.add(lemmas[j]);
					}
					bow = bag;
				}
				int k = 0;
				for (int j = 0; j < bag.size(); j+=1) {
					int wl = j;
					int wr = bag.size();
					if ((wl + swl + swr) < bag.size()) {
						wr = wl + swl + swr;
					}
					int windowCentre = 0;
					if ((wl + swl) < bag.size()) {
						windowCentre = wl + swl;
					} else if((wl + swl) >= bag.size()) {
						windowCentre = bag.size() - 1;
					}
					List<String> window = new ArrayList<String>();
					window = bag.subList(wl, wr-1);
					List<String> windowl = new ArrayList<String>();
					List<String> windowr = new ArrayList<String>();
					windowl = bag.subList(wl, windowCentre);
					windowr = bag.subList(windowCentre+1, wr);
					
					List<String> wwindowl = new ArrayList<String>();
					List<String> wwindowr = new ArrayList<String>();
					List<String> wwindow = new ArrayList<String>();
					wwindowl = bow.subList(wl, windowCentre);
					wwindowr = bow.subList(windowCentre, wr);
					wwindow = bow.subList(wl,  wr);
					int wcl = bow.get(windowCentre).length();
					if(customMatch(bow.get(windowCentre), node) & (collocate.equals("") | wwindow.contains(collocate))) {
						k++;
						Object[] r = {windowl, bag.get(windowCentre), windowr, files.get(i).FileObject.getPath()};
						els.add(r);
					}
					
				}
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SAXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (XPathExpressionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			
		}
		return els;
	}
	
	public List<String[]> formatConcordances(List<Object[]> unformatted, int s1l, boolean s1s, int s2l, boolean s2s, int winl, int winr) {
		List<String[]> els = new ArrayList();
		Collections.sort(unformatted, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				int s1si = 0;
				int s1l2 = 0;
				if(s1s == false) {
					s1si = 2;
					s1l2 = s1l - 1;
				} else {
					s1l2 = winl - s1l; 
				}
				int cp = ((List<String>) o1[s1si]).get(s1l2).compareToIgnoreCase(((List<String>) o2[s1si]).get(s1l2));
				if(cp == 0 & s2l != 0) {
					int s2si = 0;
					int s2l2 = 0;
					if(s2s == false) {
						s2si = 2;
						s2l2 = s2l - 1;
					} else {
						s2l2 = winl - s2l; 
					}
					int cp2 = ((List<String>) o1[s2si]).get(s2l2).compareToIgnoreCase(((List<String>) o2[s2si]).get(s2l2));
					return cp2;
				} else {
					return cp;
				}
			}
		});
		for(int i = 0; i < unformatted.size(); i++) {
			int wcl = ((String) unformatted.get(i)[1]).length();
			int rwl = 0;
			int rwr = 0;
			if((wcl % 2) == 0) {
				rwl = 50 - (wcl / 2);
				rwr = 50 - (wcl / 2);
			} else {
				rwl = 50 - (int) Math.floor(wcl / 2);
				rwr = 49 - (int) Math.ceil(wcl / 2);
			}
			String beginning0 = String.join(" ", (List<String>) unformatted.get(i)[0]);
			String ending0 = String.join(" " , (List<String>) unformatted.get(i)[2]);
			String beginning = "";
			if (beginning0.length() < 50) {
				beginning = beginning0;
			} else {
				beginning = beginning0.substring(beginning0.length()-rwl, beginning0.length());
			}
			String ending = "";
			if (ending0.length() < 50) {
				ending = ending0;
			} else {
				ending = ending0.substring(0, rwr);
			}
			String[] r1 = {Integer.toString(i), beginning, "<html><b>" + unformatted.get(i)[1] + "</b> " + ending + "</html>", (String) unformatted.get(i)[3]};
			els.add(r1);
		}
		return els;
	}
	
	public String viewFile(String fv, String highlight) {
        String str = new String();
        for(int i = 0; i < files.size(); i++) {
        	if(files.get(i).FileObject.getPath().equals(fv)) {
        		try {
        			if(files.get(i).Type == false) {
        				ArrayList<String> bag = new ArrayList<String>();
        				String[] str1 = new String(Files.readAllBytes(Paths.get(files.get(i).FileObject.getPath()))).split("\\s+");
        				for(int j = 0; j < str1.length; j++) {
    						if(customMatch(str1[j], highlight)) {
    							bag.add("<b>" + str1[j] + "</b>");
    						} else {
    							bag.add(str1[j]);
    						}
        				}
        				str = "<html>" + String.join(" ", (List<String>) bag) + "</html>";
        			} else {
        				ArrayList<String> bag = new ArrayList<String>();
        				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        				factory.setNamespaceAware(true); 
        				DocumentBuilder builder;
        				builder = factory.newDocumentBuilder();
    					Document doc = builder.parse(files.get(i).FileObject.getPath());
    					XPathFactory factoryx = XPathFactory.newInstance();
    					XPath xpath = factoryx.newXPath();
    					XPathExpression wordx = xpath.compile("//w/text()");
    					NodeList words = (NodeList) wordx.evaluate(doc, XPathConstants.NODESET);
    					for (int j = 0; j < words.getLength(); j++) {
    						if(customMatch(words.item(j).getNodeValue(), highlight)) {
    							bag.add("<b>" + words.item(j).getNodeValue() + "</b>");
    						} else {
    							bag.add(words.item(j).getNodeValue());
    						}
    					}
    					str = "<html>" + String.join(" ", (List<String>) bag) + "</html>";
        			}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        return str;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Collocator frame = new Collocator();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	class SaxHandler extends DefaultHandler {
		  public void startElement(String uri, String localName, String qName, Attributes attrs)
		      throws SAXException {
		    if (qName.equals("order")) {
		    }
		  }
		  public void error(SAXParseException ex) throws SAXException {
		    System.out.println("ERROR: [at " + ex.getLineNumber() + "] " + ex);
		  }
		  public void fatalError(SAXParseException ex) throws SAXException {
			  throw new org.xml.sax.SAXParseException("", "", "", 0, 0);
		    //System.out.println("FATAL_ERROR: [at " + ex.getLineNumber() + "] " + ex);
		  }
		  public void warning(SAXParseException ex) throws SAXException {
		    System.out.println("WARNING: [at " + ex.getLineNumber() + "] " + ex);
		  }
		}

	
	/**
	 * Create the frame.
	 */
	public Collocator() throws org.xml.sax.SAXParseException, IOException {
		
		setTitle("SLAC 0.1 (Apr 2020)");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new CardLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane);
		
		JLabel lblWhenGeneratingWord = new JLabel("When generating word lists from lemmatised XML files, account for:");
		
		JComboBox freqlistLemmaSetting = new JComboBox();
		freqlistLemmaSetting.setModel(new DefaultComboBoxModel(new String[] {"lemmas", "words"}));
		
		JLabel lblWhenGeneratingCollocation = new JLabel("When generating collocation lists from lemmatised XML files, account for:");
		
		JComboBox collocLemmaSetting = new JComboBox();
		collocLemmaSetting.setModel(new DefaultComboBoxModel(new String[] {"lemmas", "words"}));
		
		JLabel lblCollocationMeasure = new JLabel("Collocation measure:");
		
		JComboBox collocMeasure = new JComboBox();
		collocMeasure.setModel(new DefaultComboBoxModel(new String[] {"log-likelihood", "MI", "log-likelihood + MI", "T-score", "log-ratio"}));
		
		JLabel lblWhenGeneratingConcordances = new JLabel("When generating concordances from lemmatised XML files, account for:");
		
		JComboBox concLemmaSetting = new JComboBox();
		concLemmaSetting.setModel(new DefaultComboBoxModel(new String[] {"lemmas", "words"}));
		
		JLabel lblWhenQueryingXml = new JLabel("When querying XML files in the File view, look for:");
		
		JComboBox fvLemmaSetting = new JComboBox();
		fvLemmaSetting.setModel(new DefaultComboBoxModel(new String[] {"lemmas", "words"}));
		
		
		JPanel freqlist = new JPanel();
		tabbedPane.addTab("Word list", null, freqlist, null);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_W);
		
		JScrollPane scrollPaneFiles = new JScrollPane();
		DefaultListModel listModel;
		listModel = new DefaultListModel();
		JList listFiles = new JList(listModel);
		DefaultListCellRenderer listFilesRenderer =  (DefaultListCellRenderer) listFiles.getCellRenderer();  
		listFilesRenderer.setHorizontalAlignment(SwingConstants.RIGHT);  
		scrollPaneFiles.setViewportView(listFiles);
		final JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		
		JButton addFiles = new JButton("+ [A]");
		JButton removeFile = new JButton("- [R]");
		
		JComboBox fvPaths = new JComboBox();
		
		addFiles.setMnemonic('A');
		addFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = fc.showOpenDialog(Collocator.this);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		        	for(File file:fc.getSelectedFiles()) {
						try {
			        		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
							factory.setNamespaceAware(true); 
							DocumentBuilder builder;
							builder = factory.newDocumentBuilder();
							//SAXParserFactory factory = SAXParserFactory.newInstance();
							//factory.setValidating(true);
						    //SAXParser parser = factory.newSAXParser();
							SaxHandler handler = new SaxHandler();
							builder.setErrorHandler(handler);
							Document doc = builder.parse(file.getPath());
							//parser.parse(file.getPath(), handler);
							files.add(new FileType(file, true));
							listModel.addElement(file.getPath());
							fvPaths.addItem(file.getPath());
						} catch (org.xml.sax.SAXParseException e1) {
							files.add(new FileType(file, false));
							listModel.addElement(file.getPath());
							fvPaths.addItem(file.getPath());
						} catch (ParserConfigurationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (SAXException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		        	}
		        }
			}
		});
		addFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		removeFile.setMnemonic('R');
		removeFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int a = listFiles.getSelectedIndex();
				if (a >= 0) {
				files.remove(a);
				fvPaths.removeItemAt(a);
				((DefaultListModel) listFiles.getModel()).removeElementAt(a);
				}
			}
		});
		
		String[] tFreqCols = {"Range", "Lexeme", "Count"};
		Object[][] tFreqData = {};
		DefaultTableModel tFreqModel = new DefaultTableModel(tFreqData, tFreqCols);
		freqs = new JTable(tFreqModel);
		JScrollPane scrollPaneFreqs = new JScrollPane();
		scrollPaneFreqs.setViewportView(freqs);		
		scrollPaneFiles.setViewportView(listFiles);
		
		txtPos = new JTextField();
		txtPos.setColumns(10);
		JLabel lblPosFilter = new JLabel("POS filter:");
		
		JLabel lblNoOfTokens = new JLabel("No. of tokens:");
		JLabel tokenNumber = new JLabel("0");
		
		JButton btnGenerate = new JButton("Generate");
		getRootPane().setDefaultButton(btnGenerate);
		btnGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int freqRowCount = tFreqModel.getRowCount();
				for (int i = freqRowCount - 1; i >= 0; i--) {
				    tFreqModel.removeRow(i);
				}
				sFreqs.removeAll(sFreqs);
				sPos = txtPos.getText();
				Set<Entry<String, Integer>> frequencies = generateFrequencies(sPos);
				int k = 1;
				int sum = 0;
	            for(Entry<String, Integer> mapping : frequencies){
	            	sFreqs.add(new Object[]{k, mapping.getKey(), mapping.getValue()});
	                tFreqModel.addRow(new Object[]{k, mapping.getKey(), mapping.getValue()});
	                sum += mapping.getValue();
	                k++;
	            }
	            tokenNumber.setText(Integer.toString(sum));
	            sTokenNumber = sum;
			}
		});
		
		// Export Frequency List
		JButton btnExportFreqlist = new JButton("Export");
		btnExportFreqlist.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 	LocalDateTime dt = LocalDateTime.now();
				    DateTimeFormatter fdt = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
				    String dtf = dt.format(fdt);
				    FileWriter csv;
					try {
						csv = new FileWriter(new File("/home/piotr/nlp/slac/fl_"+dtf+".csv"));
						csv.write("Frequency list,\n");
						csv.write("Lemmas / words," + freqlistLemmaSetting.getSelectedItem() +",\n");
						csv.write("POS Filter," + sPos +",\n");
						csv.write("Token number," + sTokenNumber +",\n");
						//for (int i = 0; i < tFreqModel.getColumnCount(); i++) {
				        //    csv.write(tFreqModel.getColumnName(i) + ",");
				        //}
				        //csv.write("\n");
				        for (int i = 0; i < sFreqs.size(); i++) {
				            for (int j = 0; j < sFreqs.get(i).length; j++) {
				            	csv.write(sFreqs.get(i)[j].toString() + ",");
				            }
				            csv.write("\n");
				        }
				        csv.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}				    
			}
		});
		
		JLabel lblFiles = new JLabel("Files");
		GroupLayout gl_freqlist = new GroupLayout(freqlist);
		gl_freqlist.setHorizontalGroup(
			gl_freqlist.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_freqlist.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_freqlist.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_freqlist.createSequentialGroup()
							.addComponent(addFiles)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblFiles)
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(removeFile, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))
						.addComponent(scrollPaneFiles, GroupLayout.PREFERRED_SIZE, 209, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_freqlist.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_freqlist.createSequentialGroup()
							.addComponent(lblPosFilter)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(txtPos, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 213, Short.MAX_VALUE)
							.addComponent(btnGenerate))
						.addGroup(gl_freqlist.createSequentialGroup()
							.addComponent(lblNoOfTokens)
							.addGap(18)
							.addComponent(tokenNumber)
							.addPreferredGap(ComponentPlacement.RELATED, 316, Short.MAX_VALUE)
							.addComponent(btnExportFreqlist))
						.addComponent(scrollPaneFreqs, GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_freqlist.setVerticalGroup(
			gl_freqlist.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_freqlist.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_freqlist.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnGenerate)
						.addComponent(lblPosFilter)
						.addComponent(addFiles)
						.addComponent(txtPos, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(removeFile)
						.addComponent(lblFiles))
					.addGap(6)
					.addGroup(gl_freqlist.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_freqlist.createSequentialGroup()
							.addComponent(scrollPaneFreqs, GroupLayout.PREFERRED_SIZE, 435, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
							.addGroup(gl_freqlist.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNoOfTokens)
								.addComponent(tokenNumber)
								.addComponent(btnExportFreqlist)))
						.addComponent(scrollPaneFiles, GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE))
					.addGap(74))
		);
		freqlist.setLayout(gl_freqlist);
		
		
		JPanel collocator = new JPanel();
		tabbedPane.addTab("Collocator", null, collocator, null);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_C);
		
		JPanel concordancer = new JPanel();
		tabbedPane.addTab("Concordancer", null, concordancer, null);
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_N);
		
		JScrollPane concordancesScrollPane = new JScrollPane();
		//concordancesScrollPane.setBounds(37, 43, 488, 287);
		concordancer.add(concordancesScrollPane);
		String[] cColumns = {"", "", "", ""};
		Object[][] cData = {};
		DefaultTableModel concordancesModel;
		concordancesModel = new DefaultTableModel(cData, cColumns);
		JTable concordances = new JTable(concordancesModel);
		setJTableColumnsWidth(concordances, 800, 1, 44, 45, 12);
		concordances.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		TableColumnModel concordancesColumnModel = concordances.getColumnModel();
	    TableColumn concordancescolumn1 = concordancesColumnModel.getColumn(1);
	    TableColumn concordancescolumn3 = concordancesColumnModel.getColumn(3);
	    DefaultTableCellRenderer concordancesRenderer = new DefaultTableCellRenderer();
	    concordancesRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
	    concordancescolumn1.setCellRenderer(concordancesRenderer);
	    concordancescolumn3.setCellRenderer(concordancesRenderer);
		concordancesScrollPane.setViewportView(concordances);
		
		// switch to File View
		JLabel lblFilePath = new JLabel("File path:");
		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		concordances.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	if(event.getValueIsAdjusting()) {
	        		String fv = concordances.getValueAt(concordances.getSelectedRow(), 3).toString();
	        		String highlight = concordances.getValueAt(concordances.getSelectedRow(), 2).toString().split("\\s+")[0];
	        		String str = viewFile(fv, highlight);
	        		textPane.setText(str);
    				fvPaths.setSelectedItem(fv);
    				tabbedPane.setSelectedIndex(3);
	        	}
	        }
		});
		
		String[] tColumns = {"Range", "Collocate", "Freq", "Freq (L)", "Freq (R)", "Measure"};
		Object[][] tData = {};
		DefaultTableModel tModel = new DefaultTableModel(tData, tColumns);
		
		txtNode = new JTextField();
		txtNode.setText("");
		txtNode.setColumns(10);
		txtNode.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				txtNode.setText("");
			}
		});
		
		
		txtCollocate = new JTextField();
		txtCollocate.setText("");
		txtCollocate.setColumns(10);
		txtCollocate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				txtCollocate.setText("");
			}
		});
		
		JSpinner concSortPos1 = new JSpinner();
		concSortPos1.setModel(new SpinnerNumberModel(1, 0, 5, 1));
		
		String[] sides = {"L", "P"}; 
		JComboBox concSortSide1 = new JComboBox(sides);
		
		JSpinner concSortPos2 = new JSpinner();
		concSortPos2.setModel(new SpinnerNumberModel(1, 0, 5, 1));
		
		JComboBox concSortSide2 = new JComboBox(sides);
		
		JLabel lblWithACollocate = new JLabel("L - Collocate - R");
		
		JLabel lblstSorting = new JLabel("1st - Sorting lvl - 2nd");
		
		JLabel lblQuery = new JLabel("Query");
		
		JSpinner conWinL = new JSpinner();
		conWinL.setModel(new SpinnerNumberModel(5, 0, 10, 1));
		
		JSpinner conWinR = new JSpinner();
		conWinR.setModel(new SpinnerNumberModel(5, 0, 10, 1));
		
		txtQuery = new JTextField();
		//txtQuery.setText("Query");
		txtQuery.setColumns(10);
		txtQuery.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnOk.doClick();
			}
		});
		String keyCombination = "alt Q";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCombination);
        txtQuery.getInputMap().put(keyStroke, keyCombination);
        txtQuery.getActionMap().put(keyCombination, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	txtQuery.requestFocusInWindow();
            }});
		
		JSpinner spinWL = new JSpinner();
		spinWL.setModel(new SpinnerNumberModel(5, 0, 10, 1));
		
		JSpinner spinWR = new JSpinner();
		spinWR.setModel(new SpinnerNumberModel(5, 0, 10, 1));
		table = new JTable(tModel);
		//setJTableColumnsWidth(table, 800, 10, 65, 25);
		table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		JScrollPane scrollPaneTable = new JScrollPane(table);
		collocator.add(scrollPaneTable);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	if(event.getValueIsAdjusting()) {
					int concRowCount = concordancesModel.getRowCount();
					for (int i = concRowCount - 1; i >= 0; i--) {
					    concordancesModel.removeRow(i);
					}
					sConcordances.removeAll(sConcordances);
		            String concNode = table.getValueAt(table.getSelectedRow(), 1).toString();
		            sWL = (int) spinWL.getValue();
		            sWR = (int) spinWR.getValue();
		            List<Object[]> concResults0 = findConcordances(sQuery, concNode, sWL, sWR);
		            List<String[]> concResults = formatConcordances(concResults0, 1, true, 0, false, sWL, sWR);
		            for (int i = 0; i < concResults.size(); i+=1) {
		            	sConcordances.add(concResults.get(i));
		            	concordancesModel.addRow(concResults.get(i));
		            }
		            tabbedPane.setSelectedIndex(2);
	        	}
	        }
	    });
		
		btnOk = new JButton("OK");
		//getRootPane().setDefaultButton(btnOk);
		
		btnOk_1 = new JButton("OK");
		btnOk_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int concRowCount = concordancesModel.getRowCount();
				for (int i = concRowCount - 1; i >= 0; i--) {
				    concordancesModel.removeRow(i);
				}
				sConcordances.removeAll(sConcordances);
				sNode = txtNode.getText();
				sCollocate = txtCollocate.getText();
				sConWinL = (int) conWinL.getValue();
				sConWinR = (int) conWinR.getValue();
				sConcSortSide1 = concSortSide1.getSelectedItem().toString();
				sConcSortSide2 = concSortSide2.getSelectedItem().toString();
				sConcSortPos1 = (int) concSortPos1.getValue();
				sConcSortPos2 = (int) concSortPos2.getValue();
	            List<Object[]> concResults0 = findConcordances(sNode, sCollocate, sConWinL, sConWinR);
	            boolean sort1 = true;
	            if(sConcSortSide1 == "L") {
	            	sort1 = true;
	            } else {
	            	sort1 = false;
	            }
	            boolean sort2 = true;
	            if(sConcSortSide2 == "L") {
	            	sort2 = true;
	            } else {
	            	sort2 = false;
	            }
	            List<String[]> concResults = formatConcordances(concResults0, sConcSortPos1, sort1, sConcSortPos2, sort2, (int) spinWL.getValue(), (int) spinWR.getValue());
	            for (int i = 0; i < concResults.size(); i+=1) {
	            	sConcordances.add(concResults.get(i));
	            	concordancesModel.addRow(concResults.get(i));
	            }
			}
		});
		
		JLabel lblQuery_1 = new JLabel("<html><u>Q</u>uery:</html>");
		
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int rowCount = tModel.getRowCount();
				for (int i = rowCount - 1; i >= 0; i--) {
				    tModel.removeRow(i);
				}
				sTable.removeAll(sTable);
				sWL = (int) spinWL.getValue();
				sWR = (int) spinWR.getValue();
				sQuery = txtQuery.getText();
		        Set<Entry<String, Double[]>> collocations = findCollocations(sQuery, sWL, sWR, sCollocMeasure);
		        int k = 1;
		        for(Entry<String, Double[]> mapping : collocations){
		            sTable.add(new Object[]{k, mapping.getKey(), Math.round(mapping.getValue()[1]), Math.round(mapping.getValue()[2]), Math.round(mapping.getValue()[3]),  mapping.getValue()[0]});
		            tModel.addRow(new Object[]{k, mapping.getKey(), Math.round(mapping.getValue()[1]), Math.round(mapping.getValue()[2]), Math.round(mapping.getValue()[3]),  mapping.getValue()[0]});
		            k++;
		            }
		        }
		});
		
		JButton btnExportCollocs = new JButton("Export");
		btnExportCollocs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 	LocalDateTime dt = LocalDateTime.now();
				    DateTimeFormatter fdt = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
				    String dtf = dt.format(fdt);
				    FileWriter csv;
					try {
						csv = new FileWriter(new File("/home/piotr/nlp/slac/col_"+sQuery+"_"+dtf+".csv"));
						csv.write("Collocations:,"+sQuery+",\n");
						csv.write("Lemmas / words," + sCollocLemmaSetting +",\n");
						csv.write("Window (L-R)," + sWL +"," + sWR +",\n");
						csv.write("Lemmas / words," + sCollocMeasure +",\n");
				        for (int i = 0; i < sTable.size(); i++) {
				            for (int j = 0; j < sTable.get(i).length; j++) {
				            	csv.write(sTable.get(i)[j].toString() + ",");
				            }
				            csv.write("\n");
				        }
				        csv.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}				    
			}
		});
		
		
		GroupLayout gl_collocator = new GroupLayout(collocator);
		gl_collocator.setHorizontalGroup(
				gl_collocator.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_collocator.createSequentialGroup()
						.addGap(18)
						.addComponent(spinWL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(txtQuery, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(spinWR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(btnOk, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
						.addGap(446))
					.addGroup(gl_collocator.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_collocator.createParallelGroup(Alignment.TRAILING)
							.addComponent(btnExportCollocs, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
							.addComponent(scrollPaneTable, GroupLayout.PREFERRED_SIZE, 756, GroupLayout.PREFERRED_SIZE))
						.addContainerGap(17, Short.MAX_VALUE))
			);
			gl_collocator.setVerticalGroup(
				gl_collocator.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_collocator.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_collocator.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_collocator.createParallelGroup(Alignment.BASELINE)
								.addComponent(txtQuery, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(spinWR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnOk))
							.addComponent(spinWL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(scrollPaneTable, GroupLayout.PREFERRED_SIZE, 441, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnExportCollocs)
						.addContainerGap(53, Short.MAX_VALUE))
			);
		collocator.setLayout(gl_collocator);
		
		JButton btnExportConcs = new JButton("Export");
		btnExportConcs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 	LocalDateTime dt = LocalDateTime.now();
				    DateTimeFormatter fdt = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
				    String dtf = dt.format(fdt);
				    FileWriter csv;
					try {
						csv = new FileWriter(new File("/home/piotr/nlp/slac/con_"+sNode+"_"+dtf+".csv"));
						csv.write("Concordances:\t"+sNode+"\t\n");
						csv.write("Lemmas / words\t" + sConcLemmaSetting +"\t\n");
						csv.write("Collocate\t" + sCollocate +"\t\n");
						csv.write("Window (L-R)\t" + sConWinL +"\t" + sConWinR +"\t\n");
				        for (int i = 0; i < sConcordances.size(); i++) {
				            for (int j = 0; j < sConcordances.get(i).length; j++) {
				            	csv.write(sConcordances.get(i)[j].toString() + "\t");
				            }
				            csv.write("\n");
				        }
				        csv.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}				    
			}
		});
		
		GroupLayout gl_concordancer = new GroupLayout(concordancer);
		gl_concordancer.setHorizontalGroup(
			gl_concordancer.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_concordancer.createSequentialGroup()
					.addGroup(gl_concordancer.createParallelGroup(Alignment.LEADING)
						.addComponent(concordancesScrollPane, GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
						.addGroup(gl_concordancer.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_concordancer.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblQuery)
								.addComponent(txtNode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_concordancer.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_concordancer.createSequentialGroup()
									.addComponent(btnOk_1)
									.addGap(18)
									.addComponent(concSortPos1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(concSortSide1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(concSortPos2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(concSortSide2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addComponent(lblstSorting))
							.addGroup(gl_concordancer.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_concordancer.createSequentialGroup()
									.addGap(31)
									.addComponent(lblWithACollocate))
								.addGroup(gl_concordancer.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(conWinL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(txtCollocate, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(conWinR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
						.addGroup(Alignment.TRAILING, gl_concordancer.createSequentialGroup()
							.addContainerGap(694, Short.MAX_VALUE)
							.addComponent(btnExportConcs, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_concordancer.setVerticalGroup(
			gl_concordancer.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_concordancer.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_concordancer.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtNode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnOk_1)
						.addComponent(concSortPos1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(concSortSide1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(concSortPos2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(concSortSide2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(conWinL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtCollocate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(conWinR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_concordancer.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_concordancer.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblQuery)
							.addComponent(lblstSorting))
						.addComponent(lblWithACollocate))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(concordancesScrollPane, GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnExportConcs)
					.addGap(11))
		);
		concordancer.setLayout(gl_concordancer);
		
		JPanel fileview = new JPanel();
		tabbedPane.addTab("File view", null, fileview, null);
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_F);
		
		JScrollPane scrollPane = new JScrollPane();
		JLabel lblQuery_fv = new JLabel("Query:");
		
		fileQuery = new JTextField();
		fileQuery.setColumns(10);
		
		JButton btnOk_fv = new JButton("OK");
		btnOk_fv.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = (String) fvPaths.getSelectedItem();
				String highlight = fileQuery.getText();
				String str = viewFile(path, highlight);
				textPane.setText(str);
			}
		});
		
		GroupLayout gl_fileview = new GroupLayout(fileview);
		gl_fileview.setHorizontalGroup(
			gl_fileview.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_fileview.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_fileview.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
						.addGroup(gl_fileview.createSequentialGroup()
							.addComponent(lblFilePath)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(fvPaths, GroupLayout.PREFERRED_SIZE, 361, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
							.addComponent(lblQuery_fv)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(fileQuery, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnOk_fv)))
					.addContainerGap())
		);
		gl_fileview.setVerticalGroup(
			gl_fileview.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_fileview.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_fileview.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblFilePath)
						.addComponent(lblQuery_fv)
						.addComponent(fileQuery, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnOk_fv)
						.addComponent(fvPaths, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(15)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
					.addContainerGap())
		);
		scrollPane.setViewportView(textPane);
		fileview.setLayout(gl_fileview);
		
		JPanel settings = new JPanel();
		tabbedPane.addTab("Settings", null, settings, null);
		tabbedPane.setMnemonicAt(4, KeyEvent.VK_S);
		
		JButton btnExportSettings = new JButton("Export");
		btnExportSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sFreqlistLemmaSetting = (String) freqlistLemmaSetting.getSelectedItem();
				sCollocLemmaSetting = (String) collocLemmaSetting.getSelectedItem();
				sConcLemmaSetting = (String) concLemmaSetting.getSelectedItem();
				sFileviewLemmaSetting = (String) fvLemmaSetting.getSelectedItem();
				sCollocMeasure = collocMeasure.getSelectedIndex();
				sCorpusDir = "";
				sDefaultDir = "";
			    Properties saveProps = new Properties();
			    String sFiles = "";
			    for(int i = 0; i < files.size(); i++) {
			    	sFiles += files.get(i).FileObject.getPath() + "\t" + files.get(i).Type.toString() + ";"; 
			    }
			    sPos = txtPos.getText();
			    sWL = (int) spinWL.getValue();
			    sWR = (int) spinWR.getValue();
			    sConcSortPos1 = (int) concSortPos1.getValue();
			    sConcSortSide1 = concSortSide1.getSelectedItem().toString();
			    sConcSortPos2 = (int) concSortPos2.getValue();
			    sConcSortSide2 = concSortSide2.getSelectedItem().toString();
			    sConWinL = (int) conWinL.getValue();
			    sConWinR = (int) conWinR.getValue();
			    saveProps.setProperty("files", sFiles);
			    saveProps.setProperty("sPos", sPos);
			    saveProps.setProperty("sWL", Integer.toString(sWL));
			    saveProps.setProperty("sWR", Integer.toString(sWR));
			    saveProps.setProperty("sConcSortPos1", Integer.toString(sConcSortPos1));
			    saveProps.setProperty("sConcSortSide1", sConcSortSide1);
			    saveProps.setProperty("sConcSortPos2", Integer.toString(sConcSortPos2));
			    saveProps.setProperty("sConcSortSide2", sConcSortSide2);
			    saveProps.setProperty("sConWinL", Integer.toString(sConWinL));
			    saveProps.setProperty("sConWinR", Integer.toString(sConWinR));
			    saveProps.setProperty("sFreqlistLemmaSetting", sFreqlistLemmaSetting);
			    saveProps.setProperty("sCollocsLemmaSetting", sFreqlistLemmaSetting);
			    saveProps.setProperty("sConcLemmaSetting", sConcLemmaSetting);
			    saveProps.setProperty("sFileviewLemmaSetting", sFileviewLemmaSetting);
			    saveProps.setProperty("sCollocMeasure", Integer.toString(sCollocMeasure));
			    saveProps.setProperty("sCorpusDir", sCorpusDir);
			    saveProps.setProperty("sDefaultDir", sDefaultDir);
			    try {
					saveProps.storeToXML(new FileOutputStream("settings.xml"), "");
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		JButton btnImportSettings = new JButton("Import");
		btnImportSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    Properties loadProps = new Properties();
			    try {
					loadProps.loadFromXML(new FileInputStream("settings.xml"));
					String[] sFiles = loadProps.getProperty("files").split(";");
					files.removeAll(files);
					listModel.removeAllElements();
					fvPaths.removeAllItems();
					for(int i = 0; i < sFiles.length; i++) {
						String x[] = sFiles[i].split("\t");
						if(x.length == 2) {
							files.add(new FileType(new File(x[0]), Boolean.parseBoolean(x[1])));
							listModel.addElement(x[0]);
							fvPaths.addItem(x[0]);
						}
					}
					sPos = loadProps.getProperty("sPos");
					sWL = Integer.parseInt(loadProps.getProperty("sWL"));
					sWR = Integer.parseInt(loadProps.getProperty("sWR"));
					sConcSortPos1 = Integer.parseInt(loadProps.getProperty("sConcSortPos1"));
					sConcSortSide1 = loadProps.getProperty("sConcSortSide1");
					sConcSortPos2 = Integer.parseInt(loadProps.getProperty("sConcSortPos2"));
					sConcSortSide2 = loadProps.getProperty("sConcSortSide2");
					sConWinL = Integer.parseInt(loadProps.getProperty("sConWinL"));
					sConWinR = Integer.parseInt(loadProps.getProperty("sConWinR"));
					sFreqlistLemmaSetting = loadProps.getProperty("sFreqlistLemmaSetting");
					sCollocLemmaSetting = loadProps.getProperty("sCollocsLemmaSetting");
					sConcLemmaSetting = loadProps.getProperty("sConcLemmaSetting");
					sFileviewLemmaSetting = loadProps.getProperty("sFileviewLemmaSetting");
					sCollocMeasure = Integer.parseInt(loadProps.getProperty("sCollocMeasure"));
					sCorpusDir = loadProps.getProperty("sCorpusDir");
					sDefaultDir = loadProps.getProperty("sDefaultDir");
					freqlistLemmaSetting.setSelectedItem(sFreqlistLemmaSetting);
					collocLemmaSetting.setSelectedItem(sCollocLemmaSetting);
					concLemmaSetting.setSelectedItem(sConcLemmaSetting);
					fvLemmaSetting.setSelectedItem(sFileviewLemmaSetting);
					collocMeasure.setSelectedIndex(sCollocMeasure);
					sCorpusDir = "";
					sDefaultDir = "";
				    txtPos.setText(sPos);
				    spinWL.setValue(sWL);
				    spinWR.setValue(sWR);
				    concSortPos1.setValue(sConcSortPos1);
				    concSortSide1.setSelectedItem(sConcSortSide1);
				    concSortPos2.setValue(sConcSortPos2);
				    concSortSide2.setSelectedItem(sConcSortSide2);
				    conWinL.setValue(sConWinL);
				    conWinR.setValue(sConWinR);

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		JButton btnApplySettings = new JButton("Apply");
		btnApplySettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sFreqlistLemmaSetting = (String) freqlistLemmaSetting.getSelectedItem();
				sCollocLemmaSetting = (String) collocLemmaSetting.getSelectedItem();
				sConcLemmaSetting = (String) concLemmaSetting.getSelectedItem();
				sFileviewLemmaSetting = (String) fvLemmaSetting.getSelectedItem();
				sCollocMeasure = collocMeasure.getSelectedIndex();
				sCorpusDir = "";
				sDefaultDir = "";
			}
		});
		
	    Properties loadProps = new Properties();
	    try {
			loadProps.loadFromXML(new FileInputStream("settings.xml"));
			String[] sFiles = loadProps.getProperty("files").split(";");
			files.removeAll(files);
			listModel.removeAllElements();
			fvPaths.removeAllItems();
			for(int i = 0; i < sFiles.length; i++) {
				String x[] = sFiles[i].split("\t");
				if(x.length == 2) {
					files.add(new FileType(new File(x[0]), Boolean.parseBoolean(x[1])));
					listModel.addElement(x[0]);
					fvPaths.addItem(x[0]);
				}
			}
			sPos = loadProps.getProperty("sPos");
			sWL = Integer.parseInt(loadProps.getProperty("sWL"));
			sWR = Integer.parseInt(loadProps.getProperty("sWR"));
			sConcSortPos1 = Integer.parseInt(loadProps.getProperty("sConcSortPos1"));
			sConcSortSide1 = loadProps.getProperty("sConcSortSide1");
			sConcSortPos2 = Integer.parseInt(loadProps.getProperty("sConcSortPos2"));
			sConcSortSide2 = loadProps.getProperty("sConcSortSide2");
			sConWinL = Integer.parseInt(loadProps.getProperty("sConWinL"));
			sConWinR = Integer.parseInt(loadProps.getProperty("sConWinR"));
			sFreqlistLemmaSetting = loadProps.getProperty("sFreqlistLemmaSetting");
			sCollocLemmaSetting = loadProps.getProperty("sCollocsLemmaSetting");
			sConcLemmaSetting = loadProps.getProperty("sConcLemmaSetting");
			sFileviewLemmaSetting = loadProps.getProperty("sFileviewLemmaSetting");
			sCollocMeasure = Integer.parseInt(loadProps.getProperty("sCollocMeasure"));
			sCorpusDir = loadProps.getProperty("sCorpusDir");
			sDefaultDir = loadProps.getProperty("sDefaultDir");
			freqlistLemmaSetting.setSelectedItem(sFreqlistLemmaSetting);
			collocLemmaSetting.setSelectedItem(sCollocLemmaSetting);
			concLemmaSetting.setSelectedItem(sConcLemmaSetting);
			fvLemmaSetting.setSelectedItem(sFileviewLemmaSetting);
			collocMeasure.setSelectedIndex(sCollocMeasure);
			sCorpusDir = "";
			sDefaultDir = "";
		    txtPos.setText(sPos);
		    spinWL.setValue(sWL);
		    spinWR.setValue(sWR);
		    concSortPos1.setValue(sConcSortPos1);
		    concSortSide1.setSelectedItem(sConcSortSide1);
		    concSortPos2.setValue(sConcSortPos2);
		    concSortSide2.setSelectedItem(sConcSortSide2);
		    conWinL.setValue(sConWinL);
		    conWinR.setValue(sConWinR);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    ChangeListener changeListener = new ChangeListener() {
	        public void stateChanged(ChangeEvent changeEvent) {
	          JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
	          int index = sourceTabbedPane.getSelectedIndex();
	          if(index == 0) {
	        	  getRootPane().setDefaultButton(btnGenerate);
	          } else if (index == 1) {
	        	  txtQuery.requestFocusInWindow();
	        	  getRootPane().setDefaultButton(btnOk);	        	  
	          } else if (index == 2) {
	        	  txtCollocate.requestFocusInWindow();
	        	  getRootPane().setDefaultButton(btnOk_1);
	          } else if (index == 3) {
	        	  fileQuery.requestFocusInWindow();
	        	  getRootPane().setDefaultButton(btnOk_fv);
	          } else if (index == 4) {
	        	  collocMeasure.requestFocusInWindow();
	        	  getRootPane().setDefaultButton(btnApplySettings);
	          }
	        }
	      };
	      tabbedPane.addChangeListener(changeListener);
		
		
		GroupLayout gl_settings = new GroupLayout(settings);
		gl_settings.setHorizontalGroup(
			gl_settings.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_settings.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_settings.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_settings.createSequentialGroup()
							.addGap(12)
							.addComponent(freqlistLemmaSetting, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblWhenGeneratingWord))
					.addContainerGap(308, Short.MAX_VALUE))
				.addGroup(gl_settings.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_settings.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_settings.createSequentialGroup()
							.addGap(12)
							.addComponent(collocLemmaSetting, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblWhenGeneratingCollocation))
					.addContainerGap(269, Short.MAX_VALUE))
				.addGroup(gl_settings.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_settings.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_settings.createSequentialGroup()
							.addGap(12)
							.addComponent(concLemmaSetting, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblWhenGeneratingConcordances))
					.addContainerGap(281, Short.MAX_VALUE))
				.addGroup(gl_settings.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_settings.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_settings.createSequentialGroup()
							.addGap(12)
							.addComponent(fvLemmaSetting, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblWhenQueryingXml))
					.addContainerGap(431, Short.MAX_VALUE))
				.addGroup(gl_settings.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_settings.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_settings.createSequentialGroup()
							.addGap(12)
							.addComponent(collocMeasure, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblCollocationMeasure))
					.addContainerGap(607, Short.MAX_VALUE))
				.addGroup(gl_settings.createSequentialGroup()
					.addContainerGap(462, Short.MAX_VALUE)
					.addComponent(btnApplySettings, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btnImportSettings, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btnExportSettings, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		gl_settings.setVerticalGroup(
			gl_settings.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_settings.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblWhenGeneratingWord)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(freqlistLemmaSetting, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblWhenGeneratingCollocation)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(collocLemmaSetting, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblWhenGeneratingConcordances)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(concLemmaSetting, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblWhenQueryingXml)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(fvLemmaSetting, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblCollocationMeasure)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(collocMeasure, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 238, Short.MAX_VALUE)
					.addGroup(gl_settings.createParallelGroup(Alignment.LEADING)
						.addComponent(btnApplySettings, Alignment.TRAILING)
						.addComponent(btnImportSettings, Alignment.TRAILING)
						.addComponent(btnExportSettings, Alignment.TRAILING))
					.addGap(56))
		);
		settings.setLayout(gl_settings);
		
	}
}
	