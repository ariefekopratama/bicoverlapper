package es.usal.bicoverlapper.view.diagram.kegg;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import keggapi.Definition;
import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.PathwayElement;
import es.usal.bicoverlapper.controller.kernel.Session;
import es.usal.bicoverlapper.model.gene.GeneAnnotation;

/**
 * Class to manage Kegg information
 * 
 * @author Carlos Martín Casado
 *
 */
public class Kegg {
	
	private KEGGPortType serv;
	private List<KeggElement> keggElements = new ArrayList<KeggElement>();
	private Session sesion;
	
	/**
	 * Binds the service to the soap port
	 * @throws Exception
	 */
	public Kegg(Session _sesion) throws Exception {

		KEGGLocator locator = new KEGGLocator();
		serv = locator.getKEGGPort();
		sesion = _sesion;
	}

	/**
	 * Get list of Kegg elements
	 * @return List of Kegg elements
	 */
	public List<KeggElement> getKeggElements() {
		return keggElements;
	}

	/**
	 * Generate Kegg image as from the selected pathway and the expression of genes in the experiment loaded
	 * @param pathway Pathway of the organism
	 * @param numCondition Condition selected
	 * @return URL with the image generated
	 * @throws Exception 
	 */
	public String generateKeggImage(String pathway, int numCondition) throws Exception {
		int[] element_id_list = null;
		String[] bgs = null;
		String[] fgs = null;
		//variable que servirá para comprobar qué tipo de identificador de gen ha sido probado
		//0 = id
		//1 = entrezId
		//2 = ensemblId
		//3 => dejar de probar ya que no hay coincidencias
		int tipoIdentificador = 0;
		
		long start = System.currentTimeMillis();
		
		List<PathwayElement> resultadosValidos = new ArrayList<PathwayElement>();
		
		System.out.println("pathway = "+pathway);
		// se consultan los elementos del pathway
		PathwayElement[] pathwayElements = this.serv.get_elements_by_pathway(pathway);
		// se crea la lista con los resultados
		List<Integer> elementIdList = new ArrayList<Integer>();
		// lista de samples
		List<Float> samplesList = new ArrayList<Float>();
		
		//creo una lista de elementos que tendrá todos los elementos y que sólo usaré para obtener la imagen de KEGG en blanco
		//int[] element_id_listAux = new int[results.length];
		
		//se recogen los genes de MicroarrayData
		Map<Integer, GeneAnnotation> mapaGenes = sesion.getMicroarrayData().getGeneAnnotations();
		System.out.println("mapaGenes.size()="+mapaGenes.size());
		
		System.out.println("pathwayElements.length es "+pathwayElements.length);

		do{
			//se recorren todos los pathway elements
			for (int i = 0; i < pathwayElements.length; i++) {
				//si el elemento es de tipo gen (ya que para otro tipo de elementos no se coloreará)
				if(pathwayElements[i].getType().equals("gene")){
					ArrayList<Double> valoresExpresion = null;
					boolean hayExpresion = false;
					valoresExpresion = new ArrayList<Double>();
					//se obtienen los nombres de ese pathwayElement (nótese que puede ser 1 nombre o más de uno)
					String[] nombresKO = pathwayElements[i].getNames();
					
					//extrañamente montando este TreeMap previo tarda menos que recorriendo nombresKO abajo directamente
					//así, se prepara un mapa que tiene por clave el nombre correspondiente y una lista 
					//esa lista tendrá el nombre pero sin la identificación del organismo, es decir, si hay algo del tipo mmu:12345 se eliminará "mmu:"
					TreeMap<String, ArrayList<String>> KOTermsEnPathway=new TreeMap<String, ArrayList<String>>();
					for (String nombreKO : nombresKO){
						ArrayList<String> elementosDeKOterms = new ArrayList<String>();
						//se comprueba que el comienzo del gen coincida con el organismo cargado en el experimento
						if(nombreKO.startsWith(sesion.getMicroarrayData().getOrganismKEGG())){
							//se guarda el número del gen sin el identificador del organismo
							elementosDeKOterms.add(nombreKO.split("\\:")[1]);
							KOTermsEnPathway.put(nombreKO, elementosDeKOterms);
						}
						//System.out.println("nombreKO = "+nombreKO+", pathwayElements[i].getType() = "+pathwayElements[i].getType()+ " orgamismKegg = "+sesion.getMicroarrayData().getOrganismKEGG());
					}
					
					//para cada gen en el microarray
					for (GeneAnnotation g : mapaGenes.values()) {		
						//para cada gen en el pathway
						for (ArrayList<String> listaGenes: KOTermsEnPathway.values()) {	
							for(String gen: listaGenes){
								//si coinciden con el identificador que se esté comparando en cada momento, se marca la coincidencia y se guarda el valor de su expresión para calcular posteriormente su media
								if(tipoIdentificador == 0 && null != g.id && gen.equals(g.id)){									
									hayExpresion = true;
									double valorExp = sesion.getMicroarrayData().getExpressionAt(g.internalId, numCondition);
									valoresExpresion.add(valorExp);			
									//System.out.println("gen = "+gen+"\tvalorExp = "+valorExp+"\tpathwayElements[i].getElement_id() = "+pathwayElements[i].getElement_id());
								}
								else if(tipoIdentificador == 1 && null != g.entrezId && gen.equals(g.entrezId)){									
									hayExpresion = true;
									double valorExp = sesion.getMicroarrayData().getExpressionAt(g.internalId, numCondition);
									valoresExpresion.add(valorExp);			
									//System.out.println("gen = "+gen+"\tvalorExp = "+valorExp+"\tpathwayElements[i].getElement_id() = "+pathwayElements[i].getElement_id());
								}
								else if(tipoIdentificador == 2 && null != g.ensemblId && gen.equals(g.ensemblId)){									
									hayExpresion = true;
									double valorExp = sesion.getMicroarrayData().getExpressionAt(g.internalId, numCondition);
									valoresExpresion.add(valorExp);			
									//System.out.println("gen = "+gen+"\tvalorExp = "+valorExp+"\tpathwayElements[i].getElement_id() = "+pathwayElements[i].getElement_id());
								}								
							}
						}
					}
					
					//si se han producido coincidencias, entonces quiere decir que para ese elemento habrá que calcular un coloreado
					if(hayExpresion){
						//por tanto se añade el elemento a la lista
						elementIdList.add(pathwayElements[i].getElement_id());
						resultadosValidos.add(pathwayElements[i]);
						//se calcula el valor medio para todos los genes de ese elemento
						float media = calcularMedia(valoresExpresion);
						
						System.out.println("ELEMENTO "+pathwayElements[i].getElement_id());
						for (Double valorExp : valoresExpresion) {
							System.out.println(valorExp);
						}
						System.out.println("Media = "+media);
						
						//se guarda esta media en la lista de muestras con la que se coloreará la imagen
						samplesList.add(media);
					}
				}
				else{
					//TODO: en principio nada con los elementos que no sean genes, pero en un futuro podría necesitarse hacer algo con ellos
				}
			}
			
			//al llegar a este punto significa que se han buscado todos los elementos
			//por tanto, el tipoIdentificador se incrementará por si hay que seguir buscando
			tipoIdentificador++;
			
		} while(resultadosValidos.isEmpty() && tipoIdentificador < 4);
		
		System.out.println("for principal took " + (System.currentTimeMillis() - start) / 1000 + " seconds y tipoIdentificador usado es "+(tipoIdentificador-1));		

		if(!elementIdList.isEmpty()){
			//se convierten las colecciones anteriores en arrays
			element_id_list = toIntArray(elementIdList);
			float[] samples = toFloatArray(samplesList);		
								
			fgs = new String[element_id_list.length];
			bgs = new String[element_id_list.length];
	
			//se interpolan los colores de la muestra
			if(sesion.getScaleMode() == Session.numerical){
				interpolateColorsNumerical(element_id_list, samples, fgs, bgs);
			}
			else if(sesion.getScaleMode() == Session.quantile){
				interpolateColorsQuantile(element_id_list, samples, fgs, bgs);
			}
			// como todo en principio debería haber mantenido el orden, ahora ya
			// tendría cada elemento con su fg y bg correspondiente
			// como el id es único para cada resultado, añado el resultado que es lo
			// que tiene más información
			int i = 0;
			for (PathwayElement resultadoValido: resultadosValidos) {
				keggElements.add(new KeggElement(resultadoValido, fgs[i], bgs[i]));
				i++;
			}
		}
		
		System.out.println("El número de resultados válidos encontrados es "+keggElements.size());
		
		//para medir sólo lo que tarda este método
		long startColorKegg = System.currentTimeMillis();
		System.out.println("Llamando a colorKeggInTheCloud");
		
		//aquí creo que me dan igual fgs y bgs porque van a ir en blanco
		//pongo element_id_listAux en vez de element_id_list porque quiero todos los elementos de la imagen en blanco
		//String url = k.colorKegg2(pathway, element_id_listAux, fgs, bgs);

		String url = this.colorKeggInTheCloudHTML(pathway, element_id_list, fgs, bgs);
		
		System.out.println("colorKeggInTheCloud took "
				+ (System.currentTimeMillis() - startColorKegg) / 1000 + " seconds");		
		
		System.out.println("generarImagenKegg took "
				+ (System.currentTimeMillis() - start) / 1000 + " seconds");

		return url;
	}

	/**
	 * Get the medium of a list
	 * @param valoresExpresion List with values
	 * @return Float with the medium of the values
	 */
	private float calcularMedia(List<Double> valoresExpresion) {
		double suma = 0;
		for (Double valor : valoresExpresion) {
			suma += valor;
		}
		return (float) (suma/valoresExpresion.size());
	}

	/**
	 * Retrieves all the pathways for a given organism
	 * 
	 * @param organism Organism to seek its pathways
	 * @return String[] with pathways
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
	 * @param organism Organism to seek its pathways
	 * @return Definition[] with pathways
	 * @throws RemoteException
	 */
	public Definition[] getDefinitionPathwaysFromOrganism(String organism) throws RemoteException {
		Definition[] d = serv.list_pathways(organism);
		return d;
	}

	/**
	 * Retrieves the pathway id for a given pathway definition
	 * 
	 * @param path Pathway to seek its id
	 * @param d Definition[] with all definition pathways
	 * @return String with the id
	 */
	public String getPathwayIdFromDefinition(String path, Definition[] d) {
		for (int i = 0; i < d.length; i++) {
			if (d[i].getDefinition().contains(path)) {
				return d[i].getEntry_id();
			}
		}
		return null;
	}

	/**
	 * Get list organisms
	 * 
	 * @return String[] with the organisms
	 */
	public String[] getOrganisms() {
		Definition[] d;
		try {
			d = serv.list_organisms();
			String[] paths = new String[d.length];
			for (int i = 0; i < d.length; i++) {
				paths[i] = d[i].getDefinition();
			}
			
			//se ordena por orden alfabético
			Arrays.sort(paths);

			return paths;			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return new String[0];
	}

	/**
	 * Get the id of an organism
	 * 
	 * @param organism Organism to seek the id
	 * @return String with the id
	 * @throws Exception
	 */
	public String getOrganismId(String organism) throws Exception {
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
	 * @param pathway Pathway to seek organisms
	 * @return String[] with organisms
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
	 * Color the elements in ids in the Kegg pathway pathid, with the
	 * foreground and background colors specified
	 * 
	 * @param pathid  something like pathway:hsa4031
	 * @param ids ko ids like ko:K010267
	 * @param fgs Foreground colors like #FF0000
	 * @param bgs Background colors like #00FF00
	 * @return - the url to the colored kegg pathway in html
	 */
	public String colorKeggInTheCloudHTML(String pathid, int[] element_id_list, String[] fgcolors, String[] bgcolors) {
		String colored = null;
		try {
			colored = serv.get_html_of_colored_pathway_by_elements(pathid, element_id_list, fgcolors, bgcolors);
			if(null != element_id_list){
				System.out.println("Finished " + colored);
			}
			else{
				System.out.println("Finished "+ colored);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return colored;
	}	

	/**
	 * Color the elements in ids in the Kegg pathway pathid, with the
	 * foreground and background colors specified
	 * 
	 * @param pathid  something like pathway:hsa4031
	 * @param ids ko ids like ko:K010267
	 * @param fgs Foreground colors like #FF0000
	 * @param bgs Background colors like #00FF00
	 * @return - the url to the colored kegg pathway in png
	 */
	public String colorKeggInTheCloudPNG(String pathid, int[] element_id_list,
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
	
	/**
	 * Interpolate colors using quantile scale
	 * @param element_id_list List of elements
	 * @param samples Expression of elements
	 * @param fgs Foreground colors like #FF0000
	 * @param bgs Background colors like #00FF00
	 */
	public void interpolateColorsQuantile(int[] element_id_list, float[] samples, String[] fgs, String[] bgs) {
		try {
			for (int i = 0; i < element_id_list.length; i++) {
				fgs[i] = "#007700";

				int quantile = sesion.getMicroarrayData().getQuantile(samples[i]);

				int g = 0;
				int b = 0;
				int r = 0;

				if (quantile < 50) {
					b = 255;
					r = (int)Math.round(255-((50.0-quantile)/50)*255);
					g = r;
				} 
				
				else {
					r = 255;
					g = (int)Math.round(255-((quantile-50.0)/50)*255);
					b = g;
				}

				// System.out.println("Muestra número "+i+"\t"+r+"\t"+g+"\t"+b);
				bgs[i] = "#" + ColorToHex(r, g, b);

				System.out.println("Adding element "+element_id_list[i]+"\tvalue sample="+samples[i]+"\t"+"quantile="+quantile+"\t"+bgs[i]+"\t"+fgs[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Interpolate colors using numerical scale
	 * @param element_id_list List of elements
	 * @param samples Expression of elements
	 * @param fgs Foreground colors like #FF0000
	 * @param bgs Background colors like #00FF00
	 */
	public void interpolateColorsNumerical(int[] element_id_list, float[] samples, String[] fgs, String[] bgs) {
		try {
			float[] ranks = this.getRanks(samples);

			for (int i = 0; i < element_id_list.length; i++) {
				fgs[i] = "#007700";

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

				System.out.println("Adding element "+element_id_list[i]+"\tvalue sample="+samples[i]+"\t"+"ranks[i]="+ranks[i]+"\t"+bgs[i]+"\t"+fgs[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Convers an RGB color to its corresponding hexadecimal value
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @return
	 */
	public String ColorToHex(int r, int g, int b) {
		Color c = new Color(r, g, b);
		String rgb = Integer.toHexString(c.getRGB());
		return rgb.substring(2, rgb.length()).toUpperCase();
	}

	/**
	 * Get ranks of a list os samples
	 * @param samples float[] with samples
	 * @return ranks
	 */
	public float[] getRanks(float[] samples) {
		if (samples.length < 1) {
			System.err.println("The sample is empty");
		}

		//de esta forma se cogería el máximo y el mínimo de la muestra
		//float max = Kegg.getMaxValue(samples);
		//float min = Kegg.getMinValue(samples);
		
		//pero para que funcione como se desea en BicOverlapper, hay que coger el máximo y mínimo de todas las muestras
		float max = (float) sesion.getMicroarrayData().max;
		float min = (float) sesion.getMicroarrayData().min;
		
		float[] ranks = new float[samples.length];

		for (int i = 0; i < samples.length; i++) {
			ranks[i] = (samples[i] - min) / (max - min);
			System.out.println("Para el sample="+samples[i]+" el rank="+ranks[i]+", con max = "+max+" y min = "+min);
		}

		return ranks;
	}

	/**
	 * Get max value of a list
	 * @param numbers float[] of numbers
	 * @return float with max value
	 */
	public static float getMaxValue(float[] numbers) {
		float maxValue = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] > maxValue) {
				maxValue = numbers[i];
			}
		}

		return maxValue;
	}

	/**
	 * Get min value of a list
	 * @param numbers float[] of numbers
	 * @return float with min value
	 */
	public static float getMinValue(float[] numbers) {
		float minValue = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] < minValue) {
				minValue = numbers[i];
			}
		}

		return minValue;
	}
	
	/**
	 * Convert List<Integer> to int[]
	 * @param list List of Integers
	 * @return int[] with the same elements as List<Integer>
	 */
    public static int[] toIntArray(List<Integer> list) {  
	    int[] intArray = new int[list.size()];  
	    int i = 0;  
	       
	    for (Integer integer : list)  
	    intArray[i++] = integer;  
	      
	    return intArray;  
    }  
    
	/**
	 * Convert List<Float> to float[]
	 * @param list List of Floats
	 * @return float[] with the same elements as List<Float>
	 */
    public static float[] toFloatArray(List<Float> list) {  
	    float[] floatArray = new float[list.size()];  
	    int i = 0;  
	       
	    for (Float f: list)  
	    	floatArray[i++] = f;  
	      
	    return floatArray;  
    }    
}
