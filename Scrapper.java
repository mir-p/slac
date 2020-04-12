// TODO
// - count rows: in source DB, in target DB
// - generate random samples
// - scripts for Politika, Vecernje novosti, Novi list, Jutarnji list, Vecernji list, Gazeta Wyborcza
package scrapper;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.System.Logger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JCheckBox;


public class Scrapper extends JFrame {

	private JPanel contentPane;
	private JTextField txtStartPage;
	private JTextField txtTargetDb;
	private JTextField txtSite;
	private JTextField txtLanguage;
	private JTextField txtSourceDb;
	private JTextField txtQuery;
	private JTextField txtTargetDb2;
	private JTable tableDb;
	private ArrayList<File> scripts = new ArrayList<File>();
	Process p;
	private ArrayList<Properties> configs = new ArrayList<Properties>();
	private String activeScript = new String();
	private JTextField txtInvocation;
	private JTextField txtPythonPath;
	private String invocation = new String();
	private String pythonPath = new String();
	private JTextField txtYyyy;
	private JTextField txtMm;
	private JTextField txtDd;
	private JTextField txtYyyy_1;
	private JTextField txtMm_1;
	private JTextField txtDd_1;
	private JTextField textField_1;
	private JTable table;

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Class.forName("org.sqlite.JDBC");
					Scrapper frame = new Scrapper();
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
	public Scrapper() {
		setTitle("SLAC - Scrapper / Downsampler 0.1 (Apr 2020)");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new CardLayout(0, 0));
		
		JLabel lblPythonPath = new JLabel("Python path:");
		txtPythonPath = new JTextField();
		txtPythonPath.setText("python3");
		txtPythonPath.setColumns(10);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, "name_44461225778666");
		
		JPanel scrapper = new JPanel();
		tabbedPane.addTab("Scrapper", null, scrapper, null);
		
		JLabel lblStartFromPage = new JLabel("Start from page:");
		
		txtStartPage = new JTextField();
		txtStartPage.setText("0");
		txtStartPage.setColumns(10);
		
		JLabel lblTargetDb = new JLabel("Target DB:");
		
		txtTargetDb = new JTextField();
		txtTargetDb.setText("./target.db");
		txtTargetDb.setColumns(10);
		
		JLabel lblSiteroot = new JLabel("Site (root):");
		
		txtSite = new JTextField();
		txtSite.setColumns(10);
		
		JLabel lblScript = new JLabel("Script:");
		
		/*JComboBox cbScript = new JComboBox();
		try (Stream<Path> walk = Files.walk(Paths.get("./resources"))) {
			List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith(".py")).collect(Collectors.toList());
			for(int i = 0; i < result.size(); i++) {
				System.out.println(result.get(i));
				scripts.add(new File(result.get(i)));
				activeScript = result.get(i);
				cbScript.addItem(result.get(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		JScrollPane scrollPaneScript = new JScrollPane();
		
		JTextPane textPaneOutput = new JTextPane();
		JButton btnOkScrap = new JButton("Start");
		btnOkScrap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				new Thread(new Runnable(){
				public void run() {
				String[] cmd = {txtPythonPath.getText(), "-u", "./resources/" + activeScript, txtTargetDb.getText(), txtSite.getText(), txtStartPage.getText() };
				try {
					ProcessBuilder pb = new ProcessBuilder(cmd);
					//pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
					p = pb.start();
					System.out.println(String.join(" ", cmd));
					BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = in.readLine()) != null) {
					    textPaneOutput.setText(textPaneOutput.getText() + "\n" + line);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			}).start();
			}
		});
		
		
		JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				String path = "jdbc:sqlite:" + txtTargetDb.getText();
		        try (Connection conn = DriverManager.getConnection(path)) {
		            if (conn != null) {
		            	Statement stmt = conn.createStatement();
		            	stmt.execute("CREATE TABLE IF NOT EXISTS articles (id integer primary key, title text, publish_date text, authors text, text text, url text, access text);");
		            }
		 
		        } catch (SQLException e) {
		            System.out.println(e.getMessage());
		        }
			}
		});
		
		JLabel lblLanguage = new JLabel("Language:");
		
		JTextPane textPaneScript = new JTextPane();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textPaneOutput);
		
		JButton btnSave = new JButton("Save script");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				try (PrintWriter out = new PrintWriter("./resources/" + activeScript)) {
				    out.println(textPaneScript.getText());
				    JOptionPane.showMessageDialog(null, "Changes saved!");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		JButton btnStopScrap = new JButton("Stop");
		btnStopScrap.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			p.destroy();
		}
		});
	
		JLabel lblConfiguration = new JLabel("Configuration:");
		JComboBox cbConfig = new JComboBox();
		try (Stream<Path> walk = Files.walk(Paths.get("./resources"))) {
			List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith(".xml")).collect(Collectors.toList());
			for(int i = 0; i < result.size(); i++) {
				System.out.println(result.get(i));
				Properties loadProps = new Properties();
				loadProps.loadFromXML(new FileInputStream(result.get(i)));
				configs.add(loadProps);
				String cfgTitle = loadProps.getProperty("Title");
				cbConfig.addItem(cfgTitle);
			}
			Properties loadProps = configs.get(0);
			String cfgScript0 = loadProps.getProperty("ScriptFile");
			activeScript = cfgScript0;
			String startingPage0 = loadProps.getProperty("DefaultStartingPage");
			String url0 = loadProps.getProperty("Url");
			String str = new String(Files.readAllBytes(Paths.get("./resources/" + cfgScript0)));
			textPaneScript.setText(str);
			txtSite.setText(url0);
			txtStartPage.setText(startingPage0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		cbConfig.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {
				Properties loadProps = configs.get(cbConfig.getSelectedIndex());
				String cfgScript0 = loadProps.getProperty("ScriptFile");
				String startingPage0 = loadProps.getProperty("DefaultStartingPage");
				String url0 = loadProps.getProperty("Url");
				String str;
				activeScript = cfgScript0;
				try {
					str = new String(Files.readAllBytes(Paths.get("./resources/"+cfgScript0)));
					textPaneScript.setText(str);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				txtSite.setText(url0);
				txtStartPage.setText(startingPage0);
			}
		});
		
		
		GroupLayout gl_scrapper = new GroupLayout(scrapper);
		gl_scrapper.setHorizontalGroup(
				gl_scrapper.createParallelGroup(Alignment.TRAILING)
					.addGroup(gl_scrapper.createSequentialGroup()
						.addGap(20)
						.addGroup(gl_scrapper.createParallelGroup(Alignment.LEADING)
							.addComponent(btnSave)
							.addGroup(gl_scrapper.createSequentialGroup()
								.addGap(18)
								.addGroup(gl_scrapper.createParallelGroup(Alignment.LEADING)
									.addGroup(gl_scrapper.createSequentialGroup()
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(lblConfiguration)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(cbConfig, 0, 237, Short.MAX_VALUE))
									.addGroup(gl_scrapper.createSequentialGroup()
										.addGroup(gl_scrapper.createParallelGroup(Alignment.LEADING)
											.addComponent(lblSiteroot)
											.addComponent(lblTargetDb))
										.addGap(44)
										.addGroup(gl_scrapper.createParallelGroup(Alignment.LEADING)
											.addGroup(gl_scrapper.createSequentialGroup()
												.addComponent(txtTargetDb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(btnCreate))
											.addGroup(gl_scrapper.createSequentialGroup()
												.addComponent(txtSite, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(lblStartFromPage)
												.addPreferredGap(ComponentPlacement.UNRELATED)
												.addComponent(txtStartPage, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)))))
								.addGap(88))
							.addGroup(gl_scrapper.createSequentialGroup()
								.addGroup(gl_scrapper.createParallelGroup(Alignment.TRAILING, false)
									.addGroup(gl_scrapper.createSequentialGroup()
										.addComponent(btnOkScrap, GroupLayout.PREFERRED_SIZE, 169, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnStopScrap, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									.addComponent(scrollPaneScript, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 358, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 194, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)))
						.addGap(286))
			);
			gl_scrapper.setVerticalGroup(
				gl_scrapper.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_scrapper.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_scrapper.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblSiteroot)
							.addComponent(txtSite, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblStartFromPage)
							.addComponent(txtStartPage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_scrapper.createParallelGroup(Alignment.BASELINE)
							.addComponent(txtTargetDb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblTargetDb)
							.addComponent(btnCreate))
						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(gl_scrapper.createParallelGroup(Alignment.BASELINE)
							.addComponent(cbConfig, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblConfiguration))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(btnSave)
						.addGap(15)
						.addGroup(gl_scrapper.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_scrapper.createSequentialGroup()
								.addComponent(scrollPaneScript, GroupLayout.PREFERRED_SIZE, 194, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_scrapper.createParallelGroup(Alignment.BASELINE)
									.addComponent(btnStopScrap)
									.addComponent(btnOkScrap)))
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 194, GroupLayout.PREFERRED_SIZE))
						.addGap(13))
			);
		
		scrollPaneScript.setViewportView(textPaneScript);
		scrapper.setLayout(gl_scrapper);
		
		JPanel downsampler = new JPanel();
		tabbedPane.addTab("Downsampler", null, downsampler, null);
		
		JLabel lblDatabase = new JLabel("Database:");
		
		txtSourceDb = new JTextField();
		txtSourceDb.setText("./target.db");
		txtSourceDb.setColumns(10);
		
		JLabel lblQuery = new JLabel("Query:");
		
		txtQuery = new JTextField();
		txtQuery.setText("BETWEEN \"2019-01-01\" AND \"2019-12-31\"");
		txtQuery.setColumns(10);
		
		JLabel lblTargetDb_1 = new JLabel("Target DB:");
		
		txtTargetDb2 = new JTextField();
		txtTargetDb2.setText("./target2.db");
		txtTargetDb2.setColumns(10);
		
		JRadioButton rdbtnQuery = new JRadioButton("Query:");
		
		JRadioButton rdbtnTemporalSlices = new JRadioButton("Temporal slice(s):");
		rdbtnTemporalSlices.setSelected(true);
		
		JLabel lblfromYyyy = new JLabel("(from:) YYYY - MM - DD / (to:) YYYY - MM - DD");
		
		txtYyyy = new JTextField();
		txtYyyy.setText("YYYY");
		txtYyyy.setColumns(10);
		
		txtMm = new JTextField();
		txtMm.setText("MM");
		txtMm.setColumns(10);
		
		txtDd = new JTextField();
		txtDd.setText("DD");
		txtDd.setColumns(10);
		
		txtYyyy_1 = new JTextField();
		txtYyyy_1.setText("YYYY");
		txtYyyy_1.setColumns(10);
		
		txtMm_1 = new JTextField();
		txtMm_1.setText("MM");
		txtMm_1.setColumns(10);
		
		txtDd_1 = new JTextField();
		txtDd_1.setText("DD");
		txtDd_1.setColumns(10);
		
		JButton btnCountRows = new JButton("Count");
		btnCountRows.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				String path = "jdbc:sqlite:" + txtSourceDb.getText();
		        try (Connection conn = DriverManager.getConnection(path)) {
		            if (conn != null) {
		            	Statement stmt = conn.createStatement();
		            	String dateFrom = txtYyyy.getText() + "-" + txtMm.getText() + "-" + txtDd.getText();
		            	String dateTo = txtYyyy_1.getText() + "-" + txtMm_1.getText() + "-" + txtDd_1.getText();
		            	System.out.println(dateFrom + " " + dateTo);
		            	String sqlStmt = "SELECT COUNT(id) AS total FROM articles WHERE publish_date BETWEEN '"+dateFrom+"' AND '"+dateTo+"';";
		            	System.out.println(sqlStmt);
		            	try(ResultSet rs = stmt.executeQuery(sqlStmt)){
		            		JOptionPane.showMessageDialog(null, "No. of rows in the given time slice = " + Integer.toString(rs.getInt("total")));
		            	}
		            }
		        } catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		textField_1 = new JTextField();
		textField_1.setText("100");
		textField_1.setColumns(10);
		
		JCheckBox chckbxRandomSampleOf = new JCheckBox("random sample of:");
		
		String[] tCols = {"From", "To", "Random", "Count"};
		Object[][] tData = {};
		DefaultTableModel tModel = new DefaultTableModel(tData, tCols);
		table = new JTable(tModel);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				if((
						!(txtYyyy.getText().equals(null) | txtYyyy.getText().equals("YYYY")) &
						!(txtMm.getText().equals(null) | txtMm.getText().equals("MM")) &
						!(txtDd.getText().equals(null) | txtDd.getText().equals("DD")) 
					) & (
						!(txtYyyy_1.getText().equals(null) | txtYyyy_1.getText().equals("YYYY")) &
						!(txtMm_1.getText().equals(null) | txtMm_1.getText().equals("MM")) &
						!(txtDd_1.getText().equals(null) | txtDd_1.getText().equals("DD")) 
					)) {
					String dateFrom = txtYyyy.getText() + "-" + txtMm.getText() + "-" + txtDd.getText();
	            	String dateTo = txtYyyy_1.getText() + "-" + txtMm_1.getText() + "-" + txtDd_1.getText();
	            	String randomised = new String();
	            	String randomisedCount = new String();
	            	if(chckbxRandomSampleOf.isSelected()) {
	            		randomised = "true";
	            		randomisedCount = textField_1.getText();
	            	} else {
	            		randomised = "false";
	            		randomisedCount = "";
	            	}
					tModel.addRow(new Object[]{dateFrom, dateTo, randomised, randomisedCount});
				} else {
					JOptionPane.showMessageDialog(null, "Incorrect time slice.");
				}
			}
		});
		
		JScrollPane scrollPane_1 = new JScrollPane();
		
		JButton button = new JButton("-");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final int a = table.getSelectedRow();
				if (a >= 0) {
				((DefaultTableModel) table.getModel()).removeRow(a);
				}
			}
		});
		
		JButton button_1 = new JButton("...");
		button_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(Scrapper.this);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		        		txtSourceDb.setText(fc.getSelectedFile().getPath());
		        }
			}
		});
		
		JButton button_2 = new JButton("...");
		button_2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final JFileChooser fc2 = new JFileChooser();
				fc2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc2.setAcceptAllFileFilterUsed(false);
				int returnVal = fc2.showOpenDialog(Scrapper.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					txtTargetDb2.setText(fc2.getCurrentDirectory().getPath()+"/"+fc2.getSelectedFile().getName());
				}
			}
		});
		
		JButton btnCreate_1 = new JButton("Create");
		btnCreate_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				String path = "jdbc:sqlite:" + txtTargetDb2.getText();
		        try (Connection conn = DriverManager.getConnection(path)) {
		            if (conn != null) {
		            	Statement stmt = conn.createStatement();
		            	stmt.execute("CREATE TABLE IF NOT EXISTS articles (id integer primary key, title text, publish_date text, authors text, text text, url text, access text);");
		            }
		 
		        } catch (SQLException e) {
		            System.out.println(e.getMessage());
		        }
			}
		});
		
		JButton btnOkDownsample = new JButton("OK");
		btnOkDownsample.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				String path = "jdbc:sqlite:" + txtTargetDb2.getText();
		        try (Connection conn = DriverManager.getConnection(path)) {
		            if (conn != null) {
		            	Statement stmt = conn.createStatement();
		            	stmt.execute("CREATE TABLE IF NOT EXISTS articles (id integer primary key, title text, publish_date text, authors text, text text, url text, access text);");
		            	String path2 = "jdbc:sqlite:" + txtSourceDb.getText();
		            	 try (Connection conn2 = DriverManager.getConnection(path2)) {
		 		            if (conn2 != null) {
		 		            	String sqlStmt = new String();
		 						if(rdbtnTemporalSlices.isSelected()) {
		 							/* 
		 							SELECT id, title, publish_date FROM articles WHERE id IN (SELECT id FROM articles WHERE publish_date BETWEEN "2020-02-01" AND "2020-02-29" ORDER BY RANDOM() LIMIT 100) 
		 							UNION 
		 							SELECT id, title, publish_date FROM articles WHERE publish_date BETWEEN "2020-03-01" AND "2020-03-31";
		 							*/
		 							for(int i = 0; i < tModel.getRowCount(); i++) {
	 									String dateFrom = txtYyyy.getText() + "-" + txtMm.getText() + "-" + txtDd.getText();
	 					            	String dateTo = txtYyyy_1.getText() + "-" + txtMm_1.getText() + "-" + txtDd_1.getText();
		 								if(tModel.getValueAt(i, 2).equals("true")) {
		 									sqlStmt += "SELECT * FROM articles WHERE id IN (SELECT id FROM articles WHERE publish_date BETWEEN '"+ dateFrom +"' AND '"+ dateTo + "' ORDER BY RANDOM() LIMIT "+ textField_1.getText() +") ";
		 								} else {
		 									sqlStmt += "SELECT * FROM articles WHERE publish_date BETWEEN '"+ dateFrom +"' AND '"+ dateTo +"' ";
		 								}
		 								if(i < tModel.getRowCount()-1) {
		 									sqlStmt += "UNION ";
		 								}
		 							}
		 							sqlStmt += ";";
		 						} else {
		 							sqlStmt = txtInvocation.getText() + " " + txtQuery.getText();
		 						}
		 						System.out.println(sqlStmt);
				            	Statement stmt3 = conn2.createStatement();
				            	try(ResultSet rs = stmt3.executeQuery(sqlStmt)){
				            		while (rs.next()) {	
				            			String sqlStmt2 = "INSERT INTO articles VALUES (Null, ?, ?, ?, ?, ? , ?);";
				            			PreparedStatement pstmt = conn.prepareStatement(sqlStmt2);
				            			pstmt.setString(1, rs.getString("title"));
				            			pstmt.setString(2, rs.getString("publish_date"));
				            			pstmt.setString(3, rs.getString("authors"));
				            			pstmt.setString(4, rs.getString("text"));
				            			pstmt.setString(5, rs.getString("url"));
				            			pstmt.setString(6, rs.getString("access"));
				            			pstmt.execute();
				            		}
				            	}
				            	
		 		            }
		            	 }
		            JOptionPane.showMessageDialog(null, "Ready!");
		            }
		        } catch (SQLException e) {
		            System.out.println(e.getMessage());
		        }
			}
		});
		
		JScrollPane scrollPaneDb = new JScrollPane();
		
		
		GroupLayout gl_downsampler = new GroupLayout(downsampler);
		gl_downsampler.setHorizontalGroup(
			gl_downsampler.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_downsampler.createSequentialGroup()
					.addGroup(gl_downsampler.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_downsampler.createSequentialGroup()
							.addGap(222)
							.addGroup(gl_downsampler.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_downsampler.createSequentialGroup()
									.addComponent(txtYyyy, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(txtMm, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(txtDd, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
									.addGap(28)
									.addComponent(txtYyyy_1, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(txtMm_1, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
									.addGap(6)
									.addComponent(txtDd_1, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_downsampler.createSequentialGroup()
									.addGap(18)
									.addComponent(chckbxRandomSampleOf)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_downsampler.createParallelGroup(Alignment.LEADING, false)
								.addComponent(btnAdd, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnCountRows, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addGroup(gl_downsampler.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_downsampler.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_downsampler.createSequentialGroup()
									.addComponent(button)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_downsampler.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_downsampler.createSequentialGroup()
											.addComponent(lblDatabase)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(txtSourceDb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(button_1))
										.addGroup(gl_downsampler.createSequentialGroup()
											.addComponent(rdbtnTemporalSlices)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(lblfromYyyy))))
								.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_downsampler.createSequentialGroup()
									.addGroup(gl_downsampler.createParallelGroup(Alignment.LEADING)
										.addComponent(lblTargetDb_1)
										.addGroup(gl_downsampler.createSequentialGroup()
											.addGap(19)
											.addComponent(rdbtnQuery)))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_downsampler.createParallelGroup(Alignment.LEADING)
										.addComponent(btnOkDownsample)
										.addGroup(gl_downsampler.createSequentialGroup()
											.addComponent(txtTargetDb2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(btnCreate_1, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(button_2))
										.addComponent(txtQuery, GroupLayout.PREFERRED_SIZE, 495, GroupLayout.PREFERRED_SIZE))))))
					.addContainerGap(26, Short.MAX_VALUE))
		);
		gl_downsampler.setVerticalGroup(
			gl_downsampler.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_downsampler.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_downsampler.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblDatabase)
						.addComponent(txtSourceDb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(button_1))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_downsampler.createParallelGroup(Alignment.BASELINE)
						.addComponent(rdbtnTemporalSlices)
						.addComponent(lblfromYyyy))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_downsampler.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtYyyy, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtMm, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtDd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtYyyy_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtMm_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtDd_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnCountRows))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_downsampler.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_downsampler.createParallelGroup(Alignment.BASELINE)
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(button)
							.addComponent(btnAdd))
						.addComponent(chckbxRandomSampleOf))
					.addGap(7)
					.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
					.addGap(18)
					.addGroup(gl_downsampler.createParallelGroup(Alignment.BASELINE)
						.addComponent(rdbtnQuery)
						.addComponent(txtQuery, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(16)
					.addGroup(gl_downsampler.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblTargetDb_1)
						.addComponent(txtTargetDb2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(button_2)
						.addComponent(btnCreate_1))
					.addGap(13)
					.addComponent(btnOkDownsample)
					.addContainerGap())
		);
		
		scrollPane_1.setViewportView(table);
		downsampler.setLayout(gl_downsampler);

		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Settings", null, panel, null);
		
		JLabel lblSqlQueryInvocation = new JLabel("SQL query invocation:");
		
		txtInvocation = new JTextField();
		txtInvocation.setText("SELECT * FROM articles WHERE publish_date");
		txtInvocation.setColumns(10);
		
		JButton btnOkSettings = new JButton("OK");
		btnOkSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				invocation = txtInvocation.getText();
				pythonPath = txtPythonPath.getText();
			}
		});
		
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblSqlQueryInvocation)
						.addComponent(lblPythonPath))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(btnOkSettings)
						.addComponent(txtPythonPath, GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
						.addComponent(txtInvocation, GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblSqlQueryInvocation)
						.addComponent(txtInvocation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPythonPath)
						.addComponent(txtPythonPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnOkSettings)
					.addContainerGap(296, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);		
		
	}
}

