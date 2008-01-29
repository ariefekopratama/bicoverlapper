package biclustering;

import java.util.*;
import java.io.*;

// potrebno radi Bica GUI
import javax.swing.ProgressMonitor;

// new, added 22.02.2005:
//import bicat.run_machine.Bicluster_bitset;

/**
 * Class that performs Bimax Algorithm
 * TODO: STILL  IN DEVELOPMENT
 */
public class BiMax {

	// ===========================================================================
	// public BiMax() { }

	static BitSet[] geneStars;

	static BitSet[] chipStars;

	static int geneCount;

	static int chipCount;

	static LinkedList bcs_list;

	static ProgressMonitor pm;

	static int max;

	static BitSet bs;

	static int[][] bm;

	// ===========================================================================
	public BiMax() {
		pm = null;
		bm = null;
	}

	// ===========================================================================
	public static void run(int[][] matrix, int gd, int cd, String w, String pp,
			int g_low, int c_low, String filename) {

		System.out.println("BICA: size is " + gd + ", " + cd);
		initializeStars(matrix, gd, cd);

		BitSet gene_mask = new BitSet(geneCount);
		gene_mask.flip(0, geneCount);
		BitSet chip_mask = new BitSet(chipCount);
		chip_mask.flip(0, chipCount);

		Biclustering bcing = new Biclustering(geneStars, chipStars);
		// added, 17.03.2005:
		bcing.setBiclusterSizeConstraints(g_low, c_low);

		Vector ON = new Vector();
		ON.add(chip_mask);

		System.out.println("Running B3d-RZ...");
		// HashMap BCs = new HashMap();
		// HashMap BCs = bcing.computeBiclusters(gene_mask,chip_mask,chip_mask,
		// new BitSet(chipCount), "top",0); // CORRECT THIS!
		// HashMap BCs = bcing.computeBiclustersSwitch_RULES(gene_mask,
		// chip_mask, ON, new Vector(), "a", 0);
		// pm.close();

		HashMap BCs; /*
						 * bcing.computeBiclustersSwitch_correct_cleans(gene_mask,
						 * chip_mask, chip_mask, new BitSet(chipCount), "top",
						 * 0);
						 */
		if (gd > cd) {
			bcing = new Biclustering(geneStars, chipStars);
			// bcing.setBiclusterSizeConstraints(new
			// Integer(args[4]).intValue(), new Integer(args[5]).intValue());
			BCs = bcing.computeBiclusters(gene_mask, chip_mask, chip_mask,
					new BitSet(chipCount), "top", 0, false);
			/*
			 * BCs = bcing.computeBiclusters_NewDataStructure(bm, chip_mask, new
			 * BitSet(chipCount), "top", 0, false);
			 */
		} else {
			bcing = new Biclustering(chipStars, geneStars);
			// bcing.setBiclusterSizeConstraints(new
			// Integer(args[5]).intValue(), new Integer(args[4]).intValue());
			BCs = bcing.computeBiclusters(chip_mask, gene_mask, gene_mask,
					new BitSet(geneCount), "top", 0, false);
		}

		if (gd < cd)
			// printToFile(BCs,args[3]);
			// else
			// printToFile_reverse(BCs, args[3]);
			BCs = reverse(BCs);

		for (int i = 0; i < BCs.size(); i++) { // ovo je ovdje SKROZ bezveze!
			// if(true == pm.isCanceled()) break;
			// pm.setProgress((i>max) ? (max-1): i);
		}
		// if(pm != null) pm.close();

		// System.out.println("Recursive Bica: biclusters are COMPUTED!");
/*
		copyToList(BCs);

		System.out.println(BCs.size());

		// System.out.println("Recursive Bica; biclusters are COMPUTED (and
		// transferred to a list)!");

		if (pp.equals("pp")) {
			printToFileList(filename);
			System.out.println("Maximal biclusters stored in " + filename
					+ " (" + bcs_list.size() + ").\n");
		} else if ("pp_size".equals(pp)) {
			printToFileListSize(filename);
			System.out.println("Maximal biclusters stored (by size) in "
					+ filename + " (" + bcs_list.size() + ").\n");
		}
*/
	}

	static HashMap reverse(HashMap hm) {
		HashMap result = new HashMap();

		Set keys = hm.keySet();
		Object[] karr = keys.toArray();
		for (int i = 0; i < karr.length; i++) {
			BitSet g = (BitSet) ((BitSet) karr[i]).clone();
			result.put((BitSet) hm.get(g), g);
			g = null;
		}

		hm = null;
		keys = null;
		karr = null;

		return result;
		// return Bicluster.reverseHash(BCs);
		// return null;
	}

	// ===========================================================================
	static void initializeStars(int[][] matrix, int gd, int cd) {

		geneCount = gd;
		chipCount = cd;

		geneStars = new BitSet[geneCount];
		chipStars = new BitSet[chipCount];

		if (null != pm) { // cemu ovo sluzi?
			System.out.println("Initializing Bica...");

			max = (2 << matrix[0].length * matrix.length) / 100;

			pm.setMinimum(0);
			pm.setMaximum(max);
			pm.setProgress(0);

			pm.setMillisToDecideToPopup(0);
			pm.setMillisToPopup(0);
		}

		for (int i = 0; i < geneCount; i++) {
			BitSet gs = new BitSet(chipCount);
			for (int j = 0; j < chipCount; j++)
				if (matrix[i][j] == 1)
					gs.set(j);
			geneStars[i] = gs;
			gs = null;
		}

		for (int i = 0; i < chipCount; i++) {
			BitSet cs = new BitSet(geneCount);
			for (int j = 0; j < geneCount; j++)
				if (geneStars[j].get(i))
					cs.set(j);
			chipStars[i] = cs;
			cs = null;
		}

	}

	// ===========================================================================
	/*static void printToFileList(String filename) {
		try {
			File file = new File(filename);
			FileWriter fw = new FileWriter(file);

			System.out.print("" + bcs_list.size() + " ");

			fw.write("Number of BCs = " + bcs_list.size() + "\n\n");
			for (int i = 0; i < bcs_list.size(); i++) {
				((Bicluster_bitset) bcs_list.get(i)).printOut();
			}

			fw.close();
		} catch (IOException e) {
			System.err.println(e);
			// System.exit(1);
		}
	}

	// ===========================================================================
	static void printToFileListSize(String filename) {
		try {
			File file = new File(filename);
			FileWriter fw = new FileWriter(file);

			System.out.print("" + bcs_list.size() + " ");

			Collections.sort(bcs_list);

			fw.write("Number of BCs = " + bcs_list.size() + "\n\n");
			for (int i = 0; i < bcs_list.size(); i++) {
				((Bicluster_bitset) bcs_list.get(i)).printOut();
			}

			fw.close();
		} catch (IOException e) {
			System.err.println(e);
			// System.exit(1);
		}
	}

	// ===========================================================================
	static void copyToList(HashMap BCs) {
		bcs_list = new LinkedList();

		Set keys = BCs.keySet();
		Object[] ka = keys.toArray();
		for (int i = 0; i < ka.length; i++)
			bcs_list.add(new Bicluster_bitset((BitSet) ka[i], (BitSet) BCs
					.get(ka[i])));

		// Collections.reverse_sort(bcs_list);
		// Collections.reverse(bcs_list);
		Collections.sort(bcs_list);
	}
	*/

	// ===========================================================================
	public BiMax(ProgressMonitor m) {
		pm = m;
	}

	// ===========================================================================
	public LinkedList getBiclusters() {
		return bcs_list;
	}

	// ===========================================================================
	static void readDiscretizedMatrixFile(String filename) {
		try {
			File file = new File(filename);
			FileReader fr = new FileReader(file);
			StreamTokenizer st = new StreamTokenizer(fr);

			geneStars = new BitSet[geneCount];
			chipStars = new BitSet[chipCount];

			for (int i = 0; i < geneCount; i++) {
				BitSet gs = new BitSet(chipCount);
				for (int j = 0; j < chipCount; j++) {
					st.nextToken();
					if ((int) st.nval == 1)
						gs.set(j);
				}
				geneStars[i] = gs;
				gs = null;
			}
			fr.close();

			for (int i = 0; i < chipCount; i++) {
				BitSet cs = new BitSet(geneCount);
				for (int j = 0; j < geneCount; j++) {
					if (geneStars[j].get(i))
						cs.set(j);
				}
				chipStars[i] = cs;
				cs = null;
			}

			// check if it is read ok
			// for(int i =0; i<geneCount; i++) {
			// for(int j=0; j<chipCount; j++)
			// if(geneStars[i].get(j)) System.out.print("1 ");
			// else System.out.print("0 ");
			// System.out.println();
			// }

			bm = new int[geneStars.length][chipStars.length];
			for (int i = 0; i < geneStars.length; i++) {
				int j = 0;
				for (j = geneStars[i].nextSetBit(j); j >= 0; j = geneStars[i]
						.nextSetBit(j + 1))
					bm[i][j] = 1;
			}

		} catch (FileNotFoundException e) {
			System.err.println(e);
			// System.exit(1);
		} catch (IOException e) {
			System.err.println(e);
			// System.exit(1);
		}
	}

	// ===========================================================================
	static void printToFile(HashMap bcs, String filename) {
		try {
			File file = new File(filename);
			FileWriter fw = new FileWriter(file);

			// System.out.print(""+bcs.size()+" ");
			// fw.write("Number of BCs = "+bcs.size()+"\n\n");

			Set keys = bcs.keySet();
			Object[] ka = keys.toArray();
			for (int i = 0; i < ka.length; i++) {
				BitSet g = (BitSet) ((BitSet) ka[i]).clone();
				BitSet c = (BitSet) ((BitSet) bcs.get(ka[i])).clone();

				// fw.write("BC "+g.cardinality()+" "+c.cardinality()+"\n");
				fw.write(g.cardinality() + " " + c.cardinality() + "\n");
				for (int j = g.nextSetBit(0); j >= 0; j = g.nextSetBit(j + 1))
					// fw.write((j+1)+" ");
					fw.write("gene" + j + " ");
				fw.write("\n");
				for (int j = c.nextSetBit(0); j >= 0; j = c.nextSetBit(j + 1))
					fw.write("chip" + j + " ");
				// fw.write((j+1)+" ");
				fw.write("\n");

				// bcs.remove(g); //???? (clean progressively???)
				g = null;
				c = null;
			}

			bcs = null;
			fw.close();
		} catch (IOException e) {
			System.err.println(e);
			// System.exit(1);
		}

	}

	// ===========================================================================
	static void printToFile_reverse(HashMap bcs, String filename) {
		try {
			File file = new File(filename);
			FileWriter fw = new FileWriter(file);

			// System.out.print(""+bcs.size()+" ");
			// fw.write("Number of BCs = "+bcs.size()+"\n\n");

			Set keys = bcs.keySet();
			Object[] ka = keys.toArray();
			for (int i = 0; i < ka.length; i++) {
				BitSet g = (BitSet) ((BitSet) ka[i]).clone();
				BitSet c = (BitSet) ((BitSet) bcs.get(ka[i])).clone();

				fw.write(c.cardinality() + " " + g.cardinality() + "\n");
				for (int j = c.nextSetBit(0); j >= 0; j = c.nextSetBit(j + 1))
					fw.write("gene" + (j) + " ");
				fw.write("\n");
				for (int j = g.nextSetBit(0); j >= 0; j = g.nextSetBit(j + 1))
					fw.write("chip" + (j) + " ");
				fw.write("\n");

				// bcs.remove(g); //???? (clean progressively???)
				g = null;
				c = null;
			}

			bcs = null;
			fw.close();
		} catch (IOException e) {
			System.err.println(e);
			// System.exit(1);
		}

	}

	// ===========================================================================
	static int get_larger_than(int b, HashMap bcs) {
		int cnt = 0;

		// for(int i=0; i< bcs.size(); i++) {
		// Bicluster bc = (Bicluster)bcs.get(i);
		// }

		Set keys = bcs.keySet();
		Object[] arr = keys.toArray();
		for (int i = 0; i < arr.length; i++) {
			BitSet gs = (BitSet) arr[i];
			if (gs.cardinality() >= b) {
				BitSet cs = (BitSet) bcs.get(gs);
				if (cs.cardinality() >= b)
					cnt++;
			}
		}
		return cnt;
	}

	// ===========================================================================
	static void readline_from_file(String filename) {
		try {
			File file = new File(filename);
			FileReader fr = new FileReader(file);
			StreamTokenizer st = new StreamTokenizer(fr);

			bs = new BitSet(chipCount);
			for (int i = 0; i < chipCount; i++) {
				st.nextToken();
				if ((int) st.nval == 1)
					bs.set(i);
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
			// System.exit(1);
		} catch (IOException e) {
			System.err.println(e);
			// System.exit(1);
		}

	}

	// ===========================================================================
	// public static void main(String[] args) {
	//
	// // read the input matrix
	// // ... initialize the CHIP and GENE stars
	// geneCount = new Integer(args[1]).intValue();
	// chipCount = new Integer(args[2]).intValue();
	//
	// readDiscretizedMatrixFile(args[0]);
	//
	// BitSet gene_mask = new BitSet(geneCount);
	// gene_mask.flip(0,geneCount);
	// BitSet chip_mask = new BitSet(chipCount);
	// chip_mask.flip(0,chipCount);
	//
	// Biclustering bcing;
	// HashMap BCs;
	// if(geneCount > chipCount) {
	// bcing = new Biclustering(geneStars, chipStars);
	// bcing.setBiclusterSizeConstraints(new Integer(args[4]).intValue(), new
	// Integer(args[5]).intValue());
	// BCs = bcing.computeBiclusters(gene_mask,chip_mask,
	// chip_mask, new BitSet(chipCount),
	// "top",0, false);
	// /*
	// BCs = bcing.computeBiclusters_NewDataStructure(bm, chip_mask, new
	// BitSet(chipCount), "top", 0, false);
	// */
	// }
	// else {
	// bcing = new Biclustering(chipStars, geneStars);
	// bcing.setBiclusterSizeConstraints(new Integer(args[5]).intValue(), new
	// Integer(args[4]).intValue());
	// BCs = bcing.computeBiclusters(chip_mask,gene_mask,
	// gene_mask, new BitSet(geneCount),
	// "top",0, false);
	// /*
	// BCs = bcing.computeBiclusters_NewDataStructure(bm, gene_mask, new
	// BitSet(geneCount), "top", 0, false);
	// */
	//
	// }
	//
	// //bcing.setBiclusterSizeConstraints(new Integer(args[4]).intValue(), new
	// Integer(args[5]).intValue());
	// //HashMap BCs = bcing.computeBiclusters(gene_mask,chip_mask, chip_mask,
	// new BitSet(chipCount),"top",0, false);
	//
	//
	// ////////////////////////////////////////////////////////////////////////////
	//
	// int tri;
	// int cetiri;
	// int pet;
	//
	// tri = get_larger_than(3,BCs);
	// cetiri = get_larger_than(4,BCs);
	// pet = get_larger_than(5,BCs);
	//
	// System.out.println(BCs.size()+"\t"+tri+"\t"+cetiri+"\t"+pet);
	//
	// // print out the results
	// if(geneCount > chipCount)
	// printToFile(BCs,args[3]);
	// else
	// printToFile_reverse(BCs, args[3]);
	// }

}