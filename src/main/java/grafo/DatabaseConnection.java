package grafo;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
/**
 * AdHoc class. Requires AdHoc db.
 * @author Luigi
 *
 */
public class DatabaseConnection {
	private Connection connection = null;
	private LogUtilsRepeatingGraph utils = null;

	public DatabaseConnection(LogUtilsRepeatingGraph utils) {
		this.utils=utils;

		try {

//			String urlTest = "jdbc:mysql://localhost:3306/log_data_analysis";
//			Properties infoTest = new Properties();
//			infoTest.put("user", "root");
//			infoTest.put("password", "password");

			String url = "jdbc:mysql://90.147.42.48:3306/log_data_analysis";
			Properties info = new Properties();
			info.put("user", "luigi");
			info.put("password", "luigi_DB_2021");
			//connection = DriverManager.getConnection(url, info);
			connection = DriverManager.getConnection(url, info);
			if (connection != null) {
				System.out.println("Connected to the database db_data_analysis");
			}

		} catch (SQLException ex) {
			System.out.println("An error occurred. Cannot connect or User/Password is invalid");
			ex.printStackTrace();
		}
	}

	public void insertAll(String etichettaDataset, String notaDataset) {
		
		System.out.println("insertAll started");
		long startingTime = System.currentTimeMillis();

		if(!etichettaDataset.equals("A")) {
			insertDataset(etichettaDataset,notaDataset);
			System.out.println("dataset inserted successfully");
		}

		int n = utils.getGraphList().size();
		for(int i=0;i<n;i++) {
			Graph g= utils.getGraphList().get(i);
			File f=utils.getFileList()[i];
			List<Integer> lengths = new ArrayList<Integer>(6);

			int lenSuperSetAttivita=0;
			int lenSuperSetAttivitaR=0;
			int lenSuperSetAttivitaNR=0;
			int lenSuperSetTransizioni=0;
			int lenSuperSetTransizioniR=0;
			int lenSuperSetTransizioniNR=0;

			Iterator<Node> nIt= g.nodes().iterator();
			Iterator<Edge> eIt= g.edges().iterator();

			while(nIt.hasNext()) {
				Node node= nIt.next();
				lenSuperSetAttivita++;
				if( ((String)node.getAttribute("ui.label")).charAt(0)=='R' )
					lenSuperSetAttivitaR++;
				else lenSuperSetAttivitaNR++;

			}

			while(eIt.hasNext()) {
				Edge edge= eIt.next();
				lenSuperSetTransizioni++;
				if( ((String)edge.getAttribute("ui.label")) !=null )
					lenSuperSetTransizioniR++;
				else lenSuperSetTransizioniNR++;

			}

			lengths.add(lenSuperSetAttivita);
			lengths.add(lenSuperSetAttivitaR);
			lengths.add(lenSuperSetAttivitaNR);
			lengths.add(lenSuperSetTransizioni);
			lengths.add(lenSuperSetTransizioniR);
			lengths.add(lenSuperSetTransizioniNR);

			int numeroTracce = utils.getTraceNum()[i];
			double lenMediaTracce = utils.getAvgTraceLen()[i];
			String nomeLog=f.getName();
			insertLog(nomeLog, etichettaDataset,lengths,null,numeroTracce,lenMediaTracce,null,null,null,null);

			System.out.println("log inserted successfully");

			nIt = g.nodes().iterator();
			while(nIt.hasNext()) {
				boolean isR = false;
				Node node= nIt.next();
				String activityName = node.getId();
				if(node.getAttribute("ui.label")!=null) {
					if( ((String)node.getAttribute("ui.label")).charAt(0)=='R' )
						isR=true;
				}
				insertActivity(activityName,nomeLog,etichettaDataset,null,isR,null);
			}
			
			System.out.println("activities inserted successfully");

			eIt = g.edges().iterator();
			while(eIt.hasNext()) {
				boolean isR=false;
				Edge edge=eIt.next();
				String transitionName=edge.getId();
				if(edge.getAttribute("ui.label")!=null) {
					if( ((String)edge.getAttribute("ui.label")).charAt(0)=='R' )
						isR=true;
				}
				insertTransition(transitionName,nomeLog,etichettaDataset,null,isR,null);
			}
			
			System.out.println("transitions inserted successfully");

		}
		List<Double> params = new ArrayList<Double>();
		params.add(utils.getGamma());
		params.add(utils.getNodeEqualScore());
		params.add(utils.getNodeNotEqualScore());
		params.add(utils.getNodeSemiScore());
		params.add(utils.getEdgeEqualScore());
		params.add(utils.getEdgeNotEqualScore());
		params.add(utils.getEdgeSemiScore());
		insertClusterization(params,null,null);
		
		System.out.println("clusterization inserted successfully");
		
		System.out.println("insertAll finished, time: "+(System.currentTimeMillis()-startingTime));
	}

	public void insertDataset(String etichettaDataset, String notaDataset) {
		try {
			PreparedStatement statement= connection.prepareStatement("INSERT INTO dataset values ('"+etichettaDataset+"','"+notaDataset+"')");
			statement.execute();
		} catch (SQLException e) {
			System.out.println("An error occurred. Cannot insert data into dataset table");
			e.printStackTrace();
		}
	}

	public void insertLog(String nomeLog, String etichettaDataset, List<Integer> lengths, Boolean hasModello, Integer numeroTracce,
			double lenMediaTracce, Integer lenAlfabetoSottostringhe, Integer lenMaxRepeat, Integer lenRepeatCate, String note ) {
		try {
			PreparedStatement statement = connection.prepareStatement("INSERT INTO log values('"+nomeLog+"','"+etichettaDataset+
					"',"+lengths.get(0)+","+lengths.get(1)+","+lengths.get(2)+","+lengths.get(3)+
					","+lengths.get(4)+","+lengths.get(5)+","+hasModello+","+numeroTracce+","+lenMediaTracce+
					","+lenAlfabetoSottostringhe+","+lenMaxRepeat+","+lenRepeatCate+",'"+note+"')");
			statement.execute();

		} catch (SQLException e) {
			System.out.println("An error occurred. Couldn't insert data into log table");
			e.printStackTrace();
		}
	}

	public void insertActivity(String activityName, String nomeLog, String etichettaDataset, String asciiCode, Boolean isRepeating, String note) {

		try {
			PreparedStatement statement = connection.prepareStatement("INSERT INTO attivita values('"+activityName+"','"+nomeLog+
					"','"+etichettaDataset+"','"+asciiCode+"',"+isRepeating+",'"+note+"')");
			statement.execute();
		} catch (SQLException e) {
			System.out.println("An error occurred. Couldn't insert data into attivita table");
			e.printStackTrace();
		}

	}

	public void insertTransition(String transitionName, String nomeLog, String etichettaDataset, String transitionCoded, Boolean isRepeating, 
			String note) {

		try {
			PreparedStatement statement = connection.prepareStatement("INSERT INTO transizione values('"+transitionName+"','"+nomeLog+
					"','"+etichettaDataset+"','"+transitionCoded+"',"+isRepeating+",'"+note+"')");
			statement.execute();
		} catch (SQLException e) {
			System.out.println("An error occurred. Couldn't insert data into transizione table");
			e.printStackTrace();
		}

	}

	public void insertClusterization(List<Double> params, Double silhouetteMedia, String note) {
		try {
			PreparedStatement statement = connection.prepareStatement("INSERT INTO clusterizzazione (PesoAttivitaTransizioni1_0,"
					+ "ScoreAttivitaEqual,ScoreAttivitaNotEqual,ScoreAttivitaSemiEqual,ScoreTransizioniEqual,ScoreTransizioniNotEqual,"
					+ "ScoreTransizioniSemiEqual,SilhouetteMedia,note) values("+params.get(0)+","+
					params.get(1)+","+params.get(2)+","+params.get(3)+","+params.get(4)+","+params.get(5)+","+params.get(6)+
					","+silhouetteMedia+",'"+note+"')");
			statement.execute();
		} catch (SQLException e) {
			System.out.println("An error occurred. Couldn't insert data into clusterizzazione table");
			e.printStackTrace();
		}

	}



}
