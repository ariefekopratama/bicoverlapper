package es.usal.bicoverlapper.model.bubble;

import prefuse.data.Graph;
import prefuse.data.Table;

//Parsing
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;

import es.usal.bicoverlapper.controller.data.fileStructure.FileStructure;
import es.usal.bicoverlapper.controller.kernel.Session;
import es.usal.bicoverlapper.model.microarray.MicroarrayData;
import es.usal.bicoverlapper.view.diagram.overlapper.Overlapper;

/**
 * Class with data of Bubbles representing biclusters, using Prefuse Tables.
 * Each bubble is an entry in a Prefuse table with the following fields:
 * <p>
 * "id" -unique identifier for the bubble (int)-,
 * <p>
 * "genes" -names of the genes contained by this bubble (ArrayList<String>)-,
 * <p>
 * "conditions" -names of the conditions contained by this bubble
 * (ArrayList<String>)-,
 * <p>
 * "size" -size of the bubble, as length(genes)*length(conditions) (int)-,
 * <p>
 * "x" -x coordinate for the bubble, after a 2D projection (float)-,
 * <p>
 * "y" -y coordinate for the bubble, after a 2D projection (float)-,
 * <p>
 * "goodness" -STILL IN DEVELOPMENT-,
 * <p>
 * "homogeneity" -STILL IN DEVELOPMENT- and
 * <p>
 * "resultType" - name of the buicluster set from which the bicluster of this
 * bubble comes (String)
 * 
 * @author Rodrigo Santamaría
 */

public class BubbleData {
	private Graph g = null;
	private Table nodes = null;
	private Table edges = null;
	private FileStructure fsg = null;
	private FileStructure fsc = null;
	private FileStructure fsgn = null;
	private int numberOfMethods = 0;

	private HashMap<String, Integer> genes = new HashMap<String, Integer>();
	private HashMap<String, Integer> conditions = new HashMap<String, Integer>();
	private ArrayList<Bubble> bubbles = new ArrayList<Bubble>();
	private double maxHomogeneity;
	private double minHomogeneity;

	/**
	 * Default constructor
	 * 
	 */
	public BubbleData() {
	}

	/**
	 * File constructor. A bubble is generated for each bicluster in the
	 * required file. Size is the product of the number of genes and the number
	 * of columns. TODO: homogeneity right now is set to 0.5 for every bubble.
	 * TODO: add the kind of projection (now, by default, a hand-made projection
	 * is used)
	 * 
	 * @param filePath
	 *            Bicluster results file in the corresponding format
	 * 
	 */
	public BubbleData(String filePath, MicroarrayData md, Session sesion) throws IOException {
		// Hashmaps with gene and condition names and their positions
		FileReader fr = new FileReader(filePath);
		BufferedReader br = new BufferedReader(fr);
		String cad = "";
		String method = "";
		int cont = 0;
		int contg = 0;
		int contc = 0;
		Bubble b = null;

		maxHomogeneity = -1;
		minHomogeneity = -1;
		boolean areGenes = true;
		boolean isSize = true;
		boolean skipNext = false;
		cont = 0;
		cad = br.readLine(); // La primera línea contiene el número de
								// biclusters

		//Carlos, esto tenderá a desaparecer probablemente
		if (null != cad) {
			int numBiclusters = Integer.parseInt(cad);
			if (numBiclusters >= 200) {
				JOptionPane.showMessageDialog(
								null,
								"Too many biclusters found. Please, adjust the parameters in order to find less biclusters.",
								"Biclusters error", JOptionPane.ERROR_MESSAGE);
			}
		}	

		while ((cad = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(cad, "\t");
			if (st.countTokens() == 1 && !skipNext) {
				method = st.nextToken();
				numberOfMethods++;
			} else {
				skipNext = false;
				if (isSize)// size info, no interesa
				{
					isSize = false;
					b = new Bubble();
					if (cad.contains(":")){
						//antes estaba así
						//b.name = new StringTokenizer(cad, ":").nextToken();
						StringTokenizer stAux = new StringTokenizer(cad, ":");
						b.name = stAux.nextToken();
						System.out.println("arriba b.name="+b.name);
						/*
						if(stAux.hasMoreTokens()){
							int numNodos;
							try{
								String numeroNodos = new StringTokenizer(stAux.nextToken(), "\t").nextToken();
								numNodos = Integer.parseInt(numeroNodos.trim());
								//se establece si va a ser posible abrir las vistas de bubble map y overlapper
								if(numNodos > Session.MAX_NODES){
									sesion.setTooManyGenes(true);
									sesion.setNumNodes(numNodos);
								}
								System.out.println("numNodos="+numNodos);
							} catch(NumberFormatException nfe){
								System.err.println("Error al obtener el numNodos en BubbleData");
								nfe.printStackTrace();
							}
						}
						*/
					
					}
					else{
						b.name = "" + cont;
						JOptionPane.showMessageDialog(
								null,
								"Biclustering format without bicluster names deprecated, please refer to format section on the help",
								"Biclustering format error", JOptionPane.ERROR_MESSAGE);
						
						System.err.println("Biclustering format without bicluster names deprecated, please refer to format section on the help");
					}
					cont++;
				} else {
					if (areGenes) {
						while (st.hasMoreTokens()) {
							String c = st.nextToken();
							b.genes.add(c);
							if (!genes.containsKey(c))
								genes.put(c, contg++);
						}
					} else {
						while (st.hasMoreTokens()) {
							String c = st.nextToken();
							b.conditions.add(c);
							if (!conditions.containsKey(c))
								conditions.put(c, contc++);
						}
						b.size = b.genes.size() * b.conditions.size();
						if (md == null)
							b.homogeneity = 0.5;
						else {
							String[] s = genes.keySet().toArray(new String[0]);
							ArrayList<String> gl = new ArrayList<String>();
							for (int i = 0; i < s.length; i++)
								gl.add(s[i]);
							s = conditions.keySet().toArray(new String[0]);
							ArrayList<String> cl = new ArrayList<String>();
							for (int i = 0; i < s.length; i++)
								cl.add(s[i]);
							b.homogeneity = md.getConstance(gl, cl, 0);
							if (maxHomogeneity == -1)
								maxHomogeneity = minHomogeneity = b.homogeneity;
							else {
								if (b.homogeneity > maxHomogeneity)
									maxHomogeneity = b.homogeneity;
								if (b.homogeneity < minHomogeneity)
									minHomogeneity = b.homogeneity;
							}
						}
						b.method = method;
						bubbles.add(b);
						isSize = true;// para el siguiente
					}
					areGenes = !areGenes;
				}
			}
			if (st.countTokens() > 0) {
				st = new StringTokenizer(cad, "\t");
				String firstToken = st.nextToken();
				if (firstToken.equals("1") || firstToken.endsWith(": 1"))
					skipNext = true;
			}
			cont++;
		}
		// Ahora tenemos que hacer la proyección
		if (bubbles.size() > 0) {
			doProjection();
			buildGraphFromProjection();
			g = new Graph(nodes, false);
		} else
			throw new IOException("No biclusters found");
	}

	/**
	 * Assigns points in the space to each bubble. By now, only by average, not
	 * MDS projections
	 * 
	 */
	void doProjection() {
		for (int i = 0; i < bubbles.size(); i++) {
			Bubble b = bubbles.get(i);
			float x = 0;
			float y = 0;

			for (int j = 0; j < b.genes.size(); j++) {
				String gen = b.genes.get(j);
				x += genes.get(gen).floatValue();
			}
			x /= b.genes.size();

			for (int j = 0; j < b.conditions.size(); j++) {
				String con = b.conditions.get(j);
				y += conditions.get(con).floatValue();
			}
			y /= b.conditions.size();

			b.position.x = x;
			b.position.y = y;
		}
	}

	// Build prefuse structure from bubbles projected with doProjection();
	void buildGraphFromProjection() {
		// Añadimos todos los identificadores
		nodes = new Table();
		nodes.addColumn("id", int.class);
		nodes.addColumn("genes", ArrayList.class);
		nodes.addColumn("conditions", ArrayList.class);
		nodes.addColumn("size", int.class);
		nodes.addColumn("height", int.class);
		nodes.addColumn("width", int.class);
		nodes.addColumn("x", float.class);
		nodes.addColumn("y", float.class);
		nodes.addColumn("goodness", float.class);// De momento paso de esto un
													// poco
		nodes.addColumn("homogeneity", float.class);// De momento paso de esto
													// un poco
		nodes.addColumn("resultType", String.class);
		nodes.addColumn("name", String.class);

		LinkedList<Integer> lista = new LinkedList<Integer>();

		lista.add(Integer.valueOf(1));
		lista.add(Integer.valueOf(2));
		lista.add(Integer.valueOf(3));

		int row = 0;
		for (int i = 0; i < bubbles.size(); i++) {
			Bubble b = bubbles.get(i);
			row = nodes.addRow();

			nodes.setInt(row, "id", row + 1);
			nodes.set(i, "genes", b.genes);// En principio son listas de
											// enteros!
			nodes.set(i, "conditions", b.conditions);
			nodes.set(row, "size", b.size);
			nodes.set(row, "width", b.conditions.size());
			nodes.set(row, "height", b.genes.size());
			nodes.set(row, "x", b.position.x);
			nodes.set(row, "y", b.position.y);
			nodes.set(row, "homogeneity", (b.homogeneity - minHomogeneity)
					/ (maxHomogeneity - minHomogeneity));
			// System.out.println("Bic de tam "+b.genes.size()+"x"+b.conditions.size()+" con homog "+b.homogeneity+"\t"+(b.homogeneity-minHomogeneity)/(maxHomogeneity-minHomogeneity));
			nodes.set(row, "resultType", b.method);
			nodes.set(row, "name", b.name);
		}
	}

	void buildBubbleTable(String bubbles) {
		nodes = new Table();
		nodes.addColumn("id", int.class);
		nodes.addColumn("genes", LinkedList.class);
		nodes.addColumn("conditions", LinkedList.class);
		nodes.addColumn("size", int.class);
		nodes.addColumn("homogeneity", float.class);// De momento paso de esto
													// un poco
		nodes.addColumn("x", float.class);
		nodes.addColumn("y", float.class);// De momento paso de esto un poco

		int row = 0;
		int n = fsg.numberOfRows();
		for (int i = 0; i < n; i++) {
			row = nodes.addRow();
			nodes.setInt(row, "id", row + 1);
			nodes.set(row, "x", fsg.readFloat(row, 0));
			nodes.set(row, "y", fsg.readFloat(row, 1));
			nodes.set(row, "size", fsg.readInt(row, 2));
			nodes.set(row, "homogeneity", fsg.readFloat(row, 3));
			row++;
		}
	}

	/**
	 * Construye los datos de Tuplas de Prefuse a partir de fichero de burbujas
	 * y de dos ficheros con los genes y condiciones de cada burbuja
	 */
	void buildNodeTable(String bubbles, String genes, String conditions) {
		// Añadimos todos los identificadores
		nodes = new Table();
		nodes.addColumn("id", int.class);
		nodes.addColumn("genes", LinkedList.class);
		nodes.addColumn("conditions", LinkedList.class);
		nodes.addColumn("size", int.class);
		nodes.addColumn("x", float.class);
		nodes.addColumn("y", float.class);
		nodes.addColumn("goodness", float.class);// De momento paso de esto un
													// poco
		nodes.addColumn("homogeneity", float.class);// De momento paso de esto
													// un poco
		nodes.addColumn("resultType", String.class);
		nodes.addColumn("name", String.class);

		int row = 0;
		int n = fsg.numberOfRows();
		if (n != fsc.numberOfRows() || n != fsgn.numberOfRows()) {
			System.err.println("Error: files not coherent");
			System.exit(1);
		}

		for (int i = 0; i < n; i++) {
			LinkedList<Integer> listag = fsg.readIntRow(i);
			LinkedList<Integer> listac = fsc.readIntRow(i);
			if (listag != null && listag.size() > 0 && listac != null
					&& listac.size() > 0) {
				row = nodes.addRow();
				nodes.setInt(row, "id", row + 1);
				listag.addFirst(-333);// Tengo que add al principio un valor
										// porque el primero no me lo coge!
				listac.addFirst(-333);
				nodes.set(row, "genes", listag);
				nodes.set(row, "conditions", listac);
				nodes.set(row, "size", listag.size() * listac.size());
				nodes.set(row, "x", fsgn.readFloat(row, 0));
				nodes.set(row, "y", fsgn.readFloat(row, 1));
				nodes.set(row, "homogeneity", fsgn.readFloat(row, 3));
				nodes.set(row, "resultType", "type" + fsgn.readInt(row, 4));
				nodes.set(row, "name", "fuica");

				row++;
			}
		}
		numberOfMethods++;
	}

	/**
	 * Añade nuevas filas, correspondientes a otros ficheros de resultados. Se
	 * distinguen unos de otros mediante la condición "resultType"
	 */
	void addResults(String bubbles, String genes, String conditions) {
		fsgn = new FileStructure(bubbles, ' ');
		fsg = new FileStructure(genes, ' ');
		fsc = new FileStructure(conditions, ' ');

		int row = 0;
		int n = fsg.numberOfRows();
		if (n != fsc.numberOfRows() || n != fsgn.numberOfRows()) {
			System.err.println("Error: files not coherent");
			System.exit(1);
		}

		for (int i = 0; i < n; i++) {
			LinkedList<Integer> listag = fsg.readIntRow(i);
			LinkedList<Integer> listac = fsc.readIntRow(i);
			if (listag != null && listag.size() > 0 && listac != null
					&& listac.size() > 0) {
				row = nodes.addRow();
				nodes.setInt(row, "id", row + 1);
				listag.addFirst(0);// Tengo que add al principio porque el
									// primero no me lo coge!
				listac.addFirst(0);
				nodes.set(row, "genes", listag);
				nodes.set(row, "conditions", listac);
				nodes.set(row, "size", listag.size() * listac.size());
				nodes.set(row, "x", fsgn.readFloat(row, 0));
				nodes.set(row, "y", fsgn.readFloat(row, 1));
				double hom = fsgn.readFloat(row, 3);
				nodes.set(row, "homogeneity", hom);
				// nodes.set(row, "resultType", "type"+numberOfMethods);
				nodes.set(row, "resultType", "type" + fsgn.readInt(row, 4));

				row++;
			}
		}
		numberOfMethods++;
	}

	void buildNodeTable(String genes, String conditions) {
		nodes = new Table();
		nodes.addColumn("id", int.class);
		nodes.addColumn("genes", LinkedList.class);
		nodes.addColumn("conditions", LinkedList.class);
		nodes.addColumn("size", int.class);
		nodes.addColumn("goodness", float.class);// De momento paso de esto un
													// poco

		int row = 0;
		int n = fsg.numberOfRows();
		for (int i = 0; i < n; i++) {
			LinkedList<Integer> listag = fsg.readIntRow(i);
			LinkedList<Integer> listac = fsc.readIntRow(i);
			if (i == 0) {
				for (int j = 0; j < listag.size(); j++)
					System.out.print(listag.get(j) + " ");
				// System.out.println("Lista de tamaño "+listag.size());
			}
			if (listag != null && listag.size() > 0 && listac != null
					&& listac.size() > 0) {
				row = nodes.addRow();
				nodes.setInt(row, "id", row + 1);
				listag.addFirst(-444);// Tengo que add al principio porque el
										// primero no me lo coge!
				listac.addFirst(-444);
				nodes.set(row, "genes", listag);
				nodes.set(row, "conditions", listac);
				nodes.set(row, "size", listag.size() * listac.size());
			}
		}
	}

	void buildEdgeTable(String adjacency) {
		edges = new Table();
		edges.addColumn("id", int.class);
		edges.addColumn("source", int.class);
		edges.addColumn("target", int.class);
		edges.addColumn("weight", int.class);
		// A la larga, añadiendo un weightGenes y un weightConditions

		int row = 0;
		int w = 0;
		// Leer la matriz de coincidencias
		FileStructure fsa = new FileStructure(adjacency, ' ');
		int n = fsa.numberOfRows();
		int m = fsa.numberOfCols();
		// System.out.println("La estructura es "+n+"x"+m);
		for (int i = 0; i < n; i++)
			for (int j = 0; j < m; j++) {
				if ((w = fsa.readInt(i, j)) > 0) {
					row = edges.addRow();
					edges.setInt(row, "id", row);
					edges.setInt(row, "source", i);
					edges.setInt(row, "target", j);
					edges.setInt(row, "weight", w);
					row++;
				}
			}
		// System.out.println("Número de aristas "+(row-1));
	}

	/**
	 * @deprecated
	 * @return Graph in wich BubbleData are sets as fictional nodes
	 */
	public Graph getGraph() {
		return g;
	}
}
