// TODO
// - support spaces in lemmatiser paths (python reldi-wrap.py)
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.JCheckBox;

public class leming extends JFrame {

	private JPanel contentPane;
	private JTextField txtSourceDb;
	private JTextField txtTable;
	private JTextField txtText;
	private JTextField txtLemmatiserPath;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextField txtOutputDir;
	private ArrayList<File> files = new ArrayList<File>();
	Process p;
	
	private static String escape(String input) {
		String output = new String();
		HashMap<Character, String> esc_chars = new HashMap<Character, String>();
		esc_chars.put('"', "&quot;");
		esc_chars.put('\'', "&apos;");
		esc_chars.put('>', "&gt;");
		esc_chars.put('<', "&lt;");
		esc_chars.put('&', "&amp;");
		for(int i = 0; i < input.length(); i++) {
			Character c = input.charAt(i);
			boolean escaped = false;
			for (HashMap.Entry<Character, String> e : esc_chars.entrySet()) {
			   if(c.equals(e.getKey())) {
				   output += e.getValue();
				   escaped = true;
				   break;
			   } 
			}
			if(escaped == false) {
				output += input.charAt(i);
			}
		}
		return output;
	}
	
	private static ArrayList<String> tokeniser(String input) {
		ArrayList<String> output = new ArrayList<String>();
		String token = "";
		Pattern p1 = Pattern.compile("\\s");
		Pattern p2 = Pattern.compile("\\p{Punct}");
		for(int i = 0; i < input.length(); i++) {
			Matcher m1 = p1.matcher(Character.toString(input.charAt(i)));
			Matcher m2 = p2.matcher(Character.toString(input.charAt(i)));
			if(m1.find()) {
				if(!(token.equals(""))) {
					output.add(token);
					token = "";
				}
			} else {
				if(m2.find()) {
					output.add(token);
					output.add(m2.group());
					token = "";
				} else {
					token += input.charAt(i);
				}
			} 
		}
		return output;
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Class.forName("org.sqlite.JDBC");
					leming frame = new leming();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public leming() {
		setTitle("SLAC - LEMING 0.1 (Apr 2020)");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 375);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JRadioButton rdbtnSqliteDb = new JRadioButton("SQLite DB");
		buttonGroup.add(rdbtnSqliteDb);
		
		txtSourceDb = new JTextField();
		txtSourceDb.setText("./target2.db");
		txtSourceDb.setColumns(10);
		
		JLabel lblDb = new JLabel("DB:");
		JButton btnSelect = new JButton("...");
		btnSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(leming.this);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		        		txtSourceDb.setText(fc.getSelectedFile().getPath());
		        }
			}
		});
		
		JRadioButton rdbtnTextFiles = new JRadioButton("Text file(s)");
		buttonGroup.add(rdbtnTextFiles);
		
		JScrollPane scrollPane = new JScrollPane();
		DefaultListModel listModel;
		listModel = new DefaultListModel();
		JList list = new JList(listModel);
		scrollPane.setViewportView(list);
		JButton btnSelect_1 = new JButton("+");
		btnSelect_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final JFileChooser fc = new JFileChooser();
				fc.setMultiSelectionEnabled(true);
				int returnVal = fc.showOpenDialog(leming.this);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		        	for(File file:fc.getSelectedFiles()) {
		        		files.add(file);
		        		listModel.addElement(file.getPath());
		        	}
		        }
			}
		});
		
		JLabel lblTable = new JLabel("Table:");
		
		txtTable = new JTextField();
		txtTable.setText("articles");
		txtTable.setColumns(10);
		
		txtText = new JTextField();
		txtText.setText("text");
		txtText.setColumns(10);
		
		JLabel lblColumn = new JLabel("Column:");
		
		
		JLabel lblLemmatiser = new JLabel("Lemmatiser");
		
		JLabel lblPath = new JLabel("Path/Language:");
		
		txtLemmatiserPath = new JTextField();
		txtLemmatiserPath.setText("tree-tagger-polish");
		txtLemmatiserPath.setColumns(10);
		
		JCheckBox cbTokenise = new JCheckBox("Tokenise");
		
		JLabel lblOutputDirectory = new JLabel("Output directory:");
		
		txtOutputDir = new JTextField();
		txtOutputDir.setColumns(10);
		
		JButton btnSelect_2 = new JButton("...");
		btnSelect_2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final JFileChooser fc2 = new JFileChooser();
				fc2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc2.setAcceptAllFileFilterUsed(false);
				int returnVal = fc2.showOpenDialog(leming.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					txtOutputDir.setText(fc2.getCurrentDirectory().getPath()+"/"+fc2.getSelectedFile().getName());
				}
			}
		});
		
		JScrollPane scrollPane_1 = new JScrollPane();
		JTextPane textPane = new JTextPane();
		scrollPane_1.setViewportView(textPane);
		JButton btnStart = new JButton("START");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				if(rdbtnSqliteDb.isSelected()) {
					String path = "jdbc:sqlite:" + txtSourceDb.getText();
			        try (Connection conn = DriverManager.getConnection(path)) {
			            if (conn != null) {
			            	PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + txtTable.getText() + ";");
			            	//stmt.setString(1, txtTable.getText());
			            	try(ResultSet rs = stmt.executeQuery()){			            		
			            		int j = 0;
			            		while (rs.next()) {
			    							class GUITTRunnable implements Runnable {
			    								private int i;
												private ResultSet rs;
			    								public GUITTRunnable(int i, ResultSet rs) {
			    									this.i = i;
			    									this.rs = rs;
			    								}
			    								public void run() {
			    								FileWriter fw;
			    								try {
			    									fw = new FileWriter(new File("lemmatisation.tmp"));
			    									String txtout = rs.getString("text");
			    									if(cbTokenise.isSelected()) {
			    										ArrayList<String> tokens = tokeniser(txtout);
			    										fw.write(String.join("\n", tokens));
			    									} else {
			    										fw.write(txtout);
			    									}
			    									fw.close();
			    								} catch (IOException e1) {
			    									// TODO Auto-generated catch block
			    									e1.printStackTrace();
			    								} catch (SQLException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
			    								List<String> cmd = new ArrayList<String>(Arrays.asList(txtLemmatiserPath.getText().split(" ")));
			    								cmd.add("lemmatisation.tmp");
			    								try {
			    									ProcessBuilder pb = new ProcessBuilder(cmd);
			    									//pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			    									p = pb.start();
			    									BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			    									String line;
			    									ArrayList<String> raw = new ArrayList<String>();
			    									while ((line = in.readLine()) != null) {
			    									    raw.add(escape(line)+"\n");
			    									}
			    									String out = "<TEI><teiHeader><fileDesc><titleStmt>\n";
			    									//ResultSetMetaData metadata = rs.getMetaData();
			    									if(rs.getString("title") != null) {
			    										out += "<title>" + rs.getString("title") + "</title>";
			    									}
			    									if(rs.getString("authors") != null) {
			    										out += "<author>" + rs.getString("authors") + "</author>";
			    									}
			    									out += "</titleStmt><publicationStmt>";
			    									if(rs.getString("publish_date") != null) {
			    										out += "<date>" + rs.getString("publish_date") + "</date>";
			    									}
			    									out += "</publicationStmt><sourceDesc><bibl>";
			    									if(rs.getString("url") != null) {
			    										out += "<ref target=\"" + rs.getString("url") + "\" />";
			    									}
			    									if(rs.getString("access") != null) {
			    										out += "<date when=\"" + rs.getString("access") + "\" />";
			    									}
			    									out += "</bibl></sourceDesc></fileDesc></teiHeader><text>";
					    							for(int j = 0; j < raw.size(); j++) {
					    								if(j > 1 & j < raw.size()-2) {
					    									String[] tgd = raw.get(j).split("\t");
					    									tgd[2] = tgd[2].substring(0, tgd[2].length()-1);
					    									if(tgd[2].equals("<unknown>")) {
					    										tgd[2] = tgd[0];
					    									}
					    									out += "<w lemma=\"" + tgd[2] + "\" pos=\"" + tgd[1] + "\">" + tgd[0] + "</w>\n";
					    								}
					    							}
					    							out += "</text></TEI>";
			    									fw = new FileWriter(new File(txtOutputDir.getText() + "/" + Integer.toString(i) + ".xml"));
			    									fw.write(out);
			    									fw.close();
			    									textPane.setText(textPane.getText() + "lemmatising " + Integer.toString(i) + "\n");
			    									textPane.setText(textPane.getText() + "-- lemmatised " + txtOutputDir.getText() + "/" + Integer.toString(i) + ".xml\n");
			    								
			    								} catch (IOException | SQLException e) {
			    									// TODO Auto-generated catch block
			    									e.printStackTrace();
			    								}
			    							}
			    							}
			    							final Thread thr = new Thread(new GUITTRunnable(j, rs));
			    							thr.start();
			    							thr.join();
			    							j+=1;
			            				}
			            			} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
			            		}
			        } catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if(rdbtnTextFiles.isSelected()) {
					for(int i=0; i < files.size(); i++) {
						try {
							String str = new String(Files.readAllBytes(Paths.get(files.get(i).getPath())));
							class GUITTRunnable implements Runnable {
								private int i;
								private String str;
								public GUITTRunnable(int i, String str) {
									this.i = i;
									this.str = str;
								}
								public void run() {
								FileWriter fw;
								try {
									fw = new FileWriter(new File("lemmatisation.tmp"));
									if(cbTokenise.isSelected()) {
										ArrayList<String> tokens = tokeniser(str);
										fw.write(String.join("\n", tokens));
									} else {
										fw.write(str);
									}
									fw.close();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								List<String> cmd = new ArrayList<String>(Arrays.asList(txtLemmatiserPath.getText().split(" ")));
								cmd.add("lemmatisation.tmp");
								try {
									ProcessBuilder pb = new ProcessBuilder(cmd);
									//pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
									p = pb.start();
									BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
									String line;
									ArrayList<String> raw = new ArrayList<String>();
									while ((line = in.readLine()) != null) {
									    raw.add(line+"\n");
									}
									String out = "<text>\n";
									for(int j = 0; j < raw.size(); j++) {
										if(j > 1 & j < raw.size()-2) {
											String[] tgd = raw.get(j).split("\t");
											if(tgd.length > 2) {
												tgd[2] = tgd[2].substring(0, tgd[2].length()-1);
												if(tgd[2].equals("<unknown>")) {
													tgd[2] = tgd[0];
												}
												out += "<w lemma=\"" + escape(tgd[2]) + "\" pos=\"" + escape(tgd[1]) + "\">" + escape(tgd[0]) + "</w>\n";
											}
										}
									}
									out += "</text>";
									fw = new FileWriter(new File(txtOutputDir.getText() + "/" + files.get(i).getName() + ".xml"));
									fw.write(out);
									fw.close();
									textPane.setText(textPane.getText() + "lemmatising " + files.get(i).getName() + "\n");
									textPane.setText(textPane.getText() + "-- lemmatised " + txtOutputDir.getText() + "/" + files.get(i).getName() + ".xml\n");
								
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 
							}
							}
							Runnable r = new GUITTRunnable(i, str);
							new Thread(r).start();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					JOptionPane.showMessageDialog(null, "Choose type of data source!");
				}
			}
		});
		
		JButton btnRemove = new JButton("-");
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final int a = list.getSelectedIndex();
				if (a >= 0) {
				files.remove(a);
				((DefaultListModel) list.getModel()).removeElementAt(a);
				}
			}
		});
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(rdbtnSqliteDb)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGap(6)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
										.addComponent(rdbtnTextFiles)
										.addGroup(gl_contentPane.createSequentialGroup()
											.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addComponent(lblColumn)
												.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
													.addComponent(lblDb, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
													.addComponent(lblTable)))
											.addGap(12)))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_contentPane.createSequentialGroup()
											.addComponent(txtSourceDb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(btnSelect, GroupLayout.PREFERRED_SIZE, 35, Short.MAX_VALUE))
										.addComponent(txtText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(txtTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
								.addComponent(lblOutputDirectory)))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGap(49)
									.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGap(51)
									.addComponent(txtOutputDir, GroupLayout.PREFERRED_SIZE, 169, GroupLayout.PREFERRED_SIZE)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
									.addComponent(btnRemove, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(btnSelect_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addComponent(btnSelect_2, 0, 0, Short.MAX_VALUE))))
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGap(23)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(btnStart, GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
										.addComponent(lblLemmatiser)
										.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGap(48)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_contentPane.createSequentialGroup()
											.addGap(12)
											.addComponent(txtLemmatiserPath, GroupLayout.PREFERRED_SIZE, 216, GroupLayout.PREFERRED_SIZE))
										.addComponent(lblPath))))
							.addGap(21))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(60)
							.addComponent(cbTokenise)
							.addContainerGap())))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(rdbtnSqliteDb)
						.addComponent(lblLemmatiser))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblDb, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
								.addComponent(txtSourceDb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnSelect))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(txtTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblTable))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(txtText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblColumn))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(rdbtnTextFiles)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblOutputDirectory))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(btnSelect_1)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnRemove)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(txtOutputDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnSelect_2))
							.addGap(6))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblPath)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(txtLemmatiserPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(8)
							.addComponent(cbTokenise)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnStart)
							.addGap(18)
							.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
		);
		
		contentPane.setLayout(gl_contentPane);
	}
}

