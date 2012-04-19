package es.usal.bicoverlapper.view.diagram.kegg;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import keggapi.*;

public class Kegg {
	
	private KEGGPortType serv;
	private static Kegg k;
	private List<KeggElement> keggElements = new ArrayList<KeggElement>();

	/**
	 * Binds the service to the soap port
	 * 
	 * @throws Exception
	 */
	public Kegg() throws Exception {

		KEGGLocator locator = new KEGGLocator();
		serv = locator.getKEGGPort();
	}

	public KEGGPortType getServ() {
		return serv;
	}

	public static Kegg getK() {
		return k;
	}

	public List<KeggElement> getKeggElements() {
		return keggElements;
	}

	public String generarImagenKegg(Kegg kegg, String pathway) throws Exception {
		long start = System.currentTimeMillis();

		k = kegg;
		// se consultan los elementos del pathway

		PathwayElement[] results = k.serv.get_elements_by_pathway(pathway);
		// se crea la lista con los resultados
		int[] element_id_list = new int[results.length];
		// lista de samples
		float[] samples = new float[element_id_list.length];
		// el random es temporal, sólo para coger samples aleatorios
		Random r = new Random();
		r.setSeed(new Date().getTime());

		// extraigo los element_id_list, lo de los colores es sólo ahora que no
		// tengo de dónde extraerlos y los genero aleatorios
		for (int i = 0; i < results.length; i++) {
			element_id_list[i] = results[i].getElement_id();

			// Por el momento se le dan valores aleatorios a las muestras entre
			// 0 y 100
			samples[i] = r.nextInt(101);

			// int[] components = results[i].getComponents();
			// String[] names = results[i].getNames();

			// System.out.println(results[i].getElement_id()+" "+results[i].getType()+" "+samples[i]);
			/*
			 * for (int component: components) {
			 * System.out.println("Component: "+component); }
			 * 
			 * for (String name: names) { System.out.println("Names: "+name); }
			 */
		}

		String[] fgs = new String[element_id_list.length];
		String[] bgs = new String[element_id_list.length];

		interpolateColors(pathway, element_id_list, samples, fgs, bgs);

		// como todo en principio debería haber mantenido el orden, ahora ya
		// tendría cada elemento con su fg y bg correspondiente
		// como el id es único para cada resultado, añado el resultado que es lo
		// que tiene más información
		for (int i = 0; i < results.length; i++) {
			// Se añade a la lista de elementos
			keggElements.add(new KeggElement(results[i], fgs[i], bgs[i]));
		}

		String url = k.colorKegg2(pathway, element_id_list, fgs, bgs);

		System.out.println("generarImagenKegg took "
				+ (System.currentTimeMillis() - start) / 1000 + " seconds");

		return url;
	}

	/**
	 * Retrieves all the pathways for a given organism
	 * 
	 * @param organism
	 * @return
	 * @throws Exception
	 */
	public String[] getPathwaysFromOrganism(String organism) throws Exception {
		Definition[] d = serv.list_pathways(organism);
		String[] paths = new String[d.length];
		for (int i = 0; i < d.length; i++) {
			System.out
					.println(d[i].getEntry_id() + ", " + d[i].getDefinition());
			paths[i] = d[i].getDefinition();
		}

		return paths;
	}

	/**
	 * Retrieves all the pathways for a given organism
	 * 
	 * @param organism
	 * @return
	 * @throws RemoteException
	 */
	public Definition[] getDefinitionPathwaysFromOrganism(String organism) throws RemoteException {
		Definition[] d = serv.list_pathways(organism);
		return d;
	}

	/**
	 * Retrieves the pathway id for a given pathway definition
	 * 
	 * @param path
	 * @param d
	 * @return
	 */
	public String getPathwayIdFromDefinition(String path, Definition[] d) {
		for (int i = 0; i < d.length; i++) {
			if (d[i].getDefinition().equals(path)) {
				return d[i].getEntry_id();
			}
		}
		return null;
	}

	/**
	 * Devuelve la lista de organismos
	 * 
	 * @return
	 * @throws Exception
	 */
	public String[] getOrganism() throws Exception {
		Definition[] d = serv.list_organisms();
		String[] paths = new String[d.length];
		for (int i = 0; i < d.length; i++) {
			// System.out.println(d[i].getEntry_id()+", "+d[i].getDefinition());
			paths[i] = d[i].getDefinition();
		}

		return paths;
	}

	/**
	 * Devuelve la clave de un organismo determinado
	 * 
	 * @param organism
	 *            Organismo del que se desea obtener la clave
	 * @return
	 * @throws Exception
	 */
	public String searchOrganism(String organism) throws Exception {
		Definition[] d = serv.list_organisms();

		for (int i = 0; i < d.length; i++) {
			if (d[i].getDefinition().startsWith(organism)) {
				return d[i].getEntry_id();
			}
		}

		return null;
	}

	/**
	 * * Retrieves all the organism for a given pathway
	 * 
	 * @param pathway
	 * @return
	 * @throws Exception
	 */
	public String[] getOrganismsFromPathway(String pathway) throws Exception {
		String d = serv.bfind("path " + pathway);
		String[] tokens = d.split("\n");
		ArrayList<String> orgs = new ArrayList<String>();
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].contains(" - ")) {
				String org = "";
				if (tokens[i].indexOf(");") > 0 || tokens[i].endsWith(")")) // Species with vulgar name
					org = tokens[i].substring(tokens[i].indexOf(" - ") + 3,
							tokens[i].indexOf("(")).trim();
				// Species without vulgar name
				else if (tokens[i].indexOf(";") >= 0)
					org = tokens[i].substring(tokens[i].indexOf(" - ") + 3,
							tokens[i].indexOf(";")).trim();
				if (org.length() > 0) {
					// System.out.println(org);
					if (!orgs.contains(org))
						orgs.add(org);
				}
			}
		}
		tokens = new String[orgs.size()];
		for (int i = 0; i < tokens.length; i++)
			tokens[i] = orgs.get(i);
		// System.out.println("getOrganismsFromPathway took "+(System.currentTimeMillis()-start)/1000+" seconds");
		return tokens;
	}

	/**
	 * returns all pathways DEPRECATED: use bioconductor KEGG instead (much
	 * faster)
	 * 
	 * @return
	 */
	public String[] getAllPathways() {
		try {
			ArrayList<String> ret = new ArrayList<String>();
			Definition[] orgs = serv.list_organisms();

			// System.out.println("We have "+orgs.length+" organisms");
			for (int i = 0; i < orgs.length; i++) {
				String org = orgs[i].getDefinition();
				org = org.substring(0, org.indexOf(" ("));

				Definition[] paths = serv.list_pathways(orgs[i].getEntry_id());
				// System.out.println("Organism "+org+" has "+paths.length+" pathways");
				for (int j = 1; j < paths.length; j++) {
					String path = paths[j].getDefinition();
					path = path.substring(0, path.indexOf(" - ")).replace("/","");
					// System.out.println(path+" in \t"+org);
					if (!ret.contains(path))
						ret.add(path);
				}
			}
			// System.out.println("A total of "+ret.size()+" pathways");
			String[] ret2 = new String[ret.size()];
			for (int i = 0; i < ret2.length; i++)
				ret2[i] = ret.get(i);
			return ret2;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Colors the elements in ids in the Kegg pathway pathid, with the
	 * foreground and background colors specified DEPRECATED: entrezgene ids are
	 * preferred to ko ids, see colorKegg2
	 * 
	 * @param pathid
	 *            something like pathway:hsa4031
	 * @param ids
	 *            ko ids like ko:K010267
	 * @param fgcolors
	 *            like #FF0000
	 * @param bgcolors
	 *            like #00FF00
	 * @return - the url to the colored kegg pathway
	 */
	public String colorKegg(String pathid, String[] ids, String[] fgcolors,
			String[] bgcolors) {
		String colored = null;
		try {
			colored = serv.get_html_of_colored_pathway_by_objects(pathid, ids, fgcolors, bgcolors);
			System.out.println("Finished " + colored);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return colored;
	}

	/**
	 * Colors Kegg (colorea por el identificador normal, en principio usaremos
	 * esta)
	 * 
	 * @param pathid
	 *            something like pathway:hsa4031
	 * @param ids
	 *            entrez ids like hsa:10267
	 * @param fgcolors
	 *            like #FF0000
	 * @param bgcolors
	 *            like #00FF00
	 * @return
	 */
	public String colorKegg2(String pathid, int[] element_id_list,
			String[] fgcolors, String[] bgcolors) {
		String colored = null;
		try {
			/*
			 * //si se utiliza esta versión se colorea la imagen en el servidor
			 * de kegg pero tarda muchísimo
			 * colored=serv.get_html_of_colored_pathway_by_elements(pathid,
			 * element_id_list, fgcolors, bgcolors); //si utilizo esta versión,
			 * se colorean en el servidor de kegg por defecto, así que aparecen
			 * los verdes que no queremos
			 * //colored=serv.get_html_of_colored_pathway_by_elements(pathid,
			 * element_id_list, null, null);
			 */
			// se crean 2 arrays auxiliares para rellenarlos de blanco (que es
			// lo que menos se tarda en colorear en kegg)
			String[] fgAux = new String[element_id_list.length];
			String[] bgAux = new String[element_id_list.length];
			for (int i = 0; i < element_id_list.length; i++) {
				bgAux[i] = "#FFFFFF";
				fgAux[i] = "#000000";
			}
			colored = serv.get_html_of_colored_pathway_by_elements(pathid,
					element_id_list, fgAux, bgAux);

			System.out.println("Finished " + colored);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return colored;
	}

	/**
	 * Colors Kegg (esta versión devuelve el png simplemente)
	 * 
	 * @param pathid
	 *            something like pathway:hsa4031
	 * @param ids
	 *            entrez ids like hsa:10267
	 * @param fgcolors
	 *            like #FF0000
	 * @param bgcolors
	 *            like #00FF00
	 * @return
	 */
	public String colorKegg3(String pathid, String[] ids, String[] fgcolors,
			String[] bgcolors) {
		String colored = null;
		try {
			colored = serv.color_pathway_by_objects(pathid, ids, fgcolors,
					bgcolors);
			System.out.println("Finished " + colored);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return colored;
	}

	/**
	 * Colors Kegg (esta versión devuelve el png simplemente)
	 * 
	 * @param pathid
	 *            something like pathway:hsa4031
	 * @param ids
	 *            entrez ids like hsa:10267
	 * @param fgcolors
	 *            like #FF0000
	 * @param bgcolors
	 *            like #00FF00
	 * @return
	 */
	public String colorKegg4(String pathid, int[] element_id_list,
			String[] fgcolors, String[] bgcolors) {
		String colored = null;
		try {
			colored = serv.color_pathway_by_elements(pathid, element_id_list,
					fgcolors, bgcolors);
			System.out.println("Finished " + colored);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return colored;
	}

	public void interpolateColors(String pathway, int[] element_id_list,
			float[] samples, String[] fgs, String[] bgs) {
		try {
			float[] ranks = k.getRanks(samples);

			for (int i = 0; i < element_id_list.length; i++) {
				fgs[i] = "#007700";

				// con esta combinación vamos de blanco a rojo
				// int r= 255;
				// int g= (int)((1.0-ranks[i])*255);
				// int b= g;

				int g = 0;
				int b = 0;
				int r = 0;

				if (0 <= ranks[i] && ranks[i] <= 0.5) {
					b = 255;
					r = (int) (2 * ranks[i] * 255); // se le multiplica por 2
													// porque sino nunca va a
													// llegar a acercarse al
													// blanco al estar cogiendo
													// sólo valores menores que
													// 0.5
					g = r;
				} else if (0.5 < ranks[i] && ranks[i] <= 1) {
					r = 255;
					g = (int) ((1.0 - 2 * (ranks[i] - 0.5)) * 255); // se le
																	// multiplica
																	// por 2 y
																	// se le
																	// resta 0.5
																	// para
																	// ajustar
																	// la escala
																	// al estar
																	// partiendo
																	// de 0.5
					b = g;
				}

				// System.out.println("Muestra número "+i+"\t"+r+"\t"+g+"\t"+b);
				bgs[i] = "#" + ColorToHex(r, g, b);

				// esto es para probar los blancos
				// bgs[i]="#FFFFFF";

				// System.out.println("Adding element "+element_id_list[i]+"\tvalue sample="+samples[i]+"\t"+"ranks[i]="+ranks[i]+"\t"+bgs[i]+"\t"+fgs[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts an RGB color to its corresponding hexadecimal value
	 * 
	 * CALCULA MAL PORQUE ELIMINA LOS 0 DE LA IZQUIERDA
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public String Color2Hex(int r, int g, int b) {
		Color c = new Color(r, g, b);
		String hex = Integer.toHexString(c.getRGB() & 0x00ffffff).toUpperCase();
		return hex;
	}

	/**
	 * Convers an RGB color to its corresponding hexadecimal value
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public String ColorToHex(int r, int g, int b) {
		Color c = new Color(r, g, b);
		String rgb = Integer.toHexString(c.getRGB());
		return rgb.substring(2, rgb.length()).toUpperCase();
	}

	public float[] getRanks(float[] samples) {
		if (samples.length < 1) {
			System.err.println("The sample is empty");
		}

		float max = Kegg.getMaxValue(samples);
		float min = Kegg.getMinValue(samples);
		float[] ranks = new float[samples.length];

		for (int i = 0; i < samples.length; i++) {
			ranks[i] = (samples[i] - min) / (max - min);
			// System.out.println("Para el sample="+samples[i]+" el rank="+ranks[i]);
		}

		return ranks;
	}

	public static float getMaxValue(float[] numbers) {
		float maxValue = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] > maxValue) {
				maxValue = numbers[i];
			}
		}

		return maxValue;
	}

	public static float getMinValue(float[] numbers) {
		float minValue = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] < minValue) {
				minValue = numbers[i];
			}
		}

		return minValue;
	}
}
